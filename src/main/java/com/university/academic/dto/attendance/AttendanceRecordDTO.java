package com.university.academic.dto.attendance;

import com.university.academic.entity.attendance.AttendanceMethod;
import com.university.academic.entity.attendance.AttendanceRecord;
import com.university.academic.entity.attendance.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 考勤记录DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDTO {
    private Long id;
    private Long offeringId;
    private String courseName;
    private String courseNo;
    private Long teacherId;
    private String teacherName;
    private LocalDate attendanceDate;
    private LocalTime attendanceTime;
    private AttendanceMethod method;
    private RecordStatus status;
    private Integer totalStudents;
    private Integer presentCount;
    private Integer absentCount;
    private Integer lateCount;
    private Integer leaveCount;
    private Double attendanceRate;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为DTO
     */
    public static AttendanceRecordDTO fromEntity(AttendanceRecord record) {
        if (record == null) {
            return null;
        }
        
        // 安全地获取关联对象信息，避免懒加载异常
        Long offeringId = null;
        String courseName = null;
        String courseNo = null;
        Long teacherId = null;
        String teacherName = null;
        
        try {
            if (record.getOffering() != null) {
                offeringId = record.getOffering().getId();
                if (record.getOffering().getCourse() != null) {
                    courseName = record.getOffering().getCourse().getName();
                    courseNo = record.getOffering().getCourse().getCourseNo();
                }
            }
        } catch (Exception e) {
            // 懒加载异常，忽略
        }
        
        try {
            if (record.getTeacher() != null) {
                teacherId = record.getTeacher().getId();
                teacherName = record.getTeacher().getName();
            }
        } catch (Exception e) {
            // 懒加载异常，忽略
        }

        return AttendanceRecordDTO.builder()
                .id(record.getId())
                .offeringId(offeringId)
                .courseName(courseName)
                .courseNo(courseNo)
                .teacherId(teacherId)
                .teacherName(teacherName)
                .attendanceDate(record.getAttendanceDate())
                .attendanceTime(record.getAttendanceTime())
                .method(record.getMethod())
                .status(record.getStatus())
                .totalStudents(record.getTotalStudents())
                .presentCount(record.getPresentCount())
                .absentCount(record.getAbsentCount())
                .lateCount(record.getLateCount())
                .leaveCount(record.getLeaveCount())
                .attendanceRate(record.getAttendanceRate())
                .remarks(record.getRemarks())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}

