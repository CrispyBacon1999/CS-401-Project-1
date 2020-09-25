import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;

/**
 * Represents an individual hop. Has an array of routers
 * in it to represent each new router on a hop.
 */
public class Hop {
    public ArrayList<Router> routers;

    public Hop() {
        // Assume 3 routers by default
        routers = new ArrayList<>(3);
    }

    public double computedAvgDelay() {
        Pair<Double, Integer> d = avgDelay();
        return d.fst / d.snd;
    }

    public Pair<Double, Integer> avgDelay() {
        double delayTotal = 0;
        int reachableCount = 0;
        for(Router r : routers) {
            if(r.reachable) {
                reachableCount++;
                delayTotal += r.delay;
            }
        }
        return new Pair<>(delayTotal, reachableCount);
    }

    public boolean isReachable() {
        for(Router r : routers) {
            if(r.reachable) {
                return true;
            }
        }
        return false;
    }
}
