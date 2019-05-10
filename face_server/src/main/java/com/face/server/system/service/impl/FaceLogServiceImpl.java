package com.face.server.system.service.impl;

import com.face.server.common.utils.StringUtils;
import com.face.server.common.utils.ValidationUtil;
import com.face.server.system.domain.FaceLog;
import com.face.server.system.repository.FaceLogRepository;
import com.face.server.system.service.FaceLogService;
import com.face.server.system.service.dto.FaceLogCountDTO;
import com.face.server.system.service.dto.FaceLogCountTopNDTO;
import com.face.server.system.service.dto.FaceLogDTO;
import com.face.server.system.service.mapper.FaceLogMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FaceLogServiceImpl implements FaceLogService {


    @Autowired
    private FaceLogMapper faceLogMapper;
    @Autowired
    private FaceLogRepository faceLogRepo;
    @Value("${face.job.base-path}")
    private String basePath;

    @Override
    public FaceLogDTO findById(long id) {
        Optional<FaceLog> faceLog = faceLogRepo.findById(id);
        ValidationUtil.isNull(faceLog, "FaceLog", "id", id);
        return faceLogMapper.toDto(faceLog.get());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaceLogDTO create(FaceLog resources) {
        return faceLogMapper.toDto(faceLogRepo.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FaceLogDTO> createAll(List<FaceLog> resources) {
        return faceLogMapper.toDto(faceLogRepo.saveAll(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        faceLogRepo.deleteById(id);
    }

    /**
     * 0 禁用 1 黑名单 2 白名单 3 陌生人
     *
     * @return
     */
    @Override
    public FaceLogCountDTO count() {
        FaceLogCountDTO faceLogCountDTO = new FaceLogCountDTO();

        faceLogCountDTO.setProhibitCount(faceLogRepo.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 0))));
        faceLogCountDTO.setBlackCount(faceLogRepo.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 1))));
        faceLogCountDTO.setWhiteCount(faceLogRepo.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 2))));
        faceLogCountDTO.setStrangerCount(faceLogRepo.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 3))));
        faceLogCountDTO.setCount(faceLogRepo.count());

        return faceLogCountDTO;
    }

    @Override
    public List<FaceLogCountTopNDTO> countTopN(int n) {
        List<Map<String, Object>> maps = faceLogRepo.countTopN(n);
        List<FaceLogCountTopNDTO> faceLogCountTopNDTOS = new ArrayList<>();

        for (Map<String, Object> map : maps) {

            FaceLogCountTopNDTO faceLogCountTopNDTO = new FaceLogCountTopNDTO();
            faceLogCountTopNDTO.setCameraId((Long) map.getOrDefault("cameraId", 0L));
            faceLogCountTopNDTO.setCount((Long) map.getOrDefault("count", 0L));
            faceLogCountTopNDTO.setFaceUserStatus((Integer) map.getOrDefault("faceUserStatus", 0));
            faceLogCountTopNDTO.setIp(String.valueOf(map.getOrDefault("ip", "")));
            faceLogCountTopNDTO.setRegion(String.valueOf(map.getOrDefault("region", "")));
            faceLogCountTopNDTOS.add(faceLogCountTopNDTO);
        }

        return faceLogCountTopNDTOS;
    }

    @Override
    public void delete(List<Long> idArrays) {
        List<FaceLog> faceLogs = faceLogRepo.findAllById(idArrays);

        for (FaceLog faceLog : faceLogs) {
            @NotNull String logImg = faceLog.getLogImg();
            @NotNull Long cameraId = faceLog.getCameraId();
            int x = faceLog.getX();
            int y = faceLog.getY();
            int w = faceLog.getWidth();
            int h = faceLog.getHeight();
            try {
                FileUtils.forceDelete(new File(basePath + File.separator + cameraId + File.separator + logImg + ".sub" + "." + x + "." + y + "." + w + "." + h));
                FileUtils.forceDelete(new File(basePath + File.separator + cameraId + File.separator + logImg));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        faceLogRepo.deleteAll(faceLogs);
    }

    @Override
    public Long countNew(Long cameraId) {
        Date date = new Date(System.currentTimeMillis() - 5 * 1000 * 60);
        return cameraId == null ? faceLogRepo.countNew(date) : faceLogRepo.countNew(cameraId, date);
    }
}
