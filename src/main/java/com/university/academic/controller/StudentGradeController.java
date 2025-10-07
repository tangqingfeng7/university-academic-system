package com.university.academic.controller;

import com.university.academic.dto.GradeDTO;
import com.university.academic.entity.Grade;
import com.university.academic.entity.Student;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.GradeService;
import com.university.academic.service.StudentService;
import com.university.academic.util.DtoConverter;
import com.university.academic.util.PdfUtil;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生成绩查询控制器
 * 提供成绩查询接口（学生端）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/student/grades")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentGradeController {

    private final GradeService gradeService;
    private final DtoConverter dtoConverter;
    private final CustomUserDetailsService userDetailsService;
    private final StudentService studentService;
    private final PdfUtil pdfUtil;

    /**
     * 查询我的所有已公布成绩
     */
    @GetMapping
    public Result<List<GradeDTO>> getMyGrades(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询学生成绩: studentId={}", studentId);

        List<Grade> grades = gradeService.findPublishedByStudent(studentId);
        List<GradeDTO> gradeDTOList = grades.stream()
                .map(dtoConverter::toGradeDTO)
                .collect(Collectors.toList());

        return Result.success(gradeDTOList);
    }

    /**
     * 查询指定学期的成绩
     */
    @GetMapping("/semester/{semesterId}")
    public Result<List<GradeDTO>> getSemesterGrades(@PathVariable Long semesterId,
                                                     Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询学期成绩: studentId={}, semesterId={}", studentId, semesterId);

        List<Grade> grades = gradeService.findPublishedByStudentAndSemester(
                studentId, semesterId);
        List<GradeDTO> gradeDTOList = grades.stream()
                .map(dtoConverter::toGradeDTO)
                .collect(Collectors.toList());

        return Result.success(gradeDTOList);
    }

    /**
     * 获取学期成绩统计（包含平均绩点）
     */
    @GetMapping("/semester/{semesterId}/statistics")
    public Result<Map<String, Object>> getSemesterStatistics(
            @PathVariable Long semesterId,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询学期成绩统计: studentId={}, semesterId={}", studentId, semesterId);

        List<Grade> grades = gradeService.findPublishedByStudentAndSemester(
                studentId, semesterId);
        BigDecimal gpa = gradeService.calculateGPA(studentId, semesterId);

        int totalCredits = 0;
        int passedCredits = 0;
        int passedCourses = 0;
        int totalCourses = grades.size();

        for (Grade grade : grades) {
            int credits = grade.getCourseSelection().getOffering()
                    .getCourse().getCredits();
            totalCredits += credits;
            
            if (grade.isPassed()) {
                passedCredits += credits;
                passedCourses++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("semesterId", semesterId);
        statistics.put("totalCourses", totalCourses);
        statistics.put("passedCourses", passedCourses);
        statistics.put("totalCredits", totalCredits);
        statistics.put("passedCredits", passedCredits);
        statistics.put("gpa", gpa);

        return Result.success(statistics);
    }

    /**
     * 获取总成绩单统计
     */
    @GetMapping("/transcript/statistics")
    public Result<Map<String, Object>> getTranscriptStatistics(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询总成绩单统计: studentId={}", studentId);

        List<Grade> grades = gradeService.findPublishedByStudent(studentId);
        BigDecimal overallGPA = gradeService.calculateOverallGPA(studentId);

        int totalCredits = 0;
        int passedCredits = 0;
        int passedCourses = 0;
        int totalCourses = grades.size();

        for (Grade grade : grades) {
            int credits = grade.getCourseSelection().getOffering()
                    .getCourse().getCredits();
            totalCredits += credits;
            
            if (grade.isPassed()) {
                passedCredits += credits;
                passedCourses++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCourses", totalCourses);
        statistics.put("passedCourses", passedCourses);
        statistics.put("totalCredits", totalCredits);
        statistics.put("passedCredits", passedCredits);
        statistics.put("overallGPA", overallGPA);

        return Result.success(statistics);
    }

    /**
     * 导出成绩单（PDF格式）
     */
    @GetMapping("/transcript/export")
    public ResponseEntity<byte[]> exportTranscript(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("导出成绩单PDF: studentId={}", studentId);

        // 获取学生信息
        Student student = studentService.findById(studentId);
        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("studentNo", student.getStudentNo());
        studentInfo.put("name", student.getName());
        studentInfo.put("gender", "MALE".equals(student.getGender().toString()) ? "男" : "女");
        studentInfo.put("enrollmentYear", student.getEnrollmentYear());
        studentInfo.put("majorName", student.getMajor().getName());
        studentInfo.put("className", student.getClassName() != null ? student.getClassName() : "");

        // 获取所有已公布成绩
        List<Grade> grades = gradeService.findPublishedByStudent(studentId);
        List<GradeDTO> gradeDTOList = grades.stream()
                .map(dtoConverter::toGradeDTO)
                .collect(Collectors.toList());

        // 计算统计信息
        BigDecimal overallGPA = gradeService.calculateOverallGPA(studentId);
        int totalCredits = 0;
        int passedCredits = 0;
        int passedCourses = 0;
        int totalCourses = grades.size();

        for (Grade grade : grades) {
            int credits = grade.getCourseSelection().getOffering()
                    .getCourse().getCredits();
            totalCredits += credits;
            
            if (grade.isPassed()) {
                passedCredits += credits;
                passedCourses++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCourses", totalCourses);
        statistics.put("passedCourses", passedCourses);
        statistics.put("totalCredits", totalCredits);
        statistics.put("passedCredits", passedCredits);
        statistics.put("overallGPA", overallGPA);

        // 生成PDF
        byte[] pdfBytes = pdfUtil.generateTranscriptPdf(studentInfo, gradeDTOList, statistics);

        // 设置响应头
        String filename = "成绩单_" + student.getStudentNo() + "_" + student.getName() + ".pdf";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}

