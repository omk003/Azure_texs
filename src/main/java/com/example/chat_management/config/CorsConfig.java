package com.example.chat_management.config;
// package com.example.chat_managenent.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// import java.util.Arrays;

// @Configuration
// public class CorsConfig implements WebMvcConfigurer {

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration configuration = new CorsConfiguration();
        
//         // Allow all origins (for development); restrict this in production
//         configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        
//         // Allow common HTTP methods
//         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
//         // Allow specific headers
//         configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        
//         // Allow credentials (e.g., cookies, authorization headers)
//         configuration.setAllowCredentials(true);
        
//         // Configure the mapping for which paths should apply the CORS configuration
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
        
//         return source;
//     }

//     @Bean
//     public CorsFilter corsFilter() {
//         return new CorsFilter(corsConfigurationSource());
//     }
// }
