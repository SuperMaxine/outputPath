package com.company;
import regex.*;

/**
 * @author SuperMaxine
 */
public class Main {

    public static void main(String[] args) {
        // String regex = "(?:xy*)\\Gtest\\b(?<!z)(a|b|c){1,3}z";
        String regex = "(x|y){3}";
        Pattern p = Pattern.compile(regex);
        System.out.println(p.matcher("123-456-7890").matches());

    }
}
