package com.university.academic.dto.attendance;

import com.university.academic.entity.attendance.AttendanceDetail;
import com.university.academic.entity.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤明细DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDetailDTO {
    private Long id;
    private Long recordId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String className;
    private AttendanceStatus status;
    private LocalDateTime checkinTime;
    private Double checkinLatitude;
    private Double checkinLongitude;
    private Boolean isMakeup;
    private String remarks;
    private Long modifiedBy;
    private String modifyReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 考勤记录相关信息（用于学生端显示，避免嵌套对象的懒加载问题）
    private AttendanceRecordDTO attendanceRecord;

    /**
     * 从实体转换为DTO
     */
    public static AttendanceDetailDTO fromEntity(AttendanceDetail detail) {
        if (detail == null) {
            return null;
        }
        
        // 安全地获取recordId和嵌套的考勤记录DTO
        Long recordId = null;
        AttendanceRecordDTO recordDTO = null;
        
        try {
            if (detail.getAttendanceRecord() != null) {
                recordId = detail.getAttendanceRecord().getId();
                // 只有在已经预加载的情况下才转换完整的考勤记录
                recordDTO = AttendanceRecordDTO.fromEntity(detail.getAttendanceRecord());
            }
        } catch (Exception e) {
            // 如果发生懒加载异常，只设置recordId，recordDTO保持为null
            // 这样前端至少可以使用recordId，而不会完全失败
            try {
                recordId = detail.getAttendanceRecord().getId();
            } catch (Exception ex) {
                // 如果连ID都无法获取，保持为null
            }
        }
        
        // 安全地获取学生信息
        Long studentId = null;
        String studentNo = null;
        String studentName = null;
        String className = null;
        
        try {
            if (detail.getStudent() != null) {
                studentId = detail.getStudent().getId();
                studentNo = detail.getStudent().getStudentNo();
                studentName = detail.getStudent().getName();
                className = detail.getStudent().getClassName();
            }
        } catch (Exception e) {
            // 如果发生懒加载异常，忽略
        }

        return AttendanceDetailDTO.builder()
                .id(detail.getId())
                .recordId(recordId)
                .studentId(studentId)
                .studentNo(studentNo)
                .studentName(studentName)
                .className(className)
                .status(detail.getStatus())
                .checkinTime(detail.getCheckinTime())
                .checkinLatitude(detail.getCheckinLatitude())
                .checkinLongitude(detail.getCheckinLongitude())
                .isMakeup(detail.getIsMakeup())
                .remarks(detail.getRemarks())
                .modifiedBy(detail.getModifiedBy())
                .modifyReason(detail.getModifyReason())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .attendanceRecord(recordDTO)
                .build();
    }
}

