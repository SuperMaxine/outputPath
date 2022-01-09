package regex;

import javafx.util.Pair;
import redos.regex.redosPattern;

import java.awt.print.Printable;
import java.nio.file.Path;
import java.util.*;

/**
 * @author SuperMaxine
 */
public class Analyzer<comparePathLength> {

    Pattern pattern;
    int maxLength;
    static Set<Integer> fullCharSet;
    private ArrayList<Pattern.Node> OneLoopNodes;
    private Map<Pattern.Node, ArrayList<oldPath>> OneLoopPumpPaths;
    private Map<Pattern.Node, ArrayList<oldPath>> OneLoopPrePaths;
    private Map<Pattern.Node, String> OneLoopPreString;
    private Map<Pattern.Node, Set<Integer>> bigCharSet;

    enum returnPathsType{
        pump,
        pre
    }

    boolean getPathOverlap(oldPath path1, oldPath path2, oldPath result){
        // 如果两个路径的长度不同，则不可能有重叠
        if (path1.path.size() != path2.path.size()) {
            return false;
        }
        else {
            // 如果两个路径的长度相同，则需要比较每一个节点的字符集
            ArrayList<Set<Integer>> charSet1 = new ArrayList<>();
            for (int i = 0 ; i < path1.path.size(); i++){
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.addAll(path1.path.get(i));
                tmpCharSet.retainAll(path2.path.get(i));
                if (tmpCharSet.size() == 0){
                    return false;
                }
                else {
                    charSet1.add(tmpCharSet);
                }
            }
            result.path = charSet1;
            return true;
        }
    }

    public Analyzer(Pattern pattern, int maxLength){
        OneLoopNodes = new ArrayList<>();
        this.pattern = pattern;
        this.maxLength = maxLength;
        this.fullCharSet = new HashSet<>();
        OneLoopPumpPaths = new HashMap<>();
        OneLoopPrePaths = new HashMap<>();
        OneLoopPreString = new HashMap<>();
        bigCharSet = new HashMap<>();

        searchOneLoopNode(pattern.root, true);

        System.out.println("OneLoopNodes: " + OneLoopNodes.size());

        for (Pattern.Node node : OneLoopNodes) {
            OneLoopPumpPaths.put(node, retrunPaths(node, new oldPath(), maxLength, node.next, returnPathsType.pump));
            // OneLoopPrePaths.put(node, retrunPaths(pattern.root, new oldPath(), maxLength, node, returnPathsType.pre));
            OneLoopPrePaths.put(node, new ArrayList<>());
            retrunPaths(pattern.root, new oldPath(), maxLength, node, returnPathsType.pre);

            // printPaths(OneLoopPumpPaths.get(node));

            redosPattern testPattern = redosPattern.compile(pattern.pattern());
            for (oldPath prePath : OneLoopPrePaths.get(node)) {
                for (oldPath pumpPath : OneLoopPumpPaths.get(node)) {
                // for (int i = 0; i < OneLoopPumpPaths.get(node).size(); i++) {
                //     oldPath pumpPath = new oldPath();
                //     for (int j = i+1; j < OneLoopPumpPaths.get(node).size(); j++) {
                //         getPathOverlap(OneLoopPumpPaths.get(node).get(i), OneLoopPumpPaths.get(node).get(j), pumpPath);
                //     }
                    Enumerator preEnum = new Enumerator(prePath);
                    Enumerator pumpEnum = new Enumerator(pumpPath);

                    System.out.println("new PumpPath");
                    ArrayList<oldPath> pumpCheck = new ArrayList<oldPath>();
                    pumpCheck.add(pumpPath);
                    printPaths(pumpCheck);
                    System.out.println("new PrePath");
                    ArrayList<oldPath> preCheck = new ArrayList<oldPath>();
                    preCheck.add(prePath);
                    printPaths(preCheck);

                    System.out.println("brfore while");

                    if (preEnum.Empty()) {
                        while (pumpEnum.hasNext()) {
                            String pump = pumpEnum.next();
                            System.out.println(pump);
                            // if (pump.equals("aaa"))
                            //     System.out.println("aaa");
                            double matchingStepCnt = testPattern.getMatchingStepCnt("", pump, "\\b", 50, 10000000);
                            System.out.println(matchingStepCnt);
                            // if (pump.equals("abca"))
                            //     System.out.println("abca");
                            if (matchingStepCnt > 1e5) {
                                System.out.println("matchingStepCnt > 1e5");
                                return;
                            }
                            // System.out.println("");
                        }
                    }
                    else {
                        while (preEnum.hasNext()) {
                            String pre = preEnum.next();
                            while (pumpEnum.hasNext()) {
                                // System.out.println("brfore next");
                                String pump = pumpEnum.next();
                                // System.out.println(pre + pump);
                                double matchingStepCnt = testPattern.getMatchingStepCnt(pre, pump, "\\b", 50, 10000000);
                                System.out.println(matchingStepCnt);
                                if (matchingStepCnt > 1e5){
                                    System.out.println("matchingStepCnt > 1e5");
                                    return ;
                                }
                            }
                        }
                    }


                    System.out.println("-----------------");
                }
            }
        }



        System.out.println("[*] Analyzer done");
    }

    public void searchOneLoopNode(Pattern.Node root, boolean record){
        if (root == null || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            return;
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        if (root instanceof Pattern.Prolog) {
            searchOneLoopNode(((Pattern.Prolog)root).loop, record);
        } else if (root instanceof Pattern.Loop) {
            if (record) {
                OneLoopNodes.add(root);
            }
            searchOneLoopNode(((Pattern.Loop)root).body, record);
            searchOneLoopNode(((Pattern.Loop)root).next, record);
        } else if (root instanceof Pattern.Curly) {
            if (record) {
                OneLoopNodes.add(root);
            }
            searchOneLoopNode(((Pattern.Curly)root).atom, record);
            searchOneLoopNode(((Pattern.Curly)root).next, record);
        } else if (root instanceof Pattern.GroupCurly) {
            if (record) {
                OneLoopNodes.add(root);
            }
            searchOneLoopNode(((Pattern.GroupCurly)root).atom, record);
            searchOneLoopNode(((Pattern.GroupCurly)root).next, record);
        }

        // 2. 分支
        else if(root instanceof Pattern.Branch){
            for(Pattern.Node node : ((Pattern.Branch)root).atoms){
                if (node == null){
                    continue;
                }
                searchOneLoopNode(node, record);
            }
        } else if(root instanceof Pattern.Ques){
            searchOneLoopNode(((Pattern.Ques)root).atom, record);
            searchOneLoopNode(((Pattern.Ques)root).next, record);
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty){
            if(((Pattern.CharProperty) root).charSet.size() == 0){
                // generateCharSet((Pattern.CharProperty) root);
                generateFullCharSet((Pattern.CharProperty) root);
            }
            // path.path.add(new HashSet<>(((Pattern.CharProperty) root).charSet));
            // result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
            searchOneLoopNode(root.next, record);
        }

        else if (root instanceof Pattern.SliceNode || root instanceof Pattern.BnM){
            for (int i : ((Pattern.SliceNode) root).buffer){
                // Set<Integer> tmpCharSet = new HashSet<>();
                fullCharSet.add(i);
                // path.path.add(tmpCharSet);
            }
            // result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
            searchOneLoopNode(root.next, record);
        }

        // lookaround处理
        else if (root instanceof Pattern.Pos){
            searchOneLoopNode(((Pattern.Pos)root).cond, false);
            searchOneLoopNode(((Pattern.Pos)root).next, record);
        }else if (root instanceof Pattern.Neg){
            searchOneLoopNode(((Pattern.Neg)root).cond, false);
            searchOneLoopNode(((Pattern.Neg)root).next, record);
        }else if (root instanceof Pattern.Behind){
            searchOneLoopNode(((Pattern.Behind)root).cond, false);
            searchOneLoopNode(((Pattern.Behind)root).next, record);
        }else if (root instanceof Pattern.NotBehind){
            searchOneLoopNode(((Pattern.NotBehind)root).cond, false);
            searchOneLoopNode(((Pattern.NotBehind)root).next, record);
        }

        else {
            searchOneLoopNode(root.next, record);
        }

    }

    public ArrayList<oldPath> retrunPaths(Pattern.Node root, oldPath rawPath, int maxLength, Pattern.Node endNode, returnPathsType type){
        oldPath path = new oldPath(rawPath);
        ArrayList<oldPath> result = new ArrayList<>();
        if (root == null || path.reachEnd || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            result.add(path);
            return result;
        }else if (root == endNode){
            path.reachEnd = true;
            result.add(path);
            if (type == returnPathsType.pre){
                OneLoopPrePaths.get(endNode).add(path);
            }
            return result;
        } else if (root instanceof Pattern.LastNode || path.path.size() > maxLength){
            return result;
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        // 1. 循环
        if (root instanceof Pattern.Prolog) {
            result.addAll(retrunPaths(((Pattern.Prolog)root).loop, path, maxLength, endNode, type));
        } else if (root instanceof Pattern.Loop) {
            int limit = maxLength - path.path.size();

            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.addAll(retrunPaths(root.next, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            thisCyclePath.addAll(retrunPaths(((Pattern.Loop) root).body, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> lastPaths = new ArrayList<>(thisCyclePath);
            // lastPaths.add(path);
            for (int loopTime = 0; loopTime < ((Pattern.Loop) root).cmin; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath)cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath)cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }

            for (int loopTime = ((Pattern.Loop)root).cmin; loopTime < ((Pattern.Loop)root).cmax && loopTime < limit; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath)cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath)cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                for (oldPath p1 : lastPaths) {
                    for (oldPath p2 : nextPaths) {
                        oldPath p = new oldPath();
                        p.path.addAll(p1.path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            result.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }
            for (oldPath p1 : lastPaths) {
                for (oldPath p2 : nextPaths) {
                    oldPath p = new oldPath();
                    p.path.addAll(p1.path);
                    p.path.addAll(p2.path);
                    result.add(p);
                }
            }

        } else if (root instanceof Pattern.Curly) {
            int limit = maxLength - path.path.size();

            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.addAll(retrunPaths(root.next, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            thisCyclePath.addAll(retrunPaths(((Pattern.Curly) root).atom, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> lastPaths = new ArrayList<>(thisCyclePath);
            // lastPaths.add(path);
            for (int loopTime = 0; loopTime < ((Pattern.Curly) root).cmin; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath)cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath)cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }

            for (int loopTime = ((Pattern.Curly)root).cmin; loopTime < ((Pattern.Curly)root).cmax && loopTime < limit; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath)cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath)cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                for (oldPath p1 : lastPaths) {
                    for (oldPath p2 : nextPaths) {
                        oldPath p = new oldPath();
                        p.path.addAll(p1.path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            result.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }
            for (oldPath p1 : lastPaths) {
                for (oldPath p2 : nextPaths) {
                    oldPath p = new oldPath();
                    p.path.addAll(p1.path);
                    p.path.addAll(p2.path);
                    result.add(p);
                }
            }

        } else if (root instanceof Pattern.GroupCurly) {
            int limit = maxLength - path.path.size();

            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.addAll(retrunPaths(root.next, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            thisCyclePath.addAll(retrunPaths(((Pattern.GroupCurly) root).atom, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> lastPaths = new ArrayList<>(thisCyclePath);
            // lastPaths.add(path);
            for (int loopTime = 0; loopTime < ((Pattern.GroupCurly) root).cmin; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath)cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath)cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }

            for (int loopTime = ((Pattern.GroupCurly)root).cmin; loopTime < ((Pattern.GroupCurly)root).cmax && loopTime < limit; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath)cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath)cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                for (oldPath p1 : lastPaths) {
                    for (oldPath p2 : nextPaths) {
                        oldPath p = new oldPath();
                        p.path.addAll(p1.path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            result.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }
            for (oldPath p1 : lastPaths) {
                for (oldPath p2 : nextPaths) {
                    oldPath p = new oldPath();
                    p.path.addAll(p1.path);
                    p.path.addAll(p2.path);
                    result.add(p);
                }
            }
        }

        // 2. 分支
        else if(root instanceof Pattern.Branch){
            for(Pattern.Node node : ((Pattern.Branch)root).atoms){
                if (node == null){
                    continue;
                }
                result.addAll(retrunPaths(node, path, maxLength, endNode, type));
            }
        } else if (root instanceof Pattern.BranchConn) {
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        } else if(root instanceof Pattern.Ques){
            // TODO: 几种type并未区分
            // 1. 0或1
            ArrayList<oldPath> tmpPath1 = new ArrayList<>();
            tmpPath1.add(path);
            tmpPath1.addAll(retrunPaths(((Pattern.Ques) root).atom, path, maxLength, endNode, type));

            for(oldPath p : tmpPath1){
                result.addAll(retrunPaths(root.next, p, maxLength, endNode, type));
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
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        }

        else if (root instanceof Pattern.SliceNode || root instanceof Pattern.BnM){
            for (int i : ((Pattern.SliceNode) root).buffer){
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.add(i);
                path.path.add(tmpCharSet);
            }
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        }

        // lookaround处理
        // else if (root instanceof Pattern.Pos){
        //     ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.Pos) root).cond, maxLength));
        //     // 把tmpPath按照path的长度进行排序
        //     Collections.sort(tmpPath1, comparePathLength);
        //
        //     // 合并进posPath内
        //     ArrayList<Set<Integer>> posPath = new ArrayList<>(tmpPath1.get(0).path);
        //     for(oldPath p : tmpPath1){
        //         for(int i = 0; i < p.path.size(); i++){
        //             posPath.get(i).addAll(p.path.get(i));
        //         }
        //     }
        //
        //     // 如果posPath的长度小于path的长度，则把posPath的长度设置为path的长度
        //     if(path.posPath.size() < path.path.size() + posPath.size()){
        //         for(int i = path.posPath.size(); i < path.path.size(); i++){
        //             path.posPath.add(new HashSet<>());
        //         }
        //     }
        //
        //     // 将posPath添加到path.posPath中
        //     path.posPath.addAll(posPath);
        //
        //     result.addAll(retrunPaths(root.next, path, maxLength, endNode));
        // }else if (root instanceof Pattern.Neg){
        //     ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.Neg) root).cond, maxLength));
        //     // 把tmpPath按照path的长度进行排序
        //     Collections.sort(tmpPath1, comparePathLength);
        //
        //     // 合并进negPath内
        //     ArrayList<Set<Integer>> negPath = new ArrayList<>(tmpPath1.get(0).path);
        //     for(oldPath p : tmpPath1){
        //         for(int i = 0; i < p.path.size(); i++){
        //             negPath.get(i).addAll(p.path.get(i));
        //         }
        //     }
        //
        //     // 如果negPath的长度小于path的长度，则把negPath的长度设置为path的长度
        //     if(path.negPath.size() < path.path.size() + negPath.size()){
        //         for(int i = path.negPath.size(); i < path.path.size(); i++){
        //             path.negPath.add(new HashSet<>());
        //         }
        //     }
        //
        //     // 将negPath添加到path.negPath中
        //     path.negPath.addAll(negPath);
        //
        //     result.addAll(retrunPaths(root.next, path, maxLength, endNode));
        // }else if (root instanceof Pattern.Behind){
        //     ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.Behind) root).cond, maxLength));
        //     // 把tmpPath按照path的长度进行排序
        //     Collections.sort(tmpPath1, comparePathLength);
        //
        //     // 合并进behindPath内
        //     ArrayList<Set<Integer>> behindPath = new ArrayList<>(tmpPath1.get(0).path);
        //     for(oldPath p : tmpPath1){
        //         for(int i = 0; i < p.path.size(); i++){
        //             behindPath.get(i).addAll(p.path.get(i));
        //         }
        //     }
        //
        //     // 如果posPath的长度小于path的长度，则把posPath的长度设置为path的长度
        //     if(path.posPath.size() < path.path.size() + behindPath.size()){
        //         for(int i = path.posPath.size(); i < path.path.size(); i++){
        //             path.posPath.add(new HashSet<>());
        //         }
        //     }
        //
        //     // 将behindPath添加到path.posPath中
        //     System.out.println("path.path.size():"+path.path.size());
        //     System.out.println("behindPath.size():"+behindPath.size());
        //     for(int i = (path.path.size() >= behindPath.size())?path.path.size() - behindPath.size():0, j = 0; i < path.path.size(); i++, j++){
        //         path.posPath.get(i).addAll(behindPath.get(j));
        //     }
        //
        //     result.addAll(retrunPaths(root.next, path, maxLength, endNode));
        // }else if (root instanceof Pattern.NotBehind){
        //     ArrayList<oldPath> tmpPath1 = new ArrayList<>(oldgetPaths(((Pattern.NotBehind) root).cond, maxLength));
        //     // 把tmpPath按照path的长度进行排序
        //     Collections.sort(tmpPath1, comparePathLength);
        //
        //     // 合并进notBehindPath内
        //     ArrayList<Set<Integer>> notBehindPath = new ArrayList<>(tmpPath1.get(0).path);
        //     for(oldPath p : tmpPath1){
        //         for(int i = 0; i < p.path.size(); i++){
        //             notBehindPath.get(i).addAll(p.path.get(i));
        //         }
        //     }
        //
        //     // 如果negPath的长度小于path的长度，则把negPath的长度设置为path的长度
        //     if(path.negPath.size() < path.path.size() + notBehindPath.size()){
        //         for(int i = path.negPath.size(); i < path.path.size(); i++){
        //             path.negPath.add(new HashSet<>());
        //         }
        //     }
        //
        //     // 将notBehindPath添加到path.negPath中
        //     for(int i = (path.path.size() >= notBehindPath.size())?path.path.size() - notBehindPath.size():0, j = 0;i < path.path.size(); i++, j++){
        //         path.negPath.get(i).addAll(notBehindPath.get(j));
        //     }
        //
        //     result.addAll(retrunPaths(root.next, path, maxLength, endNode));
        // }

        // 其他的都是直接走next
        else{
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        }


        return result;
    }

    public static class oldPath {
        public boolean reachEnd;
        public ArrayList<Set<Integer>> path;
        public ArrayList<Set<Integer>> negPath; // negPath长于path是可以的,实际上最末尾的negPath并没有生效，直接扣会导致扣多，但结果至少是正确的
        public ArrayList<Set<Integer>> posPath; // posPath必须短于或等于path，因为如果长于path则不满足最末尾的posPath，导致不能被匹配

        public oldPath() {
            this.reachEnd = false;
            this.path = new ArrayList<>();
            this.negPath = new ArrayList<>();
            this.posPath = new ArrayList<>();
        }
        public oldPath(oldPath p) {
            this.reachEnd = p.reachEnd;
            this.path = new ArrayList<>(p.path);
            this.negPath = new ArrayList<>(p.negPath);
            this.posPath = new ArrayList<>(p.posPath);
        }

    }





    private static void generateCharSet(Pattern.CharProperty root){
        Set<Integer> charSet = new HashSet<>();
        for(int i = 0; i < 65536; i++){
            if(root.isSatisfiedBy(i)){
                charSet.add(i);
            }
        }
        

        // for (Integer c : fullCharSet) {
        //     if (root.isSatisfiedBy(c)) {
        //         root.charSet.add(c);
        //     }
        // }
    }


    private void generateFullCharSet(Pattern.CharProperty root){
        Set<Integer> charSet = new HashSet<>();
        for(int i = 0; i < 65536; i++){
            charSet.add(i);
        }
        // if (charSet.size() > 60000) {
        //     int count = 0;
        //     for (Integer c : charSet) {
        //         if (count >= 100) {
        //             return;
        //         } else {
        //             count++;
        //             fullCharSet.add(c);
        //         }
        //     }
        // } else {
        //     fullCharSet.addAll(charSet);
        // }
        if (charSet.size() < 30){
            fullCharSet.addAll(charSet);
        } else {
            bigCharSet.put(root, charSet);
        }
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
            // System.out.println("----");
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

    private class Enumerator {
        ArrayList<Integer> indexs;
        ArrayList<ArrayList<Integer>> path;
        public Enumerator(oldPath path){
            this.indexs = new ArrayList<>();
            this.path = new ArrayList<>();
            for (int i = 0; i < path.path.size(); i++) {
                this.path.add(new ArrayList<>(path.path.get(i)));
                this.indexs.add(0);
            }
        }

        public void reset(){
            for (int i = 0; i < this.indexs.size(); i++) {
                this.indexs.set(i, 0);
            }
        }

        public boolean Empty(){
            return this.indexs.size() == 0;
        }

        public boolean hasNext(){
            if (this.indexs.size() == 0){
                return false;
            }
            int t1 = this.indexs.get(0);
            int t2 = this.path.get(0).size();
            boolean result = t1 < t2;
            return result;
        }

        public String next(){
            // StringBuilder sb = new StringBuilder();
            String sb = "";
            for (int i = 0; i < path.size(); i++) {
                int tmp = path.get(i).get(indexs.get(i));
                sb += (char) tmp;
                // sb.append((path.get(i).get(indexs.get(i))));
            }
            // System.out.println(indexs.toString());
            for (int i = indexs.size() - 1; i >= 0 ; i--) {
                if (indexs.get(i) == path.get(i).size()){
                    indexs.set(i, 0);
                    continue;
                } else {
                    indexs.set(i, indexs.get(i) + 1);
                    // for (int j = i, j > 0 && indexs.get(i) == path.get(i).size(); j--) {
                    //     indexs.set(j-1, indexs.get(j-1) + 1);
                    //     indexs.set(j, 0);
                    // }
                    for (int j = i; j > 0 && indexs.get(j) == path.get(j).size(); j--) {
                        indexs.set(j-1, indexs.get(j-1) + 1);
                        indexs.set(j, 0);
                    }
                    break;
                }
            }
            return sb.toString();
        }
    }
}
