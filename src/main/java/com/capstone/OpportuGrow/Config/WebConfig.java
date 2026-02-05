package com.capstone.OpportuGrow.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                // المسار الأول: المجلد الجديد (C:/og-uploads/)
                .addResourceLocations("file:///C:/og-uploads/")
                // المسار الثاني: المجلد القديم اللي بعتيه
                .addResourceLocations("file:///C:/uploads/opportugrow/");

    }
}

