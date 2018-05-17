package com.yuechegang.zixing.qr;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

/**
 * Copyright 2005 Xiamen wee0 Soft Co. Ltd.All right reserved.
 *
 * @author <a href="mailto:ruanjingwang@gmail.com">runner</a>
 * @version 1.0
 * @description
 * @date 2017年10月20日 下午2:15:02
 */

public class Test {
    public static void main(String[] args) {
        OutputStream file;
        String inputFile = "C:\\Users\\cn40580\\IdeaProjects\\qr-generate\\src\\main\\resources\\input_link\\input.txt";
        String outputFolder = "C:\\Users\\cn40580\\IdeaProjects\\qr-generate\\src\\main\\resources\\output\\";
        String module = "C:\\Users\\cn40580\\IdeaProjects\\qr-generate\\src\\main\\resources\\module\\model.png";
        try {
            FileReader fr = new FileReader(inputFile);
            BufferedReader bf = new BufferedReader(fr);
            int b;
            while ((b = bf.read()) != -1) {
                String line = bf.readLine();
                file = new FileOutputStream(outputFolder + line.substring(44) + ".png");
                QRCodeUtilBk.encode(line, module, file, true);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
//    public static void main(String[] args) {
//        String text = "http://www.yuechegang.com/scan.html?cardid=2018051410254841";
//        OutputStream file;
//        try {
//            file = new FileOutputStream("C:\\Users\\cn40580\\Desktop\\Z_ONTSOL\\abc.png");
//            QRCodeUtilBk.encode(text, "C:\\Users\\cn40580\\IdeaProjects\\qr-generate\\src\\main\\resources\\module\\model.png", file, true);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}
