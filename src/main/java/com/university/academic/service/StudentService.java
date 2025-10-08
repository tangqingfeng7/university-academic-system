package com.university.academic.service;

import com.university.academic.entity.Major;
import com.university.academic.entity.Student;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生服务类
 * 提供学生CRUD、软删除、搜索等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseSelectionRepository selectionRepository;
    private final MajorService majorService;
    private final UserService userService;

    /**
     * 根据ID查询学生
     *
     * @param id 学生ID
     * @return 学生对象
     */
    @Transactional(readOnly = true)
    public Student findById(Long id) {
        Student student = studentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        
        if (student.getDeleted()) {
            throw new BusinessException(ErrorCode.STUDENT_ALREADY_DELETED);
        }
        
        return student;
    }

    /**
     * 根据学号查询学生
     *
     * @param studentNo 学号
     * @return 学生对象
     */
    @Transactional(readOnly = true)
    public Student findByStudentNo(String studentNo) {
        Student student = studentRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        
        if (student.getDeleted()) {
            throw new BusinessException(ErrorCode.STUDENT_ALREADY_DELETED);
        }
        
        return student;
    }

    /**
     * 查询所有未删除的学生
     *
     * @return 学生列表
     */
    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll().stream()
                .filter(student -> !student.getDeleted())
                .toList();
    }

    /**
     * 分页查询未删除的学生
     *
     * @param pageable 分页参数
     * @return 学生分页数据
     */
    @Transactional(readOnly = true)
    public Page<Student> findAll(Pageable pageable) {
        return studentRepository.findByDeletedFalse(pageable);
    }

    /**
     * 搜索学生
     *
     * @param keyword   关键词
     * @param majorId   专业ID（可选）
     * @param className 班级名称（可选）
     * @param pageable  分页参数
     * @return 学生分页数据
     */
    @Transactional(readOnly = true)
    public Page<Student> searchStudents(String keyword, Long majorId, String className, Pageable pageable) {
        // 使用统一的条件查询方法
        return studentRepository.searchStudentsByConditions(majorId, className, keyword, pageable);
    }

    /**
     * 根据专业ID查询学生列表
     *
     * @param majorId 专业ID
     * @return 学生列表
     */
    @Transactional(readOnly = true)
    public List<Student> findByMajorId(Long majorId) {
        return studentRepository.findByMajorId(majorId).stream()
                .filter(student -> !student.getDeleted())
                .toList();
    }

    /**
     * 创建学生
     *
     * @param student 学生对象
     * @return 创建后的学生对象
     */
    @Transactional
    public Student createStudent(Student student) {
        // 检查学号是否已存在
        if (studentRepository.existsByStudentNo(student.getStudentNo())) {
            throw new BusinessException(ErrorCode.STUDENT_NO_ALREADY_EXISTS);
        }

        // 验证专业是否存在
        if (student.getMajor() != null && student.getMajor().getId() != null) {
            Major major = majorService.findById(student.getMajor().getId());
            student.setMajor(major);
        }

        // 创建关联的用户账户
        if (student.getUser() != null && student.getUser().getUsername() != null) {
            User user = User.builder()
                    .username(student.getUser().getUsername())
                    .password(student.getUser().getPassword() != null ? 
                            student.getUser().getPassword() : "123456") // 默认密码
                    .role(User.UserRole.STUDENT)
                    .enabled(true)
                    .firstLogin(true)
                    .build();
            User createdUser = userService.createUser(user);
            student.setUser(createdUser);
        }

        student.setDeleted(false);
        Student savedStudent = studentRepository.save(student);
        log.info("创建学生成功: {} - {} (学号: {}, 专业: {})",
                savedStudent.getId(),
                savedStudent.getName(),
                savedStudent.getStudentNo(),
                savedStudent.getMajor() != null ? savedStudent.getMajor().getName() : "未设置");
        return savedStudent;
    }

    /**
     * 更新学生信息
     *
     * @param id      学生ID
     * @param student 更新的学生信息
     * @return 更新后的学生对象
     */
    @Transactional
    public Student updateStudent(Long id, Student student) {
        Student existingStudent = findById(id);

        // 如果要修改学号，检查新学号是否已存在
        if (student.getStudentNo() != null && 
            !existingStudent.getStudentNo().equals(student.getStudentNo())) {
            if (studentRepository.existsByStudentNo(student.getStudentNo())) {
                throw new BusinessException(ErrorCode.STUDENT_NO_ALREADY_EXISTS);
            }
            existingStudent.setStudentNo(student.getStudentNo());
        }

        // 更新其他字段
        if (student.getName() != null) {
            existingStudent.setName(student.getName());
        }
        if (student.getGender() != null) {
            existingStudent.setGender(student.getGender());
        }
        if (student.getBirthDate() != null) {
            existingStudent.setBirthDate(student.getBirthDate());
        }
        if (student.getEnrollmentYear() != null) {
            existingStudent.setEnrollmentYear(student.getEnrollmentYear());
        }
        if (student.getClassName() != null) {
            existingStudent.setClassName(student.getClassName());
        }
        if (student.getPhone() != null) {
            existingStudent.setPhone(student.getPhone());
        }

        // 更新专业
        if (student.getMajor() != null && student.getMajor().getId() != null) {
            Major major = majorService.findById(student.getMajor().getId());
            existingStudent.setMajor(major);
        }

        Student updatedStudent = studentRepository.save(existingStudent);
        log.info("更新学生成功: {} - {}", updatedStudent.getStudentNo(), updatedStudent.getName());
        return updatedStudent;
    }

    /**
     * 删除学生（软删除）
     *
     * @param id 学生ID
     */
    @Transactional
    public void deleteStudent(Long id) {
        Student student = findById(id);
        
        // 检查是否有在读课程，如有则不允许删除
        // 查询学生是否有状态为SELECTED的选课记录
        boolean hasActiveCourses = selectionRepository.findByStudentId(id).stream()
                .anyMatch(selection -> selection.getStatus() == com.university.academic.entity.CourseSelection.SelectionStatus.SELECTED);
        
        if (hasActiveCourses) {
            log.warn("学生 {} 有在读课程，无法删除", student.getName());
            throw new BusinessException(ErrorCode.STUDENT_HAS_COURSES);
        }
        
        // 软删除
        student.setDeleted(true);
        studentRepository.save(student);
        
        // 同时禁用关联的用户账户
        if (student.getUser() != null) {
            userService.setUserEnabled(student.getUser().getId(), false);
        }
        
        log.info("删除学生成功（软删除）: {} - {}", student.getStudentNo(), student.getName());
    }

    /**
     * 检查学号是否存在
     *
     * @param studentNo 学号
     * @return true-存在，false-不存在
     */
    @Transactional(readOnly = true)
    public boolean existsByStudentNo(String studentNo) {
        return studentRepository.existsByStudentNo(studentNo);
    }

    /**
     * 统计未删除的学生数量
     *
     * @return 学生总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return studentRepository.countByDeletedFalse();
    }

    /**
     * 批量创建学生
     *
     * @param students 学生列表
     * @return 创建成功的学生列表
     */
    @Transactional
    public List<Student> batchCreateStudents(List<Student> students) {
        return students.stream()
                .map(this::createStudent)
                .toList();
    }

    /**
     * 根据专业代码查找专业
     *
     * @param majorCode 专业代码
     * @return 专业对象
     */
    public Major findMajorByCode(String majorCode) {
        return majorService.findByCode(majorCode);
    }

    /**
     * 根据用户ID查询学生
     *
     * @param userId 用户ID
     * @return 学生对象
     */
    @Transactional(readOnly = true)
    public Student findByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }

    /**
     * 根据用户ID查询学生（预加载所有关联）
     *
     * @param userId 用户ID
     * @return 学生对象
     */
    @Transactional(readOnly = true)
    public Student findByUserIdWithDetails(Long userId) {
        return studentRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
