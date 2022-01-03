package regex;

import javafx.util.Pair;

import java.nio.file.Paths;
import java.util.*;

/**
 * @author SuperMaxine
 */
public class Analyzer {
    int maxLength;
    Pattern pattern;
    Path root;
    public Analyzer(Pattern pattern, int maxLength) {
        this.pattern = pattern;
        this.maxLength = maxLength;
        root = new Path(0);
        returnPaths(pattern.root, root);
    }

    class Path{
        ArrayList<Set<Integer>> path;
        int currentSize;
        ArrayList<Path> nextPaths;
        boolean reachedEnd;

        Path(int size){
            path = new ArrayList<>();
            currentSize = size;
            nextPaths = new ArrayList<>();
            reachedEnd = false;
        }

        Path(Path path){
            this.path = new ArrayList<>(path.path);
            this.currentSize = path.currentSize;
            this.nextPaths = new ArrayList<>();
            this.reachedEnd = path.reachedEnd;
        }
    }

    public void returnPaths(Pattern.Node root, Path rawPath) {
        if (root instanceof Pattern.LastNode){
            rawPath.reachedEnd = true;
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

            // 构造本节点的nextPaths
            Path tmpPath = copyPath(loopPath);
            for(int i = 1; i < ((Pattern.Loop)root).cmin; i++){
                addPathToEnds(tmpPath, loopPath);
            }
            for (int loopTimes = ((Pattern.Loop)root).cmin; loopTimes <= ((Pattern.Loop)root).cmax && loopTimes < maxLength; loopTimes++) {
                // 将循环路径复制一份，并且添加到循环路径的后面
                rawPath.nextPaths.add(tmpPath);
                addPathToEnds(tmpPath, loopPath);
            }

            // 将所有叶子节点送入next
            for(Path p : reachEnds(rawPath)){
                returnPaths(root.next, p);
            }

            if (((Pattern.Loop)root).cmin == 0) {
                Path nextPath = new Path(rawPath.currentSize);
                rawPath.nextPaths.add(nextPath);
                returnPaths(root.next, nextPath);
            }
        } else if (root instanceof Pattern.Curly) {
            // 获取Curly内的路径
            Path curlyPath = new Path(rawPath.currentSize);
            returnPaths(((Pattern.Curly)root).atom, curlyPath);

            // 构造本节点的nextPaths
            Path tmpPath = copyPath(curlyPath);
            for(int i = 1; i < ((Pattern.Curly)root).cmin; i++){
                addPathToEnds(tmpPath, curlyPath);
            }
            for (int curlyTimes = ((Pattern.Curly)root).cmin; curlyTimes <= ((Pattern.Curly)root).cmax && curlyTimes < maxLength; curlyTimes++) {
                // 将循环路径复制一份，并且添加到循环路径的后面
                rawPath.nextPaths.add(tmpPath);
                addPathToEnds(tmpPath, curlyPath);
            }

            // 将所有叶子节点送入next
            for(Path p : reachEnds(rawPath)){
                returnPaths(root.next, p);
            }

            if (((Pattern.Curly)root).cmin == 0) {
                Path nextPath = new Path(rawPath.currentSize);
                rawPath.nextPaths.add(nextPath);
                returnPaths(root.next, nextPath);
            }
        } else if (root instanceof Pattern.GroupCurly) {
            // 获取GroupCurly内的路径
            Path groupCurlyPath = new Path(rawPath.currentSize);
            returnPaths(((Pattern.GroupCurly)root).atom, groupCurlyPath);

            // 构造本节点的nextPaths
            Path tmpPath = copyPath(groupCurlyPath);
            for(int i = 1; i < ((Pattern.GroupCurly)root).cmin; i++){
                addPathToEnds(tmpPath, groupCurlyPath);
            }
            for (int groupCurlyTimes = ((Pattern.GroupCurly)root).cmin; groupCurlyTimes <= ((Pattern.GroupCurly)root).cmax && groupCurlyTimes < maxLength; groupCurlyTimes++) {
                // 将循环路径复制一份，并且添加到循环路径的后面
                rawPath.nextPaths.add(tmpPath);
                addPathToEnds(tmpPath, groupCurlyPath);
            }

            // 将所有叶子节点送入next
            for(Path p : reachEnds(rawPath)){
                returnPaths(root.next, p);
            }

            if (((Pattern.GroupCurly)root).cmin == 0) {
                Path nextPath = new Path(rawPath.currentSize);
                rawPath.nextPaths.add(nextPath);
                returnPaths(root.next, nextPath);
            }
        }

        // 2. 分支
        if (root instanceof Pattern.Branch) {
            for(Pattern.Node node : ((Pattern.Branch)root).atoms){
                if (node == null){
                    continue;
                }
                Path branchPath = new Path(rawPath.currentSize);
                rawPath.nextPaths.add(branchPath);
                returnPaths(node, branchPath);
            }
        } else if (root instanceof Pattern.Ques) {
            // 不进入Ques
            Path branchPath = new Path(rawPath.currentSize);
            rawPath.nextPaths.add(branchPath);
            returnPaths(root.next, branchPath);

            // 进入Ques
            // 获取Ques内的路径
            branchPath = new Path(rawPath.currentSize);
            rawPath.nextPaths.add(branchPath);
            returnPaths(((Pattern.Ques)root).atom, branchPath);
            // 将所有叶子节点送入next
            for(Path p : reachEnds(branchPath)){
                returnPaths(root.next, p);
            }
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

    Path copyPath(Path path){
        Path tmpPath = new Path(path);
        for (Path p : path.nextPaths){
            tmpPath.nextPaths.add(copyPath(p));
        }
        return tmpPath;
    }

    ArrayList<Path> reachEnds(Path path){
        ArrayList<Path> result = new ArrayList<>();
        for (Path p : path.nextPaths){
            result.addAll(reachEnds(p));
        }
        return result;
    }

    void addPathToEnds(Path targetPath, Path sourcePath){
        // 记得currentSize要每一位都加上
        for (Path end : reachEnds(sourcePath)){
            end.nextPaths.add(copyPath(targetPath));
            calibrateCurrentSize(end);
        }
    }

    void calibrateCurrentSize(Path path){
        for (Path p : path.nextPaths){
            p.currentSize = path.currentSize + p.path.size();
            calibrateCurrentSize(p);
        }
    }

    public static class oldPath {
        public boolean reachEnd;
        public ArrayList<Set<Integer>> path;
        public ArrayList<Set<Integer>> negPath; // negPath长于path是可以的,实际上最末尾的negPath并没有生效，直接扣会导致扣多，但结果至少是正确的
        public ArrayList<Set<Integer>> posPath; // posPath必须短于或等于path，因为如果长于path则不满足最末尾的posPath，导致不能被匹配
        public ArrayList<dividePoint> dividePoints;
        Map<String, Pair<String, Integer>> unmeetBranchConn;
        Map<String, Integer> cycleTimes;

        public oldPath() {
            this.reachEnd = false;
            this.path = new ArrayList<>();
            this.cycleTimes = new HashMap<>();
            this.negPath = new ArrayList<>();
            this.posPath = new ArrayList<>();
            this.dividePoints = new ArrayList<>();
            this.unmeetBranchConn = new HashMap<>();
        }
        public oldPath(oldPath p) {
            this.reachEnd = p.reachEnd;
            this.path = new ArrayList<>(p.path);
            this.cycleTimes = new HashMap<>(p.cycleTimes);
            this.negPath = new ArrayList<>(p.negPath);
            this.posPath = new ArrayList<>(p.posPath);
            this.dividePoints = new ArrayList<>(p.dividePoints);
            this.unmeetBranchConn = new HashMap<>(p.unmeetBranchConn);
        }

        private static class dividePoint {
            public String node;
            public int begin;
            public int end;

            public dividePoint(String node, int begin, int end) {
                this.node = node;
                this.begin = begin;
                this.end = end;
            }
        }
    }

    public static ArrayList<oldPath> oldgetPaths(Pattern.Node root, int maxLength){
        ArrayList<oldPath> paths = oldreturnPaths(root, new oldPath(), maxLength);
        for(oldPath p : paths){
            for(int i = 0; i < p.negPath.size() && i < p.path.size(); i++){
                Set<Integer> tmp = new HashSet<>(p.path.get(i));
                tmp.removeAll(p.negPath.get(i));
                p.path.set(i, tmp);
                if(p.path.get(i).size()==0){
                    p.reachEnd = false;
                }
            }
            for(int i = 0; i < p.posPath.size() && i < p.path.size(); i++){
                if(p.posPath.get(i).size() != 0){
                    Set<Integer> tmp = new HashSet<>(p.path.get(i));
                    tmp.retainAll(p.posPath.get(i));
                    p.path.set(i, tmp);
                }
                if(p.path.get(i).size()==0){
                    p.reachEnd = false;
                }
            }
        }
        return paths;
    }

    public static ArrayList<oldPath> getPaths(Pattern.Node root, int maxLength){
        ArrayList<oldPath> paths = oldreturnPaths(root, new oldPath(), maxLength);
        return paths;
    }

    private static void generateCharSet(Pattern.CharProperty root){
        for(int i = 0; i < 65536; i++){
            if(root.isSatisfiedBy(i)){
                root.charSet.add(i);
            }
        }
    }

    public static ArrayList<oldPath> oldreturnPaths(Pattern.Node root, oldPath rawPath, int maxLength){
        oldPath path = new oldPath(rawPath);
        ArrayList<oldPath> result = new ArrayList<>();
        if (root == null || path.path.size() > maxLength || path.reachEnd || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            result.add(path);
            return result;
        }else if (root instanceof Pattern.LastNode){
            path.reachEnd = true;
            result.add(path);
            return result;
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        // 1. 循环
        if (root instanceof Pattern.Prolog) {
            result.addAll(oldreturnPaths(((Pattern.Prolog)root).loop, path, maxLength));
        } else if (root instanceof Pattern.Loop) {
            // 前提是GroupTail到Loop的通路被打断

            // 在前往next节点前，本节点的所有结果放在tmpPath1
            ArrayList<oldPath> tmpPath1 = new ArrayList<>();

            // 重复循环，每次把上一轮循环结果存入tmpPath1后再送入下一轮循环
            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            ArrayList<oldPath> lastCyclePath = new ArrayList<>();
            lastCyclePath.add(path);
            for(int i = 0; i < ((Pattern.Loop) root).cmax && i < maxLength - path.path.size(); i++){
                path.cycleTimes.put(root.toString(), i + 1);
                for(oldPath p : lastCyclePath){
                    thisCyclePath.addAll(oldreturnPaths(((Pattern.Loop) root).body, p, maxLength));
                }
                tmpPath1.addAll(thisCyclePath);
                lastCyclePath = thisCyclePath;
                thisCyclePath = new ArrayList<>();
            }

            // 如果可以不进入循环节点，则将path也加入tmpPath1
            if (((Pattern.Loop) root).cmin == 0) {
                tmpPath1.add(path);
            }

            // 最后将所有结果送入next
            for(oldPath p : tmpPath1){
                // 为每一条path添加dividePoint
                if(p.path.size() != path.path.size()) {
                    p.dividePoints.add(new oldPath.dividePoint(root.toString(), path.path.size(), p.path.size()));
                }

                result.addAll(oldreturnPaths(root.next, p, maxLength));
            }
        } else if (root instanceof Pattern.Curly) {
            // 在前往next节点前，本节点的所有结果放在tmpPath1
            ArrayList<oldPath> tmpPath1 = new ArrayList<>();

            // 重复循环，每次把上一轮循环结果存入tmpPath1后再送入下一轮循环
            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            ArrayList<oldPath> lastCyclePath = new ArrayList<>();
            lastCyclePath.add(path);
            for(int i = 0; i < ((Pattern.Curly) root).cmax && i < maxLength - path.path.size(); i++){
                path.cycleTimes.put(root.toString(), i + 1);
                for(oldPath p : lastCyclePath){
                    thisCyclePath.addAll(oldreturnPaths(((Pattern.Curly) root).atom, p, maxLength));
                }
                tmpPath1.addAll(thisCyclePath);
                lastCyclePath = thisCyclePath;
                thisCyclePath = new ArrayList<>();
            }

            // 如果可以不进入循环节点，则将path也加入tmpPath1
            if (((Pattern.Curly) root).cmin == 0) {
                tmpPath1.add(path);
            }

            // 最后将所有结果送入next
            for(oldPath p : tmpPath1){
                // 为每一条path添加dividePoint
                if(p.path.size() != path.path.size()) {
                    p.dividePoints.add(new oldPath.dividePoint(root.toString(), path.path.size(), p.path.size()));
                }

                result.addAll(oldreturnPaths(root.next, p, maxLength));
            }
        } else if (root instanceof Pattern.GroupCurly) {
            // 在前往next节点前，本节点的所有结果放在tmpPath1
            ArrayList<oldPath> tmpPath1 = new ArrayList<>();

            // 重复循环，每次把上一轮循环结果存入tmpPath1后再送入下一轮循环
            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            ArrayList<oldPath> lastCyclePath = new ArrayList<>();
            lastCyclePath.add(path);
            for(int i = 0; i < ((Pattern.GroupCurly) root).cmax && i < maxLength - path.path.size(); i++){
                path.cycleTimes.put(root.toString(), i + 1);
                for(oldPath p : lastCyclePath){
                    thisCyclePath.addAll(oldreturnPaths(((Pattern.GroupCurly) root).atom, p, maxLength));
                }
                tmpPath1.addAll(thisCyclePath);
                lastCyclePath = thisCyclePath;
                thisCyclePath = new ArrayList<>();
            }

            // 如果可以不进入循环节点，则将path也加入tmpPath1
            if (((Pattern.GroupCurly) root).cmin == 0) {
                tmpPath1.add(path);
            }

            // 最后将所有结果送入next
            for(oldPath p : tmpPath1){
                // 为每一条path添加dividePoint
                if(p.path.size() != path.path.size()) {
                    p.dividePoints.add(new oldPath.dividePoint(root.toString(), path.path.size(), p.path.size()));
                }

                result.addAll(oldreturnPaths(root.next, p, maxLength));
            }
        }

        // 2. 分支
        else if(root instanceof Pattern.Branch){
            // 记录unmatchedBranchConn
            path.unmeetBranchConn.put(((Pattern.Branch) root).conn.toString(), new Pair<>(root.toString(), path.path.size()));

            for(Pattern.Node node : ((Pattern.Branch)root).atoms){
                if (node == null){
                    continue;
                }
                result.addAll(oldreturnPaths(node, path, maxLength));
            }
        } else if (root instanceof Pattern.BranchConn) {
            // 完成dividePoint
            Pair<String, Integer> unmeetBranchConn = path.unmeetBranchConn.get(root.toString());
            if(unmeetBranchConn.getValue() != path.path.size()) {
                path.dividePoints.add(new oldPath.dividePoint(unmeetBranchConn.getKey(), unmeetBranchConn.getValue(), path.path.size()));
            }
            // 删除unmatchedBranchConn
            path.unmeetBranchConn.remove(root.toString());

            result.addAll(oldreturnPaths(root.next, path, maxLength));
        } else if(root instanceof Pattern.Ques){
            // TODO: 几种type并未区分
            // 1. 0或1
            ArrayList<oldPath> tmpPath1 = new ArrayList<>();
            tmpPath1.add(path);
            tmpPath1.addAll(oldreturnPaths(((Pattern.Ques) root).atom, path, maxLength));

            for(oldPath p : tmpPath1){
                result.addAll(oldreturnPaths(root.next, p, maxLength));
            }
        } else if(root instanceof Pattern.Conditional){
            throw new RuntimeException("Pattern.Conditional not supported, please tell me which regex contains it.");
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty){
            if(((Pattern.CharProperty) root).charSet.size() == 0){
                generateCharSet((Pattern.CharProperty) root);
            }
            path.path.add(new HashSet<>(((Pattern.CharProperty) root).charSet));
            result.addAll(oldreturnPaths(root.next, path, maxLength));
        }

        else if (root instanceof Pattern.SliceNode || root instanceof Pattern.BnM){
            for (int i : ((Pattern.SliceNode) root).buffer){
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.add(i);
                path.path.add(tmpCharSet);
            }
            result.addAll(oldreturnPaths(root.next, path, maxLength));
        }

        // lookaround处理
        else if (root instanceof Pattern.Pos){
            ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.Pos) root).cond, maxLength));
            // 把tmpPath按照path的长度进行排序
            Collections.sort(tmpPath1, comparePathLength);

            // 合并进posPath内
            ArrayList<Set<Integer>> posPath = new ArrayList<>(tmpPath1.get(0).path);
            for(oldPath p : tmpPath1){
                for(int i = 0; i < p.path.size(); i++){
                    posPath.get(i).addAll(p.path.get(i));
                }
            }

            // 如果posPath的长度小于path的长度，则把posPath的长度设置为path的长度
            if(path.posPath.size() < path.path.size() + posPath.size()){
                for(int i = path.posPath.size(); i < path.path.size(); i++){
                    path.posPath.add(new HashSet<>());
                }
            }

            // 将posPath添加到path.posPath中
            path.posPath.addAll(posPath);

            result.addAll(oldreturnPaths(root.next, path, maxLength));
        }else if (root instanceof Pattern.Neg){
            ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.Neg) root).cond, maxLength));
            // 把tmpPath按照path的长度进行排序
            Collections.sort(tmpPath1, comparePathLength);

            // 合并进negPath内
            ArrayList<Set<Integer>> negPath = new ArrayList<>(tmpPath1.get(0).path);
            for(oldPath p : tmpPath1){
                for(int i = 0; i < p.path.size(); i++){
                    negPath.get(i).addAll(p.path.get(i));
                }
            }

            // 如果negPath的长度小于path的长度，则把negPath的长度设置为path的长度
            if(path.negPath.size() < path.path.size() + negPath.size()){
                for(int i = path.negPath.size(); i < path.path.size(); i++){
                    path.negPath.add(new HashSet<>());
                }
            }

            // 将negPath添加到path.negPath中
            path.negPath.addAll(negPath);

            result.addAll(oldreturnPaths(root.next, path, maxLength));
        }else if (root instanceof Pattern.Behind){
            ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.Behind) root).cond, maxLength));
            // 把tmpPath按照path的长度进行排序
            Collections.sort(tmpPath1, comparePathLength);

            // 合并进behindPath内
            ArrayList<Set<Integer>> behindPath = new ArrayList<>(tmpPath1.get(0).path);
            for(oldPath p : tmpPath1){
                for(int i = 0; i < p.path.size(); i++){
                    behindPath.get(i).addAll(p.path.get(i));
                }
            }

            // 如果posPath的长度小于path的长度，则把posPath的长度设置为path的长度
            if(path.posPath.size() < path.path.size() + behindPath.size()){
                for(int i = path.posPath.size(); i < path.path.size(); i++){
                    path.posPath.add(new HashSet<>());
                }
            }

            // 将behindPath添加到path.posPath中
            System.out.println("path.path.size():"+path.path.size());
            System.out.println("behindPath.size():"+behindPath.size());
            for(int i = (path.path.size() >= behindPath.size())?path.path.size() - behindPath.size():0, j = 0; i < path.path.size(); i++, j++){
                path.posPath.get(i).addAll(behindPath.get(j));
            }

            result.addAll(oldreturnPaths(root.next, path, maxLength));
        }else if (root instanceof Pattern.NotBehind){
            ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.NotBehind) root).cond, maxLength));
            // 把tmpPath按照path的长度进行排序
            Collections.sort(tmpPath1, comparePathLength);

            // 合并进notBehindPath内
            ArrayList<Set<Integer>> notBehindPath = new ArrayList<>(tmpPath1.get(0).path);
            for(oldPath p : tmpPath1){
                for(int i = 0; i < p.path.size(); i++){
                    notBehindPath.get(i).addAll(p.path.get(i));
                }
            }

            // 如果negPath的长度小于path的长度，则把negPath的长度设置为path的长度
            if(path.negPath.size() < path.path.size() + notBehindPath.size()){
                for(int i = path.negPath.size(); i < path.path.size(); i++){
                    path.negPath.add(new HashSet<>());
                }
            }

            // 将notBehindPath添加到path.negPath中
            for(int i = (path.path.size() >= notBehindPath.size())?path.path.size() - notBehindPath.size():0, j = 0;i < path.path.size(); i++, j++){
                path.negPath.get(i).addAll(notBehindPath.get(j));
            }

            result.addAll(oldreturnPaths(root.next, path, maxLength));
        }

        // 其他的都是直接走next
        else{
            result.addAll(oldreturnPaths(root.next, path, maxLength));
        }


        return result;
    }

    public static Comparator<oldPath> comparePathLength = new Comparator<oldPath>() {
        @Override
        public int compare(oldPath o1, oldPath o2) {
            // 从大到小排序
            return o2.path.size() - o1.path.size();
        }
    };

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

    private static final Pattern DotP = Pattern.compile(".");
    private static final Pattern BoundP = Pattern.compile("\\b");
    private static final Pattern SpaceP = Pattern.compile("\\s");
    private static final Pattern noneSpaceP = Pattern.compile("\\S");
    private static final Pattern wordP = Pattern.compile("\\w");

    public static void printPaths(ArrayList<oldPath> paths){
        /**
         * 对照表
         * \b : [8]
         * \\s : [32, 9, 10, 11, 12, 13]
         * . : size() == 65531
         */

        //获取特定类别的节点set
        Set<Integer> Dot = getNodeCharSet(DotP.root.next);
        Set<Integer> Bound = getNodeCharSet(BoundP.root.next);
        Set<Integer> Space = getNodeCharSet(SpaceP.root.next);
        Set<Integer> noneSpace = getNodeCharSet(noneSpaceP.root.next);
        Set<Integer> word = getNodeCharSet(wordP.root.next);

        for(oldPath p : paths){
            System.out.println("----");
            int indexP = 0;
            for(Set<Integer> s : p.path){
                if (indexP != 0) {
                    System.out.print(",");
                }
                indexP++;
                System.out.print("[");
                if (equals(s, Dot)){
                    System.out.print(".");
                }
                else if (equals(s, Bound)){
                    System.out.print("\\b");
                }
                else if (equals(s, Space)){
                    System.out.print("\\s");
                }
                else if (equals(s, noneSpace)){
                    System.out.print("\\S");
                }
                else if (equals(s, word)){
                    System.out.print("\\w");
                }
                else{
                    int indexS = 0;
                    for (int i : s){
                        if(indexS != 0){
                            System.out.print(",");
                        }
                        indexS++;
                        System.out.print((char) i);
                    }
                }
                System.out.print("]");
            }
            System.out.println("");
        }
    }
}
