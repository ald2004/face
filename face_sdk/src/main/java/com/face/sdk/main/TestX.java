package com.face.sdk.main;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * project : face-web-server
 * Code Create : 2019/4/27
 * Class : com.face.sdk.main.TestX
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class TestX {
    public TestX() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        Process exec = Runtime.getRuntime().exec("cmd /c java -jar \"C:\\Users\\wangxiaoming\\.m2\\repository\\com\\face\\face-sdk\\1.0-SNAPSHOT\\face-sdk-1.0-SNAPSHOT.jar\" \"http://localhost/api/no-user/users\" \"http://localhost/api/no-user/camera/4\" \"http://localhost/api/no-user/face/logs/all\"");

        String jarPath = "C:\\Users\\wangxiaoming\\.m2\\repository\\com\\face\\face-sdk\\1.0-SNAPSHOT\\face-sdk-1.0-SNAPSHOT.jar";
        String baseURL = "http://localhost";
        String command = String.format("cmd /c java -jar \"%s\" \"%s/%s\" \"%s/%s/%d\" \"%s/%s\"", jarPath, baseURL, "api/no-user/users", baseURL, "api/no-user/camera", 4, baseURL, "api/no-user/face/logs/all");
        System.out.println("cmd /c java -jar \"C:\\Users\\wangxiaoming\\.m2\\repository\\com\\face\\face-sdk\\1.0-SNAPSHOT\\face-sdk-1.0-SNAPSHOT.jar\" \"http://localhost/api/no-user/users\" \"http://localhost/api/no-user/camera/4\" \"http://localhost/api/no-user/face/logs/all\"");
        System.out.println(command);
        Process exec = Runtime.getRuntime().exec(command, null, new File("D:\\projects\\java\\face-server\\eladmin\\imgs\\face\\job\\4"));

//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getErrorStream(),"GBK"));
//        String line;
//        while ((line = bufferedReader.readLine()) != null && exec.isAlive()) {
//            System.out.println(line);
//        }

        while (exec.isAlive()) {

        }
        System.out.println(exec.isAlive());

    }
}
