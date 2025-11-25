package org.xiaobuding.hotsearchaiplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xiaobuding.hotsearchaiplatform.interceptor.RateLimitInterceptor;
import org.xiaobuding.hotsearchaiplatform.interceptor.OperationLoggingInterceptor;

/**
 * API 限流与操作日志相关的拦截器配置
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 先记录操作日志，完整保留访问链路信息
        registry.addInterceptor(new OperationLoggingInterceptor())
                .addPathPatterns("/api/**")
                .order(1);

        // 再进行限流控制，保障 AI 接口安全
        registry.addInterceptor(new RateLimitInterceptor())
                .addPathPatterns("/api/ai/**")
                .excludePathPatterns("/api/ai/health")
                .order(2);
    }
}
