package com.face.server.quartz.task;

import com.face.server.system.service.CameraService;
import com.face.server.system.service.FaceLogService;
import com.face.server.system.service.dto.CameraDTO;
import com.face.server.system.service.dto.FaceUserDTO;
import com.face.server.system.service.query.CameraQueryService;
import com.face.server.system.service.query.FaceUserQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * project : face-web-server
 * Code Create : 2019/4/27
 * Class : com.face.server.quartz.task.FaceJobTask
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
@Slf4j
@Component
public class FaceJobTask {
    @Autowired
    private FaceUserQueryService faceUserQueryService;

    @Autowired
    private CameraQueryService cameraQueryService;

    @Autowired
    private CameraService cameraService;

    @Autowired
    private FaceLogService faceLogService;
    @Value("${face.job.base-path}")
    private String basePath;

    @Value("${os.name}")
    private String OS_NAME;

    @Value("${face.job.jar.filepath}")
    private String jarPath;
    private String ip;

    @Value("${server.port}")
    private int port;
    private String baseURL;
    private List<FaceUserDTO> userList;

    /**
     * 判断是linux系统还是其他系统
     * 如果是Linux系统，返回true，否则返回false
     */
    public boolean isLinux() {
        return OS_NAME.toLowerCase().contains("linux");
    }

    /**
     * 判断是Windows系统还是其他系统
     * 如果是Windows系统，返回true，否则返回false
     */
    public boolean isWindows() {
        return OS_NAME.toLowerCase().contains("win");
    }

    class NullOutStream extends Thread {
        BufferedReader input;

        NullOutStream(InputStream is) {
            input = new BufferedReader(new InputStreamReader(is));
            start();
        }

        public void run() {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    log.info(line);
                }
            } catch (Exception ignored) {
            }
        }

    }

    private final class FaceJob {
        CameraDTO cameraDTO;
        boolean run;
        Process process;

        void syncStatus() {
            run = process != null && process.isAlive();
        }

        void start() {
            syncStatus();
            // 禁用状态
            if (0 == cameraDTO.getStatus()) {
                stop();
                return;
            }
            if (run) {
                return;
            }
            try {
                File dir = new File(new File(basePath + File.separator + cameraDTO.getId()).getAbsolutePath());
                FileUtils.forceMkdir(dir);
                if (isWindows()) {
                    String command = String.format("cmd /c java -jar \"%s\" \"%s/%s\" \"%s/%s/%d\" \"%s/%s\"", jarPath, baseURL, "api/no-user/users", baseURL, "api/no-user/camera", cameraDTO.getId(), baseURL, "api/no-user/face/logs/all");
                    process = Runtime.getRuntime().exec(command, null, dir);
                    log.info("start job command[" + cameraDTO.getId() + "] : " + command);
                } else {
                    String command = String.format("nice -n 19 java -jar %s \"%s/%s\" \"%s/%s/%d\" \"%s/%s\"", jarPath, baseURL, "api/no-user/users", baseURL, "api/no-user/camera", cameraDTO.getId(), baseURL, "api/no-user/face/logs/all");
                    process = Runtime.getRuntime().exec(command, null, dir);
                    log.info("start job command[" + cameraDTO.getId() + "] : " + command);
                }

                new NullOutStream(process.getInputStream());
                new NullOutStream(process.getErrorStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        void stop() {
            if (!run) return;
            process.destroy();
            try {
                Collection<File> files = FileUtils.listFiles(new File(basePath + File.separator + cameraDTO.getId()), new String[]{"pid"}, false);
                for (File file : files) {
                    if (isWindows()) {
                        Runtime.getRuntime().exec("cmd /c taskkill /F /PID " + file.getName().split("\\.")[0]);
                    } else {
                        Runtime.getRuntime().exec("kill -9 " + file.getName().split("\\.")[0]);
                    }
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("stop process:[" + cameraDTO.getId() + "]");
            run = false;
        }

        FaceJob(CameraDTO cameraDTO, boolean run) {
            this.cameraDTO = cameraDTO;
            this.run = run;
        }

        FaceJob(CameraDTO cameraDTO) {
            this.cameraDTO = cameraDTO;
        }
    }

    private Map<Long, FaceJob> faceJobMap = new HashMap<>();

    public FaceJobTask() {
    }

    @PostConstruct
    public void init() throws UnknownHostException {
        log.info("FaceJobTask inited.");
        ip = InetAddress.getLocalHost().getHostAddress();
        baseURL = String.format("http://%s:%d", ip, port);
        userList = (List<FaceUserDTO>) faceUserQueryService.queryAll(new FaceUserDTO());
    }

    public void run() {
//        log.info("执行成功");
        List<FaceUserDTO> userList = (List<FaceUserDTO>) faceUserQueryService.queryAll(new FaceUserDTO());
        List<CameraDTO> cameraDTOList = (List<CameraDTO>) cameraQueryService.queryAll(new CameraDTO());

        boolean userChange = userList.size() != this.userList.size();

        if (!userChange) {
            outer:
            for (FaceUserDTO faceUserDTO : userList) {
                for (FaceUserDTO userDTO : this.userList) {
                    if (userDTO.getId().equals(faceUserDTO.getId())) {
                        if (!userDTO.equals(faceUserDTO)) {
                            userChange = true;
                            break outer;
                        }
                        break;
                    }
                }
            }
        }
        this.userList = userList;


        // 对比现有摄像头是否有变化
        for (FaceJob faceJob : faceJobMap.values()) {
            boolean in = false;
            for (CameraDTO cameraDTO : cameraDTOList) {
                if (cameraDTO.getId().equals(faceJob.cameraDTO.getId())) {
                    in = true;
                    // 如果相机发生变化 重启进程
                    if (!cameraDTO.equals(faceJob.cameraDTO)) {
                        faceJob.stop();
                        faceJob.start();
                    }
                    break;
                }
            }
            // 不在相机列表，说明此相机已被删除
            if (!in) {
                faceJob.stop();
            }

            // 如果人脸库有变化，重启所有进程
            if (userChange) {
                faceJob.stop();
                faceJob.start();
            }

        }

        for (CameraDTO cameraDTO : cameraDTOList) {
            FaceJob faceJob = faceJobMap.getOrDefault(cameraDTO.getId(), new FaceJob(cameraDTO));
            faceJob.cameraDTO = cameraDTO;
            faceJob.start();
            faceJobMap.put(cameraDTO.getId(), faceJob);
        }

    }
}
