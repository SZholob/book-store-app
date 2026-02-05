package com.epam.rd.autocode.spring.project.conf;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/login", "/registration", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()


                        .requestMatchers(HttpMethod.GET, "/books/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/clients").permitAll()


                        .requestMatchers("/orders/manage/**").hasRole("EMPLOYEE")
                        .requestMatchers("/books/manage/**", "/books/add/**", "/books/edit/**", "/books/delete/**").hasRole("EMPLOYEE")
                        .requestMatchers("/clients/**").hasRole("EMPLOYEE")
                        .requestMatchers("/employees/**").hasRole("EMPLOYEE")

                        .requestMatchers("/orders/my/**", "/cart/**").hasRole("CUSTOMER")

                        .requestMatchers("/profile/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/books", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                ).sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                );

        return http.build();
    }
}
