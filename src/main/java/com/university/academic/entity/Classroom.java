package com.university.academic.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教室实体类
 */
@Entity
@Table(name = "classroom", indexes = {
    @Index(name = "idx_classroom_building", columnList = "building"),
    @Index(name = "idx_classroom_status", columnList = "status")
})
@Data
public class Classroom {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 教室编号
     */
    @Column(name = "room_no", nullable = false, unique = true, length = 20)
    private String roomNo;
    
    /**
     * 所在楼栋
     */
    @Column(name = "building", nullable = false, length = 50)
    private String building;
    
    /**
     * 容量（人数）
     */
    @Column(nullable = false)
    private Integer capacity;
    
    /**
     * 教室类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassroomType type;
    
    /**
     * 设备信息（JSON格式）
     * 例如：{"projector": true, "computer": true, "microphone": true, "whiteboard": true}
     */
    @Column(columnDefinition = "TEXT")
    private String equipment;
    
    /**
     * 教室状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassroomStatus status;
    
    /**
     * 是否删除（软删除）
     */
    @Column(nullable = false)
    private Boolean deleted = false;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 创建前自动设置时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deleted == null) {
            deleted = false;
        }
        if (status == null) {
            status = ClassroomStatus.AVAILABLE;
        }
    }
    
    /**
     * 更新前自动设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

