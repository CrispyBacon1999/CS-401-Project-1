import com.sun.tools.javac.util.Pair;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an individual traceroute entry from a file.
 *
 * This will handle the parsing, as long as data is fed in line by line.
 * Internally has a FSM to keep track of where in the parsing the words are.
 */
public class TraceRoute {

    private String targetHostname;
    private Inet4Address targetIP;
    private ArrayList<Hop> routes;
    private int hopMax;
    private int packetSize;

    private final int packetsPerHop = 3;

    private State currentState;
    private int currentRouterCount;

    public TraceRoute() {
        currentState = State.Start;
        currentRouterCount = 0;
        routes = new ArrayList<Hop>();
    }

    /**
     * The main part of the FSM for parsing a traceroute file.
     *
     * Adds a new line and splits it by spaces, then based off the
     * current state of the FSM, will do a different action to the
     * current token.
     *
     * @param line A line of a traceroute entry
     */
    public void addLine(String line) {
        String[] words = line.split(" ");
        for (String word : words) {
            switch (currentState) {
                case Start: {
                    if (word.equals("traceroute")) {
                        currentState = State.TargetHostname;
                    }
                    break;
                }
                case TargetHostname: {
                    // Ignore the "to" in "traceroute to"
                    if (word.equals("to")) {
                        continue;
                    }
                    String hostname = getPatternMatch(TracerouteRegex.hostnameRegex, word);
                    if (hostname != null) {
                        targetHostname = hostname;
                        currentState = State.TargetIP;
                    }
                    break;
                }
                case TargetIP: {
                    String ip = getPatternMatch(TracerouteRegex.ipRegex, word);
                    if (ip != null) {
                        try {
                            targetIP = (Inet4Address) Inet4Address.getByName(ip);
                        } catch (UnknownHostException e) {
                            System.err.println("Found an invalid IP address in the target IP field");
                        }
                        currentState = State.HopCount;
                    }
                    break;
                }
                case HopCount: {
                    String hopCount = getPatternMatch(TracerouteRegex.hopsRegex, word);
                    if (hopCount != null) {
                        hopMax = Integer.parseInt(hopCount);
                        currentState = State.PacketSize;
                    }
                    break;
                }
                case PacketSize: {
                    String p = getPatternMatch(TracerouteRegex.dataSizeRegex, word);
                    if (p != null) {
                        packetSize = Integer.parseInt(p);
                        currentState = State.HopStart;
                    }
                    break;
                }
                // Actually do the parsing of the hops
                case HopStart: {
                    String start = getPatternMatch(TracerouteRegex.hopsRegex, word);
                    if (start != null) {
                        // Create a new hop
                        routes.add(new Hop());
                        currentRouterCount = 0;
                        currentState = State.RouterHostname;
                    }
                    break;
                }
                case RouterHostname: {
                    // If hostname isn't null, but there's already a router inserted
                    // check if there's another delay tied to the previous router
                    if (currentRouterCount > 0) {
                        if (word.matches(TracerouteRegex.delayRegex)) {
                            implicitRouterDelay(word);
                            break;
                        }
                    }
                    // Use generic hostname regex
                    String hostname = getPatternMatch(TracerouteRegex.hostnameRegex, word);
                    // Check against just a star for an incomplete connection
                    if (word.equals("*")) {
                        hostname = "*";
                    }
                    // Skip the ms word that comes after a delay
                    if (word.equals("ms")) {
                        break;
                    }
                    if (hostname != null) {
                        Router r = new Router(hostname);
                        routes.get(routes.size() - 1).routers.add(r);
                        currentRouterCount++;
                        // If the hostname wasn't reached, skip to the next hostname
                        if (r.reachable) {
                            currentState = State.RouterIP;
                        } else {
                            if (currentRouterCount >= packetsPerHop) {
                                currentState = State.HopStart;
                            }
                        }

                    }

                    break;
                }
                case RouterIP: {
                    String ip = getPatternMatch(TracerouteRegex.ipRegex, word);
                    if (ip != null) {
                        Hop rs = routes.get(routes.size() - 1);
                        Router last = rs.routers.get(currentRouterCount - 1);
                        try {
                            last.ip = (Inet4Address) Inet4Address.getByName(ip);
                        } catch (UnknownHostException e) {
                            System.err.println("Found an invalid IP address in a router's IP.");
                        }
                        currentState = State.RouterDelay;
                    }
                    break;
                }
                case RouterDelay: {
                    calculateRouterDelay(word);
                    break;
                }
            }
        }
    }

    /**
     * This is just the section of the switch statement for calculating
     * the delay of a router. It's more complicated than most of the other
     * cases, and is reused when implicitly creating a new router.
     * @param word The single word parsed out from the traceroute
     */
    private void calculateRouterDelay(String word) {
        String delay = getPatternMatch(TracerouteRegex.delayRegex, word);
        if (delay != null) {
            Hop rs = routes.get(routes.size() - 1);
            Router last = rs.routers.get(currentRouterCount - 1);
            last.delay = Double.parseDouble(delay);
            if (currentRouterCount >= packetsPerHop) {
                currentState = State.HopStart;
            } else {
                currentState = State.RouterHostname;
            }
        }
    }

    /**
     * Traceroute allows for a router to have multiple delays listed
     * after each other, rather than repeating the ip and hostname for
     * each delay.
     * <br />
     * This code will create a new router, copy it from the previously created one,
     * then call the router delay calculation case to find the actual delay.
     * <br />
     * Ex. <code>1  10.8.30.1 (10.8.30.1)  0.605 ms  0.616 ms  0.584 ms</code>
     * @param word
     */
    private void implicitRouterDelay(String word) {
        // Used when the same router is hit multiple times in a row without switching to a different one.
        // Eg. 162.247.144.38 (162.247.144.38)  0.804 ms  0.895 ms 035-131-224-113.biz.spectrum.com (35.131.224.113)  1.323 ms
        // First and second delays are tied to the first router.
        Router router = routes.get(routes.size() - 1).routers.get(currentRouterCount - 1);
        // Copy the router and insert it into the list of routers
        Router duplicated = new Router(router);
        routes.get(routes.size() - 1).routers.add(duplicated);
        // Incremement router count
        currentRouterCount++;
        // Calculate the delay of the current router with a duplicated one inserted into the list
        calculateRouterDelay(word);
    }

    private String getPatternMatch(String regex, String word) {
        Matcher m = Pattern.compile(regex).matcher(word);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    /**
     * Figures out whether or not the target IP was reachable through traceroute
     * @return If the ip was reached
     */
    public boolean isReachable() {
        Hop lasthop = routes.get(routes.size() - 1);
        for (Router r : lasthop.routers) {
            if (r.reachable) {
                if (r.ip.equals(targetIP)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The number of hops to get to the end of the traceroute
     * @return number of hops, or just the size of the routes array
     */
    public int hopCount() {
        return routes.size();
    }

    /**
     * Computes the total amount of delay to get to the end of the traceroute.
     * @return A pair of delay and the number of hops to get to the end
     */
    public Pair<Double, Integer> avgDelay() {
        Hop lasthop = routes.get(routes.size() - 1);
        return lasthop.avgDelay();
    }

    /**
     * Computes the total amount of delay between hops
     * @return A pair of delay and the number of transitions between hops
     */
    public Pair<Double, Integer> linkDelayTransitionOverall() {
        double totalDelay = 0;
        int validTransitions = 0;
        for(int i = 0; i < routes.size() - 2; i++) {
            Hop h1 = routes.get(i);
            Hop h2 = routes.get(i + 1);
            if(h1.isReachable() && h2.isReachable()) {
                validTransitions++;
                double delay = h2.computedAvgDelay() - h1.computedAvgDelay();
                if(delay >= 0) {
                    totalDelay += delay;
                }
            }
        }
        return new Pair<>(totalDelay, validTransitions);
    }
}

/**
 * FSM States for parsing out traceroutes
 */
enum State {
    Start,
    TargetHostname,
    TargetIP,
    HopCount,
    PacketSize,
    HopStart,
    RouterHostname,
    RouterIP,
    RouterDelay
}