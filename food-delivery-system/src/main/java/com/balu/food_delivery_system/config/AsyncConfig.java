package com.balu.food_delivery_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {

        //   Step 1: Create ThreadPoolTaskExecutor
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        //   Step 2: Set core pool size (e.g., 5)
        //           minimum threads always alive
        executor.setCorePoolSize(5);

        //   Step 3: Set max pool size (e.g., 10)
        //           maximum threads under heavy load
        executor.setMaxPoolSize(10);

        //   Step 4: Set queue capacity (e.g., 100)
        //           tasks waiting when all threads busy
        executor.setQueueCapacity(100);

        //   Step 5: Set thread name prefix
        //           e.g., "fds-async-"
        //           helps identify threads in logs
        executor.setThreadNamePrefix("fds-async-");

        //   Step 6: Initialize and return executor
        executor.initialize();
        return executor;
    }
}
