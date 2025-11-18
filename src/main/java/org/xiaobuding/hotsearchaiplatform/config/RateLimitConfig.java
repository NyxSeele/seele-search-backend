package org.xiaobuding.hotsearchaiplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xiaobuding.hotsearchaiplatform.interceptor.RateLimitInterceptor;
import org.xiaobuding.hotsearchaiplatform.interceptor.OperationLoggingInterceptor;

/**
 * API 闄愭祦鍜屾棩蹇楅厤缃?
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 鎿嶄綔鏃ュ織鎷︽埅鍣紙浼樺厛绾ф渶楂橈級
        registry.addInterceptor(new OperationLoggingInterceptor())
                .addPathPatterns("/api/**")
                .order(1);

        // 闄愭祦鎷︽埅鍣?
        registry.addInterceptor(new RateLimitInterceptor())
                .addPathPatterns("/api/ai/**")
                .excludePathPatterns("/api/ai/health")
                .order(2);
    }
}
