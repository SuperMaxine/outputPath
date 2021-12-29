package com.company;

import regex.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author SuperMaxine
 */
public class Main {

    public static void main(String[] args) {
        // String regex = "(?:xy*)\\Gtest\\b(?<!z)(a|b|c){1,3}z";
        // String regex = "(?<year>\\d{4})-(?<md>(?<month>\\d{2})-(?<date>\\d{2}))";
        // String regex = "(x)(?<name>y)(z)\\1\\k<name>\\3";
        // String regex = "^(a?(cd|ef))+g$";

        // 以下两个应该一样
        // String regex = "(xy|cd|z)+";
        // String regex = "(\\.\\d|\\d\\.|\\d(?!\\.\\d))+";


        // String regex = "a(ba*)+$";
        // String regex = "^(ab|a|b)+$";
        // String regex = "\\b.*\\b";
        // String regex = "^\\s+|\\s+$";

        String regex = "(\\d)+(?<=12)";
        // String regex = "(?!a)[ab]+";
        Pattern p = Pattern.compile(regex);
        Pattern.printObjectTree(p.root);
        // Analyzer a = new Analyzer(p);
        ArrayList<Analyzer.Path> tmp = Analyzer.getPaths(p.root, 4);
        ArrayList<Analyzer.Path> paths = new ArrayList<>();
        for (Analyzer.Path path : tmp) {
            if (path.reachEnd && path.path.size() > 0) {
                paths.add(path);
            }
        }
        System.out.println(regex);
        Analyzer.printPaths(paths);
        System.out.println(p.matcher("123-456-7890").matches());

        // 1. Len
        // 2. 给出路径Path
        // 3. 分割前中后缀

        // ^\b_((?:__|[\s\S])+?)_\b|^\*((?:\*\*|[\s\S])+?)\*(?!\*)
        // lookaround用测试

    }

}
