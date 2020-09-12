package com.github.io3x.app.sboot.Config;

import com.github.io3x.app.sboot.Interceptor.SecurityInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebSecurityConfig extends WebMvcConfigurerAdapter {


    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration addInterceptor = registry.addInterceptor(getSecurityInterceptor());
        // 排除配置--对下面的不进行拦截
        addInterceptor.excludePathPatterns("/index");
        addInterceptor.excludePathPatterns("/login");
        addInterceptor.excludePathPatterns("/start/**");
        // 拦截配置
        addInterceptor.addPathPatterns("/scheduled/**");
    }
}