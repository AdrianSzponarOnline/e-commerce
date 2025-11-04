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
@org.springframework.context.annotation.Profile("!test")
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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/error").permitAll()
                            // Auth endpoints - public
                            .requestMatchers(
                                    "/api/auth/login",
                                    "/api/auth/register"
                            ).permitAll()
                            // Categories - public GET endpoints
                            .requestMatchers(
                                    "/api/categories/public",
                                    "/api/categories/public/**",
                                    "/api/categories/public/active",
                                    "/api/categories",
                                    "/api/categories/{id}",
                                    "/api/categories/active",
                                    "/api/categories/parent/{parentId}",
                                    "/api/categories/slug/{slug}",
                                    "/api/categories/*/attributes",
                                    "/api/categories/*/attributes/**"
                            ).permitAll()
                            // Products - public GET endpoints
                            .requestMatchers(
                                    "/api/products",
                                    "/api/products/*",
                                    "/api/products/slug/*",
                                    "/api/products/sku/*",
                                    "/api/products/category/*",
                                    "/api/products/category-slug/*",
                                    "/api/products/price-range",
                                    "/api/products/featured",
                                    "/api/products/active",
                                    "/api/products/search/name",
                                    "/api/products/search/description",
                                    "/api/products/category/*/price-range",
                                    "/api/products/category/*/featured",
                                    "/api/products/price-range/featured",
                                    "/api/products/stats/category/*/count",
                                    "/api/products/stats/featured/count",
                                    "/api/products/stats/active/count"
                            ).permitAll()
                            // Product Images - public GET endpoints
                            .requestMatchers(
                                    HttpMethod.GET,
                                    "/api/products/*/images"
                            ).permitAll()
                            // Product Attribute Values - public GET endpoints
                            .requestMatchers(
                                    "/api/product-attribute-values",
                                    "/api/product-attribute-values/{id}",
                                    "/api/product-attribute-values/product/{productId}",
                                    "/api/product-attribute-values/category-attribute/{categoryAttributeId}",
                                    "/api/product-attribute-values/product/{productId}/category-attribute/{categoryAttributeId}",
                                    "/api/product-attribute-values/product/{productId}/paginated",
                                    "/api/product-attribute-values/category-attribute/{categoryAttributeId}/paginated",
                                    "/api/product-attribute-values/category/{categoryId}",
                                    "/api/product-attribute-values/search/value",
                                    "/api/product-attribute-values/attribute-type/{attributeType}",
                                    "/api/product-attribute-values/product/{productId}/key-attributes",
                                    "/api/product-attribute-values/search/advanced",
                                    "/api/product-attribute-values/stats/product/{productId}",
                                    "/api/product-attribute-values/stats/category-attribute/{categoryAttributeId}",
                                    "/api/product-attribute-values/stats/category/{categoryId}",
                                    "/api/product-attribute-values/distinct-values/category-attribute/{categoryAttributeId}",
                                    "/api/product-attribute-values/product/{productId}/key-attributes/list",
                                    "/api/product-attribute-values/product/{productId}/attribute-type/{attributeType}"
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
                    // All other endpoints require authentication
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
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}