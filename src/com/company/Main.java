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
        // String regex = "((?:^|[&(])[ \\t]*)for(?: ?\\/[a-z?](?:[ :](?:\"[^\"]*\"|\\S+))?)* \\S+ in \\([^)]+\\) do"; //ReDos 45行 //success
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
        /*
        x:"
        y:\00*50
        z:\b
        1.0000315E7
         */
        // String regex = "^\\|={3,}(?:(?:\\r?\\n|\\r).*)*?(?:\\r?\\n|\\r)\\|={3,}$"; //ReDos 42行
        // String regex = "((\\r\\n|\\r|\\n)+) *$";
        /*
        x:
        y:
        *50
        z:\b
        1.0000254E7
         */
        // String regex = "(?:\\[(?:[^\\]\\\\\"]|([\"'])(?:(?!\\1)[^\\\\]|\\\\.)*\\1|\\\\.)*\\])";
        /*
        x:[
        y:'*50
        z:\b
        1.0000167E7
         */

        // String regex = "^((([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+(\\.([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+)*)|((\\x22)((((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|\\x21|[\\x23-\\x5b]|[\\x5d-\\x7e]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(\\\\([\\x01-\\x09\\x0b\\x0c\\x0d-\\x7f]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]))))*(((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(\\x22)))@((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)+(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.?$";
        // String regex = "((?:^|[&(])[ \\t]*)for(?: ?\\/[a-z?](?:[ :](?:\"[^\"]*\"|\\S+))?)* \\S+ in \\([^)]+\\) do";
        // String regex = "";
        // String regex = "";
        // String regex = "";
        // String regex = "";

        // String regex = "[_$A-Za-z\\xA0-\\uFFFF] ";
        // String regex = "[$\\w\\xA0-\\uFFFF]";
        // String regex = "\\s";
        // String regex = "[^()]";
        // String regex = "(function(?:\\s+[_$A-Za-z\\xA0-\\uFFFF][$\\w\\xA0-\\uFFFF]*)?\\s*\\(\\s*)(?!\\s)(?:[^()]|\\([^()]*\\))+?(?=\\s*\\))";
        String regex = "^\\S+@\\S+\\.\\w+$";
        // String regex = "(\\(\\s*)(?!\\s)(?:[^()]|\\([^()]*\\))+?(?=\\s*\\)\\s*=>)[\\s\\S]*";
        // String regex = "(^[ \\t]*)(?:(?=\\S)(?:[^{}\\r\\n:()]|::?[\\w-]+(?:\\([^)\\r\\n]*\\))?|\\{[^}\\r\\n]+\\})+)(?:(?:\\r?\\n|\\r)(?:\\1(?:(?=\\S)(?:[^{}\\r\\n:()]|::?[\\w-]+(?:\\([^)\\r\\n]*\\))?|\\{[^}\\r\\n]+\\})+)))*(?:,$|\\{|(?=(?:\\r?\\n|\\r)(?:\\{|\\1[ \\t]+)))";
        /*
        "(a" + " ".repeat(i*10000) + "\n!\n";
         */
        // TODO: 所有正则都在正则结尾加上[\s\S]*，用来生成路径
        regex += "[\\s\\S]*";
        Pattern p = Pattern.compile(regex);
        // Pattern.printObjectTree(p.root);
        System.out.println("flowchart TD");
        Analyzer.printPatternStruct(p.root);
        Analyzer a = new Analyzer(p, 8);



    }

}
