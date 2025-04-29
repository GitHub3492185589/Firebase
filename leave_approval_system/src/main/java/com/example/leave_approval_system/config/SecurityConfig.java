package com.example.leave_approval_system.config;


import com.example.leave_approval_system.security.JwtAccessDeniedHandler;
import com.example.leave_approval_system.security.JwtAuthenticationEntryPoint;
import com.example.leave_approval_system.security.JwtRequestFilter;
import com.example.leave_approval_system.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // 引入 Value
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 启用方法级安全注解
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // 用于禁用 CSRF 的 lambda 配置
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration // 标记为配置类
@EnableWebSecurity // 启用 Spring Security 的 Web 安全支持
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true) // 启用方法级安全注解 (可选)
@RequiredArgsConstructor // Lombok: 自动生成构造函数注入 final 字段
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService; // 注入自定义 UserDetailsService
    private final JwtRequestFilter jwtRequestFilter;         // 注入 JWT 过滤器
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // 注入认证失败处理器
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler; // 注入访问拒绝处理器

    // 从配置文件读取允许的跨域来源，如果需要的话
    // @Value("${app.cors.allowed-origins}")
    // private String[] allowedOrigins;

    /**
     * 配置密码编码器 Bean
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 使用 BCrypt 算法进行密码哈希
    }

    /**
     * 配置认证提供者 Bean (DaoAuthenticationProvider)
     * @return AuthenticationProvider 实例
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // 设置 UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());     // 设置密码编码器
        return authProvider;
    }

    /**
     * 配置认证管理器 Bean
     * @param authConfig 认证配置对象
     * @return AuthenticationManager 实例
     * @throws Exception 如果获取 AuthenticationManager 失败
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 配置 CORS (跨域资源共享) Bean
     * @return CorsConfigurationSource 实例
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 设置允许的来源 (例如 Vue 开发服务器地址，生产环境应替换为实际域名)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173")); // 允许多个源
        // 设置允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        // 设置允许的请求头 (允许所有常用头)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        // 是否允许发送 Cookie 等凭证信息
        configuration.setAllowCredentials(true);
        // 设置预检请求 (OPTIONS) 的缓存时间 (秒)
        configuration.setMaxAge(3600L); // 1 小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 /api/** 路径应用此 CORS 配置
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * 配置安全过滤器链 Bean (核心配置)
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 实例
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 禁用 CSRF (Cross-Site Request Forgery) 保护，因为我们使用 JWT，不需要 Session
                .csrf(AbstractHttpConfigurer::disable) // 使用 lambda 表达式简化配置

                // 3. 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问登录和注册接口
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // 允许匿名访问 OPTIONS 预检请求 (对于所有路径)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                       
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 4. 配置 Session 管理策略为 STATELESS (无状态)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. 设置自定义的 AuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // 6. 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                
                // 7. 设置自定义的认证失败处理器和访问拒绝处理器
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        return http.build(); // 构建并返回 SecurityFilterChain
    }
}