package com.face.sdk.main;

import com.face.sdk.jni.FACE_BOX;
import com.face.sdk.jni.FaceSDK;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainTest {


    public static void main(String[] args) throws IOException {

        FaceSDK faceSDK = new FaceSDK();
        faceSDK.sayHello();

        BufferedImage image = ImageIO.read(new File("D:\\projects\\cpp\\face\\imgs\\王晓明.jpg"));

        faceSDK.faceModelConf(new float[]{0.6f, 0.7f, 0.8f}, 60);
        faceSDK.faceModelInit(3);
        ArrayList<FACE_BOX> faceBoxes = new ArrayList<FACE_BOX>();

        long start = System.currentTimeMillis();
        faceSDK.faceDetect(image, faceBoxes);

        long end = System.currentTimeMillis();

        System.out.println(end - start);
        for (FACE_BOX faceBox : faceBoxes) {

            System.out.println(Arrays.toString(faceBox.embedding));
            int[] data = new int[faceBox.imgCols * faceBox.imgRows];
            BufferedImage img = new BufferedImage(faceBox.imgCols, faceBox.imgRows, faceBox.imgChs == 1 ? BufferedImage.TYPE_BYTE_GRAY : faceBox.imgChs == 3 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_4BYTE_ABGR);
            System.out.println(faceBox.faceImg.length);
            System.out.println(faceBox.faceImg.length / faceBox.imgCols / faceBox.imgRows);

            for (int i = 0; i < faceBox.faceImg.length; i += faceBox.imgChs) {
                int b = ((faceBox.faceImg[i + 0] & 0x0FF));
                int g = ((faceBox.faceImg[i + 1] & 0x0FF));
                int r = ((faceBox.faceImg[i + 2] & 0x0FF));
                data[i / 3] = (r << 16) | (g << 8) | (b << 0);
//                data[i] = (255 << 16) | (128 << 8) | (0 << 0);
//                data[i / 3] = Color.RED.getRGB();

//                System.out.println((r) + "," + (g) + "," + b);
            }


            img.getRaster().setDataElements(0, 0, faceBox.imgRows, faceBox.imgCols, faceBox.faceImg);
//            img.setRGB(0, 0, faceBox.imgCols, faceBox.imgRows, data, 0, faceBox.imgRows);

            File outfile = new File("1.png");
            ImageIO.write(img, "png", outfile);
            Desktop.getDesktop().open(outfile);
        }
    }
}
