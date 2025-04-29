package com.example.leave_approval_system.service;


import com.example.leave_approval_system.model.User;
import com.example.leave_approval_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入事务注解

@Service // 标记为 Spring 服务 Bean
@RequiredArgsConstructor // Lombok: 自动生成包含 final 字段的构造函数，实现依赖注入
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository; // 注入 UserRepository

    /**
     * 根据用户名加载用户信息 (Spring Security 调用此方法进行认证)
     * @param username 用户名
     * @return UserDetails 对象 (这里直接返回 User 实体，因为它实现了 UserDetails)
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    @Transactional(readOnly = true) // 数据库读取操作，设置为只读事务可以优化性能
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查找用户
        User user = userRepository.findByUsername(username)
                // 如果找不到用户，抛出 Spring Security 的 UsernameNotFoundException
                .orElseThrow(() -> new UsernameNotFoundException("用户未找到，用户名: " + username));

        // User 实体类已经实现了 UserDetails 接口，直接返回即可
        return user;
    }
}