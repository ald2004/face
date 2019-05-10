package com.face.server.quartz.task;

import com.face.server.monitor.service.VisitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author jie
 * @date 2018-12-25
 */
@Component
public class VisitsTask {

    @Autowired
    private VisitsService visitsService;

    public void run() {
        visitsService.save();
    }
}
