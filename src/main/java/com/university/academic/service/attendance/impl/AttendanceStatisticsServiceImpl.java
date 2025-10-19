package com.university.academic.service.attendance.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.university.academic.entity.attendance.AttendanceRecord;
import com.university.academic.entity.attendance.AttendanceStatistics;
import com.university.academic.entity.attendance.RecordStatus;
import com.university.academic.repository.attendance.AttendanceRecordRepository;
import com.university.academic.repository.attendance.AttendanceStatisticsRepository;
import com.university.academic.service.attendance.AttendanceStatisticsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 考勤统计服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceStatisticsServiceImpl implements AttendanceStatisticsService {

    private final AttendanceStatisticsRepository statisticsRepository;
    private final AttendanceRecordRepository recordRepository;

    /**
     * 统计数据缓存
     */
    private Cache<String, Map<String, Object>> statisticsCache;

    @PostConstruct
    public void init() {
        // 初始化统计缓存，30分钟过期
        statisticsCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(500)
                .build();

        log.info("考勤统计缓存初始化完成，过期时间: 30分钟");
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "courseStatistics", key = "#offeringId")
    public Map<String, Object> getCourseStatistics(Long offeringId) {
        log.info("获取课程考勤统计: offeringId={}", offeringId);

        // 尝试从缓存获取
        String cacheKey = "course_" + offeringId;
        Map<String, Object> cached = statisticsCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取课程统计: offeringId={}", offeringId);
            return cached;
        }

        // 获取所有学生的统计数据
        List<AttendanceStatistics> statsList = statisticsRepository.findByOfferingIdWithStudent(offeringId);

        if (statsList.isEmpty()) {
            return createEmptyStatistics();
        }

        // 聚合统计
        int totalStudents = statsList.size();
        int totalClasses = statsList.stream()
                .mapToInt(AttendanceStatistics::getTotalClasses)
                .max()
                .orElse(0);

        long totalPresent = statsList.stream().mapToInt(AttendanceStatistics::getPresentCount).sum();
        long totalLate = statsList.stream().mapToInt(AttendanceStatistics::getLateCount).sum();
        long totalEarlyLeave = statsList.stream().mapToInt(AttendanceStatistics::getEarlyLeaveCount).sum();
        long totalLeave = statsList.stream().mapToInt(AttendanceStatistics::getLeaveCount).sum();
        long totalAbsent = statsList.stream().mapToInt(AttendanceStatistics::getAbsentCount).sum();

        double avgAttendanceRate = statsList.stream()
                .mapToDouble(s -> s.getAttendanceRate() != null ? s.getAttendanceRate() : 0.0)
                .average()
                .orElse(0.0);

        // 计算各项比率
        long totalRecords = totalStudents * totalClasses;
        double presentRate = totalRecords > 0 ? (totalPresent * 100.0 / totalRecords) : 0.0;
        double lateRate = totalRecords > 0 ? (totalLate * 100.0 / totalRecords) : 0.0;
        double absentRate = totalRecords > 0 ? (totalAbsent * 100.0 / totalRecords) : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("offeringId", offeringId);
        result.put("totalStudents", totalStudents);
        result.put("totalClasses", totalClasses);
        result.put("avgAttendanceRate", Math.round(avgAttendanceRate * 100.0) / 100.0);
        result.put("presentRate", Math.round(presentRate * 100.0) / 100.0);
        result.put("lateRate", Math.round(lateRate * 100.0) / 100.0);
        result.put("absentRate", Math.round(absentRate * 100.0) / 100.0);
        result.put("totalPresent", totalPresent);
        result.put("totalLate", totalLate);
        result.put("totalEarlyLeave", totalEarlyLeave);
        result.put("totalLeave", totalLeave);
        result.put("totalAbsent", totalAbsent);

        // 缓存结果
        statisticsCache.put(cacheKey, result);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentStatistics(Long studentId, Long offeringId) {
        log.info("获取学生考勤统计: studentId={}, offeringId={}", studentId, offeringId);

        AttendanceStatistics stats = statisticsRepository
                .findByStudentIdAndOfferingIdWithDetails(studentId, offeringId)
                .orElse(null);

        if (stats == null) {
            return createEmptyStudentStatistics(studentId, offeringId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("offeringId", offeringId);
        result.put("totalClasses", stats.getTotalClasses());
        result.put("presentCount", stats.getPresentCount());
        result.put("lateCount", stats.getLateCount());
        result.put("earlyLeaveCount", stats.getEarlyLeaveCount());
        result.put("leaveCount", stats.getLeaveCount());
        result.put("absentCount", stats.getAbsentCount());
        result.put("attendanceRate", stats.getAttendanceRate());
        result.put("lastUpdated", stats.getLastUpdated());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentSemesterStatistics(Long studentId, Long semesterId) {
        log.info("获取学生学期考勤统计: studentId={}, semesterId={}", studentId, semesterId);

        List<AttendanceStatistics> statsList = statisticsRepository
                .findByStudentIdAndSemesterId(studentId, semesterId);

        return statsList.stream().map(stats -> {
            Map<String, Object> map = new HashMap<>();
            map.put("offeringId", stats.getOffering().getId());
            map.put("courseName", stats.getOffering().getCourse().getName());
            map.put("courseNo", stats.getOffering().getCourse().getCourseNo());
            map.put("totalClasses", stats.getTotalClasses());
            map.put("presentCount", stats.getPresentCount());
            map.put("lateCount", stats.getLateCount());
            map.put("absentCount", stats.getAbsentCount());
            map.put("attendanceRate", stats.getAttendanceRate());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("获取院系考勤统计: departmentId={}, startDate={}, endDate={}", 
                departmentId, startDate, endDate);

        // 获取院系下所有教师的考勤记录
        List<AttendanceRecord> records = recordRepository
                .findByDepartmentIdAndDateRange(departmentId, startDate, endDate);

        if (records.isEmpty()) {
            return createEmptyDepartmentStatistics(departmentId);
        }

        // 统计汇总
        int totalRecords = records.size();
        int totalStudents = records.stream()
                .mapToInt(AttendanceRecord::getTotalStudents)
                .sum();
        int totalPresent = records.stream()
                .mapToInt(AttendanceRecord::getPresentCount)
                .sum();
        int totalLate = records.stream()
                .mapToInt(AttendanceRecord::getLateCount)
                .sum();
        int totalAbsent = records.stream()
                .mapToInt(AttendanceRecord::getAbsentCount)
                .sum();

        double avgAttendanceRate = records.stream()
                .filter(r -> r.getAttendanceRate() != null)
                .mapToDouble(AttendanceRecord::getAttendanceRate)
                .average()
                .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("departmentId", departmentId);
        result.put("totalRecords", totalRecords);
        result.put("totalStudents", totalStudents);
        result.put("totalPresent", totalPresent);
        result.put("totalLate", totalLate);
        result.put("totalAbsent", totalAbsent);
        result.put("avgAttendanceRate", Math.round(avgAttendanceRate * 100.0) / 100.0);
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherStatistics(Long teacherId) {
        log.info("获取教师考勤统计: teacherId={}", teacherId);

        List<AttendanceRecord> records = recordRepository.findByTeacherId(teacherId).stream()
                .filter(r -> r.getStatus() == RecordStatus.SUBMITTED)
                .toList();

        return calculateTeacherStats(teacherId, records);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherStatistics(Long teacherId, LocalDate startDate, LocalDate endDate) {
        log.info("获取教师考勤统计: teacherId={}, startDate={}, endDate={}", 
                teacherId, startDate, endDate);

        List<AttendanceRecord> records = recordRepository
                .findByTeacherIdAndDateRange(teacherId, startDate, endDate).stream()
                .filter(r -> r.getStatus() == RecordStatus.SUBMITTED)
                .toList();

        Map<String, Object> result = calculateTeacherStats(teacherId, records);
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAttendanceTrend(Long offeringId) {
        log.info("获取课程出勤率趋势: offeringId={}", offeringId);

        List<AttendanceRecord> records = recordRepository.findByOfferingIdWithDetails(offeringId).stream()
                .filter(r -> r.getStatus() == RecordStatus.SUBMITTED)
                .sorted(Comparator.comparing(AttendanceRecord::getAttendanceDate)
                        .thenComparing(AttendanceRecord::getAttendanceTime))
                .toList();

        return records.stream().map(record -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", record.getAttendanceDate());
            point.put("time", record.getAttendanceTime());
            point.put("attendanceRate", record.getAttendanceRate());
            point.put("presentCount", record.getPresentCount());
            point.put("totalStudents", record.getTotalStudents());
            return point;
        }).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "courseStatistics", key = "#offeringId")
    public void refreshStatisticsCache(Long offeringId) {
        log.info("刷新统计缓存: offeringId={}", offeringId);
        String cacheKey = "course_" + offeringId;
        statisticsCache.invalidate(cacheKey);
    }

    /**
     * 计算教师统计数据
     */
    private Map<String, Object> calculateTeacherStats(Long teacherId, List<AttendanceRecord> records) {
        int totalRecords = records.size();
        double avgAttendanceRate = records.stream()
                .filter(r -> r.getAttendanceRate() != null)
                .mapToDouble(AttendanceRecord::getAttendanceRate)
                .average()
                .orElse(0.0);

        int totalClasses = records.stream()
                .mapToInt(AttendanceRecord::getTotalStudents)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("teacherId", teacherId);
        result.put("totalRecords", totalRecords);
        result.put("totalClasses", totalClasses);
        result.put("avgAttendanceRate", Math.round(avgAttendanceRate * 100.0) / 100.0);

        return result;
    }

    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalStudents", 0);
        result.put("totalClasses", 0);
        result.put("avgAttendanceRate", 0.0);
        result.put("presentRate", 0.0);
        result.put("lateRate", 0.0);
        result.put("absentRate", 0.0);
        return result;
    }

    private Map<String, Object> createEmptyStudentStatistics(Long studentId, Long offeringId) {
        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("offeringId", offeringId);
        result.put("totalClasses", 0);
        result.put("presentCount", 0);
        result.put("lateCount", 0);
        result.put("absentCount", 0);
        result.put("attendanceRate", 0.0);
        return result;
    }

    private Map<String, Object> createEmptyDepartmentStatistics(Long departmentId) {
        Map<String, Object> result = new HashMap<>();
        result.put("departmentId", departmentId);
        result.put("totalRecords", 0);
        result.put("totalStudents", 0);
        result.put("avgAttendanceRate", 0.0);
        return result;
    }
}

