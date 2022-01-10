package com.company;

import javafx.scene.layout.AnchorPane;
import regex.*;


/**
 * @author SuperMaxine
 */
public class Main {

    public static void main(String[] args) {
        // (a|b)  (a|b)? (a|b?)   (a?|b)  (a?|b?)
        // String regex = "(a|b)";
        // String regex = "(a|b)?";
        // String regex = "(a|b?)";
        // String regex = "(a?|b)";
        // String regex = "(a?|b?)";
        // (a|b)+ (a|b)* (a|b){0,3}
        // String regex= "(a|b)+";
        // String regex= "(a|b)*";
        // String regex= "(a|b){0,3}";
        // String regex= "(ab)?";
        // String regex= "(a?b?(ab)?)+";
        // Pattern p = Pattern.compile(regex);
        // Analyzer a = new Analyzer(p, 8);
        // a.printPatternStruct(p.root);


        // String regex = "^(a|\\w+)+$";
        // String regex = "<\\*(?:[^<*]|\\*|<|<\\*(?:[\\s\\S])*\\*>)*\\*>";
        // String regex = "^(a|(?=@bba)[@#]\\w+)+$";

        // String regex = "^(a|(?=abc)\\w+)+$"; //success
        // String regex = "(^[ \\t]*)\\[(?!\\[)(?:([\"'$`])(?:(?!\\2)[^\\\\]|\\\\.)*\\2|\\[(?:[^\\]\\\\]|\\\\.)*\\]|[^\\]\\\\]|\\\\.)*\\]"; //ReDos 41行 success

        // String regex = "(?: ?\\/[a-z?](?:[ :](?:\\"[^\\\"]*\\"|\\S+))?)*"; // 画了图
        String regex = "((?:^|[&(])[ \\t]*)for(?: ?\\/[a-z?](?:[ :](?:\"[^\"]*\"|\\S+))?)* \\S+ in \\([^)]+\\) do"; //ReDos 45行 //success
        /*
        x:for
        y:/?:*50
        z:\b
        1.0000289E7
          */
        // [\\][a-z?]:\S\S\S
        // [\\][a-z?]:\S\S\S [\\][a-z?]:\S\S[\\][a-z?]

        // [\\][a-z?]:\S\S\S\S
        // [\\][a-z?]:\S\S[\\][a-z?]

        // String regex = "((?:^|[&(])[ \\t]*)if(?: ?\\/[a-z?](?:[ :](?:\"[^\"]*\"|\\S+))?)* (?:not )?(?:cmdextversion \\d+|defined \\w+|errorlevel \\d+|exist \\S+|(?:\"[^\"]*\"|\\S+)?(?:==| (?:equ|neq|lss|leq|gtr|geq) )(?:\"[^\"]*\"|\\S+))"; //ReDos 46行 success
        /*
        attack_string:
        x:if
        y:/?:*50
        z:\b
          */

        // String regex = "((?:^|[&(])[ \\t]*)set(?: ?\\/[a-z](?:[ :](?:\"[^\"]*\"|\\S+))?)* (?:[^^&)\\r\\n]|\\^(?:\\r\\n|[\\s\\S]))*"; //ReDos 47行 success
        /*
        x:set
        y:/a:*50
        z:\b
        1.0000289E7
          */
        // String regex = "\"(?:[^\\\\\"\\r\\n]|\\\\(?:[abfnrtv\\\\\"]|\\d+|x[0-9a-fA-F]+))*\""; //ReDos 49行
        // String regex = "^\\|={3,}(?:(?:\\r?\\n|\\r).*)*?(?:\\r?\\n|\\r)\\|={3,}$"; //ReDos 42行
        // String regex = "(?:a|b(?:c|d+|ef+))*";
        Pattern p = Pattern.compile(regex);
        Pattern.printObjectTree(p.root);
        Analyzer a = new Analyzer(p, 8);



    }

}
