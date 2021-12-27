package com.company;

import regex.*;

/**
 * @author SuperMaxine
 */
public class Main {

    public static void main(String[] args) {
        // String regex = "(?:xy*)\\Gtest\\b(?<!z)(a|b|c){1,3}z";
        // String regex = "(?<year>\\d{4})-(?<md>(?<month>\\d{2})-(?<date>\\d{2}))";
        // String regex = "(x)(?<name>y)(z)\\1\\k<name>\\3";
        // String regex = "^(a?(a(b)))+c$";
        String regex = "(x|y){3,3}";
        Pattern p = Pattern.compile(regex);
        Analyzer a = new Analyzer(p);
        System.out.println(Analyzer.calcLen(p.root));
        System.out.println(p.matcher("123-456-7890").matches());

        // 1. Len
        // 2. 给出路径Path
        // 3. 分割前中后缀

        // ^\b_((?:__|[\s\S])+?)_\b|^\*((?:\*\*|[\s\S])+?)\*(?!\*)
        // lookaround用测试

    }

}
