package com.company;

import regex.*;

/**
 * @author SuperMaxine
 */
public class Main {

    public static void main(String[] args) {
        // String regex = "ab*c";
        // String regex = "^((?=123)\\w+\\s+)+(?=\\t)\\s(\\w+(?<!456)\\s+)+$";
        // String regex = "\\{.+\\}";
        // String regex = "<\\*(?:[^<*]|\\*|<|<\\*(?:(?!\\*>)[\\s\\S])*\\*>)*\\*>";
        // String regex = "^c(a|(?=abc)\\w+)+$";
        // String regex = "^(\\d1|2\\w)+$";
        // String regex = "^c(a|a)+$";
        String regex = "a+b+$";
        // String regex = "^c(a|b|c)+d$";
        // String regex = "a|b";


        Pattern p = Pattern.compile(regex);
        Analyzer a = new Analyzer(p, 4);

        System.out.println("[*] done");
    }

}
