package com.example.job_manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. URLごとのアクセス許可設定
                .authorizeHttpRequests(auth -> auth
                        // loginページ自体も誰でもアクセスできるようにする
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/signup", "/manifest.json", "/login").permitAll()
                        .anyRequest().authenticated() // それ以外はログイン必須
                )
                // 2. ログインフォームの設定
                .formLogin(login -> login
                        .loginPage("/login") // 自作のログイン画面を使う設定
                        .permitAll() // ログイン画面には誰でもアクセスOK
                        .defaultSuccessUrl("/", true) // ログイン成功後はトップページへ
                )
                // 3. ログアウトの設定
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}