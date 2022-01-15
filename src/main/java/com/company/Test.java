package com.company;

import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String regex = "^(a?a)+$";
        for (int i = 0; i < 100; i++) {
            String text = "";
            for (int j = 0; j < i; j++) {
                text += "a";
            }
            text += "!";
            // print run Time
            long startTime = System.currentTimeMillis();
            System.out.println(i);
            System.out.println(Pattern.matches(regex, text));
            long endTime = System.currentTimeMillis();
            System.out.println("Run Time: " + (endTime - startTime) + "ms");
        }
    }
}
