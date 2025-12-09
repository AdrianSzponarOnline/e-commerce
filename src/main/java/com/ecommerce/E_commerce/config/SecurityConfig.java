package com.ecommerce.E_commerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final Environment environment;

    @Autowired
    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          JsonAuthenticationEntryPoint authenticationEntryPoint,
                          JsonAccessDeniedHandler accessDeniedHandler,
                          Environment environment) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDevOrTest = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("dev") || p.equalsIgnoreCase("test"));

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers(
                                    HttpMethod.POST,
                                    "/api/search",
                                    "/api/ai/chat",
                                    "/api/auth/activate",
                                    "/api/auth/forgot-password",
                                    "/api/auth/reset-password").permitAll()
                            .requestMatchers(
                                    HttpMethod.GET,
                                    "/api/categories",
                                    "/api/categories/**",
                                    "/api/products",
                                    "/api/products/*",
                                    "/api/products/slug/*",
                                    "/api/products/sku/*",
                                    "/api/products/category/*",
                                    "/api/products/category-slug/*",
                                    "/api/products/featured",
                                    "/api/products/active",
                                    "/api/products/stats/category/*/count",
                                    "/api/products/stats/featured/count",
                                    "/api/products/stats/active/count",
                                    "/api/products/*/images",
                                    "/api/product-attribute-values",
                                    "/api/product-attribute-values/{id}",
                                    "/api/product-attribute-values/product/{productId}",
                                    "/api/product-attribute-values/attribute/{attributeId}",
                                    "/api/product-attribute-values/product/{productId}/attribute/{attributeId}",
                                    "/api/product-attribute-values/product/{productId}/paginated",
                                    "/api/product-attribute-values/attribute/{attributeId}/paginated",
                                    "/api/product-attribute-values/category/{categoryId}",
                                    "/api/product-attribute-values/search/value",
                                    "/api/product-attribute-values/attribute-type/{attributeType}",
                                    "/api/product-attribute-values/product/{productId}/key-attributes",
                                    "/api/product-attribute-values/search/advanced",
                                    "/api/product-attribute-values/stats/product/{productId}",
                                    "/api/product-attribute-values/stats/attribute/{attributeId}",
                                    "/api/product-attribute-values/stats/category/{categoryId}",
                                    "/api/product-attribute-values/distinct-values/attribute/{attributeId}",
                                    "/api/product-attribute-values/product/{productId}/key-attributes/list",
                                    "/api/product-attribute-values/product/{productId}/attribute-type/{attributeType}",
                                    "/api/inventory",
                                    "/api/inventory/{id}",
                                    "/api/inventory/product/{productId}",
                                    "/api/inventory/summary",
                                    "/api/inventory/product/{productId}/available"
                            ).permitAll()
                            .requestMatchers(
                                    "/error",
                                    "/api/auth/login",
                                    "/api/auth/register",
                                    "/uploads/**"
                            ).permitAll();
                    if (isDevOrTest) {
                        authorize.requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/webjars/**"
                        ).permitAll();
                    }
                    authorize
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}