import java.io.FileNotFoundException;
import java.io.IOException;

public class App {

    public static void main(String[] args) {
        System.out.println("Args:");
        for(String arg : args){
            System.out.println("\t" + arg);
        }
        if(args.length < 1) {
            System.err.println("Missing file name in arguments");
            System.exit(1);
        }
        LogFile file = null;
        try {
            file = new LogFile(args[0]);
            file.writeAverage("avg.txt");
            file.writeHistogram("hist.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
