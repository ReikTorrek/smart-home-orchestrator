package ru.reik.smarthome.orchestrator.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.reik.smarthome.orchestrator.config.assistant.AssistantApiProperties;
import ru.reik.smarthome.orchestrator.security.AssistantApiTokenFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public AssistantApiTokenFilter assistantApiTokenFilter(AssistantApiProperties properties) {
        return new AssistantApiTokenFilter(properties);
    }

    @Bean
    public FilterRegistrationBean<AssistantApiTokenFilter> assistantApiTokenFilterRegistration(AssistantApiTokenFilter filter) {
        FilterRegistrationBean<AssistantApiTokenFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AssistantApiTokenFilter assistantApiTokenFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        assistantApiTokenFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}
