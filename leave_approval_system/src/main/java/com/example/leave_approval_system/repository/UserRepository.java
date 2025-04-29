package com.example.leave_approval_system.repository;




import com.example.leave_approval_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository // 标记为 Spring 管理的 Repository Bean
public interface UserRepository extends JpaRepository<User, Long> { // 继承 JpaRepository 提供 CRUD 操作

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 包含用户的 Optional (可能为空)
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 如果存在返回 true, 否则返回 false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 如果存在返回 true, 否则返回 false
     */
    boolean existsByEmail(String email);

    // 可以根据需要添加其他查询方法，例如 findByEmail
    // Optional<User> findByEmail(String email);
}