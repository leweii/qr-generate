package com.yuechegang.zixing.qr;

/**
 * Copyright 2005 Xiamen wee0 Soft Co. Ltd.All right reserved.
 *
 * @description
 * @author <a href="mailto:ruanjingwang@gmail.com">runner</a>
 * @version 1.0
 * @date 2017年10月20日 下午2:14:28
 */

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * @author Liuy
 * @ClassName: QRCodeUtil
 * @Description: 二维码编码
 * @date 2016年7月9日 下午3:03:24
 */
public class QRCodeUtilBk {
    private static final int SIZE_TIMES = 8;
    // 设置二维码编码格式
    private static final String CHARSET = "utf-8";
    // 保存的二维码格式
    private static final String FORMAT_NAME = "PNG";
    // 二维码尺寸
    private static final int QRCODE_SIZE = 630;
    // LOGO宽度
    private static final int BACKGROUND_WIDTH = 810;
    // LOGO高度
    private static final int BACKGROUND_HEIGHT = 810;

    /**
     * @param content        二维码内容
     * @param backgroundPath logo图片地址
     * @param needCompress   是否压缩logo图片大小
     * @return BufferedImage 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: createImage
     * @Description: 将二维码内容创建到Image流
     */
    private static BufferedImage createImage(String content, String backgroundPath, boolean needCompress) throws Exception {

        Image src = ImageIO.read(new File(backgroundPath));

        BufferedImage image = new BufferedImage(BACKGROUND_WIDTH * SIZE_TIMES, BACKGROUND_HEIGHT * SIZE_TIMES, BufferedImage.TYPE_INT_RGB);
        Graphics2D graph = image.createGraphics();

        graph.drawImage(src, 0, 0, BACKGROUND_WIDTH * SIZE_TIMES, BACKGROUND_HEIGHT * SIZE_TIMES, null);
        Shape shape = new RoundRectangle2D.Float(0, 0, BACKGROUND_WIDTH * SIZE_TIMES, BACKGROUND_HEIGHT * SIZE_TIMES, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();

        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 0);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE * SIZE_TIMES, QRCODE_SIZE * SIZE_TIMES, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x + 160 * SIZE_TIMES, y + 160 * SIZE_TIMES, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }

    /**
     * 将图片背景透明化处理
     *
     * @param image
     * @return
     * @throws Exception
     */
    private static BufferedImage transferBackgroundAlpha(Image image) throws Exception {
        ImageIcon imageIcon = new ImageIcon(image);
        BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
        g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
        //透明度
        int alpha = 100;
        for (int j1 = bufferedImage.getMinY(); j1 < bufferedImage.getHeight(); j1++) {
            for (int j2 = bufferedImage.getMinX(); j2 < bufferedImage.getWidth(); j2++) {
                int rgb = bufferedImage.getRGB(j2, j1);
                int R = (rgb & 0xff0000) >> 16;
                int G = (rgb & 0xff00) >> 8;
                int B = (rgb & 0xff);
                if (((255 - R) < 30) && ((255 - G) < 30) && ((255 - B) < 30)) {
                    //背景颜色设置
                    rgb = ((alpha + 1) << 24) | (rgb & 0XFFFFD700);
                }
                bufferedImage.setRGB(j2, j1, rgb);
            }
        }
        g2D.drawImage(bufferedImage, 0, 0, imageIcon.getImageObserver());
        return bufferedImage;
    }

    /**
     * @param source       二维码Image流
     * @param logoPath     logo地址
     * @param needCompress 是否压缩大小
     * @return void 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: insertImage
     * @Description: 将logo插入到二维码中
     */
    private static void insertImage(BufferedImage source, String logoPath, boolean needCompress) throws Exception {
        File file = new File(logoPath);
        if (!file.exists()) {
            System.err.println("" + logoPath + "   该文件不存在！");
            return;
        }
        Image src = ImageIO.read(new File(logoPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (needCompress) { // 压缩LOGO
            if (width > BACKGROUND_WIDTH * SIZE_TIMES) {
                width = BACKGROUND_WIDTH * SIZE_TIMES;
            }
            if (height > BACKGROUND_HEIGHT * SIZE_TIMES) {
                height = BACKGROUND_HEIGHT * SIZE_TIMES;
            }
            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            src = image;
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (QRCODE_SIZE * SIZE_TIMES - width) / 2;
        int y = (QRCODE_SIZE * SIZE_TIMES - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * @param destPath 文件夹地址
     * @return void 返回类型
     * @throws
     * @Title: mkdirs
     * @Description: 创建文件夹
     */
    private static boolean mkdirs(String destPath) {
        File file = new File(destPath);
        // 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
            return true;
        }

        return false;
    }

    /**
     * @param content        二维码内容
     * @param backgroundPath backgroundPath
     * @param destPath       目标保存地址
     * @param needCompress   是否压缩logo图片大小
     * @return void 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: encode
     * @Description: 生成二维码
     */
    private static void encode(String content, String backgroundPath, String destPath, boolean needCompress) throws Exception {
        BufferedImage image = QRCodeUtilBk.createImage(content, backgroundPath, needCompress);

        if (mkdirs(destPath)) {
            String file = new Random().nextInt(99999999) + ".jpg";
            ImageIO.write(image, FORMAT_NAME, new File(destPath + "/" + file));
        }
    }

    /**
     * @param content  二维码内容
     * @param destPath 目标保存地址
     * @return void 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: encode
     * @Description: 生成二维码
     */
    public static void encode(String content, String destPath) throws Exception {
        QRCodeUtilBk.encode(content, null, destPath, false);
    }

    /**
     * @param content      二维码内容
     * @param logoPath     logo图片地址
     * @param output       输出流
     * @param needCompress 是否压缩logo图片大小
     * @return void 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: encode
     * @Description: 生成二维码
     */
    public static void encode(String content, String logoPath, OutputStream output, boolean needCompress) throws Exception {
        BufferedImage image = QRCodeUtilBk.createImage(content, logoPath, needCompress);
        // 透明化处理白色背景
        image = transferBackgroundAlpha(image);
        ImageIO.write(image, FORMAT_NAME, output);

    }

    /**
     * @param content 二维码内容
     * @param output  输出流
     * @return void 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: encode
     * @Description: 生成二维码
     */
    public static void encode(String content, OutputStream output) throws Exception {
        QRCodeUtilBk.encode(content, null, output, false);
    }


    /**
     * @param file 文件对象
     * @return String 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: decode
     * @Description: 对二维码解码
     */
    private static String decode(File file) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        return new MultiFormatReader().decode(bitmap, hints).getText();
    }

    /**
     * @param path 文件路径
     * @return String 返回类型
     * @throws Exception 参数说明
     * @throws
     * @Title: decode
     * @Description: 对二维码解码
     */
    public static String decode(String path) throws Exception {
        return QRCodeUtilBk.decode(new File(path));
    }
}