package regex;

import java.util.*;

/**
 * @author SuperMaxine
 */
public class Analyzer {
    int maxLength;
    Pattern pattern;
    Path root;
    Map<Path, Path> countingBiggerThan2; // 中缀的结束节点作为Key，开始节点作为Value
    Map<Path, ArrayList<Set<Integer>>> PrePaths; // Path是中缀的开始节点
    public Map<Path, ArrayList<Set<Integer>>> PumpPaths; // Path是中缀的结束节点
    public Map<Pattern.Node, ArrayList<Set<Integer>>> PumpPaths2; // PumpPaths的字符串形式

    public Analyzer(Pattern pattern, int maxLength) {
        this.pattern = pattern;
        this.maxLength = maxLength;
        root = new Path(0);
        countingBiggerThan2 = new HashMap<>();
        PrePaths = new HashMap<>();
        PumpPaths = new HashMap<>();
        returnPaths(pattern.root, root);
        cutFailedBrach(root);

        generateAttackArgs(this.root, new ArrayList<>());

        System.out.println("flowchart LR");
        printPath.print(root);

        System.out.println("\n\n\n");
        printPath.printPump(PumpPaths);
    }

    class Path{
        ArrayList<Set<Integer>> path;
        int currentSize;
        ArrayList<Path> nextPaths;
        boolean reachedEnd;
        boolean LastNode;

        Path(int size){
            path = new ArrayList<>();
            currentSize = size;
            nextPaths = new ArrayList<>();
            reachedEnd = false;
            LastNode = false;
        }

        Path(Path path){
            this.path = new ArrayList<>(path.path);
            this.currentSize = path.currentSize;
            this.nextPaths = new ArrayList<>();
            this.reachedEnd = path.reachedEnd;
            this.LastNode = path.LastNode;
        }
    }

    public void returnPaths(Pattern.Node root, Path rawPath) {
        if (root instanceof Pattern.LastNode){
            rawPath.reachedEnd = true;
            Path LastNode = new Path(0);
            LastNode.LastNode = true;
            LastNode.reachedEnd = true;
            rawPath.nextPaths.add(LastNode);
            return;
        }else if (root == null || rawPath.currentSize > maxLength || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            return;
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        // 1. 循环
        if (root instanceof Pattern.Prolog) {
            returnPaths(((Pattern.Prolog)root).loop, rawPath);
        } else if (root instanceof Pattern.Loop) {
            // 获取Loop内的路径
            Path loopPath = new Path(rawPath.currentSize);
            returnPaths(((Pattern.Loop)root).body, loopPath);

            // 获取Loop后的路径
            Path nextPath = new Path(rawPath.currentSize);
            returnPaths(root.next, nextPath);

            ArrayList<Path> lastEnds = reachEnds(rawPath);
            for (int i = 0; i < ((Pattern.Loop)root).cmin; i++) {
                ArrayList<Path> newEnds = new ArrayList<>();
                for (Path p : lastEnds){
                    p.nextPaths.add(copyPath(loopPath));
                    calibrateCurrentSize(p);
                    newEnds.addAll(reachEnds(p));
                }
                lastEnds = newEnds;
            }
            for (int loopTimes = ((Pattern.Loop)root).cmin; loopTimes <= ((Pattern.Loop)root).cmax && loopTimes < maxLength; loopTimes++) {
                ArrayList<Path> newEnds = new ArrayList<>();
                for (Path p : lastEnds){
                    p.nextPaths.add(copyPath(loopPath));
                    calibrateCurrentSize(p);
                    newEnds.addAll(reachEnds(p));
                }
                for (Path p : lastEnds){
                    countingBiggerThan2.put(p, rawPath);
                    p.nextPaths.add(copyPath(nextPath));
                    calibrateCurrentSize(p);
                }
                lastEnds = newEnds;
            }
            for (Path p : lastEnds){
                p.nextPaths.add(copyPath(nextPath));
                calibrateCurrentSize(p);
            }

            calibrateCurrentSize(rawPath);
        } else if (root instanceof Pattern.Curly) {
            // 获取Curly内的路径
            Path curlyPath = new Path(rawPath.currentSize);
            returnPaths(((Pattern.Curly)root).atom, curlyPath);

            // 获取Curly后的路径
            Path nextPath = new Path(rawPath.currentSize);
            returnPaths(root.next, nextPath);

            ArrayList<Path> lastEnds = reachEnds(rawPath);
            for (int i = 0; i < ((Pattern.Curly)root).cmin; i++) {
                ArrayList<Path> newEnds = new ArrayList<>();
                for (Path p : lastEnds){
                    p.nextPaths.add(copyPath(curlyPath));
                    calibrateCurrentSize(p);
                    newEnds.addAll(reachEnds(p));
                }
                lastEnds = newEnds;
            }
            for (int curlyTimes = ((Pattern.Curly)root).cmin; curlyTimes <= ((Pattern.Curly)root).cmax && curlyTimes < maxLength; curlyTimes++) {
                ArrayList<Path> newEnds = new ArrayList<>();
                for (Path p : lastEnds){
                    p.nextPaths.add(copyPath(curlyPath));
                    calibrateCurrentSize(p);
                    newEnds.addAll(reachEnds(p));
                }
                for (Path p : lastEnds){
                    countingBiggerThan2.put(p, rawPath);
                    p.nextPaths.add(copyPath(nextPath));
                    calibrateCurrentSize(p);
                }
                lastEnds = newEnds;
            }
            for (Path p : lastEnds){
                p.nextPaths.add(copyPath(nextPath));
                calibrateCurrentSize(p);
            }

            calibrateCurrentSize(rawPath);
        } else if (root instanceof Pattern.GroupCurly) {
            // 获取GroupCurly内的路径
            Path groupCurlyPath = new Path(rawPath.currentSize);
            returnPaths(((Pattern.GroupCurly)root).atom, groupCurlyPath);

            // 获取GroupCurly后的路径
            Path nextPath = new Path(rawPath.currentSize);
            returnPaths(root.next, nextPath);

            ArrayList<Path> lastEnds = reachEnds(rawPath);
            for (int i = 0; i < ((Pattern.GroupCurly)root).cmin; i++) {
                ArrayList<Path> newEnds = new ArrayList<>();
                for (Path p : lastEnds){
                    p.nextPaths.add(copyPath(groupCurlyPath));
                    calibrateCurrentSize(p);
                    newEnds.addAll(reachEnds(p));
                }
                lastEnds = newEnds;
            }
            for (int groupCurlyTimes = ((Pattern.GroupCurly)root).cmin; groupCurlyTimes <= ((Pattern.GroupCurly)root).cmax && groupCurlyTimes < maxLength; groupCurlyTimes++) {
                ArrayList<Path> newEnds = new ArrayList<>();
                for (Path p : lastEnds){
                    p.nextPaths.add(copyPath(groupCurlyPath));
                    calibrateCurrentSize(p);
                    newEnds.addAll(reachEnds(p));
                }
                for (Path p : lastEnds){
                    countingBiggerThan2.put(p, rawPath);
                    p.nextPaths.add(copyPath(nextPath));
                    calibrateCurrentSize(p);
                }
                lastEnds = newEnds;
            }
            for (Path p : lastEnds){
                p.nextPaths.add(copyPath(nextPath));
                calibrateCurrentSize(p);
            }

            calibrateCurrentSize(rawPath);
        }

        // 2. 分支
        else if (root instanceof Pattern.Branch) {
            for(Pattern.Node node : ((Pattern.Branch)root).atoms){
                if (node == null){
                    continue;
                }
                Path branchPath = new Path(rawPath.currentSize);
                rawPath.nextPaths.add(branchPath);
                returnPaths(node, branchPath);
            }
        } else if (root instanceof Pattern.Ques) {
            // 获取Ques内的路径
            Path quesPath = new Path(rawPath.currentSize);
            returnPaths(((Pattern.Ques)root).atom, quesPath);

            // 获取Ques后的路径
            Path nextPath = new Path(rawPath.currentSize);
            returnPaths(root.next, nextPath);

            ArrayList<Path> lastEnds = reachEnds(rawPath);
            ArrayList<Path> newEnds = new ArrayList<>();
            for (Path p : lastEnds){
                p.nextPaths.add(copyPath(quesPath));
                calibrateCurrentSize(p);
                newEnds.addAll(reachEnds(p));
                p.nextPaths.add(copyPath(nextPath));
            }
            for (Path p : newEnds){
                p.nextPaths.add(copyPath(nextPath));
                calibrateCurrentSize(p);
            }

            calibrateCurrentSize(rawPath);

        } else if(root instanceof Pattern.Conditional){
            throw new RuntimeException("Pattern.Conditional not supported, please tell me which regex contains it.");
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty){
            if(((Pattern.CharProperty) root).charSet.size() == 0){
                generateCharSet((Pattern.CharProperty) root);
            }
            rawPath.path.add(new HashSet<>(((Pattern.CharProperty) root).charSet));
            rawPath.currentSize++;
            returnPaths(root.next, rawPath);
        } else if (root instanceof Pattern.SliceNode || root instanceof Pattern.BnM){
            for (int i : ((Pattern.SliceNode) root).buffer){
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.add(i);
                rawPath.path.add(tmpCharSet);
                rawPath.currentSize++;
            }
            returnPaths(root.next, rawPath);
        }

        // 其他的都是直接走next
        else{
            returnPaths(root.next, rawPath);
        }

        return;
    }

    void generateAttackArgs(Path root, ArrayList<Set<Integer>> rawPre){
        ArrayList<Set<Integer>> pre = new ArrayList<>(rawPre);
        pre.addAll(root.path);

        if (countingBiggerThan2.containsValue(root)){
            PrePaths.put(root, new ArrayList<>(pre));
        }

        if (countingBiggerThan2.containsKey(root)) {
            ArrayList<Set<Integer>> pump = new ArrayList<>(pre);
            Iterator iteratorPump = pump.iterator();
            Iterator iteratorPre = (PrePaths.get(countingBiggerThan2.get(root))).iterator();
            while (iteratorPump.hasNext() && iteratorPre.hasNext()) {
                Object curPump = iteratorPump.next();
                Object curPre = iteratorPre.next();
                if (curPump.equals(curPre)) {
                    // 注意！！！这里时Iterator.remove()!!!而不是list.remove()!!!
                    iteratorPump.remove();
                } else {
                    break;
                }
            }
            PumpPaths.put(root, pump);
        }

        for (Path p : root.nextPaths){
            generateAttackArgs(p, pre);
        }
    }

    Path copyPath(Path path){
        Path tmpPath = new Path(path);
        for (Path p : path.nextPaths){
            tmpPath.nextPaths.add(copyPath(p));
        }
        return tmpPath;
    }

    ArrayList<Path> reachEnds(Path path){
        ArrayList<Path> result = new ArrayList<>();
        if (path.nextPaths.size() == 0) {
            result.add(path);
        } else {
            for (Path p : path.nextPaths){
                result.addAll(reachEnds(p));
            }
        }
        return result;
    }

    void calibrateCurrentSize(Path path){
        Iterator iterator = path.nextPaths.iterator();
        while (iterator.hasNext()) {
            Object cur = iterator.next();
            if (((Path) cur).currentSize > maxLength) {
                iterator.remove();
            } else{
                ((Path) cur).currentSize = path.currentSize + ((Path) cur).path.size();
                calibrateCurrentSize(((Path) cur));
            }
        }
    }

    boolean cutFailedBrach(Path path){
        ArrayList<Path> tmpPaths = new ArrayList<>();
        Iterator iterator = path.nextPaths.iterator();
        while (iterator.hasNext()) {
            Object cur = iterator.next();
            if (!cutFailedBrach(((Path) cur))) {
                countingBiggerThan2.remove(cur);
                iterator.remove();
            } else {
                path.reachedEnd = true;
                // if (((Path) cur).path.size() == 0) {
                //     tmpPaths.addAll(((Path) cur).nextPaths);
                //     iterator.remove();
                // }
            }
        }
        path.nextPaths.addAll(tmpPaths);
        return path.reachedEnd;
    }

    public static class printPath{
        //获取特定类别的节点set
        private static final Pattern DotP = Pattern.compile(".");
        private static final Pattern BoundP = Pattern.compile("\\b");
        private static final Pattern SpaceP = Pattern.compile("\\s");
        private static final Pattern noneSpaceP = Pattern.compile("\\S");
        private static final Pattern wordP = Pattern.compile("\\w");

        static Set<Integer> Dot = getNodeCharSet(DotP.root.next);
        static Set<Integer> Bound = getNodeCharSet(BoundP.root.next);
        static Set<Integer> Space = getNodeCharSet(SpaceP.root.next);
        static Set<Integer> noneSpace = getNodeCharSet(noneSpaceP.root.next);
        static Set<Integer> word = getNodeCharSet(wordP.root.next);

        public static boolean equals(Set<?> set1, Set<?> set2){
            //null就直接不比了
            if(set1 == null || set2 ==null){
                return false;
            }
            //大小不同也不用比了
            if(set1.size()!=set2.size()){
                return false;
            }
            //最后比containsAll
            return set1.containsAll(set2);
        }

        private static String getContent(Path path){
            if (path.LastNode) {
                return "LastNode";
            }
            StringBuilder sb = new StringBuilder();
            int indexP = 0;
            if (path.path.size() == 0){
                return "None";
            }
            for (Set<Integer> set : path.path){
                if (indexP != 0) {
                    sb.append(",");
                }
                indexP++;

                sb.append("[");

                if (equals(set, Dot)){
                    sb.append(".");
                }
                else if (equals(set, Bound)){
                    sb.append("\\b");
                }
                else if (equals(set, Space)){
                    sb.append("\\s");
                }
                else if (equals(set, noneSpace)){
                    sb.append("\\S");
                }
                else if (equals(set, word)){
                    sb.append("\\w");
                }
                else{
                    int indexS = 0;
                    for (int i : set){
                        if(indexS != 0){
                            sb.append(",");
                        }
                        indexS++;
                        sb.append((char) i);
                    }
                }
                sb.append("]");
            }
            return sb.toString();
        }

        private static String getContent(ArrayList<Set<Integer>> list){
            StringBuilder sb = new StringBuilder();
            int indexP = 0;
            if (list.size() == 0){
                return "None";
            }
            for (Set<Integer> set : list){
                if (indexP != 0) {
                    sb.append(",");
                }
                indexP++;

                sb.append("[");

                if (equals(set, Dot)){
                    sb.append(".");
                }
                else if (equals(set, Bound)){
                    sb.append("\\b");
                }
                else if (equals(set, Space)){
                    sb.append("\\s");
                }
                else if (equals(set, noneSpace)){
                    sb.append("\\S");
                }
                else if (equals(set, word)){
                    sb.append("\\w");
                }
                else{
                    int indexS = 0;
                    for (int i : set){
                        if(indexS != 0){
                            sb.append(",");
                        }
                        indexS++;
                        sb.append((char) i);
                    }
                }
                sb.append("]");
            }
            return sb.toString();
        }

        public static void print(Path path){
            String pathName = path.toString().replace("regex.Analyzer$Path@", "");
            System.out.println(pathName + "[\"" + getContent(path) + "\"]");
            for (Path p : path.nextPaths){
                System.out.println(pathName + "-->" + p.toString().replace("regex.Analyzer$Path@", ""));
            }
            System.out.println();
            for (Path p : path.nextPaths){
                print(p);
            }
        }

        public static void printPump(Map<Path, ArrayList<Set<Integer>>> pump){
            ArrayList<String> list = new ArrayList<>();
            for (Map.Entry<Path, ArrayList<Set<Integer>>> entry : pump.entrySet()) {
                list.add(getContent(entry.getValue()).toString());
            }

            // sort list by length
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.length() - o1.length();
                }
            });

            for (String s : list){
                System.out.println(s);
                System.out.println("-------------------------------");
            }
        }
    }

    private static void generateCharSet(Pattern.CharProperty root){
        for(int i = 0; i < 65536; i++){
            if(root.isSatisfiedBy(i)){
                root.charSet.add(i);
            }
        }
    }

    private static Set<Integer> getNodeCharSet(Pattern.Node node){
        if(node instanceof Pattern.CharProperty){
            if(((Pattern.CharProperty) node).charSet.size()==0) {
                generateCharSet((Pattern.CharProperty) node);
            }
            return ((Pattern.CharProperty) node).charSet;
        }
        else if(node instanceof Pattern.SliceNode || node instanceof Pattern.BnM){
            Set<Integer> charSet = new HashSet<>();
            for (int i : ((Pattern.SliceNode) node).buffer){
                charSet.add(i);
            }
            return charSet;
        }
        else{
            return null;
        }
    }

}
