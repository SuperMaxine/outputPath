package regex;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author SuperMaxine
 */
public class Analyzer {
    private static class Path {
        public boolean reachEnd;
        public ArrayList<Set<Integer>> paths;

        public Path() {
            this.reachEnd = false;
            this.paths = new ArrayList<>();
        }
    }
    ArrayList<Path> Paths;
    public Analyzer(Pattern pattern){
        Paths = new ArrayList<>();
    }
    public static void returnPath(Pattern.Node root){
        if (root == null) {
            return;
        }


    }
}
