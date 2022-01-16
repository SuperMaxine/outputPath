package com.company.regex;

import com.company.redos.regex.Pattern4Search;
import com.company.redos.regex.redosPattern;

import java.util.*;

/**
 * @author SuperMaxine
 */
public class Analyzer<comparePathLength> {

    private static final Pattern DotP = Pattern.compile(".");
    private static final Pattern BoundP = Pattern.compile("\\b");
    private static final Pattern SpaceP = Pattern.compile("\\s");
    private static final Pattern noneSpaceP = Pattern.compile("\\S");
    private static final Pattern wordP = Pattern.compile("\\w");

    private final boolean OneCounting = false;
    private final boolean POA = false;
    private final boolean SLQ = true;
    private final Pattern4Search testPattern;
    private final redosPattern testPattern4Search;
    public boolean attackable = false;
    public String attackMsg = "";
    Pattern pattern;
    int maxLength;
    private Set<Integer> fullSmallCharSet;
    private Map<Pattern.Node, Set<Integer>> bigCharSetMap;
    private ArrayList<Pattern.Node> OneLoopNodes;
    private Map<Pattern.Node, ArrayList<oldPath>> OneLoopPumpPaths;
    private Map<Pattern.Node, ArrayList<oldPath>> OneLoopPrePaths;
    private ArrayList<oldPath> fixedPrePaths;
    private boolean lookaround;

    private ArrayList<Pattern.Node> allCountingAtBegin = new ArrayList<>();

    public Analyzer() {
        this.testPattern = null;
        this.testPattern4Search = null;
    }

    public Analyzer(Pattern pattern, int maxLength, String rawPattern) {
        OneLoopNodes = new ArrayList<>();
        this.pattern = pattern;
        this.maxLength = maxLength;
        fullSmallCharSet = new HashSet<>();
        OneLoopPumpPaths = new HashMap<>();
        OneLoopPrePaths = new HashMap<>();
        bigCharSetMap = new HashMap<>();
        fixedPrePaths = new ArrayList<>();
        fixedPrePaths.add(new oldPath());
        lookaround = false;
        this.testPattern = Pattern4Search.compile(rawPattern);
        this.testPattern4Search = redosPattern.compile(rawPattern);

        searchOneLoopNode(pattern.root, true);


        String regex = ".{1,3}";
        Pattern p = Pattern.compile(regex);
        generateCharSet((Pattern.CharProperty) ((Pattern.Curly) p.root.next).atom);
        bigCharSetMap.put(((Pattern.Curly) p.root.next).atom, ((Pattern.CharProperty) ((Pattern.Curly) p.root.next).atom).charSet);
        ((Pattern.CharProperty) ((Pattern.Curly) p.root.next).atom).charSet = new HashSet<>();

        // Done:关于压缩字符集，目前会在returnPaths中重复生成，考虑直接使用node.charSet，待优化
        generateAllBigCharSet();
        if (pattern.root.next instanceof Pattern.Branch) {
            fixedPrePaths = retrunPaths(p.root, new oldPath(), 3, p.root.next.next.next, returnPathsType.pump);
        }


        // System.out.println("OneLoopNodes: " + OneLoopNodes.size());

        // 获取每一个生成每一个Loop相关的数据，并进行OneLoop类型处理
        for (Pattern.Node node : OneLoopNodes) {
            OneLoopPumpPaths.put(node, retrunPaths(node, new oldPath(), maxLength, node.next, returnPathsType.pump));
            // OneLoopPrePaths.put(node, retrunPaths(pattern.root, new oldPath(), maxLength, node, returnPathsType.pre));
            OneLoopPrePaths.put(node, new ArrayList<>());
            retrunPaths(pattern.root, new oldPath(), maxLength, node, returnPathsType.pre);

            // System.out.println("OneLoopPumpPaths: " + OneLoopPumpPaths.get(node).size());
            // printPaths(OneLoopPumpPaths.get(node));
            // System.out.println("OneLoopPrePaths: " + OneLoopPrePaths.get(node).size());
            // printPaths(OneLoopPrePaths.get(node));
        }

        // TODO: 在后面的循环中加入判断 !Thread.currentThread().isInterrupted() 并return

        if (OneCounting) {
            for (Pattern.Node node : OneLoopNodes) {
                // 前缀变成\.{0,3}×前缀×中缀
                ArrayList<oldPath> newPrePaths = new ArrayList<>();
                for (oldPath rawPrePath : OneLoopPrePaths.get(node)) {
                    for (oldPath pumpPath : OneLoopPumpPaths.get(node)) {
                        for (oldPath fixedPrePath : fixedPrePaths) {
                            oldPath newPrePath = new oldPath();
                            newPrePath.path.addAll(fixedPrePath.path);
                            newPrePath.path.addAll(rawPrePath.path);
                            newPrePath.path.addAll(pumpPath.path);
                            newPrePaths.add(newPrePath);
                        }
                    }
                }

                // 排序前缀和中缀
                Collections.sort((newPrePaths), new Comparator<oldPath>() {
                    @Override
                    public int compare(oldPath o1, oldPath o2) {
                        return o1.path.size() - o2.path.size();
                    }
                });
                Collections.sort(OneLoopPumpPaths.get(node), new Comparator<oldPath>() {
                    @Override
                    public int compare(oldPath o1, oldPath o2) {
                        return o1.path.size() - o2.path.size();
                    }
                });

                // System.out.println("--------------------Pre--------------------");
                // printPaths(newPrePaths);
                // System.out.println("--------------------Pump--------------------");
                // printPaths(OneLoopPumpPaths.get(node));

                for (oldPath prePath : newPrePaths) {
                    // for (oldPath pumpPath : OneLoopPumpPaths.get(node)) {
                    for (int i = 0; i < OneLoopPumpPaths.get(node).size(); i++) {
                        oldPath pumpPath = new oldPath();
                        for (int j = i + 1; j < OneLoopPumpPaths.get(node).size(); j++) {
                            if(Thread.currentThread().isInterrupted()){
                                System.out.println("线程请求中断...");
                                return;
                            }
                            if (getPathTotalOverlap(OneLoopPumpPaths.get(node).get(i), OneLoopPumpPaths.get(node).get(j), pumpPath)) {
                                Enumerator preEnum = new Enumerator(prePath);
                                Enumerator pumpEnum = new Enumerator(pumpPath);

                                // ArrayList<oldPath> forPrint = new ArrayList<>();
                                // forPrint.add(pumpPath);
                                // printPaths(forPrint);
                                //
                                // System.out.println("new PumpPath");
                                // ArrayList<oldPath> pumpCheck = new ArrayList<oldPath>();
                                // pumpCheck.add(pumpPath);
                                // printPaths(pumpCheck);
                                // System.out.println("new PrePath");
                                // ArrayList<oldPath> preCheck = new ArrayList<oldPath>();
                                // preCheck.add(prePath);
                                // printPaths(preCheck);
                                //
                                // System.out.println("brfore while");

                                if (preEnum.Empty()) {
                                    while (pumpEnum.hasNext()) {
                                        String pump = pumpEnum.next();
                                        // System.out.println(pump);
                                        // if (pump.equals("aaa"))
                                        //     System.out.println("aaa");
                                        double matchingStepCnt = testPattern.getMatchingStepCnt("", pump, "\\b", 50, 100000);
                                        // System.out.println(matchingStepCnt);
                                        // if (pump.equals("abca"))
                                        //     System.out.println("abca");
                                        if (matchingStepCnt > 1e5) {
                                            attackable = true;
                                            attackMsg = "OneCounting\nprefix:\n" + "pump:" + pump + "\nsuffix:\\n\\b\\n";
                                            return;
                                        }
                                        // System.out.println("");
                                    }
                                } else {
                                    while (preEnum.hasNext()) {
                                        String pre = preEnum.next();
                                        while (pumpEnum.hasNext()) {
                                            // System.out.println("brfore next");
                                            String pump = pumpEnum.next();
                                            // System.out.println(pre + pump);
                                            double matchingStepCnt = testPattern.getMatchingStepCnt(pre, pump, "\\b", 50, 100000);
                                            // System.out.println(matchingStepCnt);
                                            if(Thread.currentThread().isInterrupted()){
                                                System.out.println("线程请求中断...");
                                                return;
                                            }
                                            if (matchingStepCnt > 1e5) {
                                                attackable = true;
                                                attackMsg = "OneCounting\nprefix:" + pre + "\n" + "pump:" + pump + "\nsuffix:\\n\\b\\n";
                                                return;
                                            }
                                        }
                                    }
                                }


                                // System.out.println("-----------------");
                            }
                        }

                    }
                }
            }
        }

        if (POA) {
            for (int i = 0; i < OneLoopNodes.size(); i++) {
                for (int j = i + 1; j < OneLoopNodes.size(); j++) {
                    if(Thread.currentThread().isInterrupted()){
                        System.out.println("线程请求中断...");
                        return;
                    }
                    // 判断嵌套、直接相邻，以及夹着内容相邻
                    int type = 0; // 0为嵌套等不需要考虑的情况，1为两者直接相邻，2为两者夹着东西
                    Pattern.Node frontNode = null, backNode = null;
                    ArrayList<oldPath> midPaths = retrunPaths(OneLoopNodes.get(i).next, new oldPath(), maxLength, OneLoopNodes.get(j), returnPathsType.pump);
                    boolean reachEnd = false;
                    int mid = Integer.MAX_VALUE;
                    for (oldPath path : midPaths) {
                        if (path.reachEnd) {
                            reachEnd = true;
                            if (path.path.size() < mid) mid = path.path.size();
                        }
                    }
                    if (reachEnd) {
                        // 说明OneLoopNodes.get(j)在OneLoopNodes.get(i)的next路径上
                        frontNode = OneLoopNodes.get(i);
                        backNode = OneLoopNodes.get(j);
                        //判断两者是否紧挨着
                        if (mid == 0) type = 1;
                        else {
                            // 中间夹着内容
                            type = 2;
                        }
                    } else {
                        midPaths = retrunPaths(OneLoopNodes.get(j).next, new oldPath(), maxLength, OneLoopNodes.get(i), returnPathsType.pump);
                        reachEnd = false;
                        mid = Integer.MAX_VALUE;
                        for (oldPath path : midPaths) {
                            if (path.reachEnd) {
                                reachEnd = true;
                                if (path.path.size() < mid) mid = path.path.size();
                            }
                        }
                        if (reachEnd) {
                            // 说明OneLoopNodes.get(i)在OneLoopNodes.get(j)的next路径上
                            frontNode = OneLoopNodes.get(i);
                            backNode = OneLoopNodes.get(j);
                            if (mid == 0) type = 1;
                            else {
                                // 中间夹着内容
                                type = 2;
                            }
                        }
                    }

                    if (type == 0) {
                        continue;
                    } else if (type == 1) {
                        // 两者直接相邻
                        // 获取中缀集合
                        ArrayList<oldPath> pumpPaths = new ArrayList<>();
                        for (int k = 0; k < OneLoopPumpPaths.get(OneLoopNodes.get(i)).size(); k++) {
                            for (int l = 0; l < OneLoopPumpPaths.get(OneLoopNodes.get(j)).size(); l++) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                // 两个路径集合中的路径两两配对，求重叠路径
                                oldPath pumpPath = new oldPath();
                                if (getPathTotalOverlap(OneLoopPumpPaths.get(OneLoopNodes.get(i)).get(k), OneLoopPumpPaths.get(OneLoopNodes.get(j)).get(l), pumpPath)) {
                                    pumpPaths.add(pumpPath);
                                }
                            }
                        }

                        // \.{0,3}×前缀×中缀
                        ArrayList<oldPath> newPrePaths = new ArrayList<>();
                        for (oldPath rawPrePath : OneLoopPrePaths.get(frontNode)) {
                            for (oldPath pumpPath : pumpPaths) {
                                for (oldPath fixedPrePath : fixedPrePaths) {
                                    if(Thread.currentThread().isInterrupted()){
                                        System.out.println("线程请求中断...");
                                        return;
                                    }
                                    oldPath newPrePath = new oldPath();
                                    newPrePath.path.addAll(fixedPrePath.path);
                                    newPrePath.path.addAll(rawPrePath.path);
                                    newPrePath.path.addAll(pumpPath.path);
                                    newPrePaths.add(newPrePath);
                                }
                            }
                        }

                        // 排序前缀和中缀
                        Collections.sort((newPrePaths), new Comparator<oldPath>() {
                            @Override
                            public int compare(oldPath o1, oldPath o2) {
                                return o1.path.size() - o2.path.size();
                            }
                        });
                        Collections.sort((pumpPaths), new Comparator<oldPath>() {
                            @Override
                            public int compare(oldPath o1, oldPath o2) {
                                return o1.path.size() - o2.path.size();
                            }
                        });

                        for (oldPath prePath : newPrePaths) {
                            for (oldPath pumpPath : pumpPaths) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                Enumerator preEnum = new Enumerator(prePath);
                                Enumerator pumpEnum = new Enumerator(pumpPath);
                                if (dynamicValidate(preEnum, pumpEnum, "POA")) return;
                            }
                        }
                    } else if (type == 2) {
                        // \w+0\d+
                        // 从midPaths中去除reachEnd为false的元素
                        Iterator iterator = midPaths.iterator();
                        while (iterator.hasNext()) {
                            Object cur = iterator.next();
                            if (!((oldPath) cur).reachEnd) {
                                iterator.remove();
                            }
                        }
                        ArrayList<oldPath> frontPaths = OneLoopPumpPaths.get(frontNode); //\w+
                        ArrayList<oldPath> backPaths = OneLoopPumpPaths.get(backNode); //\d+

                        //------------------------------- \w+ vs 0\d+ -------------------------------
                        // 获取tailPaths，0\d+（将midPaths分别缀在OneLoopNodes.get(j)的末尾）
                        ArrayList<oldPath> tailPaths = new ArrayList<oldPath>();
                        for (oldPath midp : midPaths) {
                            for (oldPath backp : backPaths) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                oldPath tmpPath = new oldPath();
                                tmpPath.path.addAll(midp.path);
                                tmpPath.path.addAll(backp.path);
                                tailPaths.add(tmpPath);
                            }
                        }

                        ArrayList<oldPath> pumpPaths = new ArrayList<>();

                        for (int k = 0; k < frontPaths.size(); k++) {
                            for (int l = 0; l < tailPaths.size(); l++) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                // 两个路径集合中的路径两两配对，求重叠路径
                                oldPath pumpPath = new oldPath();
                                if (getPathTotalOverlap(frontPaths.get(k), tailPaths.get(l), pumpPath)) {
                                    pumpPaths.add(pumpPath);
                                }
                            }
                        }

                        // \.{0,3}×前缀×中缀
                        ArrayList<oldPath> newPrePaths = new ArrayList<>();
                        for (oldPath rawPrePath : OneLoopPrePaths.get(frontNode)) {
                            for (oldPath pumpPath : pumpPaths) {
                                for (oldPath fixedPrePath : fixedPrePaths) {
                                    if(Thread.currentThread().isInterrupted()){
                                        System.out.println("线程请求中断...");
                                        return;
                                    }
                                    oldPath newPrePath = new oldPath();
                                    newPrePath.path.addAll(fixedPrePath.path);
                                    newPrePath.path.addAll(rawPrePath.path);
                                    newPrePath.path.addAll(pumpPath.path);
                                    newPrePaths.add(newPrePath);
                                }
                            }
                        }

                        // 排序前缀和中缀
                        Collections.sort((newPrePaths), new Comparator<oldPath>() {
                            @Override
                            public int compare(oldPath o1, oldPath o2) {
                                return o1.path.size() - o2.path.size();
                            }
                        });
                        Collections.sort((pumpPaths), new Comparator<oldPath>() {
                            @Override
                            public int compare(oldPath o1, oldPath o2) {
                                return o1.path.size() - o2.path.size();
                            }
                        });

                        for (oldPath prePath : newPrePaths) {
                            for (oldPath pumpPath : pumpPaths) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                Enumerator preEnum = new Enumerator(prePath);
                                Enumerator pumpEnum = new Enumerator(pumpPath);
                                if (dynamicValidate(preEnum, pumpEnum, "POA")) return;
                            }
                        }

                        //------------------------------- \w+0 vs \d+ -------------------------------
                        // 获取\w+0（将midPaths分别缀在OneLoopNodes.get(j)的末尾）
                        ArrayList<oldPath> headPaths = new ArrayList<oldPath>();
                        for (oldPath midp : midPaths) {
                            for (oldPath backp : backPaths) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                oldPath tmpPath = new oldPath();
                                tmpPath.path.addAll(backp.path);
                                tmpPath.path.addAll(midp.path);
                                headPaths.add(tmpPath);
                            }
                        }

                        pumpPaths = new ArrayList<>();
                        for (int k = 0; k < frontPaths.size(); k++) {
                            for (int l = 0; l < tailPaths.size(); l++) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                // 两个路径集合中的路径两两配对，求重叠路径
                                oldPath pumpPath = new oldPath();
                                if (getPathTotalOverlap(frontPaths.get(k), headPaths.get(l), pumpPath)) {
                                    pumpPaths.add(pumpPath);
                                }
                            }
                        }

                        // \.{0,3}×前缀×中缀
                        newPrePaths = new ArrayList<>();
                        for (oldPath rawPrePath : OneLoopPrePaths.get(frontNode)) {
                            for (oldPath pumpPath : pumpPaths) {
                                for (oldPath fixedPrePath : fixedPrePaths) {
                                    if(Thread.currentThread().isInterrupted()){
                                        System.out.println("线程请求中断...");
                                        return;
                                    }
                                    oldPath newPrePath = new oldPath();
                                    newPrePath.path.addAll(fixedPrePath.path);
                                    newPrePath.path.addAll(rawPrePath.path);
                                    newPrePath.path.addAll(pumpPath.path);
                                    newPrePaths.add(newPrePath);
                                }
                            }
                        }


                        // 排序前缀和中缀
                        Collections.sort((newPrePaths), new Comparator<oldPath>() {
                            @Override
                            public int compare(oldPath o1, oldPath o2) {
                                return o1.path.size() - o2.path.size();
                            }
                        });
                        Collections.sort((pumpPaths), new Comparator<oldPath>() {
                            @Override
                            public int compare(oldPath o1, oldPath o2) {
                                return o1.path.size() - o2.path.size();
                            }
                        });

                        for (oldPath prePath : newPrePaths) {
                            for (oldPath pumpPath : pumpPaths) {
                                if(Thread.currentThread().isInterrupted()){
                                    System.out.println("线程请求中断...");
                                    return;
                                }
                                Enumerator preEnum = new Enumerator(prePath);
                                Enumerator pumpEnum = new Enumerator(pumpPath);
                                if (dynamicValidate(preEnum, pumpEnum, "POA")) return;
                            }
                        }
                    }
                }
            }
        }

        if (SLQ) {
            // SLQ1
            // 获取所有root到counting的路径
            getAllCountingAtBegin(pattern.root, 0);
            for (Pattern.Node node : allCountingAtBegin) {
                // 因为前缀必定可空，所以不必考虑前缀
                // 排序中缀
                Collections.sort(OneLoopPumpPaths.get(node), new Comparator<oldPath>() {
                    @Override
                    public int compare(oldPath o1, oldPath o2) {
                        return o1.path.size() - o2.path.size();
                    }
                });
                // for (oldPath pumpPath : OneLoopPumpPaths.get(node)) {
                //     if (Thread.currentThread().isInterrupted()) return;
                //     Enumerator preEnum = new Enumerator(new oldPath());
                //     Enumerator pumpEnum = new Enumerator(pumpPath);
                //     if (dynamicValidate(preEnum, pumpEnum, "SLQ")) return;
                // }
                for (oldPath pumpPath : OneLoopPumpPaths.get(node)) {
                    for (oldPath fixedPrePath : fixedPrePaths) {
                        if(Thread.currentThread().isInterrupted()){
                            System.out.println("线程请求中断...");
                            return;
                        }
                        Enumerator preEnum = new Enumerator(fixedPrePath);
                        Enumerator pumpEnum = new Enumerator(pumpPath);
                        if (dynamicValidate(preEnum, pumpEnum, "SLQ")) return;
                    }
                }

                // ArrayList<oldPath> newPumpPaths = new ArrayList<>();>
                // for (oldPath fixedPrePath : fixedPrePaths) {
                //     for (oldPath pumpPath : OneLoopPumpPaths.get(node)) {
                //         oldPath newPumpPath = new oldPath();
                //         newPumpPath.path.addAll(fixedPrePath.path);
                //         newPumpPath.path.addAll(pumpPath.path);
                //         newPumpPaths.add(newPumpPath);
                //     }
                // }
                //
                // for (oldPath pumpPath : newPumpPaths) {
                //     if (Thread.currentThread().isInterrupted()) return;
                //     Enumerator preEnum = new Enumerator(new oldPath());
                //     Enumerator pumpEnum = new Enumerator(pumpPath);
                //     if (dynamicValidate(preEnum, pumpEnum, "SLQ")) return;
                // }
            }

            //SLQ2
            for (Pattern.Node node : OneLoopNodes) {
                for (oldPath prePath : OneLoopPrePaths.get(node)) {
                    for (oldPath pumpPath : OneLoopPumpPaths.get(node)) {
                        ArrayList<oldPath> overlapPaths = new ArrayList<>();
                        if (getPathOverlaps(pumpPath, prePath, overlapPaths)) {
                            for (oldPath overlapPath : overlapPaths) {
                                for (oldPath fixedPrePath : fixedPrePaths) {
                                    if(Thread.currentThread().isInterrupted()){
                                        System.out.println("线程请求中断...");
                                        return;
                                    }
                                    Enumerator preEnum = new Enumerator(fixedPrePath);
                                    Enumerator pumpEnum = new Enumerator(overlapPath);
                                    // Enumerator pumpEnum = new Enumerator(pumpPath);
                                    if (dynamicValidate(preEnum, pumpEnum, "SLQ")) return;
                                }
                            }
                        }
                    }
                }
            }
        }

        // System.out.println("[*] Analyzer done");
    }

    // record用来排除lookaround中的counting
    public void searchOneLoopNode(Pattern.Node root, boolean record) {
        if (root == null || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            return;
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        if (root instanceof Pattern.Prolog) {
            searchOneLoopNode(((Pattern.Prolog) root).loop, record);
        } else if (root instanceof Pattern.Loop) {
            if (record) {
                OneLoopNodes.add(root);
            }
            searchOneLoopNode(((Pattern.Loop) root).body, record);
            searchOneLoopNode(((Pattern.Loop) root).next, record);
        } else if (root instanceof Pattern.Curly) {
            if (record) {
                OneLoopNodes.add(root);
            }
            searchOneLoopNode(((Pattern.Curly) root).atom, record);
            searchOneLoopNode(((Pattern.Curly) root).next, record);
        } else if (root instanceof Pattern.GroupCurly) {
            if (record) {
                OneLoopNodes.add(root);
            }
            searchOneLoopNode(((Pattern.GroupCurly) root).atom, record);
            searchOneLoopNode(((Pattern.GroupCurly) root).next, record);
        }

        // 2. 分支
        else if (root instanceof Pattern.Branch) {
            for (Pattern.Node node : ((Pattern.Branch) root).atoms) {
                if (node == null) {
                    continue;
                }
                searchOneLoopNode(node, record);
            }
        } else if (root instanceof Pattern.Ques) {
            searchOneLoopNode(((Pattern.Ques) root).atom, record);
            searchOneLoopNode(((Pattern.Ques) root).next, record);
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty) {
            if (((Pattern.CharProperty) root).charSet.size() == 0) {
                // generateCharSet((Pattern.CharProperty) root);
                generateFullSmallCharSet((Pattern.CharProperty) root);
            }
            // path.path.add(new HashSet<>(((Pattern.CharProperty) root).charSet));
            // result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
            searchOneLoopNode(root.next, record);
        } else if (root instanceof Pattern.SliceNode) {
            for (int i : ((Pattern.SliceNode) root).buffer) {
                // Set<Integer> tmpCharSet = new HashSet<>();
                fullSmallCharSet.add(i);
                // path.path.add(tmpCharSet);
            }
            // result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
            searchOneLoopNode(root.next, record);
        } else if (root instanceof Pattern.BnM) {
            for (int i : ((Pattern.BnM) root).buffer) {
                // Set<Integer> tmpCharSet = new HashSet<>();
                fullSmallCharSet.add(i);
                // path.path.add(tmpCharSet);
            }
            // result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
            searchOneLoopNode(root.next, record);
        }

        // lookaround处理
        else if (root instanceof Pattern.Pos) {
            lookaround = true;
            searchOneLoopNode(((Pattern.Pos) root).cond, false);
            searchOneLoopNode(((Pattern.Pos) root).next, record);
        } else if (root instanceof Pattern.Neg) {
            lookaround = true;
            searchOneLoopNode(((Pattern.Neg) root).cond, false);
            searchOneLoopNode(((Pattern.Neg) root).next, record);
        } else if (root instanceof Pattern.Behind) {
            lookaround = true;
            searchOneLoopNode(((Pattern.Behind) root).cond, false);
            searchOneLoopNode(((Pattern.Behind) root).next, record);
        } else if (root instanceof Pattern.NotBehind) {
            lookaround = true;
            searchOneLoopNode(((Pattern.NotBehind) root).cond, false);
            searchOneLoopNode(((Pattern.NotBehind) root).next, record);
        } else {
            searchOneLoopNode(root.next, record);
        }

    }

    boolean getPathTotalOverlap(oldPath path1, oldPath path2, oldPath result) {
        // 如果两个路径的长度不同，则不可能有重叠
        if (path1.path.size() != path2.path.size()) {
            return false;
        } else {
            // 如果两个路径的长度相同，则需要比较每一个节点的字符集
            ArrayList<Set<Integer>> charSet1 = new ArrayList<>();
            for (int i = 0; i < path1.path.size(); i++) {
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.addAll(path1.path.get(i));
                tmpCharSet.retainAll(path2.path.get(i));
                if (tmpCharSet.size() == 0) {
                    return false;
                } else {
                    charSet1.add(tmpCharSet);
                }
            }
            result.path = charSet1;
            return true;
        }
    }

    boolean getPathOverlaps(oldPath path1, oldPath path2, ArrayList<oldPath> result) {
        // 默认path1是大串，path2是小串，如果path1.path.size() < path2.path.size()，return false
        if (path1.path.size() < path2.path.size()) {
            return false;
        } else {
            // 对path1.path滑动窗口，从开始到path1.path.size() - path2.path.size()，每次滑动一位
            for (int i = 0; i < path1.path.size() - path2.path.size() + 1; i++) {
                // 对每一个滑动窗口，比较每一个节点的字符集
                ArrayList<Set<Integer>> charSet1 = new ArrayList<>();
                for (int j = 0; j < path2.path.size(); j++) {
                    Set<Integer> tmpCharSet = new HashSet<>();
                    tmpCharSet.addAll(path1.path.get(i + j));
                    tmpCharSet.retainAll(path2.path.get(j));
                    if (tmpCharSet.size() == 0) {
                        break;
                    } else {
                        charSet1.add(tmpCharSet);
                    }
                }
                for (int j = path2.path.size(); j < path1.path.size(); j++) {
                    charSet1.add(new HashSet<>(path1.path.get(j)));
                }
                oldPath tmpPath = new oldPath();
                tmpPath.path = charSet1;
                result.add(tmpPath);
            }

            if (result.size() > 0) return true;
        }
        return false;
    }

    private boolean dynamicValidate(Enumerator preEnum, Enumerator pumpEnum, String type) {
        int max_length = 500;
        if ("POA".equals(type)) {
            max_length = 30000;
        } else if ("SLQ".equals(type)) {
            max_length = 30000;
        }
        if (preEnum.Empty()) {
            while (pumpEnum.hasNext()) {
                String pump = pumpEnum.next();
                // System.out.println(pump);
                // if (pump.equals("aaa"))
                //     System.out.println("aaa");
                double matchingStepCnt;
                if ("SLQ".equals(type))
                    matchingStepCnt = testPattern.getMatchingStepCnt("", pump, "\\b", max_length, 100000);
                else matchingStepCnt = testPattern4Search.getMatchingStepCnt("", pump, "\\b", max_length, 100000);
                // System.out.println(matchingStepCnt);
                // if (pump.equals("abca"))
                //     System.out.println("abca");
                if (matchingStepCnt > 1e5) {
                    attackable = true;
                    attackMsg = type + "\nprefix:\n" + "pump:" + pump + "\nsuffix:\\n\\b\\n";
                    return true;
                }
                // System.out.println("");
            }
        } else {
            while (preEnum.hasNext()) {
                String pre = preEnum.next();
                while (pumpEnum.hasNext()) {
                    // System.out.println("brfore next");
                    String pump = pumpEnum.next();
                    // System.out.println(pre + pump);
                    double matchingStepCnt;
                    if ("SLQ".equals(type))
                        matchingStepCnt = testPattern.getMatchingStepCnt("", pump, "\\b", max_length, 100000);
                    else matchingStepCnt = testPattern4Search.getMatchingStepCnt("", pump, "\\b", max_length, 100000);
                    // System.out.println(matchingStepCnt);
                    if (matchingStepCnt > 1e6) {
                        // System.out.println("matchingStepCnt > 1e5");
                        attackable = true;
                        attackMsg = type + "\nprefix:" + pre + "\n" + "pump:" + pump + "\nsuffix:\\n\\b\\n";
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void printPatternStruct(Pattern.Node root) {
        if (root == null || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            return;
        }
        System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_"));

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        if (root instanceof Pattern.Prolog) {
            printPatternStruct(((Pattern.Prolog) root).loop);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--loop-->" + ((Pattern.Prolog) root).loop.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else if (root instanceof Pattern.Loop) {
            printPatternStruct(((Pattern.Loop) root).body);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--body-->" + ((Pattern.Loop) root).body.toString().replace("regex.Pattern$", "").replace("@", "_"));
            printPatternStruct(((Pattern.Loop) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else if (root instanceof Pattern.Curly) {
            printPatternStruct(((Pattern.Curly) root).atom);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--atom-->" + ((Pattern.Curly) root).atom.toString().replace("regex.Pattern$", "").replace("@", "_"));
            printPatternStruct(((Pattern.Curly) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else if (root instanceof Pattern.GroupCurly) {
            printPatternStruct(((Pattern.GroupCurly) root).atom);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--atom-->" + ((Pattern.GroupCurly) root).atom.toString().replace("regex.Pattern$", "").replace("@", "_"));
            printPatternStruct(((Pattern.GroupCurly) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        }

        // 2. 分支
        else if (root instanceof Pattern.Branch) {
            for (Pattern.Node node : ((Pattern.Branch) root).atoms) {
                if (node == null) {
                    continue;
                }
                System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--atoms-->" + node.toString().replace("regex.Pattern$", "").replace("@", "_"));
                printPatternStruct(node);
            }
        } else if (root instanceof Pattern.Ques) {
            printPatternStruct(((Pattern.Ques) root).atom);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--atom-->" + ((Pattern.Ques) root).atom.toString().replace("regex.Pattern$", "").replace("@", "_"));
            printPatternStruct(((Pattern.Ques) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty) {
            // if(((Pattern.CharProperty) root).charSet.size() == 0){
            //     // generateCharSet((Pattern.CharProperty) root);
            //     printPatternStruct((Pattern.CharProperty) root);
            // }
            // path.path.add(new HashSet<>(((Pattern.CharProperty) root).charSet));
            // result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
            printPatternStruct(root.next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else if (root instanceof Pattern.SliceNode || root instanceof Pattern.BnM) {
            // for (int i : ((Pattern.SliceNode) root).buffer){
            //     // Set<Integer> tmpCharSet = new HashSet<>();
            //     fullSmallCharSet.add(i);
            //     // path.path.add(tmpCharSet);
            // }
            // // result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
            printPatternStruct(root.next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        }

        // lookaround处理
        else if (root instanceof Pattern.Pos) {
            printPatternStruct(((Pattern.Pos) root).cond);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--cond-->" + ((Pattern.Pos) root).cond.toString().replace("regex.Pattern$", "").replace("@", "_"));
            printPatternStruct(((Pattern.Pos) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else if (root instanceof Pattern.Neg) {
            printPatternStruct(((Pattern.Neg) root).cond);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--cond-->" + ((Pattern.Neg) root).cond.toString().replace("regex.Pattern$", "").replace("@", "_"));
            printPatternStruct(((Pattern.Neg) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else if (root instanceof Pattern.Behind) {
            printPatternStruct(((Pattern.Behind) root).cond);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--cond-->" + ((Pattern.Behind) root).cond.toString().replace("regex.Pattern$", "").replace("@", "_"));
            printPatternStruct(((Pattern.Behind) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else if (root instanceof Pattern.NotBehind) {
            printPatternStruct(((Pattern.NotBehind) root).cond);
            printPatternStruct(((Pattern.NotBehind) root).next);
            System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
        } else {
            printPatternStruct(root.next);
            if (root.next != null) {
                System.out.println(root.toString().replace("regex.Pattern$", "").replace("@", "_") + "--next-->" + root.next.toString().replace("regex.Pattern$", "").replace("@", "_"));
            }
        }

    }

    private oldPath getShortestPath(Pattern.Node root, oldPath rawPath, Pattern.Node endNode) {
        oldPath path = new oldPath(rawPath);
        ArrayList<oldPath> result = new ArrayList<>();
        if (root == null || root instanceof Pattern.LastNode || root == endNode || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            result.add(path);
            return result.get(0);
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        // 1. 循环
        if (root instanceof Pattern.Prolog) {
            result.add(getShortestPath(((Pattern.Prolog) root).loop, path, endNode));
        } else if (root instanceof Pattern.Loop) {
            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.add(getShortestPath(root.next, new oldPath(), endNode));

            if (((Pattern.Loop) root).cmin == 0) {
                result.addAll(nextPaths);
            } else {
                ArrayList<oldPath> thisCyclePath = new ArrayList<>();
                thisCyclePath.add(getShortestPath(((Pattern.Loop) root).body, new oldPath(), endNode));

                for (oldPath p : thisCyclePath) {
                    for (oldPath np : nextPaths) {
                        oldPath newPath = new oldPath(path);
                        newPath.path.addAll(p.path);
                        newPath.path.addAll(np.path);
                        result.add(newPath);
                    }
                }
            }

        } else if (root instanceof Pattern.Curly) {
            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.add(getShortestPath(root.next, new oldPath(), endNode));

            if (((Pattern.Curly) root).cmin == 0) {
                result.addAll(nextPaths);
            } else {
                ArrayList<oldPath> thisCyclePath = new ArrayList<>();
                thisCyclePath.add(getShortestPath(((Pattern.Curly) root).atom, new oldPath(), endNode));

                for (oldPath p : thisCyclePath) {
                    for (oldPath np : nextPaths) {
                        oldPath newPath = new oldPath(path);
                        newPath.path.addAll(p.path);
                        newPath.path.addAll(np.path);
                        result.add(newPath);
                    }
                }
            }

        } else if (root instanceof Pattern.GroupCurly) {
            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.add(getShortestPath(root.next, new oldPath(), endNode));

            if (((Pattern.GroupCurly) root).cmin == 0) {
                result.addAll(nextPaths);
            } else {
                ArrayList<oldPath> thisCyclePath = new ArrayList<>();
                thisCyclePath.add(getShortestPath(((Pattern.GroupCurly) root).atom, new oldPath(), endNode));

                for (oldPath p : thisCyclePath) {
                    for (oldPath np : nextPaths) {
                        oldPath newPath = new oldPath(path);
                        newPath.path.addAll(p.path);
                        newPath.path.addAll(np.path);
                        result.add(newPath);
                    }
                }
            }
        }

        // 2. 分支
        else if (root instanceof Pattern.Branch) {
            if (((Pattern.Branch) root).getSize() == 1) {
                result.add(path);
            } else {
                for (Pattern.Node node : ((Pattern.Branch) root).atoms) {
                    if (node == null) {
                        continue;
                    }
                    result.add(getShortestPath(node, path, endNode));
                }
            }
        } else if (root instanceof Pattern.BranchConn) {
            result.add(getShortestPath(root.next, path, endNode));
        } else if (root instanceof Pattern.Ques) {
            result.add(path);
        } else if (root instanceof Pattern.Conditional) {
            throw new RuntimeException("Pattern.Conditional not supported, please tell me which regex contains it.");
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty) {
            path.path.add(new HashSet<>(((Pattern.CharProperty) root).charSet));
            result.add(getShortestPath(root.next, path, endNode));
        } else if (root instanceof Pattern.SliceNode) {
            for (int i : ((Pattern.SliceNode) root).buffer) {
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.add(i);
                path.path.add(tmpCharSet);
            }
            result.add(getShortestPath(root.next, path, endNode));
        } else if (root instanceof Pattern.BnM) {
            for (int i : ((Pattern.BnM) root).buffer) {
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.add(i);
                path.path.add(tmpCharSet);
            }
            result.add(getShortestPath(root.next, path, endNode));
        }

        // 其他的都是直接走next
        else {
            result.add(getShortestPath(root.next, path, endNode));
        }

        // 排序result
        Collections.sort((result), new Comparator<oldPath>() {
            @Override
            public int compare(oldPath o1, oldPath o2) {
                return o1.path.size() - o2.path.size();
            }
        });

        return result.get(0);
    }

    private void getAllCountingAtBegin(Pattern.Node root, int length) {
        if (length > 0 || root instanceof Pattern.Begin || root == null || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            return;
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        if (root instanceof Pattern.Prolog) {
            getAllCountingAtBegin(((Pattern.Prolog) root).loop, length);
        } else if (root instanceof Pattern.Loop) {
            if (((Pattern.Loop) root).cmax > 100) allCountingAtBegin.add(root);
            if (getShortestPath(((Pattern.Loop) root).body, new oldPath(), root.next).path.size() == 0) {
                getAllCountingAtBegin(root.next, length);
            }
        } else if (root instanceof Pattern.Curly) {
            if (((Pattern.Curly) root).cmax > 100) allCountingAtBegin.add(root);
            if (getShortestPath(((Pattern.Curly) root).atom, new oldPath(), root.next).path.size() == 0) {
                getAllCountingAtBegin(root.next, length);
            }
        } else if (root instanceof Pattern.GroupCurly) {
            if (((Pattern.GroupCurly) root).cmax > 100) allCountingAtBegin.add(root);
            if (getShortestPath(((Pattern.GroupCurly) root).atom, new oldPath(), root.next).path.size() == 0) {
                getAllCountingAtBegin(root.next, length);
            }
        }

        // 2. 分支
        else if (root instanceof Pattern.Branch) {
            if (((Pattern.Branch) root).getSize() == 1) {
                getAllCountingAtBegin(((Pattern.Branch) root).conn, length);
            } else {
                for (Pattern.Node node : ((Pattern.Branch) root).atoms) {
                    if (node == null) {
                        continue;
                    }
                    getAllCountingAtBegin(root.next, length);
                }
            }
        } else if (root instanceof Pattern.Ques) {
            getAllCountingAtBegin(root.next, length);
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty) {
            getAllCountingAtBegin(root.next, length + 1);
        } else if (root instanceof Pattern.SliceNode) {
            getAllCountingAtBegin(root.next, length + ((Pattern.SliceNode) root).buffer.length);
        } else if (root instanceof Pattern.BnM) {
            getAllCountingAtBegin(root.next, length + ((Pattern.BnM) root).buffer.length);
        }

        // lookaround处理
        else {
            getAllCountingAtBegin(root.next, length);
        }
    }

    public ArrayList<oldPath> retrunPaths(Pattern.Node root, oldPath rawPath, int maxLength, Pattern.Node endNode, returnPathsType type) {
        oldPath path = new oldPath(rawPath);
        ArrayList<oldPath> result = new ArrayList<>();
        if (root == null || path.reachEnd || (root instanceof Pattern.GroupTail && root.next instanceof Pattern.Loop)) {
            result.add(path);
            return result;
        }
        else if (root instanceof Pattern.LastNode) {
            path.reachEnd = true;
            result.add(path);
            return result;
        }
        else if (root == endNode) {
            path.reachEnd = true;
            result.add(path);
            if (type == returnPathsType.pre) {
                OneLoopPrePaths.get(endNode).add(path);
            }
            return result;
        }
        else if (path.path.size() > maxLength) {
            return result;
        }

        // 需要特殊处理的节点（下一个节点不在next或者不止在next）
        // 1. 循环
        if (root instanceof Pattern.Prolog) {
            result.addAll(retrunPaths(((Pattern.Prolog) root).loop, path, maxLength, endNode, type));
        }
        else if (root instanceof Pattern.Loop) {
            int limit = maxLength;

            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.addAll(retrunPaths(root.next, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            thisCyclePath.addAll(retrunPaths(((Pattern.Loop) root).body, new oldPath(), limit, endNode, type));

            // System.out.println("in Lazy Loop");
            // printPaths(thisCyclePath);

            // printPaths(thisCyclePath);
            // System.out.println("-----------------------");

            // ArrayList<oldPath> lastPaths = new ArrayList<>(thisCyclePath);
            /*
            循环结构的循环次数被存在当前Node节点的cmin和cmax中
            首先生成“前缀路径+循环路径*cmin"作为第一部分
            然后在第一部分后分别拼接”循环路径*循环次数+后缀路径“（cmin < 循环次数 + cmin < cmax）
            即可获得经过当前节点的所有路径
             */

            ArrayList<oldPath> lastPaths = new ArrayList<>();
            lastPaths.add(path);
            for (int loopTime = 0; loopTime < ((Pattern.Loop) root).cmin; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath) cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath) cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }

            for (int loopTime = ((Pattern.Loop) root).cmin; loopTime < ((Pattern.Loop) root).cmax && loopTime < limit; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath) cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath) cur).path);
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
                        if (p2.reachEnd) {
                            p.reachEnd = true;
                        }
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
                    if (p2.reachEnd) {
                        p.reachEnd = true;
                    }
                    result.add(p);
                }
            }
            // System.out.println("in Lazy Loop");
            // printPaths(result);
        }
        else if (root instanceof Pattern.Curly) {
            int limit = maxLength;

            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.addAll(retrunPaths(root.next, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            thisCyclePath.addAll(retrunPaths(((Pattern.Curly) root).atom, new oldPath(), limit, endNode, type));

            // ArrayList<oldPath> lastPaths = new ArrayList<>(thisCyclePath);
            ArrayList<oldPath> lastPaths = new ArrayList<>();
            lastPaths.add(path);
            for (int loopTime = 0; loopTime < ((Pattern.Curly) root).cmin; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath) cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath) cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }

            for (int loopTime = ((Pattern.Curly) root).cmin; loopTime < ((Pattern.Curly) root).cmax && loopTime < limit; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath) cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath) cur).path);
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
                        if (p2.reachEnd) {
                            p.reachEnd = true;
                        }
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
                    if (p2.reachEnd) {
                        p.reachEnd = true;
                    }
                    result.add(p);
                }
            }

        }
        else if (root instanceof Pattern.GroupCurly) {
            int limit = maxLength;

            ArrayList<oldPath> nextPaths = new ArrayList<>();
            nextPaths.addAll(retrunPaths(root.next, new oldPath(), limit, endNode, type));

            ArrayList<oldPath> thisCyclePath = new ArrayList<>();
            thisCyclePath.addAll(retrunPaths(((Pattern.GroupCurly) root).atom, new oldPath(), limit, endNode, type));

            // ArrayList<oldPath> lastPaths = new ArrayList<>(thisCyclePath);
            ArrayList<oldPath> lastPaths = new ArrayList<>();
            lastPaths.add(path);
            for (int loopTime = 0; loopTime < ((Pattern.GroupCurly) root).cmin; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath) cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath) cur).path);
                        p.path.addAll(p2.path);
                        if (p.path.size() < limit) {
                            thisPaths.add(p);
                        }
                    }
                }
                lastPaths = thisPaths;
            }

            for (int loopTime = ((Pattern.GroupCurly) root).cmin; loopTime < ((Pattern.GroupCurly) root).cmax && loopTime < limit; loopTime++) {
                ArrayList<oldPath> thisPaths = new ArrayList<>();
                Iterator iterator = lastPaths.iterator();
                while (iterator.hasNext()) {
                    Object cur = iterator.next();
                    if (((oldPath) cur).path.size() > limit) {
                        iterator.remove();
                    }
                    for (oldPath p2 : thisCyclePath) {
                        oldPath p = new oldPath();
                        p.path.addAll(((oldPath) cur).path);
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
                        if (p2.reachEnd) {
                            p.reachEnd = true;
                        }
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
                    if (p2.reachEnd) {
                        p.reachEnd = true;
                    }
                    result.add(p);
                }
            }
        }

        // 2. 分支
        /*
        分支结构的生成共有两种情况
        1. 正则表达式r?
        - 当r的内容比较简单时，会生成一个Ques节点，分支内容在
        2. 正则表达式a|b
         */
        else if (root instanceof Pattern.Branch) {
            if (((Pattern.Branch) root).getSize() == 1) {
                // Done：Ques作Branch的正确逻辑应该如下注释，先不改，等测试
                // /*
                result.addAll(retrunPaths(((Pattern.Branch) root).conn, path, maxLength, endNode, type));
                result.addAll(retrunPaths(((Pattern.Branch) root).atoms[0], path, maxLength, endNode, type));
                // */
                // ArrayList<oldPath> tmpPath1 = new ArrayList<>();
                // tmpPath1.add(path);
                // tmpPath1.addAll(retrunPaths(((Pattern.Branch) root).atoms[0], path, maxLength, endNode, type));
                //
                // for(oldPath p : tmpPath1){
                //     result.addAll(retrunPaths(root.next, p, maxLength, endNode, type));
                // }
            } else {
                // TODO: 重写Branch，切断atoms结尾到conn的途径（遇到conn就return），像Loop一样通过conn构造nextPath再和branchPath组合
                for (Pattern.Node node : ((Pattern.Branch) root).atoms) {
                    if (node == null) {
                        continue;
                    }
                    result.addAll(retrunPaths(node, path, maxLength, endNode, type));
                }
            }
        }
        else if (root instanceof Pattern.BranchConn) {
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        }
        else if (root instanceof Pattern.Ques) {
            // TODO: 几种type并未区分
            // 1. 0或1
            ArrayList<oldPath> tmpPath1 = new ArrayList<>();
            tmpPath1.add(path);
            tmpPath1.addAll(retrunPaths(((Pattern.Ques) root).atom, new oldPath(), maxLength, endNode, type));

            // ArrayList<oldPath> tmpPath2 = new ArrayList<>();
            // tmpPath2.addAll(retrunPaths(((Pattern.Ques) root).atom, new oldPath(), maxLength, endNode, type));
            // for(oldPath p : tmpPath2){
            //     oldPath p1 = new oldPath();
            //     p1.path.addAll(path.path);
            //     p1.path.addAll(p.path);
            //     tmpPath1.add(p1);
            // }

            for (oldPath p : tmpPath1) {
                result.addAll(retrunPaths(root.next, p, maxLength, endNode, type));
            }
        }
        else if (root instanceof Pattern.Conditional) {
            throw new RuntimeException("Pattern.Conditional not supported, please tell me which regex contains it.");
        }

        // 具有实际字符意义
        else if (root instanceof Pattern.CharProperty) {
            // if(((Pattern.CharProperty) root).charSet.size() == 0){
            //     // oldGenerateCharSet((Pattern.CharProperty) root);
            //     generateCharSet((Pattern.CharProperty) root);
            // }
            // path.path.add(new HashSet<>(((Pattern.CharProperty) root).charSet));
            path.path.add(((Pattern.CharProperty) root).charSet);
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        }
        else if (root instanceof Pattern.SliceNode) {
            for (int i : ((Pattern.SliceNode) root).buffer) {
                Set<Integer> tmpCharSet = new HashSet<>();
                tmpCharSet.add(i);
                path.path.add(tmpCharSet);
            }
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        }
        else if (root instanceof Pattern.BnM) {
            for (int i : ((Pattern.BnM) root).buffer) {
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
        else {
            result.addAll(retrunPaths(root.next, path, maxLength, endNode, type));
        }


        return result;
    }

    private void oldGenerateCharSet(Pattern.CharProperty root) {
        // 默认的处理方法
        for (int i = 0; i < 127; i++) {
            if (root.isSatisfiedBy(i)) {
                root.charSet.add(i);
            }
        }
    }

    private void generateCharSet(Pattern.CharProperty root) {
        // 默认的处理方法
        for (int i = 0; i < 65536; i++) {
            if (root.isSatisfiedBy(i)) {
                root.charSet.add(i);
            }
        }

        // 压缩字符集方法
        // 如果已经生成过了，说明是小于30的直接使用
        // if (root.charSet.size() > 0){
        //     return;
        // }
        // // 如果没有生成过，说明是大字符集
        // else {
        //     Random rand = new Random();
        //     Set<Integer> result = new HashSet<>();
        //
        //     // set2&set8
        //     Set<Integer> tmp;
        //
        //     for (Map.Entry<Pattern.Node, Set<Integer>> entry : bigCharSetMap.entrySet()) {
        //         // System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        //         if (entry.getKey() == root){
        //             // 加入root和fullSmallCharSet的交集
        //             // set2&set8
        //             tmp = new HashSet<>(entry.getValue());
        //             tmp.retainAll(fullSmallCharSet);
        //             result.addAll(tmp);
        //             continue;
        //         } else {
        //             // 加入和本bigCharSet的差集
        //             // (set2-set5)
        //             tmp = new HashSet<>(bigCharSetMap.get(root));
        //             tmp.removeAll(entry.getValue());
        //             result.addAll(tmp);
        //
        //             // 随机加入一个root和本bigCharSet的并集-其他bigCharSet
        //             // random 1个(set2&set5-set7-set8)
        //             tmp = new HashSet<>(bigCharSetMap.get(root));
        //             tmp.retainAll(entry.getValue());
        //             tmp.removeAll(fullSmallCharSet);
        //             for (Map.Entry<Pattern.Node, Set<Integer>> entry_ : bigCharSetMap.entrySet()) {
        //                 if (entry_.getKey() == root || entry_.getKey() == entry.getKey()) {
        //                     continue;
        //                 } else {
        //                     tmp.removeAll(entry_.getValue());
        //                 }
        //             }
        //
        //             if (tmp.size() > 0) {
        //                 int index = rand.nextInt(tmp.size());
        //                 Iterator<Integer> iter = tmp.iterator();
        //                 for (int i = 0; i < index; i++) {
        //                     iter.next();
        //                 }
        //                 result.add(iter.next());
        //             }
        //         }
        //     }
        //
        //     // 最后random 1个(set2-set8)
        //     tmp = new HashSet<>(bigCharSetMap.get(root));
        //     tmp.removeAll(fullSmallCharSet);
        //     if (tmp.size() > 0) {
        //         int index = rand.nextInt(tmp.size());
        //         Iterator<Integer> iter = tmp.iterator();
        //         for (int i = 0; i < index; i++) {
        //             iter.next();
        //         }
        //         result.add(iter.next());
        //     }
        //
        //     root.charSet.addAll(result);
        // }
    }

    private void generateAllBigCharSet() {
        // 遍历化bigCharSetMap节点
        for (Map.Entry<Pattern.Node, Set<Integer>> entry : bigCharSetMap.entrySet()) {
            Pattern.CharProperty root = (Pattern.CharProperty) entry.getKey();
            generateBigCharSet(root);
        }

    }

    private void generateBigCharSet(Pattern.CharProperty root) {
        Random rand = new Random();
        Set<Integer> result = new HashSet<>();

        // set2&set8
        Set<Integer> tmp;

        for (Map.Entry<Pattern.Node, Set<Integer>> entry : bigCharSetMap.entrySet()) {
            // System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            if (entry.getKey() == root) {
                // 加入root和fullSmallCharSet的交集
                // set2&set8
                tmp = new HashSet<>(entry.getValue());
                tmp.retainAll(fullSmallCharSet);
                result.addAll(tmp);
                continue;
            } else {
                // 加入和本bigCharSet的差集
                // (set2-set5)
                tmp = new HashSet<>(bigCharSetMap.get(root));
                tmp.removeAll(entry.getValue());
                result.addAll(tmp);

                // 随机加入一个root和本bigCharSet的并集-其他bigCharSet
                // random 1个(set2&set5-set7-set8)
                tmp = new HashSet<>(bigCharSetMap.get(root));
                tmp.retainAll(entry.getValue());
                tmp.removeAll(fullSmallCharSet);
                for (Map.Entry<Pattern.Node, Set<Integer>> entry_ : bigCharSetMap.entrySet()) {
                    if (entry_.getKey() == root || entry_.getKey() == entry.getKey()) {
                        continue;
                    } else {
                        tmp.removeAll(entry_.getValue());
                    }
                }

                if (tmp.size() > 0) {
                    int index = rand.nextInt(tmp.size());
                    Iterator<Integer> iter = tmp.iterator();
                    for (int i = 0; i < index; i++) {
                        iter.next();
                    }
                    result.add(iter.next());
                }
            }
        }

        // 最后random 1个(set2-set8)
        tmp = new HashSet<>(bigCharSetMap.get(root));
        tmp.removeAll(fullSmallCharSet);
        if (tmp.size() > 0) {
            int index = rand.nextInt(tmp.size());
            Iterator<Integer> iter = tmp.iterator();
            for (int i = 0; i < index; i++) {
                iter.next();
            }
            result.add(iter.next());
        }

        root.charSet = new HashSet<>();
        root.charSet.addAll(result);
    }

    private void generateFullSmallCharSet(Pattern.CharProperty root) {
        Set<Integer> charSet = new HashSet<>();
        for (int i = 0; i < 65536; i++) {
            if (root.isSatisfiedBy(i)) {
                charSet.add(i);
            }
        }
        if (charSet.size() < 128) {
            fullSmallCharSet.addAll(charSet);
            root.charSet.addAll(charSet);
        } else {
            bigCharSetMap.put(root, charSet);
        }
    }

    public Comparator<oldPath> comparePathLength = new Comparator<oldPath>() {
        @Override
        public int compare(oldPath o1, oldPath o2) {
            // 从大到小排序
            return o2.path.size() - o1.path.size();
        }
    };

    public boolean equals(Set<?> set1, Set<?> set2) {
        //null就直接不比了
        if (set1 == null || set2 == null) {
            return false;
        }
        //大小不同也不用比了
        if (set1.size() != set2.size()) {
            return false;
        }
        //最后比containsAll
        return set1.containsAll(set2);
    }

    private Set<Integer> getNodeCharSet(Pattern.Node node) {
        if (node instanceof Pattern.CharProperty) {
            if (((Pattern.CharProperty) node).charSet.size() == 0) {
                oldGenerateCharSet((Pattern.CharProperty) node);
            }
            return ((Pattern.CharProperty) node).charSet;
        } else if (node instanceof Pattern.SliceNode || node instanceof Pattern.BnM) {
            Set<Integer> charSet = new HashSet<>();
            for (int i : ((Pattern.SliceNode) node).buffer) {
                charSet.add(i);
            }
            return charSet;
        } else {
            return null;
        }
    }

    public void printPaths(ArrayList<oldPath> paths) {
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

        for (oldPath p : paths) {
            // if (p.path.size() != 2) continue;
            // System.out.println("----");
            int indexP = 0;
            for (Set<Integer> s : p.path) {
                if (indexP != 0) {
                    System.out.print(",");
                }
                indexP++;
                System.out.print("[");
                if (equals(s, Dot)) {
                    System.out.print(".");
                } else if (equals(s, Bound)) {
                    System.out.print("\\b");
                } else if (equals(s, Space)) {
                    System.out.print("\\s");
                } else if (equals(s, noneSpace)) {
                    System.out.print("\\S");
                } else if (equals(s, word)) {
                    System.out.print("\\w");
                } else {
                    int indexS = 0;
                    for (int i : s) {
                        if (indexS != 0) {
                            System.out.print(",");
                        }
                        indexS++;
                        // System.out.print((char) i);
                        if (i == 10) {
                            System.out.print("\\n");
                        } else if (i == 13) {
                            System.out.print("\\r");
                        } else {
                            System.out.print((char) i);
                        }
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

        public Enumerator(oldPath path) {
            this.indexs = new ArrayList<>();
            this.path = new ArrayList<>();
            for (int i = 0; i < path.path.size(); i++) {
                this.path.add(new ArrayList<>(path.path.get(i)));
                this.indexs.add(0);
            }
        }

        public void reset() {
            for (int i = 0; i < this.indexs.size(); i++) {
                this.indexs.set(i, 0);
            }
        }

        public boolean Empty() {
            return this.indexs.size() == 0;
        }

        public boolean hasNext() {
            if (this.indexs.size() == 0) {
                return false;
            }
            int t1 = this.indexs.get(0);
            int t2 = this.path.get(0).size();
            boolean result = t1 < t2;
            return result;
        }

        public String next() {
            // StringBuilder sb = new StringBuilder();
            String sb = "";
            for (int i = 0; i < path.size(); i++) {
                int tmp = path.get(i).get(indexs.get(i));
                sb += (char) tmp;
                // sb.append((path.get(i).get(indexs.get(i))));
            }
            // System.out.println(indexs.toString());
            for (int i = indexs.size() - 1; i >= 0; i--) {
                if (indexs.get(i) == path.get(i).size()) {
                    indexs.set(i, 0);
                    continue;
                } else {
                    indexs.set(i, indexs.get(i) + 1);
                    // for (int j = i, j > 0 && indexs.get(i) == path.get(i).size(); j--) {
                    //     indexs.set(j-1, indexs.get(j-1) + 1);
                    //     indexs.set(j, 0);
                    // }
                    for (int j = i; j > 0 && indexs.get(j) == path.get(j).size(); j--) {
                        indexs.set(j - 1, indexs.get(j - 1) + 1);
                        indexs.set(j, 0);
                    }
                    break;
                }
            }
            return sb.toString();
        }
    }

    enum returnPathsType {
        pump, pre
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
}
