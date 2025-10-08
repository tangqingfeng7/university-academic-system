package com.university.academic.service;

import com.university.academic.entity.Department;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 教师服务类
 * 提供教师CRUD、搜索等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentService departmentService;
    private final UserService userService;

    /**
     * 根据ID查询教师
     *
     * @param id 教师ID
     * @return 教师对象
     */
    @Transactional(readOnly = true)
    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
    }

    /**
     * 根据工号查询教师
     *
     * @param teacherNo 工号
     * @return 教师对象
     */
    @Transactional(readOnly = true)
    public Teacher findByTeacherNo(String teacherNo) {
        return teacherRepository.findByTeacherNo(teacherNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
    }

    /**
     * 查询所有教师
     *
     * @return 教师列表
     */
    @Transactional(readOnly = true)
    public List<Teacher> findAll() {
        return teacherRepository.findAllWithDepartment();
    }

    /**
     * 分页查询教师
     *
     * @param pageable 分页参数
     * @return 教师分页数据
     */
    @Transactional(readOnly = true)
    public Page<Teacher> findAll(Pageable pageable) {
        return teacherRepository.findAllWithDepartment(pageable);
    }

    /**
     * 搜索教师
     *
     * @param keyword      关键词
     * @param departmentId 院系ID（可选）
     * @param pageable     分页参数
     * @return 教师分页数据
     */
    @Transactional(readOnly = true)
    public Page<Teacher> searchTeachers(String keyword, Long departmentId, Pageable pageable) {
        if (departmentId != null) {
            return teacherRepository.searchTeachersByDepartment(departmentId, keyword, pageable);
        } else {
            return teacherRepository.searchTeachers(keyword, pageable);
        }
    }

    /**
     * 根据院系ID查询教师列表
     *
     * @param departmentId 院系ID
     * @return 教师列表
     */
    @Transactional(readOnly = true)
    public List<Teacher> findByDepartmentId(Long departmentId) {
        return teacherRepository.findByDepartmentId(departmentId);
    }

    /**
     * 创建教师
     *
     * @param teacher 教师对象
     * @return 创建后的教师对象
     */
    @Transactional
    public Teacher createTeacher(Teacher teacher) {
        // 检查工号是否已存在
        if (teacherRepository.existsByTeacherNo(teacher.getTeacherNo())) {
            throw new BusinessException(ErrorCode.TEACHER_NO_ALREADY_EXISTS);
        }

        // 验证院系是否存在
        if (teacher.getDepartment() != null && teacher.getDepartment().getId() != null) {
            Department department = departmentService.findById(teacher.getDepartment().getId());
            teacher.setDepartment(department);
        }

        // 创建关联的用户账户
        if (teacher.getUser() != null && teacher.getUser().getUsername() != null) {
            User user = User.builder()
                    .username(teacher.getUser().getUsername())
                    .password(teacher.getUser().getPassword() != null ? 
                            teacher.getUser().getPassword() : "123456") // 默认密码
                    .role(User.UserRole.TEACHER)
                    .enabled(true)
                    .firstLogin(true)
                    .build();
            User createdUser = userService.createUser(user);
            teacher.setUser(createdUser);
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("创建教师成功: {} - {} (工号: {}, 院系: {})",
                savedTeacher.getId(),
                savedTeacher.getName(),
                savedTeacher.getTeacherNo(),
                savedTeacher.getDepartment() != null ? savedTeacher.getDepartment().getName() : "未设置");
        return savedTeacher;
    }

    /**
     * 更新教师信息
     *
     * @param id      教师ID
     * @param teacher 更新的教师信息
     * @return 更新后的教师对象
     */
    @Transactional
    public Teacher updateTeacher(Long id, Teacher teacher) {
        Teacher existingTeacher = findById(id);

        // 如果要修改工号，检查新工号是否已存在
        if (teacher.getTeacherNo() != null && 
            !existingTeacher.getTeacherNo().equals(teacher.getTeacherNo())) {
            if (teacherRepository.existsByTeacherNo(teacher.getTeacherNo())) {
                throw new BusinessException(ErrorCode.TEACHER_NO_ALREADY_EXISTS);
            }
            existingTeacher.setTeacherNo(teacher.getTeacherNo());
        }

        // 更新其他字段
        if (teacher.getName() != null) {
            existingTeacher.setName(teacher.getName());
        }
        if (teacher.getGender() != null) {
            existingTeacher.setGender(teacher.getGender());
        }
        if (teacher.getTitle() != null) {
            existingTeacher.setTitle(teacher.getTitle());
        }
        if (teacher.getPhone() != null) {
            existingTeacher.setPhone(teacher.getPhone());
        }
        if (teacher.getEmail() != null) {
            existingTeacher.setEmail(teacher.getEmail());
        }

        // 更新院系
        if (teacher.getDepartment() != null && teacher.getDepartment().getId() != null) {
            Department department = departmentService.findById(teacher.getDepartment().getId());
            existingTeacher.setDepartment(department);
        }

        Teacher updatedTeacher = teacherRepository.save(existingTeacher);
        log.info("更新教师成功: {} - {}", updatedTeacher.getTeacherNo(), updatedTeacher.getName());
        return updatedTeacher;
    }

    /**
     * 删除教师
     *
     * @param id 教师ID
     */
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = findById(id);
        
        // 检查是否有授课任务
        if (teacherRepository.hasActiveOfferings(id)) {
            throw new BusinessException(ErrorCode.TEACHER_HAS_COURSES);
        }
        
        // 删除教师
        teacherRepository.delete(teacher);
        
        // 同时禁用关联的用户账户
        if (teacher.getUser() != null) {
            userService.setUserEnabled(teacher.getUser().getId(), false);
        }
        
        log.info("删除教师成功: {} - {}", teacher.getTeacherNo(), teacher.getName());
    }

    /**
     * 检查工号是否存在
     *
     * @param teacherNo 工号
     * @return true-存在，false-不存在
     */
    @Transactional(readOnly = true)
    public boolean existsByTeacherNo(String teacherNo) {
        return teacherRepository.existsByTeacherNo(teacherNo);
    }

    /**
     * 统计教师数量
     *
     * @return 教师总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return teacherRepository.count();
    }

    /**
     * 检查教师是否有授课任务
     *
     * @param teacherId 教师ID
     * @return true-有授课任务，false-无授课任务
     */
    @Transactional(readOnly = true)
    public boolean hasActiveOfferings(Long teacherId) {
        return teacherRepository.hasActiveOfferings(teacherId);
    }

    /**
     * 根据用户ID查询教师
     *
     * @param userId 用户ID
     * @return 教师对象
     */
    @Transactional(readOnly = true)
    public Teacher findByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
    }

    /**
     * 根据用户ID查询教师（预加载所有关联）
     * 注：findByUserId已包含预加载，此方法作为别名保持API一致性
     *
     * @param userId 用户ID
     * @return 教师对象
     */
    @Transactional(readOnly = true)
    public Teacher findByUserIdWithDetails(Long userId) {
        return findByUserId(userId);
    }

    /**
     * 统计教师总数（别名方法，保持API一致性）
     *
     * @return 教师总数
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return count();
    }
}

