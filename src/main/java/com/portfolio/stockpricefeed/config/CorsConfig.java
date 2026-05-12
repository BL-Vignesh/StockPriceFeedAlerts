package com.portfolio.stockpricefeed.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Allows cross-origin requests from:
     * - https://real-time-portfolio-alerts.vercel.app (the deployed UI)
     * - http://localhost:3000 (local dev)
     *
     * This is required because:
     * - The UI is hosted on Vercel (different origin from your Spring Boot backend)
     * - Without this, the browser blocks all requests with a CORS error
     *
     * If you deploy your backend to a server (not localhost), you do NOT need
     * to change this file — the Vercel domain is already allowed.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://stock-price-feed-app.vercel.app",
                        "http://localhost:3000",
                        "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
