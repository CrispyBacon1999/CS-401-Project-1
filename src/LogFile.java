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
