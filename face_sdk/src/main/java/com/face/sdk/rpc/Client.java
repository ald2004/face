package com.face.sdk.rpc;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * project : face-web-server
 * Code Create : 2019/5/5
 * Class : com.face.sdk.rpc.Client
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class Client {
    public Client() {
    }


    public static void main(String[] args) throws IOException {

        while (true) {
            long start = System.currentTimeMillis();
            Socket socket = new Socket("172.168.3.98", 8088);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage bi = ImageIO.read(new File("D:\\face\\imgs\\王晓明3.jpg"));
            ImageIO.write(bi, "jpg", socket.getOutputStream());
            socket.getOutputStream().flush();
            socket.shutdownOutput();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = br.readLine();

            if ("error".equals(line)) {
                throw new IOException();
            } else {
                JSONArray array = JSON.parseArray(line);
                for (Object o : array) {
                    System.out.println(o);
                }
            }

            socket.shutdownInput();
//            socket.shutdownOutput();
            socket.close();
            long end = System.currentTimeMillis();
            System.out.println(end - start);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
