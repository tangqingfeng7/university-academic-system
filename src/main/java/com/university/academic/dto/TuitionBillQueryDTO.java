package com.university.academic.dto;

import com.university.academic.entity.tuition.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学费账单查询DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionBillQueryDTO {

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 账单状态
     */
    private BillStatus status;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 院系ID
     */
    private Long departmentId;
}

