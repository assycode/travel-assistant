package com.example.demo.config;

import com.example.demo.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 仅负责注册拦截器，跨域配置移到 SecurityConfig 中
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径（路径统一加 / 前缀）
                .excludePathPatterns(
                        "/user/login",
                        "/user/register"
                );
    }
}