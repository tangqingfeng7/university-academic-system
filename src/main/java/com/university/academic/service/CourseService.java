package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.Course;
import com.university.academic.entity.CoursePrerequisite;
import com.university.academic.entity.Department;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CoursePrerequisiteRepository;
import com.university.academic.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务类
 * 提供课程CRUD、先修课程管理等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CoursePrerequisiteRepository coursePrerequisiteRepository;
    private final DepartmentService departmentService;

    /**
     * 根据ID查询课程
     *
     * @param id 课程ID
     * @return 课程对象
     */
    @Cacheable(value = CacheConfig.CACHE_COURSES, key = "#id")
    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
    }

    /**
     * 根据课程编号查询课程
     *
     * @param courseNo 课程编号
     * @return 课程对象
     */
    @Transactional(readOnly = true)
    public Course findByCourseNo(String courseNo) {
        return courseRepository.findByCourseNo(courseNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
    }

    /**
     * 查询所有课程
     *
     * @return 课程列表
     */
    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAllWithDepartment();
    }

    /**
     * 分页查询课程
     *
     * @param pageable 分页参数
     * @return 课程分页数据
     */
    @Transactional(readOnly = true)
    public Page<Course> findAll(Pageable pageable) {
        return courseRepository.findAllWithDepartment(pageable);
    }

    /**
     * 搜索课程
     *
     * @param keyword      关键词
     * @param departmentId 院系ID（可选）
     * @param type         课程类型（可选）
     * @param pageable     分页参数
     * @return 课程分页数据
     */
    @Transactional(readOnly = true)
    public Page<Course> searchCourses(String keyword, Long departmentId, 
                                      Course.CourseType type, Pageable pageable) {
        if (departmentId != null || type != null) {
            return courseRepository.searchCoursesWithFilters(departmentId, type, keyword, pageable);
        } else {
            return courseRepository.searchCourses(keyword, pageable);
        }
    }

    /**
     * 根据院系ID查询课程列表
     *
     * @param departmentId 院系ID
     * @return 课程列表
     */
    @Transactional(readOnly = true)
    public List<Course> findByDepartmentId(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId);
    }

    /**
     * 创建课程
     *
     * @param course 课程对象
     * @return 创建后的课程对象
     */
    @CacheEvict(value = CacheConfig.CACHE_COURSES, allEntries = true)
    @Transactional
    public Course createCourse(Course course) {
        // 检查课程编号是否已存在
        if (courseRepository.existsByCourseNo(course.getCourseNo())) {
            throw new BusinessException(ErrorCode.COURSE_NO_ALREADY_EXISTS);
        }

        // 验证院系是否存在
        if (course.getDepartment() != null && course.getDepartment().getId() != null) {
            Department department = departmentService.findById(course.getDepartment().getId());
            course.setDepartment(department);
        }

        Course savedCourse = courseRepository.save(course);
        log.info("创建课程成功: {} - {} (课程编号: {}, 学分: {}, 学时: {})",
                savedCourse.getId(),
                savedCourse.getName(),
                savedCourse.getCourseNo(),
                savedCourse.getCredits(),
                savedCourse.getHours());
        return savedCourse;
    }

    /**
     * 更新课程信息
     *
     * @param id     课程ID
     * @param course 更新的课程信息
     * @return 更新后的课程对象
     */
    @CacheEvict(value = CacheConfig.CACHE_COURSES, allEntries = true)
    @Transactional
    public Course updateCourse(Long id, Course course) {
        Course existingCourse = findById(id);

        // 如果要修改课程编号，检查新课程编号是否已存在
        if (course.getCourseNo() != null && 
            !existingCourse.getCourseNo().equals(course.getCourseNo())) {
            if (courseRepository.existsByCourseNo(course.getCourseNo())) {
                throw new BusinessException(ErrorCode.COURSE_NO_ALREADY_EXISTS);
            }
            existingCourse.setCourseNo(course.getCourseNo());
        }

        // 更新其他字段
        if (course.getName() != null) {
            existingCourse.setName(course.getName());
        }
        if (course.getCredits() != null) {
            existingCourse.setCredits(course.getCredits());
        }
        if (course.getHours() != null) {
            existingCourse.setHours(course.getHours());
        }
        if (course.getType() != null) {
            existingCourse.setType(course.getType());
        }
        if (course.getDescription() != null) {
            existingCourse.setDescription(course.getDescription());
        }

        // 更新院系
        if (course.getDepartment() != null && course.getDepartment().getId() != null) {
            Department department = departmentService.findById(course.getDepartment().getId());
            existingCourse.setDepartment(department);
        }

        Course updatedCourse = courseRepository.save(existingCourse);
        log.info("更新课程成功: {} - {}", updatedCourse.getCourseNo(), updatedCourse.getName());
        return updatedCourse;
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     */
    @CacheEvict(value = CacheConfig.CACHE_COURSES, allEntries = true)
    @Transactional
    public void deleteCourse(Long id) {
        Course course = findById(id);
        
        // 检查是否有学生选课
        if (courseRepository.hasStudentSelections(id)) {
            throw new BusinessException(ErrorCode.COURSE_HAS_STUDENTS);
        }

        // 检查是否有开课计划
        if (courseRepository.hasOfferings(id)) {
            throw new BusinessException(ErrorCode.COURSE_HAS_OFFERINGS);
        }
        
        // 删除课程（会级联删除先修课程关系）
        courseRepository.delete(course);
        
        log.info("删除课程成功: {} - {}", course.getCourseNo(), course.getName());
    }

    /**
     * 检查课程编号是否存在
     *
     * @param courseNo 课程编号
     * @return true-存在，false-不存在
     */
    @Transactional(readOnly = true)
    public boolean existsByCourseNo(String courseNo) {
        return courseRepository.existsByCourseNo(courseNo);
    }

    /**
     * 统计课程数量
     *
     * @return 课程总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return courseRepository.count();
    }

    /**
     * 获取课程的所有先修课程
     *
     * @param courseId 课程ID
     * @return 先修课程列表
     */
    @Transactional(readOnly = true)
    public List<Course> getPrerequisites(Long courseId) {
        // 验证课程是否存在
        findById(courseId);
        
        List<CoursePrerequisite> prerequisites = coursePrerequisiteRepository.findByCourseId(courseId);
        return prerequisites.stream()
                .map(CoursePrerequisite::getPrerequisiteCourse)
                .collect(Collectors.toList());
    }

    /**
     * 设置课程的先修课程
     *
     * @param courseId             课程ID
     * @param prerequisiteCourseIds 先修课程ID列表
     */
    @Transactional
    public void setPrerequisites(Long courseId, List<Long> prerequisiteCourseIds) {
        Course course = findById(courseId);

        // 删除现有的先修课程关系
        List<CoursePrerequisite> existingPrerequisites = 
                coursePrerequisiteRepository.findByCourseId(courseId);
        coursePrerequisiteRepository.deleteAll(existingPrerequisites);

        // 添加新的先修课程关系
        if (prerequisiteCourseIds != null && !prerequisiteCourseIds.isEmpty()) {
            for (Long prerequisiteCourseId : prerequisiteCourseIds) {
                addPrerequisite(courseId, prerequisiteCourseId);
            }
        }

        log.info("设置课程先修课程成功: {} - {}, 先修课程数量: {}", 
                course.getCourseNo(), course.getName(), 
                prerequisiteCourseIds != null ? prerequisiteCourseIds.size() : 0);
    }

    /**
     * 添加先修课程
     *
     * @param courseId             课程ID
     * @param prerequisiteCourseId 先修课程ID
     */
    @Transactional
    public void addPrerequisite(Long courseId, Long prerequisiteCourseId) {
        // 验证课程是否存在
        Course course = findById(courseId);
        Course prerequisiteCourse = findById(prerequisiteCourseId);

        // 不能将课程设置为自己的先修课程
        if (courseId.equals(prerequisiteCourseId)) {
            throw new BusinessException(ErrorCode.PREREQUISITE_SELF);
        }

        // 检查是否已存在该先修课程关系
        if (coursePrerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(
                courseId, prerequisiteCourseId)) {
            log.warn("先修课程关系已存在: {} -> {}", courseId, prerequisiteCourseId);
            return;
        }

        // 检查是否存在循环依赖
        if (coursePrerequisiteRepository.hasCircularDependency(courseId, prerequisiteCourseId)) {
            throw new BusinessException(ErrorCode.PREREQUISITE_CIRCULAR);
        }

        // 创建先修课程关系
        CoursePrerequisite prerequisite = CoursePrerequisite.builder()
                .course(course)
                .prerequisiteCourse(prerequisiteCourse)
                .build();
        coursePrerequisiteRepository.save(prerequisite);

        log.info("添加先修课程成功: {} -> {}", course.getCourseNo(), prerequisiteCourse.getCourseNo());
    }

    /**
     * 删除先修课程
     *
     * @param courseId             课程ID
     * @param prerequisiteCourseId 先修课程ID
     */
    @Transactional
    public void removePrerequisite(Long courseId, Long prerequisiteCourseId) {
        CoursePrerequisite prerequisite = coursePrerequisiteRepository
                .findByCourseIdAndPrerequisiteCourseId(courseId, prerequisiteCourseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));

        coursePrerequisiteRepository.delete(prerequisite);
        log.info("删除先修课程成功: {} -> {}", courseId, prerequisiteCourseId);
    }
}

