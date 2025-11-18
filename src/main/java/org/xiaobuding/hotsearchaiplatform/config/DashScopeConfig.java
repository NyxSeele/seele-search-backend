package org.xiaobuding.hotsearchaiplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DashScopeConfig {

    @Bean
    public DashScopeProperties dashScopeProperties(
            @Value("${dashscope.api.key}") String apiKey,
            @Value("${dashscope.api.base-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}") String baseUrl,
            @Value("${dashscope.api.model:qwen-max}") String model,
            @Value("${dashscope.api.max-retries:3}") int maxRetries,
            @Value("${dashscope.api.retry-base-delay-ms:1500}") long retryBaseDelayMs,
            @Value("${dashscope.api.request-timeout-ms:20000}") long requestTimeoutMs) {
        return new DashScopeProperties(apiKey, baseUrl, model, maxRetries, retryBaseDelayMs, requestTimeoutMs);
    }

    public record DashScopeProperties(String apiKey,
                                      String baseUrl,
                                      String model,
                                      int maxRetries,
                                      long retryBaseDelayMs,
                                      long requestTimeoutMs) {
    }
}
