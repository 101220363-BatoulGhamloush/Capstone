package com.capstone.OpportuGrow.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // الربط بين الرابط الظاهري والمسار الحقيقي على جهازك
        // تأكدي من وجود 3 سلاش بعد file:
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/og-uploads/");
    }

}