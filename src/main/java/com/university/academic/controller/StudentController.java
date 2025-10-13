package com.university.academic.controller;

import com.university.academic.annotation.OperationLog;
import com.university.academic.dto.CreateStudentRequest;
import com.university.academic.dto.StudentDTO;
import com.university.academic.dto.UpdateStudentRequest;
import com.university.academic.entity.Major;
import com.university.academic.entity.Student;
import com.university.academic.entity.User;
import com.university.academic.service.StudentService;
import com.university.academic.util.DtoConverter;
import com.university.academic.util.ExcelUtil;
import com.university.academic.vo.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生管理控制器
 * 提供学生CRUD接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final DtoConverter dtoConverter;
    private final ExcelUtil excelUtil;

    /**
     * 分页查询学生列表
     */
    @GetMapping("/admin/students")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getStudentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Integer enrollmentYear) {

        log.info("查询学生列表: page={}, size={}, keyword={}, majorId={}, className={}, enrollmentYear={}", 
                page, size, keyword, majorId, className, enrollmentYear);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Student> studentPage;
        // 如果有任何筛选条件，使用搜索方法
        if ((keyword != null && !keyword.trim().isEmpty()) || majorId != null || 
            (className != null && !className.trim().isEmpty()) || enrollmentYear != null) {
            String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : "";
            studentPage = studentService.searchStudents(searchKeyword, majorId, className, enrollmentYear, pageable);
        } else {
            studentPage = studentService.findAll(pageable);
        }

        List<StudentDTO> studentDTOList = studentPage.getContent().stream()
                .map(dtoConverter::toStudentDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", studentDTOList);
        response.put("totalElements", studentPage.getTotalElements());
        response.put("totalPages", studentPage.getTotalPages());
        response.put("currentPage", studentPage.getNumber());
        response.put("pageSize", studentPage.getSize());

        return Result.success(response);
    }

    /**
     * 获取所有学生（不分页）
     */
    @GetMapping("/admin/students/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<StudentDTO>> getAllStudents() {
        log.info("查询所有学生");

        List<Student> students = studentService.findAll();
        List<StudentDTO> studentDTOList = students.stream()
                .map(dtoConverter::toStudentDTO)
                .collect(Collectors.toList());

        return Result.success(studentDTOList);
    }

    /**
     * 获取所有班级列表（去重）
     */
    @GetMapping("/admin/students/classes")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> getAllClasses() {
        log.info("查询所有班级列表");

        List<String> classes = studentService.findAll().stream()
                .map(Student::getClassName)
                .filter(className -> className != null && !className.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return Result.success(classes);
    }

    /**
     * 根据专业ID获取学生列表
     */
    @GetMapping("/admin/students/by-major/{majorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<StudentDTO>> getStudentsByMajor(@PathVariable Long majorId) {
        log.info("根据专业ID查询学生列表: majorId={}", majorId);

        List<Student> students = studentService.findByMajorId(majorId);
        List<StudentDTO> studentDTOList = students.stream()
                .map(dtoConverter::toStudentDTO)
                .collect(Collectors.toList());

        return Result.success(studentDTOList);
    }

    /**
     * 获取学生详情
     */
    @GetMapping("/admin/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StudentDTO> getStudentById(@PathVariable Long id) {
        log.info("查询学生详情: id={}", id);

        Student student = studentService.findById(id);
        StudentDTO studentDTO = dtoConverter.toStudentDTO(student);

        return Result.success(studentDTO);
    }

    /**
     * 创建学生
     */
    @PostMapping("/admin/students")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("创建学生")
    public Result<StudentDTO> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        log.info("创建学生: studentNo={}, name={}, majorId={}",
                request.getStudentNo(), request.getName(), request.getMajorId());

        Major major = new Major();
        major.setId(request.getMajorId());

        User user = new User();
        user.setUsername(request.getUsername() != null ? request.getUsername() : request.getStudentNo());
        user.setPassword(request.getPassword());

        Student student = Student.builder()
                .studentNo(request.getStudentNo())
                .name(request.getName())
                .gender(Student.Gender.valueOf(request.getGender()))
                .birthDate(request.getBirthDate())
                .enrollmentYear(request.getEnrollmentYear())
                .major(major)
                .className(request.getClassName())
                .phone(request.getPhone())
                .user(user)
                .build();

        Student createdStudent = studentService.createStudent(student);
        StudentDTO studentDTO = dtoConverter.toStudentDTO(createdStudent);

        return Result.success("学生创建成功", studentDTO);
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/admin/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("更新学生信息")
    public Result<StudentDTO> updateStudent(@PathVariable Long id,
                                            @Valid @RequestBody UpdateStudentRequest request) {
        log.info("更新学生: id={}", id);

        Major major = null;
        if (request.getMajorId() != null) {
            major = new Major();
            major.setId(request.getMajorId());
        }

        Student student = Student.builder()
                .studentNo(request.getStudentNo())
                .name(request.getName())
                .gender(request.getGender() != null ? Student.Gender.valueOf(request.getGender()) : null)
                .birthDate(request.getBirthDate())
                .enrollmentYear(request.getEnrollmentYear())
                .major(major)
                .className(request.getClassName())
                .phone(request.getPhone())
                .build();

        Student updatedStudent = studentService.updateStudent(id, student);
        StudentDTO studentDTO = dtoConverter.toStudentDTO(updatedStudent);

        return Result.success("学生更新成功", studentDTO);
    }

    /**
     * 删除学生（软删除）
     */
    @DeleteMapping("/admin/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("删除学生")
    public Result<String> deleteStudent(@PathVariable Long id) {
        log.info("删除学生: id={}", id);

        studentService.deleteStudent(id);

        return Result.success("学生删除成功");
    }

    /**
     * 检查学号是否存在
     */
    @GetMapping("/admin/students/check-student-no")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Boolean>> checkStudentNo(@RequestParam String studentNo) {
        boolean exists = studentService.existsByStudentNo(studentNo);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success(response);
    }

    /**
     * 获取学生统计信息
     */
    @GetMapping("/admin/students/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getStudentStatistics() {
        log.info("查询学生统计信息");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalStudents", studentService.count());

        return Result.success(statistics);
    }

    /**
     * 导入学生数据（Excel）
     */
    @PostMapping("/admin/students/import")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("批量导入学生")
    public Result<Map<String, Object>> importStudents(@RequestParam("file") MultipartFile file) {
        log.info("导入学生数据: filename={}", file.getOriginalFilename());

        try {
            // 解析Excel文件
            List<ExcelUtil.StudentImportDTO> importData = excelUtil.importStudents(file.getInputStream());

            List<Student> successList = new ArrayList<>();
            List<Map<String, Object>> errorList = new ArrayList<>();

            // 处理每一条数据
            for (ExcelUtil.StudentImportDTO importDto : importData) {
                if (!importDto.isValid()) {
                    // 记录错误
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", importDto.getRowNumber());
                    error.put("studentNo", importDto.getStudentNo());
                    error.put("name", importDto.getName());
                    error.put("error", importDto.getErrorMessage());
                    errorList.add(error);
                    continue;
                }

                try {
                    // 查找专业
                    Major major = studentService.findMajorByCode(importDto.getMajorCode());

                    // 检查学号是否已存在
                    if (studentService.existsByStudentNo(importDto.getStudentNo())) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", importDto.getRowNumber());
                        error.put("studentNo", importDto.getStudentNo());
                        error.put("name", importDto.getName());
                        error.put("error", "学号已存在");
                        errorList.add(error);
                        continue;
                    }

                    // 创建用户
                    User user = new User();
                    user.setUsername(importDto.getStudentNo());
                    user.setPassword("123456"); // 默认密码

                    // 创建学生
                    Student student = Student.builder()
                            .studentNo(importDto.getStudentNo())
                            .name(importDto.getName())
                            .gender(Student.Gender.valueOf(importDto.getGender()))
                            .birthDate(importDto.getBirthDate())
                            .enrollmentYear(importDto.getEnrollmentYear())
                            .major(major)
                            .className(importDto.getClassName())
                            .phone(importDto.getPhone())
                            .user(user)
                            .build();

                    Student createdStudent = studentService.createStudent(student);
                    successList.add(createdStudent);
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", importDto.getRowNumber());
                    error.put("studentNo", importDto.getStudentNo());
                    error.put("name", importDto.getName());
                    error.put("error", e.getMessage());
                    errorList.add(error);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", importData.size());
            result.put("successCount", successList.size());
            result.put("errorCount", errorList.size());
            result.put("errors", errorList);

            return Result.success("导入完成", result);
        } catch (IOException e) {
            log.error("导入学生数据失败", e);
            return Result.error("文件读取失败");
        }
    }

    /**
     * 导出学生数据（Excel）
     */
    @GetMapping("/admin/students/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Integer enrollmentYear) {
        log.info("导出学生数据: keyword={}, majorId={}, className={}, enrollmentYear={}", 
                keyword, majorId, className, enrollmentYear);

        List<Student> students;
        if ((keyword != null && !keyword.trim().isEmpty()) || majorId != null || 
            (className != null && !className.trim().isEmpty()) || enrollmentYear != null) {
            String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : "";
            students = studentService.searchStudents(searchKeyword, majorId, className, enrollmentYear, Pageable.unpaged()).getContent();
        } else {
            students = studentService.findAll();
        }

        List<StudentDTO> studentDTOList = students.stream()
                .map(dtoConverter::toStudentDTO)
                .collect(Collectors.toList());

        byte[] excelBytes = excelUtil.exportStudents(studentDTOList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "students.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * 教师端查询学生列表（用于选择学生）
     */
    @GetMapping("/teacher/students")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Map<String, Object>> getStudentListForTeacher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        log.info("教师查询学生列表: page={}, size={}, keyword={}", page, size, keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        Page<Student> studentPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            studentPage = studentService.searchStudents(keyword, null, null, null, pageable);
        } else {
            studentPage = studentService.findAll(pageable);
        }

        List<StudentDTO> studentDTOList = studentPage.getContent().stream()
                .map(dtoConverter::toStudentDTO)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", studentDTOList);
        result.put("totalElements", studentPage.getTotalElements());
        result.put("totalPages", studentPage.getTotalPages());
        result.put("currentPage", studentPage.getNumber());
        result.put("pageSize", studentPage.getSize());

        return Result.success(result);
    }
}

