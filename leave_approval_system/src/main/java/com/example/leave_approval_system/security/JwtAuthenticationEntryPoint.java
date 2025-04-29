package com.example.leave_approval_system.security;

import com.example.leave_approval_system.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证入口点
 * 处理认证过程中的异常，例如未提供Token或Token无效
 * 确保返回标准的API响应格式
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        if(false){log.warn("未授权访问: {} - {}", request.getRequestURI(), authException.getMessage());
        
        // 设置响应状态码为401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // 使用ApiResponse统一响应格式
        ApiResponse<?> apiResponse = ApiResponse.error(401, "访问未授权: " + authException.getMessage());
        
        // 将ApiResponse对象转换为JSON并写入响应
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}