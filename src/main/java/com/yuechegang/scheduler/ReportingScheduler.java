package com.yuechegang.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yuechegang.service.ReportingService;

@Service
public class ReportingScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingScheduler.class);

    @Autowired
    private ReportingService reportingService;

    @Scheduled(cron = "${spring.scheduling.cronExp.process}")
    public synchronized void processFiles() {
        LOG.debug("Executing job groupFiles() start...");
        reportingService.groupFiles();
        LOG.debug("Executing job groupFiles() end!");
        LOG.debug("Executing job validateFiles() start...");
        reportingService.validateFiles();
        LOG.debug("Executing job validateFiles() end!");
        LOG.debug("Executing job compressFiles() start...");
        reportingService.compressFiles();
        LOG.debug("Executing job compressFiles() end!");
    }

    @Scheduled(cron = "${spring.scheduling.cronExp.upload}")
    public synchronized void uploadFiles() {
        LOG.debug("Executing job uploadFiles() start...");
        reportingService.uploadFiles();
        LOG.debug("Executing job uploadFiles() end!");
    }

    @Scheduled(cron = "${spring.scheduling.cronExp.archive}")
    public synchronized void archiveFiles() {
        LOG.debug("Executing job archiveFiles() start...");
        reportingService.archiveFiles();
        LOG.debug("Executing job archiveFiles() end!");
    }
}
