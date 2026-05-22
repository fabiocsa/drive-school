package com.driveschool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @org.springframework.beans.factory.annotation.Value("${file.upload.path}")
    private String uploadPath;

    @org.springframework.beans.factory.annotation.Value("${file.upload.allowed-extensions}")
    private String allowedExtensions;

    @org.springframework.beans.factory.annotation.Value("${pdf.output.path}")
    private String pdfOutputPath;

    public static String UPLOAD_PATH;
    public static String PDF_OUTPUT_PATH;
    public static List<String> ALLOWED_EXTENSIONS;

    @PostConstruct
    public void init() {
        UPLOAD_PATH = uploadPath;
        PDF_OUTPUT_PATH = pdfOutputPath;
        ALLOWED_EXTENSIONS = Arrays.asList(allowedExtensions.split(","));
        new File(uploadPath).mkdirs();
        new File(pdfOutputPath).mkdirs();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    public static boolean isAllowedExtension(String filename) {
        if (filename == null) return false;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) return false;
        String ext = filename.substring(dotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }
}
