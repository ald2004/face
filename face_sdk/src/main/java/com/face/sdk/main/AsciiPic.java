package com.face.sdk.main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 */
public class AsciiPic {

    /**
     * @param path 图片路径
     */
    private static void createAsciiPic(final String path, int outWidth) {
        final String base = "@#8%O0o=!;~,.";// 字符串由复杂到简单
        try {
            final BufferedImage tmp = ImageIO.read(new File(path));

            BufferedImage image = new BufferedImage(outWidth, (int) (tmp.getHeight() * (outWidth*1.0 / tmp.getWidth())), BufferedImage.TYPE_INT_ARGB);

            Graphics graphics = image.getGraphics();
            graphics.drawImage(tmp, 0, 0, image.getWidth(), image.getHeight(), null);

            for (int y = 0; y < image.getHeight(); y += 2) {
                for (int x = 0; x < image.getWidth(); x++) {
                    final int pixel = image.getRGB(x, y);
                    final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
                    final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                    final int index = Math.round(gray * (base.length() + 1) / 255);
                    System.out.print(index >= base.length() ? " " : String.valueOf(base.charAt(index)));
                }
                System.out.println();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test
     *
     * @param args
     */
    public static void main(final String[] args) {
        AsciiPic.createAsciiPic("C:\\Users\\wangxiaoming\\Desktop\\logo.png", 900);
    }
}
