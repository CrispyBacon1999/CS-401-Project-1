import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Router {
    public String hostname;
    public Inet4Address ip;
    public double delay;
    public boolean reachable;

    public Router(String hostname) {
        if(hostname.equals("*")) {
            this.hostname = null;
            ip = null;
            delay = 0;
            reachable = false;
        } else {
            this.hostname = hostname;
            ip = null;
            delay = 0;
            reachable = true;
        }
    }

    /**
     * Copy constructor
     * @param other
     */
    public Router(Router other) {
        this.hostname = other.hostname;
        this.reachable = other.reachable;
        this.ip = other.ip;
        this.delay = 0;
    }
}
