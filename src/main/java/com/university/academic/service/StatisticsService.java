package com.university.academic.service;

import com.university.academic.dto.*;
import com.university.academic.entity.*;
import com.university.academic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务类
 * 提供各种数据统计功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseOfferingRepository offeringRepository;
    private final GradeRepository gradeRepository;
    private final TeacherRepository teacherRepository;

    /**
     * 获取学生统计数据
     */
    @Transactional(readOnly = true)
    public StudentStatisticsDTO getStudentStatistics() {
        log.info("获取学生统计数据");

        List<Student> students = studentRepository.findAll().stream()
                .filter(s -> !s.getDeleted())
                .collect(Collectors.toList());

        // 总数
        long totalStudents = students.size();

        // 按专业分布
        Map<String, Long> byMajor = students.stream()
                .filter(s -> s.getMajor() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getMajor().getName(),
                        Collectors.counting()
                ));

        // 按入学年份分布
        Map<Integer, Long> byEnrollmentYear = students.stream()
                .collect(Collectors.groupingBy(
                        Student::getEnrollmentYear,
                        Collectors.counting()
                ));

        // 按性别分布
        Map<String, Long> byGender = students.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getGender().name(),
                        Collectors.counting()
                ));

        // 按院系分布
        Map<String, Long> byDepartment = students.stream()
                .filter(s -> s.getMajor() != null && s.getMajor().getDepartment() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getMajor().getDepartment().getName(),
                        Collectors.counting()
                ));

        return StudentStatisticsDTO.builder()
                .totalStudents(totalStudents)
                .byMajor(byMajor)
                .byEnrollmentYear(byEnrollmentYear)
                .byGender(byGender)
                .byDepartment(byDepartment)
                .build();
    }

    /**
     * 获取课程统计数据
     */
    @Transactional(readOnly = true)
    public CourseStatisticsDTO getCourseStatistics(Long semesterId) {
        log.info("获取课程统计数据: semesterId={}", semesterId);

        long totalCourses = courseRepository.count();

        List<CourseOffering> offerings;
        if (semesterId != null) {
            offerings = offeringRepository.findBySemesterId(semesterId);
        } else {
            offerings = offeringRepository.findAll();
        }

        long totalOfferings = offerings.size();

        // 选课总人次
        long totalSelections = offerings.stream()
                .mapToLong(CourseOffering::getEnrolled)
                .sum();

        // 平均容量利用率
        double averageUtilization = offerings.stream()
                .filter(o -> o.getCapacity() > 0)
                .mapToDouble(o -> (double) o.getEnrolled() / o.getCapacity() * 100)
                .average()
                .orElse(0.0);

        // 各课程详细统计
        List<CourseStatisticsDTO.CourseOfferingStatDTO> courseDetails = offerings.stream()
                .map(offering -> {
                    double utilizationRate = offering.getCapacity() > 0 ?
                            (double) offering.getEnrolled() / offering.getCapacity() * 100 : 0.0;

                    return CourseStatisticsDTO.CourseOfferingStatDTO.builder()
                            .offeringId(offering.getId())
                            .courseName(offering.getCourse().getName())
                            .teacherName(offering.getTeacher().getName())
                            .capacity(offering.getCapacity())
                            .enrolled(offering.getEnrolled())
                            .utilizationRate(BigDecimal.valueOf(utilizationRate)
                                    .setScale(2, RoundingMode.HALF_UP).doubleValue())
                            .build();
                })
                .sorted(Comparator.comparing(CourseStatisticsDTO.CourseOfferingStatDTO::getUtilizationRate).reversed())
                .collect(Collectors.toList());

        return CourseStatisticsDTO.builder()
                .totalCourses(totalCourses)
                .totalOfferings(totalOfferings)
                .totalSelections(totalSelections)
                .averageCapacityUtilization(BigDecimal.valueOf(averageUtilization)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue())
                .courseDetails(courseDetails)
                .build();
    }

    /**
     * 获取成绩统计数据
     */
    @Transactional(readOnly = true)
    public GradeStatisticsDTO getGradeStatistics(Long semesterId) {
        log.info("获取成绩统计数据: semesterId={}", semesterId);

        List<Grade> grades;
        if (semesterId != null) {
            grades = gradeRepository.findBySemesterId(semesterId);
        } else {
            grades = gradeRepository.findAll();
        }

        long totalGrades = grades.size();

        // 已发布成绩数
        long publishedGrades = grades.stream()
                .filter(g -> g.getStatus() == Grade.GradeStatus.PUBLISHED)
                .count();

        // 全局统计（只统计已发布的成绩）
        List<Grade> publishedGradeList = grades.stream()
                .filter(g -> g.getStatus() == Grade.GradeStatus.PUBLISHED && g.getTotalScore() != null)
                .collect(Collectors.toList());

        GradeStatisticsDTO.GlobalGradeStats globalStats = calculateGlobalStats(publishedGradeList);

        // 按课程统计
        Map<Long, List<Grade>> gradesByCourse = publishedGradeList.stream()
                .collect(Collectors.groupingBy(g -> g.getCourseSelection().getOffering().getId()));

        List<GradeStatisticsDTO.CourseGradeStats> courseStats = gradesByCourse.entrySet().stream()
                .map(entry -> {
                    Long offeringId = entry.getKey();
                    List<Grade> courseGrades = entry.getValue();

                    CourseOffering offering = courseGrades.get(0).getCourseSelection().getOffering();

                    return calculateCourseStats(offeringId, offering, courseGrades);
                })
                .sorted(Comparator.comparing(GradeStatisticsDTO.CourseGradeStats::getAverageScore).reversed())
                .collect(Collectors.toList());

        return GradeStatisticsDTO.builder()
                .totalGrades(totalGrades)
                .publishedGrades(publishedGrades)
                .globalStats(globalStats)
                .courseStats(courseStats)
                .build();
    }

    /**
     * 获取教师工作量统计
     */
    @Transactional(readOnly = true)
    public List<TeacherWorkloadDTO> getTeacherWorkload(Long semesterId) {
        log.info("获取教师工作量统计: semesterId={}", semesterId);

        List<Teacher> teachers = teacherRepository.findAll();

        return teachers.stream()
                .map(teacher -> {
                    List<CourseOffering> offerings;
                    if (semesterId != null) {
                        offerings = offeringRepository.findByTeacherIdAndSemesterId(
                                teacher.getId(), semesterId);
                    } else {
                        offerings = offeringRepository.findByTeacherId(teacher.getId());
                    }

                    long offeringCount = offerings.size();

                    // 统计授课的不同课程数
                    long courseCount = offerings.stream()
                            .map(o -> o.getCourse().getId())
                            .distinct()
                            .count();

                    // 授课学生总人数
                    long totalStudents = offerings.stream()
                            .mapToLong(CourseOffering::getEnrolled)
                            .sum();

                    // 总学时
                    long totalHours = offerings.stream()
                            .mapToLong(o -> o.getCourse().getHours())
                            .sum();

                    // 平均每班人数
                    double avgStudents = offeringCount > 0 ?
                            (double) totalStudents / offeringCount : 0.0;

                    return TeacherWorkloadDTO.builder()
                            .teacherId(teacher.getId())
                            .teacherNo(teacher.getTeacherNo())
                            .teacherName(teacher.getName())
                            .departmentName(teacher.getDepartment() != null ?
                                    teacher.getDepartment().getName() : null)
                            .title(teacher.getTitle())
                            .courseCount(courseCount)
                            .offeringCount(offeringCount)
                            .totalStudents(totalStudents)
                            .totalHours(totalHours)
                            .averageStudentsPerClass(BigDecimal.valueOf(avgStudents)
                                    .setScale(2, RoundingMode.HALF_UP).doubleValue())
                            .build();
                })
                .sorted(Comparator.comparing(TeacherWorkloadDTO::getTotalHours).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 计算全局成绩统计
     */
    private GradeStatisticsDTO.GlobalGradeStats calculateGlobalStats(List<Grade> grades) {
        if (grades.isEmpty()) {
            return GradeStatisticsDTO.GlobalGradeStats.builder()
                    .averageScore(0.0)
                    .passRate(0.0)
                    .excellentRate(0.0)
                    .goodRate(0.0)
                    .build();
        }

        double avgScore = grades.stream()
                .mapToDouble(g -> g.getTotalScore().doubleValue())
                .average()
                .orElse(0.0);

        long passCount = grades.stream()
                .filter(g -> g.getTotalScore().compareTo(BigDecimal.valueOf(60)) >= 0)
                .count();

        long excellentCount = grades.stream()
                .filter(g -> g.getTotalScore().compareTo(BigDecimal.valueOf(90)) >= 0)
                .count();

        long goodCount = grades.stream()
                .filter(g -> g.getTotalScore().compareTo(BigDecimal.valueOf(80)) >= 0)
                .count();

        double passRate = (double) passCount / grades.size() * 100;
        double excellentRate = (double) excellentCount / grades.size() * 100;
        double goodRate = (double) goodCount / grades.size() * 100;

        return GradeStatisticsDTO.GlobalGradeStats.builder()
                .averageScore(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .passRate(BigDecimal.valueOf(passRate).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .excellentRate(BigDecimal.valueOf(excellentRate).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .goodRate(BigDecimal.valueOf(goodRate).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .build();
    }

    /**
     * 计算课程成绩统计
     */
    private GradeStatisticsDTO.CourseGradeStats calculateCourseStats(
            Long offeringId, CourseOffering offering, List<Grade> grades) {

        double avgScore = grades.stream()
                .mapToDouble(g -> g.getTotalScore().doubleValue())
                .average()
                .orElse(0.0);

        long passCount = grades.stream()
                .filter(g -> g.getTotalScore().compareTo(BigDecimal.valueOf(60)) >= 0)
                .count();

        long excellentCount = grades.stream()
                .filter(g -> g.getTotalScore().compareTo(BigDecimal.valueOf(90)) >= 0)
                .count();

        double passRate = (double) passCount / grades.size() * 100;
        double excellentRate = (double) excellentCount / grades.size() * 100;

        double highestScore = grades.stream()
                .mapToDouble(g -> g.getTotalScore().doubleValue())
                .max()
                .orElse(0.0);

        double lowestScore = grades.stream()
                .mapToDouble(g -> g.getTotalScore().doubleValue())
                .min()
                .orElse(0.0);

        return GradeStatisticsDTO.CourseGradeStats.builder()
                .courseOfferingId(offeringId)
                .courseName(offering.getCourse().getName())
                .teacherName(offering.getTeacher().getName())
                .studentCount((long) grades.size())
                .averageScore(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .passRate(BigDecimal.valueOf(passRate).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .excellentRate(BigDecimal.valueOf(excellentRate).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .build();
    }
}

