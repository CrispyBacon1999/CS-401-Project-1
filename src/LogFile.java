import com.sun.tools.javac.util.Pair;
import sun.misc.Regexp;

import java.io.*;
import java.util.ArrayList;

public class LogFile {

    private ArrayList<TraceRoute> routes;

    private String traceRouteStartPattern = "traceroute to.+";

    public LogFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        TraceRoute traceRoute = null;
        routes = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if(line.matches(traceRouteStartPattern)) {
                if(traceRoute != null) {
                    routes.add(traceRoute);
                }
                traceRoute = new TraceRoute();
                traceRoute.addLine(line);
            }
            else {
                if(traceRoute != null) {
                    traceRoute.addLine(line);
                }
            }
        }
        // Add the last traceroute
        if(traceRoute != null) {
            routes.add(traceRoute);
        }
        System.out.println("All traceroutes calculated. Count: " + routes.size());
        System.out.println("Average delay: " + avgDelay());
        System.out.println("Average hops: " + avgHops());
        System.out.println("Average link delay: " + avgLinkDelay());
    }

    private double avgDelay() {
        double totalDelay = 0;
        int reachableRoutes = 0;
        for(TraceRoute tr : routes) {
            if(tr.isReachable()) {
                Pair<Double, Integer> delay = tr.avgDelay();
                reachableRoutes += delay.snd;
                totalDelay += delay.fst;
            }
        }
        return totalDelay / reachableRoutes;
    }

    private double avgLinkDelay() {
        double totalDelay = 0;
        int reachableRoutes = 0;
        for(TraceRoute tr : routes) {
            if(tr.isReachable()) {
                Pair<Double, Integer> delay = tr.linkDelayTransitionOverall();
                totalDelay += delay.fst;
                reachableRoutes += delay.snd;
            }
        }
        return totalDelay / reachableRoutes;
    }

    private double avgHops() {
        double totalHops = 0;
        int reachableRoutes = 0;
        for(TraceRoute tr : routes) {
            if(tr.isReachable()) {
                reachableRoutes++;
                totalHops += tr.hopCount();
            }
        }
        return totalHops / reachableRoutes;
    }

    public void writeAverage(String path) {

    }

    public void writeHistogram(String path) {
        writeToHistogramTextFile(path);
    }

    public void writeHistogram(String textPath, String imgPath) {
        writeToHistogramTextFile(textPath);
        writeToHistogramImageFile(imgPath);
    }

    private void writeToHistogramTextFile(String path) {

    }

    private void writeToHistogramImageFile(String path) {

    }
}
