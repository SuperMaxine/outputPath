package com.company;

import regex.*;

import java.io.*;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

/**
 * @author SuperMaxine
 */
public class Main {

    public static void main(String[] args) {
        // NQ、EOA、EOD
        // testSingleRegex("^(?:-+|\\.+)|(?:-+|\\.+)$");
        // testSingleRegex("@[\\w-]+[\\s\\S]*?(?:;|(?=\\s*\\{))");
        // testSingleRegex("^(a|(?=abc)\\w+)+$");
        // testSingleRegex("^(a|(?=abc)\\\\w+)+$"); //表现为正则引擎stackoverflow
        // testSingleRegex("((?:^|[&(])[ \\t]*)for(?: ?\\/[a-z?](?:[ :](?:\"[^\"]*\"|\\S+))?)* \\S+ in \\([^)]+\\) do");
        // testSingleRegex("((?:^|[&(])[ \\t]*)if(?: ?\\/[a-z?](?:[ :](?:\"[^\"]*\"|\\S+))?)* (?:not )?(?:cmdextversion \\d+|defined \\w+|errorlevel \\d+|exist \\S+|(?:\"[^\"]*\"|\\S+)?(?:==| (?:equ|neq|lss|leq|gtr|geq) )(?:\"[^\"]*\"|\\S+))");
        // testSingleRegex("((?:^|[&(])[ \\t]*)if(?: ?\\/[a-z?](?:[ :](?:\"[^\"]*\"|\\S+))?)* (?:not )?(?:cmdextversion \\d+|defined \\w+|errorlevel \\d+|exist \\S+|(?:\"[^\"]*\"|\\S+)?(?:==| (?:equ|neq|lss|leq|gtr|geq) )(?:\"[^\"]*\"|\\S+))");

        //POA


        // SLQ
        // testSingleRegex("<!--[\\s\\S]*?-->");
        // testSingleRegex("<\\?[\\s\\S]+?\\?>");
        // testSingleRegex("<!\\[CDATA\\[[\\s\\S]*?]]>");
        // testSingleRegex("(\\[)[\\s\\S]+(?=\\]>$)");
        // testSingleRegex("\\=\\S*(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s'\\\">=]+)");
        // testSingleRegex("[-_a-z\\xA0-\\uFFFF][-\\w\\xA0-\\uFFFF]*(?=\\s*:)");
        // testSingleRegex("[-a-z0-9]+(?=\\()");
        // testSingleRegex("(<style[\\s\\S]*?>)(?:<!\\[CDATA\\[(?:[^\\]]|\\](?!\\]>))*\\]\\]>|(?!<!\\[CDATA\\[)[\\s\\S])*?(?=<\\/style>)");
        // testSingleRegex("<!\\[CDATA\\[[\\s\\S]*?\\]\\]>");
        // testSingleRegex("\\s*style=(\\\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1");
        // testSingleRegex("\\w+(?=\\()");
        // testSingleRegex("#?[_$a-zA-Z\\xA0-\\uFFFF][$\\w\\xA0-\\uFFFF]*(?=\\s*(?:\\.\\s*(?:apply|bind|call)\\s*)?\\()");
        // testSingleRegex("((?:^|[^$\\w\\xA0-\\uFFFF.\\\"'\\])\\s]|\\b(?:return|yield))\\s*)\\/(?:\\[(?:[^\\]\\\\\\r\\n]|\\\\.)*]|\\\\.|[^/\\\\\\[\\r\\n])+\\/[gimyus]{0,6}(?=(?:\\s|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/)*(?:$|[\\r\\n,.;:})\\]]|\\/\\/))");
        // testSingleRegex("#?[_$a-zA-Z\\xA0-\\uFFFF][$\\w\\xA0-\\uFFFF]*(?=\\s*[=:]\\s*(?:async\\s*)?(?:\\bfunction\\b|(?:\\((?:[^()]|\\([^()]*\\))*\\)|[_$a-zA-Z\\xA0-\\uFFFF][$\\w\\xA0-\\uFFFF]*)\\s*=>))");
        // testSingleRegex("[a-z]+$");
        // testSingleRegex("(function(?:\\s+[_$A-Za-z\\xA0-\\uFFFF][$\\w\\xA0-\\uFFFF]*)?\\s*\\(\\s*)(?!\\s)(?:[^()]|\\([^()]*\\))+?(?=\\s*\\))");


        // 引擎stackoverflow
        // testSingleRegex("[^{}\\s](?:[^{};\\\"']|(\\\"|')(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1)*?(?=\\s*\\{)");

        // 需要实现优化“某些后缀正则影响了中缀构造方式”
        // testSingleRegex("\\/\\*[\\s\\S]*?\\*\\/");

        // 暂不明，可能是反向引用
        // testSingleRegex("(\\\"|')(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1");
        // testSingleRegex("([\\\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1");

        // 不明原因
        // testSingleRegex("`(?:\\\\[\\s\\S]|\\${(?:[^{}]|{(?:[^{}]|{[^}]*})*})+}|(?!\\${)[^\\\\`])*`"); //Exception in thread "main" regex.PatternSyntaxException: Illegal repetition near index 14
        // testSingleRegex("<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s(?:\\s*[^\\s>\\/=]+(?:\\s*=\\s*(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s'\\\">=]+(?=[\\s>]))|(?=[\\s/>])))+)?\\s*\\/?>");

        // testSingleRegex("ab|cd");
        testDataSet("prism.txt");



        // testSingleRegex("(\\(\\s*)(?!\\s)(?:[^()]|\\([^()]*\\))+(?=\\s*\\)\\s*=>) ");
        // String file = "prism.txt";
        // FileInputStream inputStream = null;
        // try {
        //     inputStream = new FileInputStream(file);
        // } catch (FileNotFoundException e) {
        //     e.printStackTrace();
        // }
        // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        //
        // Analyzer

        // Sola-da
        // testSingleRegex("\\s*\\n\\s*");
        // testSingleRegex(".*[/\\\\]");
        // testSingleRegex(".*\\.");
        // testSingleRegex(" *, *");
        // testSingleRegex("\\&([^;]+);");
        // testSingleRegex("\\s+$");
        // testSingleRegex("(?: BePC|[ .]*fc[ \\d.]+)$");
        // testSingleRegex("^ +| +$");
        // testSingleRegex("(\\d+)milli(?:second)?[s]?");
        // testSingleRegex("(\\d+)second[s]?");
        // testSingleRegex("(\\d+)minute[s]?");
        // testSingleRegex("(\\d+)hour[s]?");
        // testSingleRegex("(\\d+)day[s]?");
        // testSingleRegex("([A-Z\\d]+)([A-Z][a-z])");
        // testSingleRegex("\\&([^;]+);");
        // testSingleRegex("Dell.*Streak|Dell.*Aero|Dell.*Venue|DELL.*Venue Pro|Dell Flash|Dell Smoke|Dell Mini 3iX|XCD28|XCD35|\\b001DL\\b|\\b101DL\\b|\\bGS01\\b");
        // testSingleRegex("(?=.*\\bAndroid\\b)(?=.*\\bMobile\\b)");
        // testSingleRegex("(?=.*\\bAndroid\\b)(?=.*\\bSD4930UR\\b)");
        // testSingleRegex("(?=.*\\bAndroid\\b)(?=.*\\b(?:KFOT|KFTT|KFJWI|KFJWA|KFSOWI|KFTHWI|KFTHWA|KFAPWI|KFAPWA|KFARWI|KFASWI|KFSAWI|KFSAWA)\\b)");
        // testSingleRegex("(?=.*\\bWindows\\b)(?=.*\\bARM\\b)");
        // testSingleRegex("(CriOS|Chrome)(?=.*\\bMobile\\b)");
        // testSingleRegex("(\\d+[a-zA-Z]+)?");
        // testSingleRegex("if\\s*\\([^)]+\\)\\s*\\{\\s*\\}(?!\\s*else)");
        // testSingleRegex("[a-z][A-Z]|[A-Z]{2,}[a-z]|[0-9][a-zA-Z]|[a-zA-Z][0-9]|[^a-zA-Z0-9 ]");
        // testSingleRegex("^\\s+|\\s+$");
        // testSingleRegex("(^\\s+|\\s+$)");

        // 失败案例
        // testSingleRegex("[0-9]*['a-z\\u00A0-\\u05FF\\u0700-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]+|[\\u0600-\\u06FF\\/]+(\\s*?[\\u0600-\\u06FF]+){1,2}"); // fasle


        // testSingleRegex("^\\|={3,}(?:(?:y?\\n|\\r).*)*?(?:\\r?\\n|\\r)\\|={3,}$"); // "|===" + "\r\n".repeat(i*10000) + ""

        // testSingleRegex("(^|[^\\\\])(?:(?:\\B\\[(?:[^\\]\\\\\\\"]|([\\\"'])(?:(?!\\2)[^\\\\]|\\\\.)*\\2|\\\\.)*\\])?(?:\\b_(?!\\s)(?: _|[^_\\\\\\r\\n]|\\\\.)+(?:(?:\\r?\\n|\\r)(?: _|[^_\\\\\\r\\n]|\\\\.)+)*_\\b|\\B``(?!\\s).+?(?:(?:\\r?\\n|\\r).+?)*''\\B|\\B`(?!\\s)(?:[^`'\\s]|\\s+\\S)+['`]\\B|\\B(['*+#])(?!\\s)(?: \\3|(?!\\3)[^\\\\\\r\\n]|\\\\.)+(?:(?:\\r?\\n|\\r)(?: \\3|(?!\\3)[^\\\\\\r\\n]|\\\\.)+)*\\3\\B)|(?:\\[(?:[^\\]\\\\\\\"]|([\\\"'])(?:(?!\\4)[^\\\\]|\\\\.)*\\4|\\\\.)*\\])?(?:(__|\\*\\*|\\+\\+\\+?|##|\\$\\$|[~^]).+?(?:(?:\\r?\\n|\\r).+?)*\\5|\\{[^}\\r\\n]+\\}|\\[\\[\\[?.+?(?:(?:\\r?\\n|\\r).+?)*\\]?\\]\\]|<<.+?(?:(?:\\r?\\n|\\r).+?)*>>|\\(\\(\\(?.+?(?:(?:\\r?\\n|\\r).+?)*\\)?\\)\\)))");
        // testSingleRegex("(?:\\[(?:[^\\]\\\\\\\"]|([\\\"'])(?:(?!\\1)[^\\\\]|\\\\.)*\\1|\\\\.)*\\])"); // √
        /*
        MatchSteps: 10000115
        true
        OneCounting
        prefix:[
        pump:'
        suffix:\n\b\n
        Run time: 1280ms
         */
        // testSingleRegex("(^|[^\\\\](?:\\\\\\\\)*)([\\\"'])(?:\\\\[\\s\\S]|\\$\\([^)]+\\)|`[^`]+`|(?!\\2)[^\\\\])*\\2");
        /*
        MatchSteps: 10000132
        true
        OneCounting
        prefix:"
        pump:`\`
        suffix:\n\b\n
        Run time: 879ms
         */
        // testSingleRegex("(\\\"|')(?:#\\{[^}]+\\}|\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1");
        /*
        MatchSteps: 10000158
        true
        OneCounting
        prefix:"
        pump:#{\}
        suffix:\n\b\n
        Run time: 739ms
         */
        // testSingleRegex("\\\"(?:[^\\\\\\\"\\r\\n]|\\\\(?:[abfnrtv\\\\\\\"]|\\d+|x[0-9a-fA-F]+))*\\\"");
        /*
        MatchSteps: 10000163
        true
        OneCounting
        prefix:"
        pump:\00
        suffix:\n\b\n
        Run time: 407ms
         */
        // testSingleRegex("([\\\"'])(?:(?!\\1)[^\\\\\\r\\n]|\\\\z(?:\\r\\n|\\s)|\\\\(?:\\r\\n|[\\s\\S]))*\\1|\\[(=*)\\[[\\s\\S]*?\\]\\2\\]");

        // testSingleRegex("((?:^|\\r?\\n|\\r)[\\t ]*)[%.#][\\w\\-#.]*[\\w\\-](?:\\([^)]+\\)|\\{(?:\\{[^}]+\\}|[^}])+\\}|\\[[^\\]]+\\])*[\\/<>]*");
        // testSingleRegex("(^[ \\t]*)(?:(?=\\S)(?:[^{}\\r\\n:()]|::?[\\w-]+(?:\\([^)\\r\\n]*\\))?|\\{[^}\\r\\n]+\\})+)(?:(?:\\r?\\n|\\r)(?:\\1(?:(?=\\S)(?:[^{}\\r\\n:()]|::?[\\w-]+(?:\\([^)\\r\\n]*\\))?|\\{[^}\\r\\n]+\\})+)))*(?:,$|\\{|(?=(?:\\r?\\n|\\r)(?:\\{|\\1[ \\t]+)))");
        // testSingleRegex("(^|\\r?\\n|\\r)\\/[\\t ]*(?:(?:\\r?\\n|\\r)(?:.*(?:\\r?\\n|\\r))*?(?:\\\\(?=[\\t ]*(?:\\r?\\n|\\r))|$)|\\S.*)");
        /*
        MatchSteps: 10000145
        true
        OneCounting
        prefix:

        pump:/

        suffix:\n\b\n
        Run time: 9984ms
         */
        // testSingleRegex("=(?:(\\\"|')(?:\\\\[\\s\\S]|\\{(?!\\{)(?:\\{(?:\\{[^{}]*}|[^{}])*}|[^{}])+}|(?!\\1)[^\\\\])*\\1|[^\\s'\\\">=]+)");
        /*
        MatchSteps: 10000121
        true
        OneCounting
        prefix:="
        pump:{\}
        suffix:\n\b\n
        Run time: 1431ms
         */
        // testSingleRegex("(\\()lambda\\s+\\((?:&?[-+*/_~!@$%^=<>{}\\w]+\\s*)*\\)");
        // testSingleRegex("^[A-Za-z][A-Za-z0-9+-.]*:\\/\\/");
        // testSingleRegex("^([a-z][a-z0-9.+-]*:)?(\\/\\/)?([\\\\/]+)?([\\S\\s]*)");
    }

    private static void testSingleRegex(String regex) {
        Pattern p = Pattern.compile(regex + "[\\s\\S]*");
        System.out.println("flowchart TD");
        Analyzer.printPatternStruct(p.root);
        // log start time
        long startTime = System.currentTimeMillis();
        Analyzer a = new Analyzer(p, 10, regex);
        // log end time and print run time
        long endTime = System.currentTimeMillis();
        System.out.println(a.attackable);
        System.out.println(a.attackMsg);
        System.out.println("Run time: " + (endTime - startTime) + "ms");
    }

    private static void testDataSet(String file){
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        // 如果不存在result.txt文件，则创建
        if (!new File("result.txt").exists()) {
            try {
                new File("result.txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ExecutorService es;
        int count = 1;
        String str = null;
        while(true)
        {
            try {
                if (!((str = bufferedReader.readLine()) != null)) break;
                regexAnalyzeLimitTime(str, count);
            } catch (IOException e) {
                e.printStackTrace();
            }
            count ++ ;
            // sleep 2 second
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //close
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class attackResult{
        public boolean attackable;
        public String attackMsg;

        public attackResult(){
            attackable = false;
            attackMsg = "";
        }

        public attackResult(boolean attackable, String attackMsg){
            this.attackable = attackable;
            this.attackMsg = attackMsg;
        }
    }

    private static void regexAnalyzeLimitTime(String regex, int id) {
        attackResult attackMsg;
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        Callable<attackResult> call = new Callable<attackResult>() {
            @Override
            public attackResult call() throws Exception {
                //开始执行耗时操作 ，这个方法为你要限制执行时间的方法
                try {
                    Pattern p = Pattern.compile(regex + "[\\s\\S]*");
                    Analyzer a = new Analyzer(p, 10, regex);
                    return new attackResult(a.attackable, a.attackMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new attackResult(false, e.toString());
                }
            }
        };
        String result = "";
        Future<attackResult> future = null;
        try {
            future = exec.submit(call);
            //返回值类型为限制的方法的返回值类型
            attackMsg = future.get(1000 * 30, TimeUnit.MILLISECONDS); //任务处理超时时间设为 5 秒

            result += "id:" + id + "\n" + regex + "\n" + attackMsg.attackable + "\n" + attackMsg.attackMsg + "\n\n";
        } catch (TimeoutException ex) {
            future.cancel(true);
            result += "id:" + id + "\n" + regex + "\n" + "Timeout\n\n";
        } catch (Exception e) {
            result += "id:" + id + "\n" + regex + "\n" + e.toString();
            e.printStackTrace();
        }

        System.out.println(result);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("result.txt", true));
            writer.write(result);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 关闭线程池
        exec.shutdown();
        return;
    }

}
