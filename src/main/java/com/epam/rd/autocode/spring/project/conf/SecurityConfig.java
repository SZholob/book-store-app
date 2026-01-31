package com.epam.rd.autocode.spring.project.conf;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig{


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Вимикаємо CSRF для простоти тестів
                .authorizeHttpRequests(auth -> auth
                        // -- ПУБЛІЧНИЙ ДОСТУП --
                        .requestMatchers("/", "/login", "/registration", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Дозволяємо перегляд книг усім (навіть незалогіненим)
                        .requestMatchers(HttpMethod.GET, "/books/**").permitAll()

                        // Дозволяємо реєстрацію через API (якщо форма використовує цей ендпоінт)
                        .requestMatchers(HttpMethod.POST, "/clients").permitAll()

                        // -- ЗАХИЩЕНІ СЕКЦІЇ --
                        // Співробітники
                        .requestMatchers("/employees/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/books/**").hasRole("EMPLOYEE")

                        // Клієнти
                        .requestMatchers("/orders/**").hasRole("CUSTOMER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Ваша кастомна сторінка входу (HomeController)
                        .loginProcessingUrl("/login") // Куди форма має слати POST запит (Spring обробить сам)
                        .defaultSuccessUrl("/books", true) // Куди кинути після успішного входу
                        .failureUrl("/login?error") // Куди, якщо пароль неправильний
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
