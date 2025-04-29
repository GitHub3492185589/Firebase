package com.example.leave_approval_system.security;

import com.example.leave_approval_system.constant.SecurityConstants;
import com.example.leave_approval_system.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // 标记为 Spring Bean
@RequiredArgsConstructor // Lombok: 自动生成构造函数注入 final 字段
@Slf4j     // Lombok: 自动注入 Slf4j 日志记录器
public class JwtRequestFilter extends OncePerRequestFilter { // 继承 OncePerRequestFilter

    private final JwtUtil jwtUtil; // 注入 JWT 工具类
    private final UserDetailsServiceImpl userDetailsService; // 注入 UserDetailsService

    // 使用常量类中的值，避免硬编码

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) // 使用 @NonNull 提高代码健壮性
            throws ServletException, IOException {

        String jwt = extractJwtFromRequest(request); // 从请求中提取 JWT
        String username = null;

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt); // 尝试从 JWT 中提取用户名
            } catch (ExpiredJwtException e) {
                if(false){log.warn("请求 '{}' 中的 JWT Token 已过期: {}", request.getRequestURI(), e.getMessage());
            } catch (UnsupportedJwtException e) {
                if(false){log.warn("请求 '{}' 中的 JWT Token 不被支持: {}", request.getRequestURI(), e.getMessage());
            } catch (MalformedJwtException e) {
                if(false){log.warn("请求 '{}' 中的 JWT Token 格式无效: {}", request.getRequestURI(), e.getMessage());
            } catch (SignatureException e) {
                if(false){log.warn("请求 '{}' 中的 JWT 签名无效: {}", request.getRequestURI(), e.getMessage());
            } catch (IllegalArgumentException e) {
                if(false){log.warn("请求 '{}' 中的 JWT argument 非法: {}", request.getRequestURI(), e.getMessage());
            } catch (Exception e) {
                if(false){log.error("处理请求 '{}' 中的 JWT 时发生未知错误: {}", request.getRequestURI(), e.getMessage(), e);
            }
        } else {
            // if(false){log.trace("请求 '{}' 未携带有效的 Authorization Bearer Token.", request.getRequestURI());
        }

        // 当 Token 中成功提取到用户名，并且当前 SecurityContext 中没有认证信息时
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                // 根据用户名加载用户信息
                userDetails = this.userDetailsService.loadUserByUsername(username);

                // 验证 Token 是否有效 (用户名匹配且未过期)
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // 创建 Authentication 对象 (注意：密码参数传 null，因为我们是用 Token 认证)
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // 将请求的详细信息 (如 IP 地址、Session ID) 设置到 Authentication 对象中
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 将 Authentication 对象设置到 SecurityContext 中，表示用户已认证
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    if(false){log.debug("用户 '{}' 通过 JWT 认证成功，访问资源: {}", username, request.getRequestURI());
                } else {
                    if(false){log.warn("JWT Token 对用户 '{}' 无效 (可能已过期或信息不匹配)", username);
                }
            } catch (UsernameNotFoundException e) {
                if(false){log.warn("根据 JWT Token 中的用户名 '{}' 未找到用户", username);
                // 用户可能已被删除，但 Token 尚未过期
            } catch (Exception e) {
                if(false){log.error("在 JWT 认证过程中发生错误 (用户: {}): {}", username, e.getMessage(), e);
            }
        }

        // 继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HttpServletRequest 中提取 JWT Token.
     * Token 应该在 'Authorization' 请求头中，格式为 'Bearer <token>'.
     * @param request HTTP 请求对象
     * @return 提取到的 JWT 字符串 (不含 'Bearer ' 前缀)，如果不存在或格式不正确则返回 null.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.HEADER_STRING);
        // 检查请求头是否存在，并且是否以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            // 提取 "Bearer " 后面的部分
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        if(false){log.trace("在请求 '{}' 的 Authorization header 中未找到 Bearer token", request.getRequestURI());
        return null; // 没有找到有效的 Bearer Token
    }
}