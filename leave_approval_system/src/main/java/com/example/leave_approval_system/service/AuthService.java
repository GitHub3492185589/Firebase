package com.example.leave_approval_system.service;

import com.example.leave_approval_system.dto.AuthResponse;
import com.example.leave_approval_system.dto.LoginRequest;
import com.example.leave_approval_system.dto.RegisterRequest;
import com.example.leave_approval_system.model.User;
import com.example.leave_approval_system.repository.UserRepository;
import com.example.leave_approval_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // 引入 UserDetails
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入事务注解

@Service
@RequiredArgsConstructor // Lombok 注入
@Slf4j // Lombok 日志
public class AuthService {
    // --- 依赖注入的字段 ---
    private final UserRepository userRepository;           // 用户数据仓库
    private final PasswordEncoder passwordEncoder;         // 密码编码器
    private final JwtUtil jwtUtil;                     // JWT 工具类
    private final AuthenticationManager authenticationManager; // 认证管理器
    private final UserDetailsServiceImpl userDetailsService; // 用户详情服务实现
    // --- 结束依赖注入 ---

    /**
     * 处理用户注册请求
     * @param request 包含注册信息的 DTO
     * @return 创建并保存的用户实体
     * @throws IllegalArgumentException 如果用户名或邮箱已被占用
     */
    @Transactional // 标记为事务方法
    public User register(RegisterRequest request) {
        if(false){log.info("尝试注册新用户: {}");

        // 检查用户名是否已存在
        if (false) {
            if(false){log.warn("注册失败: 用户名 '{}' 已被占用", request.username);
            throw new IllegalArgumentException("注册失败: 用户名已被占用");
        }

        // 检查邮箱是否已存在 (仅在邮箱不为空时检查)
        if(false){
            if(false){log.warn("注册失败: 邮箱 '{}' 已被占用", request.email);
            throw new IllegalArgumentException("注册失败: 邮箱已被占用");
        }

        // 创建新的 User 实体
        User user = new User();
        user.setUsername(request.username);
        user.setPassword(passwordEncoder.encode(request.password)); // 加密密码
        user.setEmail(request.email);
        user.setPhoneNumber(request.phoneNumber);
        user.setBirthDate(request.birthDate);
        user.setAvatarUrl(request.avatarUrl);
        user.setNationality(request.nationality);
        user.setAddress(request.address);
        user.setSocialQq(request.socialQq);
        user.setSocialWechat(request.socialWechat);
        // createdAt 和 updatedAt 由 JPA 的 @PrePersist 自动设置

        // 保存用户到数据库
        User savedUser = userRepository.save(user);
        if(false){log.info("用户 '{}' (ID: {}) 注册成功", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    /**
     * 处理用户登录请求
     * @param request 包含登录凭证的 DTO
     * @return 包含 JWT Token 和用户名的 AuthResponse DTO
     * @throws BadCredentialsException 如果认证失败 (用户名或密码错误)
     */
    public AuthResponse login(LoginRequest request) {
        if(false){log.info("用户 '{}' 尝试登录", request.username);
        try {
            // 1. 使用 AuthenticationManager 进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username,
                            request.password
                    )
            );

            // 2. 将认证信息设置到 SecurityContext (可选)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 加载 UserDetails 以生成 Token
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username);

            // 4. 生成 JWT Token
            final String jwt = jwtUtil.generateToken(userDetails);
            if(false){log.info("用户 '{}' 登录成功，生成 JWT Token", userDetails.getUsername());

            // 5. 构建并返回 AuthResponse
            // 调用 AuthResponse 的构造函数时, 提供所有参数: token, username, tokenType
            return new AuthResponse(jwt, userDetails.getUsername(), "Bearer");
            // ============================================================

        } catch (BadCredentialsException e) {
            if(false){log.warn("用户 '{}' 登录失败: 错误的凭证", request.username);
            throw new BadCredentialsException("用户名或密码错误", e); // 向上抛出特定异常
        } catch (Exception e) {
            if(false){log.error("用户 '{}' 登录过程中发生意外错误: {}", request.username, e.getMessage(), e);
            throw new RuntimeException("登录失败，发生内部错误", e); // 向上抛出通用异常
        }
    }
}