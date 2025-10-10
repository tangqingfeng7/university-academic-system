package com.university.academic.service;

import com.university.academic.entity.Student;
import com.university.academic.entity.StudentStatus;
import com.university.academic.entity.StudentStatusChange;

/**
 * 学生状态更新Service接口
 *
 * @author Academic System Team
 */
public interface StudentStatusUpdateService {

    /**
     * 根据异动申请更新学生状态
     *
     * @param statusChange 异动申请
     * @return 更新后的学生
     */
    Student updateStudentStatus(StudentStatusChange statusChange);

    /**
     * 更新学生状态
     *
     * @param studentId 学生ID
     * @param status    新状态
     * @return 更新后的学生
     */
    Student updateStatus(Long studentId, StudentStatus status);

    /**
     * 检查学生是否可以选课
     *
     * @param studentId 学生ID
     * @return 是否可以选课
     */
    boolean canSelectCourse(Long studentId);

    /**
     * 验证学生状态（选课前检查）
     *
     * @param studentId 学生ID
     * @throws com.university.academic.exception.BusinessException 如果学生状态不允许选课
     */
    void validateStudentStatusForSelection(Long studentId);
}

