package com.community.community.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Path.of(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}