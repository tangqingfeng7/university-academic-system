package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价统计分析服务类
 * 提供综合统计、教学质量报告等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationStatisticsService {

    private final CourseEvaluationRepository courseEvaluationRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final SemesterService semesterService;
    private final CourseEvaluationService courseEvaluationService;
    private final TeacherEvaluationService teacherEvaluationService;

    /**
     * 获取课程评价统计（带缓存）
     *
     * @param offeringId 开课计划ID
     * @return 课程评价统计对象
     */
    @Cacheable(value = CacheConfig.CACHE_EVALUATION_STATS, key = "'course_stats_' + #offeringId")
    @Transactional(readOnly = true)
    public CourseEvaluationStatisticsDTO getCourseStatistics(Long offeringId) {
        CourseEvaluationService.EvaluationStatistics stats = 
            courseEvaluationService.calculateCourseStatistics(offeringId);

        CourseOffering offering = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        return CourseEvaluationStatisticsDTO.builder()
                .offeringId(offeringId)
                .courseName(offering.getCourse().getName())
                .courseNo(offering.getCourse().getCourseNo())
                .teacherName(offering.getTeacher().getName())
                .semesterName(offering.getSemester().getSemesterName())
                .averageRating(stats.getAverageRating())
                .totalEvaluations(stats.getTotalEvaluations())
                .totalStudents(stats.getTotalStudents())
                .participationRate(stats.getParticipationRate())
                .count1Star(stats.getCount1Star())
                .count2Star(stats.getCount2Star())
                .count3Star(stats.getCount3Star())
                .count4Star(stats.getCount4Star())
                .count5Star(stats.getCount5Star())
                .build();
    }

    /**
     * 获取教师评价统计（带缓存）
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID（可选）
     * @return 教师评价统计对象
     */
    @Cacheable(value = CacheConfig.CACHE_EVALUATION_STATS, 
               key = "'teacher_stats_' + #teacherId + '_' + (#semesterId != null ? #semesterId : 'all')")
    @Transactional(readOnly = true)
    public TeacherEvaluationStatisticsDTO getTeacherStatistics(Long teacherId, Long semesterId) {
        TeacherEvaluationService.TeacherEvaluationStatistics stats = 
            teacherEvaluationService.calculateTeacherStatistics(teacherId, semesterId);

        return TeacherEvaluationStatisticsDTO.builder()
                .teacherId(teacherId)
                .averageTeachingRating(stats.getAverageTeachingRating())
                .averageAttitudeRating(stats.getAverageAttitudeRating())
                .averageContentRating(stats.getAverageContentRating())
                .overallRating(stats.getOverallRating())
                .totalEvaluations(stats.getTotalEvaluations())
                .build();
    }

    /**
     * 生成学期教学质量报告
     *
     * @param semesterId 学期ID
     * @return 教学质量报告对象
     */
    @Cacheable(value = CacheConfig.CACHE_EVALUATION_STATS, key = "'quality_report_' + #semesterId")
    @Transactional(readOnly = true)
    public TeachingQualityReportDTO generateQualityReport(Long semesterId) {
        // 验证学期存在
        Semester semester = semesterService.findById(semesterId);

        log.info("生成教学质量报告: semesterId={}", semesterId);

        // 1. 统计课程评价数据
        List<CourseOffering> offerings = courseOfferingRepository.findBySemesterId(semesterId);
        List<CourseEvaluationStatisticsDTO> courseStatsList = new ArrayList<>();
        
        double totalCourseRating = 0.0;
        long totalCourseEvaluations = 0;
        int courseCount = 0;

        for (CourseOffering offering : offerings) {
            CourseEvaluationService.EvaluationStatistics stats = 
                courseEvaluationService.calculateCourseStatistics(offering.getId());
            
            if (stats.getTotalEvaluations() > 0) {
                CourseEvaluationStatisticsDTO dto = CourseEvaluationStatisticsDTO.builder()
                        .offeringId(offering.getId())
                        .courseName(offering.getCourse().getName())
                        .courseNo(offering.getCourse().getCourseNo())
                        .teacherName(offering.getTeacher().getName())
                        .semesterName(semester.getSemesterName())
                        .averageRating(stats.getAverageRating())
                        .totalEvaluations(stats.getTotalEvaluations())
                        .totalStudents(stats.getTotalStudents())
                        .participationRate(stats.getParticipationRate())
                        .count1Star(stats.getCount1Star())
                        .count2Star(stats.getCount2Star())
                        .count3Star(stats.getCount3Star())
                        .count4Star(stats.getCount4Star())
                        .count5Star(stats.getCount5Star())
                        .build();
                
                courseStatsList.add(dto);
                totalCourseRating += stats.getAverageRating() * stats.getTotalEvaluations();
                totalCourseEvaluations += stats.getTotalEvaluations();
                courseCount++;
            }
        }

        // 2. 统计教师评价数据
        Map<Long, TeacherEvaluationStatisticsDTO> teacherStatsMap = new HashMap<>();
        double totalTeacherRating = 0.0;
        long totalTeacherEvaluations = 0;

        for (CourseOffering offering : offerings) {
            Long teacherId = offering.getTeacher().getId();
            if (!teacherStatsMap.containsKey(teacherId)) {
                TeacherEvaluationService.TeacherEvaluationStatistics stats = 
                    teacherEvaluationService.calculateTeacherStatistics(teacherId, semesterId);
                
                if (stats.getTotalEvaluations() > 0) {
                    TeacherEvaluationStatisticsDTO dto = TeacherEvaluationStatisticsDTO.builder()
                            .teacherId(teacherId)
                            .averageTeachingRating(stats.getAverageTeachingRating())
                            .averageAttitudeRating(stats.getAverageAttitudeRating())
                            .averageContentRating(stats.getAverageContentRating())
                            .overallRating(stats.getOverallRating())
                            .totalEvaluations(stats.getTotalEvaluations())
                            .build();
                    
                    teacherStatsMap.put(teacherId, dto);
                    totalTeacherRating += stats.getOverallRating() * stats.getTotalEvaluations();
                    totalTeacherEvaluations += stats.getTotalEvaluations();
                }
            }
        }

        // 3. 计算总体统计
        double avgCourseRating = totalCourseEvaluations > 0 ? 
            totalCourseRating / totalCourseEvaluations : 0.0;
        double avgTeacherRating = totalTeacherEvaluations > 0 ? 
            totalTeacherRating / totalTeacherEvaluations : 0.0;

        // 4. 计算参与率
        long totalStudentCount = offerings.stream()
                .mapToLong(CourseOffering::getEnrolled)
                .sum();
        double overallParticipationRate = totalStudentCount > 0 ? 
            (double) totalCourseEvaluations / totalStudentCount * 100 : 0.0;

        // 5. 找出优秀课程（评分>=4.5且参与率>=50%）
        List<CourseEvaluationStatisticsDTO> excellentCourses = courseStatsList.stream()
                .filter(c -> c.getAverageRating() >= 4.5 && c.getParticipationRate() >= 50.0)
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .limit(10)
                .toList();

        // 6. 找出需改进课程（评分<3.0）
        List<CourseEvaluationStatisticsDTO> coursesNeedImprovement = courseStatsList.stream()
                .filter(c -> c.getAverageRating() < 3.0)
                .sorted((a, b) -> Double.compare(a.getAverageRating(), b.getAverageRating()))
                .toList();

        // 7. 找出优秀教师（综合评分>=4.5）
        List<TeacherEvaluationStatisticsDTO> excellentTeachers = new ArrayList<>(teacherStatsMap.values()).stream()
                .filter(t -> t.getOverallRating() >= 4.5)
                .sorted((a, b) -> Double.compare(b.getOverallRating(), a.getOverallRating()))
                .limit(10)
                .toList();

        // 8. 构建报告
        TeachingQualityReportDTO report = TeachingQualityReportDTO.builder()
                .semesterId(semesterId)
                .semesterName(semester.getSemesterName())
                .totalCourses(courseCount)
                .totalTeachers(teacherStatsMap.size())
                .totalEvaluations(totalCourseEvaluations)
                .totalStudents(totalStudentCount)
                .overallParticipationRate(overallParticipationRate)
                .averageCourseRating(avgCourseRating)
                .averageTeacherRating(avgTeacherRating)
                .courseStatistics(courseStatsList)
                .teacherStatistics(new ArrayList<>(teacherStatsMap.values()))
                .excellentCourses(excellentCourses)
                .coursesNeedImprovement(coursesNeedImprovement)
                .excellentTeachers(excellentTeachers)
                .build();

        log.info("教学质量报告生成完成: semesterId={}, totalCourses={}, avgRating={}", 
                semesterId, courseCount, avgCourseRating);

        return report;
    }

    /**
     * 获取学期评价参与率统计
     *
     * @param semesterId 学期ID
     * @return 参与率统计对象
     */
    @Cacheable(value = CacheConfig.CACHE_EVALUATION_STATS, key = "'participation_' + #semesterId")
    @Transactional(readOnly = true)
    public ParticipationStatisticsDTO getParticipationStatistics(Long semesterId) {
        Semester semester = semesterService.findById(semesterId);
        
        List<CourseOffering> offerings = courseOfferingRepository.findBySemesterId(semesterId);
        
        long totalStudents = 0;
        long totalEvaluated = 0;
        int highParticipationCount = 0; // 参与率>=80%
        int mediumParticipationCount = 0; // 参与率50-80%
        int lowParticipationCount = 0; // 参与率<50%

        for (CourseOffering offering : offerings) {
            long studentCount = offering.getEnrolled();
            long evaluationCount = courseEvaluationRepository.countByCourseOfferingIdAndStatus(
                    offering.getId(), EvaluationStatus.SUBMITTED);
            
            totalStudents += studentCount;
            totalEvaluated += evaluationCount;
            
            if (studentCount > 0) {
                double rate = (double) evaluationCount / studentCount * 100;
                if (rate >= 80) {
                    highParticipationCount++;
                } else if (rate >= 50) {
                    mediumParticipationCount++;
                } else {
                    lowParticipationCount++;
                }
            }
        }

        double overallRate = totalStudents > 0 ? (double) totalEvaluated / totalStudents * 100 : 0.0;

        return ParticipationStatisticsDTO.builder()
                .semesterId(semesterId)
                .semesterName(semester.getSemesterName())
                .totalStudents(totalStudents)
                .totalEvaluated(totalEvaluated)
                .participationRate(overallRate)
                .highParticipationCourses(highParticipationCount)
                .mediumParticipationCourses(mediumParticipationCount)
                .lowParticipationCourses(lowParticipationCount)
                .build();
    }

    /**
     * 课程评价统计DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CourseEvaluationStatisticsDTO {
        private Long offeringId;
        private String courseName;
        private String courseNo;
        private String teacherName;
        private String semesterName;
        private Double averageRating;
        private Long totalEvaluations;
        private Long totalStudents;
        private Double participationRate;
        private Long count1Star;
        private Long count2Star;
        private Long count3Star;
        private Long count4Star;
        private Long count5Star;
    }

    /**
     * 教师评价统计DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TeacherEvaluationStatisticsDTO {
        private Long teacherId;
        private Double averageTeachingRating;
        private Double averageAttitudeRating;
        private Double averageContentRating;
        private Double overallRating;
        private Long totalEvaluations;
    }

    /**
     * 教学质量报告DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TeachingQualityReportDTO {
        private Long semesterId;
        private String semesterName;
        private Integer totalCourses;
        private Integer totalTeachers;
        private Long totalEvaluations;
        private Long totalStudents;
        private Double overallParticipationRate;
        private Double averageCourseRating;
        private Double averageTeacherRating;
        private List<CourseEvaluationStatisticsDTO> courseStatistics;
        private List<TeacherEvaluationStatisticsDTO> teacherStatistics;
        private List<CourseEvaluationStatisticsDTO> excellentCourses;
        private List<CourseEvaluationStatisticsDTO> coursesNeedImprovement;
        private List<TeacherEvaluationStatisticsDTO> excellentTeachers;
    }

    /**
     * 参与率统计DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParticipationStatisticsDTO {
        private Long semesterId;
        private String semesterName;
        private Long totalStudents;
        private Long totalEvaluated;
        private Double participationRate;
        private Integer highParticipationCourses;
        private Integer mediumParticipationCourses;
        private Integer lowParticipationCourses;
    }
}

