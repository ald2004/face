package com.face.server.monitor.config;

import lombok.extern.slf4j.Slf4j;
import com.face.server.monitor.service.VisitsService;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化站点统计
 *
 * @author jie
 */

@Slf4j
@Configuration
public class VisitsInitialization {

    public VisitsInitialization(VisitsService visitsService) {
        log.info("--------------- 初始化站点统计，如果存在今日统计则跳过 ---------------");
        visitsService.save();
    }
}
