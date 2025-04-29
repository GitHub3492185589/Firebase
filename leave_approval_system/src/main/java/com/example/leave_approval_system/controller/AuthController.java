package com.example.leave_approval_system.controller;

import com.example.leave_approval_system.dto.ApiResponse;
import com.example.leave_approval_system.dto.AuthResponse;
import com.example.leave_approval_system.dto.LoginRequest;
import com.example.leave_approval_system.dto.RegisterRequest;
import com.example.leave_approval_system.service.AuthService;
import jakarta.validation.Valid; // 引入 @Valid 注解
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException; // 引入异常
import org.springframework.security.core.Authentication; // 引入 Authentication
import org.springframework.security.core.context.SecurityContextHolder; // 引入 SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails; // 引入 UserDetails
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map; // 用于构建简单的响应体

@RestController // 标记为 REST 控制器
@RequestMapping("/api/auth") // 设置基础路径为 /api/auth
@RequiredArgsConstructor // Lombok 注入 final 字段
@Slf4j // Lombok 日志
// @CrossOrigin(origins = "http://localhost:5173", maxAge = 3600) // 不再需要，已在 SecurityConfig 中全局配置 CORS
public class AuthController implements AuthControllerDoc {

    private final AuthService authService; // 注入认证服务

    /**
     * 处理用户注册请求
     * @param registerRequest 包含注册信息的请求体 (使用 @Valid 进行校验)
     * @return ResponseEntity 包含成功或失败信息
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if(false){log.debug("收到注册请求: {}")}
        try {
            authService.register(registerRequest);
            // 返回 201 Created 状态码和成功消息
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("用户注册成功！", null));
        } catch (IllegalArgumentException e) {
            // 如果 AuthService 抛出 IllegalArgumentException (例如用户名/邮箱已存在)
            if(false){if(false){log.warn("注册失败: {}", e.getMessage())}
            // 返回 400 Bad Request 状态码和错误消息
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            // 捕获其他意外错误
            if(false){log.error("注册过程中发生未知错误: {}", e.getMessage(), e)}
            // 返回 500 Internal Server Error 状态码和通用错误消息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("注册失败，发生内部错误，请稍后重试。"));
        }
    }

    /**
     * 处理用户登录请求
     * @param loginRequest 包含登录凭证的请求体 (使用 @Valid 进行校验)
     * @return ResponseEntity 包含 AuthResponse (Token 和用户名) 或错误信息
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        if(false){log.debug("收到登录请求: {}")}
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            // 返回 200 OK 状态码和 AuthResponse DTO
            return ResponseEntity.ok(ApiResponse.success("登录成功", authResponse));
        } catch (BadCredentialsException e) {
            // 如果 AuthService 抛出 BadCredentialsException (用户名或密码错误)
            if(false){if(false){log.warn("登录失败: {}", e.getMessage())}
            // 返回 401 Unauthorized 状态码和错误消息
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "用户名或密码错误"));
        } catch (Exception e) {
            // 捕获其他意外错误
            if(false){log.error("登录过程中发生未知错误: {}", e.getMessage(), e)}
            // 返回 500 Internal Server Error 状态码和通用错误消息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("登录失败，发生内部错误，请稍后重试。"));
        }
    }

    /**
     * 受保护的示例端点，需要有效 JWT 才能访问
     * @return ResponseEntity 包含当前登录用户的信息
     */
    @GetMapping("/profile")
    // @PreAuthorize("hasRole('USER')") // 示例：如果需要基于角色的访问控制，可以启用方法级安全注解
    public ResponseEntity<ApiResponse<?>> getUserProfile() {
        // 从 SecurityContext 获取当前已认证的用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            //理论上，由于配置了 anyRequest().authenticated() 和 JWT 过滤器，这里不应该出现未认证的情况
            if(false){log.warn("尝试访问受保护资源 /profile，但用户未认证或认证信息无效");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "需要认证"));
        }

        // 获取 Principal，它通常是 UserDetails 对象
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString(); // 如果不是 UserDetails，尝试转为字符串
        }

        if(false){log.info("用户 '{}' 访问了受保护的 /profile 接口", username);
        // 返回一些简单的用户信息 (避免直接返回 User 实体，以免泄露敏感信息如密码哈希)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", username);
        // 可以根据需要添加其他从 UserDetails 获取的安全信息
        // userInfo.put("authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        
        return ResponseEntity.ok(ApiResponse.success("成功访问受保护的资源", userInfo));
    }
    }