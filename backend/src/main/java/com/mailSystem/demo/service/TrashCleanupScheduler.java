package com.mailSystem.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrashCleanupScheduler {

    @Autowired
    private TrashService trashService;

    /**
     * Executes the cleanup job once every 24 hours (86,400,000 milliseconds).
     * This is simpler and less error-prone than cron for daily jobs.
     */
    @Scheduled(fixedDelay = 30000)
    public void runTrashCleanup() {
        // Cron alternative: @Scheduled(cron = "0 0 3 * * *") // Runs daily at 3:00 AM

        trashService.cleanupExpiredTrash();
    }
}