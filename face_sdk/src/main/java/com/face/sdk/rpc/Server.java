package com.face.sdk.rpc;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.face.sdk.jni.FACE_BOX;
import com.face.sdk.jni.FaceSDK;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * project : face-web-server
 * Code Create : 2019/5/5
 * Class : com.face.sdk.rpc.Server
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class Server {
    public Server() {
    }

    private static final FaceSDK faceSDK;

    static {
        faceSDK = new FaceSDK();
        faceSDK.faceModelConf(new float[]{0.7f, 0.8f, 0.9f}, 100);
        faceSDK.faceModelInit(4);
        System.out.println("inited.");
    }

    private static synchronized void faceDetect(BufferedImage image, ArrayList<FACE_BOX> faceBoxes) {
        faceSDK.faceDetect(image, faceBoxes);
    }


    private static void startServer(String hostName, int port) throws IOException {
        ServerSocket ss = new ServerSocket(port, 50, InetAddress.getByName(hostName));
        int size = 2;
        ExecutorService executorService = new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        while (true) {
            final Socket socket = ss.accept();
            executorService.submit(() -> {
                try {
                    System.out.println("===========================================");

                    PrintWriter pw = new PrintWriter(socket.getOutputStream());
                    try {

                        InputStream is = socket.getInputStream();

                        long start = System.currentTimeMillis();
                        byte[] buff = new byte[4096];
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int len;
                        while ((len = is.read(buff)) != -1) {
                            baos.write(buff, 0, len);
                        }
                        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                        long end = System.currentTimeMillis();
                        System.out.println("拉流：" + (end - start + "ms"));


                        start = System.currentTimeMillis();
                        BufferedImage image = ImageIO.read(bais);
                        if (image == null) throw new IOException();
                        end = System.currentTimeMillis();
                        System.out.println("解析图片：" + (end - start + "ms"));


                        start = System.currentTimeMillis();
                        ArrayList<FACE_BOX> faceBoxes = new ArrayList<>();
                        faceDetect(image, faceBoxes);
                        end = System.currentTimeMillis();
                        System.out.println("算法：" + (end - start + "ms"));


                        pw.println(JSON.toJSONString(faceBoxes, new SimplePropertyPreFilter(FACE_BOX.class, "x", "y", "width", "height", "points", "imgRows", "imgCols", "imgChs", "embedding")));
                        pw.flush();
                        System.out.println("===========================================");
                    } catch (Exception e) {
                        pw.println("error");
                        pw.flush();
                    }

                    socket.shutdownInput();
                    socket.shutdownOutput();

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        }

    }


    public static void main(String[] args) throws IOException {
        Server.startServer("0.0.0.0", 8088);
    }
}
