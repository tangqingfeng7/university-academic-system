package com.university.academic.service.impl;

import com.university.academic.entity.StudentDiscipline;
import com.university.academic.entity.User;
import com.university.academic.entity.enums.ApprovalStatus;
import com.university.academic.entity.enums.DisciplineStatus;
import com.university.academic.entity.enums.DisciplineType;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StudentDisciplineRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.StudentDisciplineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生处分服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentDisciplineServiceImpl implements StudentDisciplineService {

    private final StudentDisciplineRepository disciplineRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StudentDiscipline createDiscipline(StudentDiscipline discipline) {
        log.info("创建处分记录: studentId={}, type={}", 
                discipline.getStudent().getId(), discipline.getDisciplineType());
        
        discipline.setStatus(DisciplineStatus.ACTIVE);
        discipline.setApprovalStatus(ApprovalStatus.PENDING); // 初始状态为待审批
        discipline.setDeleted(false);
        
        StudentDiscipline saved = disciplineRepository.save(discipline);
        
        // 重新查询以预加载关联数据，避免懒加载异常
        return disciplineRepository.findById(saved.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DISCIPLINE_NOT_FOUND));
    }

    @Override
    @Transactional
    public StudentDiscipline updateDiscipline(Long id, StudentDiscipline discipline) {
        log.info("更新处分记录: id={}", id);
        
        StudentDiscipline existing = findById(id);
        
        // 更新可编辑字段
        existing.setReason(discipline.getReason());
        existing.setDescription(discipline.getDescription());
        existing.setOccurrenceDate(discipline.getOccurrenceDate());
        existing.setPunishmentDate(discipline.getPunishmentDate());
        existing.setCanRemove(discipline.getCanRemove());
        existing.setAttachmentUrl(discipline.getAttachmentUrl());
        
        disciplineRepository.save(existing);
        
        // 重新查询以预加载关联数据
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISCIPLINE_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteDiscipline(Long id) {
        log.info("删除处分记录: id={}", id);
        
        StudentDiscipline discipline = findById(id);
        discipline.setDeleted(true);
        disciplineRepository.save(discipline);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDiscipline findById(Long id) {
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISCIPLINE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDiscipline> findByStudentId(Long studentId) {
        return disciplineRepository.findByStudentIdAndDeletedFalse(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDiscipline> findActiveByStudentId(Long studentId) {
        return disciplineRepository.findByStudentIdAndStatusAndDeletedFalse(
                studentId, DisciplineStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentDiscipline> findAll(Pageable pageable) {
        return disciplineRepository.findByDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentDiscipline> findByConditions(
            Long studentId,
            Long reporterId,
            DisciplineType disciplineType,
            DisciplineStatus status,
            ApprovalStatus approvalStatus,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        
        return disciplineRepository.findByConditions(
                studentId, reporterId, disciplineType, status, approvalStatus, startDate, endDate, pageable);
    }

    @Override
    @Transactional
    public StudentDiscipline removeDiscipline(Long id, String reason, Long operatorId) {
        log.info("解除处分: id={}, operatorId={}", id, operatorId);
        
        StudentDiscipline discipline = findById(id);
        
        // 检查是否可以解除
        if (!discipline.getCanRemove()) {
            throw new BusinessException(ErrorCode.DISCIPLINE_CANNOT_REMOVE);
        }
        
        // 检查是否已解除
        if (discipline.getStatus() == DisciplineStatus.REMOVED) {
            throw new BusinessException(ErrorCode.DISCIPLINE_ALREADY_REMOVED);
        }
        
        // 查询操作人
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        discipline.setStatus(DisciplineStatus.REMOVED);
        discipline.setRemovedDate(LocalDate.now());
        discipline.setRemovedReason(reason);
        discipline.setRemovedBy(operator);
        
        disciplineRepository.save(discipline);
        
        // 重新查询以预加载关联数据
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISCIPLINE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveDiscipline(Long studentId) {
        return disciplineRepository.hasActiveDiscipline(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByStudentId(Long studentId) {
        return disciplineRepository.countByStudentIdAndDeletedFalse(studentId);
    }

    @Override
    @Transactional
    public StudentDiscipline approveDiscipline(Long id, Long approverId) {
        log.info("审批处分: id={}, approverId={}", id, approverId);
        
        StudentDiscipline discipline = findById(id);
        
        // 检查是否已审批
        if (discipline.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该处分已审批，无法重复审批");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        discipline.setApprover(approver);
        discipline.setApprovedAt(LocalDateTime.now());
        discipline.setApprovalStatus(ApprovalStatus.APPROVED);
        
        disciplineRepository.save(discipline);
        
        // 重新查询以预加载关联数据
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISCIPLINE_NOT_FOUND));
    }

    /**
     * 审批处分（批准或拒绝）
     */
    @Transactional
    public StudentDiscipline reviewDiscipline(Long id, Long approverId, boolean approved, String comment) {
        log.info("审批处分: id={}, approverId={}, approved={}", id, approverId, approved);
        
        StudentDiscipline discipline = findById(id);
        
        // 检查是否已审批
        if (discipline.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该处分已审批，无法重复审批");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        discipline.setApprover(approver);
        discipline.setApprovedAt(LocalDateTime.now());
        discipline.setApprovalStatus(approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        discipline.setApprovalComment(comment);
        
        // 如果拒绝，同时将处分状态设为已解除
        if (!approved) {
            discipline.setStatus(DisciplineStatus.REMOVED);
            discipline.setRemovedDate(LocalDate.now());
            discipline.setRemovedReason("审批被拒绝");
            discipline.setRemovedBy(approver);
        }
        
        disciplineRepository.save(discipline);
        
        // 重新查询以预加载关联数据
        return disciplineRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISCIPLINE_NOT_FOUND));
    }
}

