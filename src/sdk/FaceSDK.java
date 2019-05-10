package com.face.sdk.jni;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static java.lang.Math.sqrt;

public class FaceSDK {
    public static final int FACE_SDK_STATUS_OK = 0;// 正常
    public static final int FACE_SDK_STATUS_LICENSE_ERROR = -1;// 授权错误

    public static final int FACE_SDK_STATUS_NOT_IN_USER = 201; // 未识别到脸

    public static final int FACE_SDK_STATUS_ILLEGAL_PARAMETER = 300; // 非法参数
    public static final int FACE_SDK_STATUS_FACE_SIZE_TOO_SMALL = 301; // 最小人脸尺寸必须 48像素

    public static final int FACE_SDK_UNKNOWN_ERROR = 400;// 未知
    public static final int FACE_SDK_STATUS_NOT_INIT_ERROR = 401;// 模型未初始化
    public static final int FACE_SDK_STATUS_EMPTY_MAT_ERROR = 402; // 空的图片
    public static final int FACE_SDK_STATUS_EMPTY_USER_ERROR = 403; // 空的图片

    public static final int FACE_SDK_STATUS_NOT_FOUND_ERROR = 404;// 找不到目录或文件
    public static final int FACE_SDK_STATUS_IO_ERROR = 405; // IO 异常 打开或写入文件失败

    public static final int FACE_SDK_STATUS_MODEL_LOAD_ERROR = 405; // 找不到目录或文件

    public double calcSimilar(float[] v1, float[] v2) {
        if (v1.length != v2.length || v1.length == 0)
            return 0;
        double ret = 0, mod1 = 0, mod2 = 0;
        for (int i = 0; i < v1.length; ++i) {
            ret += v1[i] * v2[i];
            mod1 += v1[i] * v1[i];
            mod2 += v2[i] * v2[i];
        }
        return ret / sqrt(mod1) / sqrt(mod2);
    }

    /**
     * 初始化模型
     *
     * @param threadNum def 3
     * @return
     */
    public native int faceModelInit(int threadNum);

    /**
     * 设置
     *
     * @param threshold   float threshold[3] = {0.9f, 0.9f, 0.99f};
     * @param minFaceSize
     * @return
     */
    public native int faceModelConf(float[] threshold, int minFaceSize);


    /**
     * 寻找人脸，未旋转矫正
     *
     * @param faceBoxes face box
     * @return
     */
    public native int faceDetect(byte[] data, int rows, int cols, ArrayList<FACE_BOX> faceBoxes);

    /**
     * 封装 使java 更方便调用
     *
     * @param image
     * @param faceBoxes
     * @return
     */
    public int faceDetect(BufferedImage image, ArrayList<FACE_BOX> faceBoxes) {
        if (BufferedImage.TYPE_3BYTE_BGR != image.getType()) {
            BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            bi.getGraphics().drawImage(image, 0, 0, null);
            image = bi;
        }

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        return faceDetect(pixels, image.getHeight(), image.getWidth(), faceBoxes);
    }

    /**
     * 打开 video
     *
     * @param url
     * @return
     */
    public native long VideoCapture(char[] url);

    /**
     * 打开本地摄像头
     *
     * @param device
     * @return
     */
    public native long VideoCapture(int device);

    /**
     * 释放摄像头
     *
     * @param videoCaptureHandle
     */
    public native void releaseVideo(long videoCaptureHandle);

    /**
     * 判断摄像头是否开启成功
     *
     * @param videoCaptureHandle
     * @return
     */
    public native boolean isVideoOpened(long videoCaptureHandle);

    /**
     * 读取视频图片
     *
     * @param videoCaptureHandle cv::VideoCapture 指针
     * @param matHandle          cv::Mat 指针,读取到的图片存放于此
     * @return
     */
    public native long read(long videoCaptureHandle, long matHandle);


    /**
     * 寻找人脸，未旋转矫正
     *
     * @param matHandle cv::Mat 指针
     * @param faceBoxes face box
     * @return
     */
    public native int faceDetect(long matHandle, ArrayList<FACE_BOX> faceBoxes);


    public native void sayHello();

    /**
     * 保存图片
     *
     * @param matHandle
     * @param savePath
     */
    public native boolean writeMat(long matHandle, char[] savePath);

    /**
     * 显示Mat
     *
     * @param matHandle
     */
    public native boolean showMat(char[] frameName, long matHandle);


    /**
     * cv::rectangle(dst, Point(box[i].x1, box[i].y1), Point(box[i].x2, box[i].y2), Scalar(225, 0, 225));
     *
     * @param matHandle
     * @param x
     * @param y
     * @param w
     * @param h
     * @param r
     * @param g
     * @param b
     * @return
     */
    public native boolean rectangleMat(long matHandle, int x, int y, int w, int h, int r, int g, int b);


    /**
     * cv::putText(img, txt, cv::Point(60, 20), 0.5, 0.5, cv::Scalar(0, 0, 255));
     *
     * @param matHandle
     * @param text
     * @param x
     * @param y
     * @param fontFace
     * @param fontScale
     * @param r
     * @param g
     * @param b
     * @return
     */
    public native boolean putTextMat(long matHandle, char[] text, int x, int y, int fontFace, double fontScale, int r, int g, int b);

    /**
     * 复制 Mat
     */
    public native void cloneMat(long matHandle, long cloneMatHandle);


    /**
     * 释放Mat
     *
     * @param matHandle
     */
    public native void releaseMat(long matHandle);

    /**
     * 等待按键
     *
     * @param delay
     */
    public native int waitKey(int delay);

    public native void showImg(byte[] data, int rows, int cols);

    private static void addDir(String s) {
        try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (String path : paths) {
                if (s.equals(path)) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = s;
            field.set(null, tmp);
        } catch (IllegalAccessException e) {
            //throw new IOException("Failed to get permissions to set library path");
        } catch (NoSuchFieldException e) {
            //throw new IOException("Failed to get field handle to set library path");
        }
    }

    private static String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static String SUFFIX = OS_NAME.contains("win") ? ".dll" : ".so";
    public static final String NATIVE_NAME = "face_sdk_jni";

    static {
        InputStream inputStream = null;
        String name = "/jni/"+(OS_NAME.contains("win") ? "" : "lib") + NATIVE_NAME + SUFFIX;
        try {
            inputStream = FaceSDK.class.getResource(name).openStream();
            String id = UUID.randomUUID().toString().toUpperCase().replace("-", "");
            File temp = new File(FileUtils.getTempDirectoryPath() + File.separator + "face" + File.separator + id + File.separator + name);
            FileUtils.forceMkdir(temp.getParentFile());
            FileUtils.copyToFile(inputStream, temp);
            String jniLibraryPath = temp.getParentFile().getAbsolutePath();
//            System.out.println("add library path:" + jniLibraryPath);
            addDir(jniLibraryPath);
            FileUtils.forceDeleteOnExit(temp.getParentFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.loadLibrary(NATIVE_NAME);
    }

    /**
     * 创建一个空Mat
     *
     * @return
     */
    public native long createMat();
}
