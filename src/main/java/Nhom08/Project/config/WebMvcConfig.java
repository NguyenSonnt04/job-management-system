package Nhom08.Project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded CV files (PDF/DOC) via /cv-files/**
        String uploadPath = Paths.get("uploads", "cv").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/cv-files/**")
                .addResourceLocations(uploadPath);
    }
}
