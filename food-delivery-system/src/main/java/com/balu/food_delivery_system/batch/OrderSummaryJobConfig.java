package com.balu.food_delivery_system.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OrderSummaryJobConfig {

    @Bean
    public Step orderSummaryStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 JdbcCursorItemReader<OrderSummaryDTO> reader,
                                 OrderSummaryItemProcessor itemProcessor,
                                 OrderSummaryItemWriter writer) {

        // WHAT to do:
        //   Build a Step using StepBuilder
        //   chunk size = 10 (process 10 items at a time before writing)
        //   .reader(reader)
        //   .processor(processor)
        //   .writer(writer)
        //   .build()
        return new StepBuilder("orderSummaryStep", jobRepository)
                .<OrderSummaryDTO, OrderSummaryDTO>chunk(10, transactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job orderSummaryJob(JobRepository jobRepository, Step orderSummaryStep) {

        // WHAT to do:
        //   Build a Job using JobBuilder
        //   .start(orderSummaryStep)
        //   .build()
        return new JobBuilder("orderSummaryJob", jobRepository)
                .start(orderSummaryStep)
                .build();
    }
}
