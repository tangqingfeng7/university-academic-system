package com.university.academic.service.impl;

import com.university.academic.dto.ClassDTO;
import com.university.academic.dto.CreateClassRequest;
import com.university.academic.dto.UpdateClassRequest;
import com.university.academic.entity.Class;
import com.university.academic.entity.Major;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ClassRepository;
import com.university.academic.repository.MajorRepository;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TeacherRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 班级管理Service实现
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final MajorRepository majorRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public ClassDTO createClass(CreateClassRequest request) {
        log.info("创建班级: classCode={}, className={}", request.getClassCode(), request.getClassName());

        // 检查班级代码是否已存在
        if (classRepository.existsByClassCodeAndDeletedFalse(request.getClassCode())) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "班级代码已存在");
        }

        // 验证专业是否存在
        Major major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "专业不存在"));

        // 验证辅导员是否存在（如果提供）
        if (request.getCounselorId() != null) {
            User counselor = userRepository.findById(request.getCounselorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "辅导员不存在"));
            
            if (!counselor.getRole().equals(User.UserRole.TEACHER)) {
                throw new BusinessException(ErrorCode.INVALID_PARAM, "只能指定教师作为辅导员");
            }
        }

        Class clazz = Class.builder()
                .classCode(request.getClassCode())
                .className(request.getClassName())
                .major(major)
                .enrollmentYear(request.getEnrollmentYear())
                .capacity(request.getCapacity())
                .counselorId(request.getCounselorId())
                .remarks(request.getRemarks())
                .deleted(false)
                .build();

        Class savedClass = classRepository.save(clazz);
        log.info("班级创建成功: id={}, classCode={}", savedClass.getId(), savedClass.getClassCode());

        return convertToDTO(savedClass);
    }

    @Override
    @Transactional
    public ClassDTO updateClass(Long id, UpdateClassRequest request) {
        log.info("更新班级: id={}", id);

        Class clazz = classRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "班级不存在"));

        // 验证专业是否存在
        Major major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "专业不存在"));

        // 验证辅导员是否存在（如果提供）
        if (request.getCounselorId() != null) {
            User counselor = userRepository.findById(request.getCounselorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "辅导员不存在"));
            
            if (!counselor.getRole().equals(User.UserRole.TEACHER)) {
                throw new BusinessException(ErrorCode.INVALID_PARAM, "只能指定教师作为辅导员");
            }
        }

        // 更新班级信息
        clazz.setClassName(request.getClassName());
        clazz.setMajor(major);
        clazz.setEnrollmentYear(request.getEnrollmentYear());
        clazz.setCapacity(request.getCapacity());
        clazz.setCounselorId(request.getCounselorId());
        clazz.setRemarks(request.getRemarks());

        Class updatedClass = classRepository.save(clazz);
        log.info("班级更新成功: id={}", updatedClass.getId());

        return convertToDTO(updatedClass);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        log.info("删除班级: id={}", id);

        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "班级不存在"));

        // 软删除
        clazz.setDeleted(true);
        classRepository.save(clazz);

        log.info("班级删除成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassDTO getClassById(Long id) {
        Class clazz = classRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "班级不存在"));
        return convertToDTO(clazz);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassDTO> getClassList(Long majorId, Integer enrollmentYear, Pageable pageable) {
        Page<Class> classPage;

        if (majorId != null) {
            classPage = classRepository.findByMajorIdAndDeletedFalse(majorId, pageable);
        } else {
            classPage = classRepository.findByDeletedFalse(pageable);
        }

        return classPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getAllClasses() {
        List<Class> classes = classRepository.findAllWithMajorAndDepartment();
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByMajor(Long majorId) {
        List<Class> classes = classRepository.findByMajorIdAndDeletedFalseOrderByEnrollmentYearDescClassCodeAsc(majorId);
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByEnrollmentYear(Integer enrollmentYear) {
        List<Class> classes = classRepository.findByEnrollmentYearAndDeletedFalseOrderByClassCodeAsc(enrollmentYear);
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByMajorAndYear(Long majorId, Integer enrollmentYear) {
        List<Class> classes = classRepository.findByMajorIdAndEnrollmentYearAndDeletedFalse(majorId, enrollmentYear);
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByClassCode(String classCode) {
        return classRepository.existsByClassCodeAndDeletedFalse(classCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getClassStudents(Long classId) {
        log.info("查询班级学生: classId={}", classId);

        // 获取班级信息
        Class clazz = classRepository.findByIdWithDetails(classId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "班级不存在"));

        // 根据班级名称查询学生
        List<com.university.academic.entity.Student> students = 
                studentRepository.findByClassName(clazz.getClassName());

        // 转换为简化的Map格式
        return students.stream()
                .map(student -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", student.getId());
                    map.put("studentNo", student.getStudentNo());
                    map.put("name", student.getName());
                    map.put("gender", student.getGender() != null ? student.getGender().getDescription() : null);
                    map.put("phone", student.getPhone());
                    map.put("email", student.getEmail());
                    map.put("enrollmentYear", student.getEnrollmentYear());
                    map.put("status", student.getStatus() != null ? student.getStatus().name() : null);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> autoAssignCounselors() {
        log.info("开始自动分配辅导员");

        // 查询所有未分配辅导员的班级
        List<Class> unassignedClasses = classRepository.findUnassignedClasses();
        
        if (unassignedClasses.isEmpty()) {
            log.info("没有需要分配辅导员的班级");
            return createAssignmentResult(0, 0, "没有需要分配辅导员的班级");
        }

        // 执行分配
        int assignedCount = performCounselorAssignment(unassignedClasses);

        log.info("辅导员自动分配完成: 共分配{}个班级", assignedCount);
        return createAssignmentResult(unassignedClasses.size(), assignedCount, 
                String.format("成功为%d个班级分配了辅导员", assignedCount));
    }

    @Override
    @Transactional
    public Map<String, Object> autoAssignCounselors(Long majorId, Integer enrollmentYear) {
        log.info("开始自动分配辅导员: majorId={}, enrollmentYear={}", majorId, enrollmentYear);

        List<Class> unassignedClasses;

        // 根据条件查询未分配的班级
        if (majorId != null && enrollmentYear != null) {
            // 同时指定专业和年份
            unassignedClasses = classRepository.findUnassignedClassesByMajor(majorId).stream()
                    .filter(c -> c.getEnrollmentYear().equals(enrollmentYear))
                    .collect(Collectors.toList());
        } else if (majorId != null) {
            // 仅指定专业
            unassignedClasses = classRepository.findUnassignedClassesByMajor(majorId);
        } else if (enrollmentYear != null) {
            // 仅指定年份
            unassignedClasses = classRepository.findUnassignedClassesByYear(enrollmentYear);
        } else {
            // 都不指定，分配所有
            unassignedClasses = classRepository.findUnassignedClasses();
        }

        if (unassignedClasses.isEmpty()) {
            log.info("没有符合条件的未分配班级");
            return createAssignmentResult(0, 0, "没有符合条件的未分配班级");
        }

        // 执行分配
        int assignedCount = performCounselorAssignment(unassignedClasses);

        log.info("辅导员自动分配完成: 共分配{}个班级", assignedCount);
        return createAssignmentResult(unassignedClasses.size(), assignedCount,
                String.format("成功为%d个班级分配了辅导员", assignedCount));
    }

    /**
     * 执行辅导员分配逻辑（负载均衡算法）
     */
    private int performCounselorAssignment(List<Class> unassignedClasses) {
        int assignedCount = 0;

        // 按院系分组处理
        Map<Long, List<Class>> classesByDepartment = unassignedClasses.stream()
                .collect(Collectors.groupingBy(c -> c.getMajor().getDepartment().getId()));

        for (Map.Entry<Long, List<Class>> entry : classesByDepartment.entrySet()) {
            Long departmentId = entry.getKey();
            List<Class> classes = entry.getValue();

            // 获取该院系的所有教师
            List<com.university.academic.entity.Teacher> teachers = 
                    teacherRepository.findByDepartmentId(departmentId);

            if (teachers.isEmpty()) {
                log.warn("院系{}没有可用的教师，无法分配辅导员", departmentId);
                continue;
            }

            // 计算每个教师当前负责的班级数量
            Map<Long, Long> teacherWorkload = new HashMap<>();
            for (com.university.academic.entity.Teacher teacher : teachers) {
                long classCount = classRepository.countByCounselorId(teacher.getUser().getId());
                teacherWorkload.put(teacher.getUser().getId(), classCount);
            }

            // 按负载均衡原则分配
            for (Class clazz : classes) {
                // 找到负责班级最少的教师
                Long selectedCounselorId = findLeastLoadedTeacher(teacherWorkload);
                
                if (selectedCounselorId != null) {
                    clazz.setCounselorId(selectedCounselorId);
                    classRepository.save(clazz);
                    
                    // 更新该教师的负载
                    teacherWorkload.put(selectedCounselorId, 
                            teacherWorkload.get(selectedCounselorId) + 1);
                    
                    assignedCount++;
                    log.info("班级{}已分配辅导员: counselorId={}", 
                            clazz.getClassCode(), selectedCounselorId);
                }
            }
        }

        return assignedCount;
    }

    /**
     * 找到负载最少的教师
     */
    private Long findLeastLoadedTeacher(Map<Long, Long> teacherWorkload) {
        if (teacherWorkload.isEmpty()) {
            return null;
        }

        return teacherWorkload.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 创建分配结果
     */
    private Map<String, Object> createAssignmentResult(int totalClasses, int assignedCount, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalClasses", totalClasses);
        result.put("assignedCount", assignedCount);
        result.put("unassignedCount", totalClasses - assignedCount);
        result.put("message", message);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByCounselor(Long counselorId) {
        log.info("查询辅导员管理的班级: counselorId={}", counselorId);
        
        List<Class> classes = classRepository.findByCounselorId(counselorId);
        return classes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private ClassDTO convertToDTO(Class clazz) {
        ClassDTO.ClassDTOBuilder builder = ClassDTO.builder()
                .id(clazz.getId())
                .classCode(clazz.getClassCode())
                .className(clazz.getClassName())
                .enrollmentYear(clazz.getEnrollmentYear())
                .capacity(clazz.getCapacity())
                .counselorId(clazz.getCounselorId())
                .remarks(clazz.getRemarks())
                .createdAt(clazz.getCreatedAt())
                .updatedAt(clazz.getUpdatedAt());

        // 设置专业信息
        if (clazz.getMajor() != null) {
            builder.majorId(clazz.getMajor().getId())
                   .majorName(clazz.getMajor().getName())
                   .majorCode(clazz.getMajor().getCode());

            // 设置院系信息
            if (clazz.getMajor().getDepartment() != null) {
                builder.departmentId(clazz.getMajor().getDepartment().getId())
                       .departmentName(clazz.getMajor().getDepartment().getName());
            }
        }

        // 设置辅导员姓名（如果需要）
        if (clazz.getCounselorId() != null) {
            // 通过user_id查询teacher表获取真实姓名
            teacherRepository.findByUserId(clazz.getCounselorId()).ifPresent(teacher -> {
                builder.counselorName(teacher.getName());
            });
        }

        return builder.build();
    }
}

