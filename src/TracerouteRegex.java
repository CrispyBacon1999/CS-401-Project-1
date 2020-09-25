public class TracerouteRegex {
    //public static final String hostnameRegex = "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]*|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
    public static final String hostnameRegex = "([\\w\\-\\.]{2,})";
    public static final String ipRegex = "(([0-9]{0,3})\\.){3}([0-9]{0,3})";
    public static final String hopsRegex = "[0-9]{1,3}"; // Max hops for traceroutes is 255.
    public static final String dataSizeRegex = "[0-9]{1,4}"; // Max byte count is 1460.
    public static final String delayRegex = "([0-9]{1,6})\\.([0-9]{1,3})";  // Max wait time is 300 seconds, or 300000 ms
}
