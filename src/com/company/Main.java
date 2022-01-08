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
        // String regex = "^(a|(?=abc)\\w+)+$";
        String regex = "^<\\*(?:[^<*]|\\*|<|<\\*(?:(?!\\*>)[\\s\\S])*\\*>)*\\*>$";
        // String regex = "^(a|(?=@bba)[@#]\\w+)+$";
        Pattern p = Pattern.compile(regex);
        Pattern.printObjectTree(p.root);
        Analyzer a = new Analyzer(p, 7);



    }

}
