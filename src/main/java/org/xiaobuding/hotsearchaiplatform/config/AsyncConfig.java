package org.xiaobuding.hotsearchaiplatform.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "hotSearchExecutor")
    public Executor hotSearchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("hot-search-");
        // 浼樺寲锛氬鍔犳牳蹇冪嚎绋嬫暟浠?鍒?锛屾彁楂樺苟鍙戝鐞嗚兘鍔?
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        // 浼樺寲锛氬鍔犻槦鍒楀閲忎粠20鍒?00锛屽噺灏戜换鍔℃嫆缁?
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

