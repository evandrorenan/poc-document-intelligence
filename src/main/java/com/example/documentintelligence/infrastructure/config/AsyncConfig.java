package com.example.documentintelligence.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "documentAnalysisExecutor")
    public Executor documentAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // Number of threads to keep alive at all times
        executor.setMaxPoolSize(4);         // Maximum number of threads
        executor.setQueueCapacity(100);     // Queue capacity for tasks when all threads are busy
        executor.setThreadNamePrefix("doc-analysis-");
        executor.initialize();
        return executor;
    }
}
