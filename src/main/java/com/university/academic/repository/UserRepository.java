package com.university.academic.repository;

import com.university.academic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据角色查询用户列表
     *
     * @param role 用户角色
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findByRole(@Param("role") User.UserRole role);

    /**
     * 查询第一个符合条件的管理员用户
     *
     * @return 管理员用户
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.enabled = true ORDER BY u.id LIMIT 1")
    Optional<User> findFirstAdmin();

    /**
     * 查询第一个符合条件的教师用户
     *
     * @return 教师用户
     */
    @Query("SELECT u FROM User u WHERE u.role = 'TEACHER' AND u.enabled = true ORDER BY u.id LIMIT 1")
    Optional<User> findFirstTeacher();
}

