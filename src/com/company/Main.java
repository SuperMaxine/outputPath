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

        ExecutorService es = Executors.newFixedThreadPool(1);

        String str = null;
        while(true)
        {
            try {
                if (!((str = bufferedReader.readLine()) != null)) break;
                System.out.println(str);
                Future<?> future = es.submit( new Mythread(str) );
                try {
                    future.get(30, TimeUnit.SECONDS); // This waits timeout seconds; returns null
                } catch(TimeoutException e) {
                    future.cancel(true);
                    System.out.println("Timeout");
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt", true));
                        writer.write(str + "\n" + "Timeout" + "\n\n");
                        writer.close();
                    } catch (IOException ee) {
                        ee.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    future.cancel(true);
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    future.cancel(true);
                    e.printStackTrace();
                }
            } catch (IOException e) {
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

    public static class Mythread implements Runnable {
        private String str;
        public Mythread(String str){
            this.str = str;
        }
        @Override
        public void run()
        {
            try{
                Pattern p = Pattern.compile(str);
                Analyzer a = new Analyzer(p, 7);

                try {
                    // System.out.println("write");
                    BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt", true));
                    writer.write(str + "\n" +String.valueOf(a.attackable) + "\n" + a.attackMsg + "\n\n");
                    writer.close();
                    // System.out.println("write done");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt", true));
                    writer.write(str + "\n" + e.getMessage() + "\n\n");
                    writer.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

}
