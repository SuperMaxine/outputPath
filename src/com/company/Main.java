package com.company;

import regex.*;

import java.io.*;
import java.util.concurrent.*;

/**
 * @author SuperMaxine
 */
public class Main {

    public static void main(String[] args) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream("prism.txt");
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
        int count = 0;
        String str = null;
        while(true)
        {
            try {
                if (!((str = bufferedReader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            count ++ ;
            regexAnalyzeLimitTime(str, count);
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
        try {
            Future<attackResult> future = exec.submit(call);
            //返回值类型为限制的方法的返回值类型
            attackMsg = future.get(1000 * 30, TimeUnit.MILLISECONDS); //任务处理超时时间设为 5 秒

            result += "id:" + id + "\n" + regex + "\n" + attackMsg.attackable + "\n" + attackMsg.attackMsg + "\n\n";
        } catch (TimeoutException ex) {
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
        return ;
    }

}
