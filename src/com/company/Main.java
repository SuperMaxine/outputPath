package com.company;

import regex.*;

import java.io.*;

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
        // BufferedWriter writer = null;
        // try {
        //     writer = new BufferedWriter(new FileWriter("prism_result.txt"));
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }

        // 如果不存在result.txt文件，则创建
        if (!new File("result.txt").exists()) {
            try {
                new File("result.txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // try {
        //     BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt", true));
        //     writer.write("Start");
        //     writer.close();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }

        String str = null;
        while(true)
        {
            try {
                if (!((str = bufferedReader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        //close
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
