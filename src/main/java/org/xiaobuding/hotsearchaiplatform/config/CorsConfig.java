package org.xiaobuding.hotsearchaiplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的OSS域名（请根据实际OSS域名修改）
        config.addAllowedOrigin("https://seele0420.cloud.oss-cn-hongkong.aliyuncs.com");
        // 允许OSS静态网站域名（不带后缀的）
        config.addAllowedOrigin("https://seele0420.cloud");
        config.addAllowedOrigin("http://seele0420.cloud");
        // 也允许本地开发
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        // 允许所有来源（使用Pattern，因为不能同时使用通配符和credentials）
        config.addAllowedOriginPattern("*");
        
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 注意：使用 addAllowedOriginPattern("*") 时，不能同时使用 setAllowCredentials(true)
        // 如果需要 credentials，必须明确指定每个 origin，不能使用通配符
        // config.setAllowCredentials(true); // 暂时禁用，因为使用了通配符
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

