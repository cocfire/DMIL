package com.routon.plcloud.device.api.utils;

import com.google.zxing.common.BitMatrix;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author FireWang
 * @date 2020/5/11 10:32
 * 二维码生成工具
 */
public class QrCodeUtils {
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private QrCodeUtils() {
    }

    /**
     * 绘制二维码流
     *
     * @param matrix
     * @return
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    public static BufferedImage writeToFile(BitMatrix matrix, String format)
            throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        return image;
    }

    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream)
            throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }

    /**
     * @param title     标题文字
     * @param pressText 文字
     * @param path      图片路径
     * @param image     需要添加文字的图片
     * @param color     字体颜色
     * @param fontSize  字体大小
     * @param width     图片宽
     * @param height    图片高
     * @为图片添加文字
     */
    public static boolean pressText(String title, List<String> pressText, String path, BufferedImage image, String fontStyle, Color color, int fontSize, int width, int height) {
        boolean isoperate = false;
        try {
            int imageW = image.getWidth();
            int imageH = image.getHeight();
            Graphics g = image.createGraphics();
            g.drawImage(image, 0, 0, imageW, imageH, null);
            g.setColor(color);

            //二维码上方写入标题
            g.setFont(new Font(fontStyle, Font.BOLD, fontSize + 4));
            int titlelen = stringLength(title) / 2;
            //x开始的位置：（图片宽度-字体大小*字的个数）/2，英文转换为半个汉子
            int titleX = (width - (fontSize * titlelen)) / 2 - 6;
            //y开始的位置：（图片高度-图片宽度）/2 - 字体宽度
            int titleY = (height - width) / 2 - fontSize + 4;
            g.drawString(title, titleX, titleY);

            //二维码下方写入每行文字
            g.setFont(new Font(fontStyle, Font.BOLD, fontSize));
            for (int i = 0; i < pressText.size(); i++) {
                //计算文字开始的位置
                //x开始的位置：（图片宽度-字体大小*字的个数）/2，英文转换为半个汉子
                int stringlen = stringLength(pressText.get(i)) / 2;
                int startX = (width - (fontSize * stringlen)) / 2 - 6;
                //y开始的位置：图片高度-（图片高度-图片宽度）/2
                int startY = height - (height - width) / 2 + 25;
                g.drawString(pressText.get(i), startX, startY + 23 * i);
            }
            g.dispose();

            FileOutputStream out = new FileOutputStream(path);
            ImageIO.write(image, "JPEG", out);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(image);
            out.close();
            System.out.println("image press success");
            isoperate = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return isoperate;
    }

    /**
     * 判断文字长度
     *
     * @param value
     * @return
     */
    public static int stringLength(String value) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }
}
