package com.works.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Angular'ın varsayılan portuna izin veriyoruz
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Tüm HTTP metotlarına izin veriyoruz (GET, POST, PUT, DELETE vb.)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Tüm başlıklara izin veriyoruz
        config.setAllowedHeaders(List.of("*"));

        // Session (Cookie) taşımasına izin veriyoruz! (Bu çok kritik, yoksa login çalışmaz)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Bu kuralları tüm API uç noktalarımız (/**) için geçerli kılıyoruz
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}