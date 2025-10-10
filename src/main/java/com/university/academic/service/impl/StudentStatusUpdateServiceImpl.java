package com.university.academic.service.impl;

import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.MajorRepository;
import com.university.academic.repository.StudentRepository;
import com.university.academic.service.StudentService;
import com.university.academic.service.StudentStatusUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学生状态更新Service实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentStatusUpdateServiceImpl implements StudentStatusUpdateService {

    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final MajorRepository majorRepository;

    @Override
    @Transactional
    public Student updateStudentStatus(StudentStatusChange statusChange) {
        log.info("根据异动申请更新学生状态: 申请ID={}, 类型={}",
                statusChange.getId(), statusChange.getType());

        Student student = statusChange.getStudent();

        switch (statusChange.getType()) {
            case SUSPENSION -> {
                // 休学：更新状态为休学
                student.setStatus(StudentStatus.SUSPENDED);
                log.info("学生状态更新为休学: 学生={}, 期限={} 至 {}",
                        student.getName(),
                        statusChange.getStartDate(),
                        statusChange.getEndDate());
            }
            case RESUMPTION -> {
                // 复学：恢复状态为在读
                student.setStatus(StudentStatus.ACTIVE);
                log.info("学生状态更新为在读: 学生={}, 复学日期={}",
                        student.getName(),
                        statusChange.getStartDate());
            }
            case TRANSFER -> {
                // 转专业：更新专业信息（状态保持不变）
                if (statusChange.getTargetMajorId() != null) {
                    Major targetMajor = majorRepository.findById(statusChange.getTargetMajorId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_MAJOR_NOT_FOUND));

                    Major oldMajor = student.getMajor();
                    student.setMajor(targetMajor);

                    log.info("学生专业更新: 学生={}, 原专业={}, 新专业={}",
                            student.getName(),
                            oldMajor.getName(),
                            targetMajor.getName());
                }
            }
            case WITHDRAWAL -> {
                // 退学：更新状态为退学，并标记删除
                student.setStatus(StudentStatus.WITHDRAWN);
                student.setDeleted(true);
                log.info("学生状态更新为退学: 学生={}", student.getName());
            }
        }

        Student updated = studentRepository.save(student);
        log.info("学生状态更新成功: 学生ID={}, 新状态={}",
                updated.getId(), updated.getStatus());

        return updated;
    }

    @Override
    @Transactional
    public Student updateStatus(Long studentId, StudentStatus status) {
        log.info("更新学生状态: 学生ID={}, 新状态={}", studentId, status);

        Student student = studentService.findById(studentId);
        student.setStatus(status);

        Student updated = studentRepository.save(student);
        log.info("学生状态更新成功: 学生={}, 状态={}",
                updated.getName(), updated.getStatus());

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSelectCourse(Long studentId) {
        try {
            Student student = studentService.findById(studentId);
            // 只有在读状态的学生可以选课
            return student.getStatus() == StudentStatus.ACTIVE;
        } catch (Exception e) {
            log.error("检查学生选课权限失败: 学生ID={}", studentId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateStudentStatusForSelection(Long studentId) {
        Student student = studentService.findById(studentId);

        // 检查学生状态
        if (student.getStatus() != StudentStatus.ACTIVE) {
            String message = switch (student.getStatus()) {
                case SUSPENDED -> "学生当前处于休学状态，无法选课";
                case WITHDRAWN -> "学生已退学，无法选课";
                case GRADUATED -> "学生已毕业，无法选课";
                default -> "学生状态异常，无法选课";
            };

            log.warn("学生状态不允许选课: 学生={}, 状态={}",
                    student.getName(), student.getStatus());
            throw new BusinessException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED, message);
        }

        log.debug("学生状态验证通过: 学生={}, 状态={}",
                student.getName(), student.getStatus());
    }
}

