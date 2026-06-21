package com.balu.food_delivery_system.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSummaryJobLauncher {

    // fields: JobLauncher, Job (orderSummaryJob bean)
    private final JobLauncher jobLauncher;
    private final Job job;

    public void runJob() throws Exception {

        //   Step 1: Build JobParameters
        //           must include a unique parameter (e.g. timestamp)
        //           otherwise Spring Batch won't rerun a job with identical parameters
        //   Step 2: jobLauncher.run(job, jobParameters)
        //   Step 3: log.info "[BATCH] Job triggered"

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
        log.info("[BATCH] Job triggered");
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void scheduledRun() {
        try {
            runJob();
        } catch (Exception e) {
            log.error("[BATCH] Scheduled job failed", e);
        }
    }
}
