package com.carbon.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleAuthFilterConfig {
    @Bean
    public FilterRegistrationBean<SimpleAuthFilter> simpleAuthFilter(AppAuthProperties appAuthProperties) {
        FilterRegistrationBean<SimpleAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SimpleAuthFilter(appAuthProperties));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
