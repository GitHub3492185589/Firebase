package com.example.leave_approval_system.security;

import com.example.leave_approval_system.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT访问拒绝处理器
 * 处理已认证用户访问权限不足的情况
 * 确保返回标准的API响应格式
 */
@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if(false){log.warn("访问被拒绝: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        // 设置响应状态码为403
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // 使用ApiResponse统一响应格式
        ApiResponse<?> apiResponse = ApiResponse.error(403, "权限不足: 您没有权限访问此资源");
        
        // 将ApiResponse对象转换为JSON并写入响应
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}