package com.university.academic.service.impl;

import com.university.academic.entity.DisciplineAppeal;
import com.university.academic.entity.StudentDiscipline;
import com.university.academic.entity.User;
import com.university.academic.entity.enums.AppealResult;
import com.university.academic.entity.enums.AppealStatus;
import com.university.academic.entity.enums.DisciplineStatus;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.DisciplineAppealRepository;
import com.university.academic.repository.StudentDisciplineRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.DisciplineAppealService;
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
 * 处分申诉服务实现类
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisciplineAppealServiceImpl implements DisciplineAppealService {

    private final DisciplineAppealRepository appealRepository;
    private final StudentDisciplineRepository disciplineRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DisciplineAppeal createAppeal(DisciplineAppeal appeal) {
        log.info("创建申诉: disciplineId={}, studentId={}", 
                appeal.getDiscipline().getId(), appeal.getStudent().getId());
        
        // 检查处分是否存在
        StudentDiscipline discipline = disciplineRepository.findById(appeal.getDiscipline().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DISCIPLINE_NOT_FOUND));
        
        // 检查处分状态
        if (discipline.getStatus() == DisciplineStatus.REMOVED) {
            throw new BusinessException(ErrorCode.DISCIPLINE_ALREADY_REMOVED);
        }
        
        // 检查是否已有待审核的申诉
        if (appealRepository.hasPendingAppeal(discipline.getId())) {
            throw new BusinessException(ErrorCode.APPEAL_ALREADY_EXISTS);
        }
        
        appeal.setStatus(AppealStatus.PENDING);
        DisciplineAppeal saved = appealRepository.save(appeal);
        
        // 更新处分状态为申诉中
        discipline.setStatus(DisciplineStatus.APPEALING);
        disciplineRepository.save(discipline);
        
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public DisciplineAppeal findById(Long id) {
        return appealRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPEAL_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisciplineAppeal> findByStudentId(Long studentId) {
        return appealRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisciplineAppeal> findByDisciplineId(Long disciplineId) {
        return appealRepository.findByDisciplineId(disciplineId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisciplineAppeal> findPendingAppeals(Pageable pageable) {
        return appealRepository.findByStatus(AppealStatus.PENDING, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisciplineAppeal> findByConditions(
            AppealStatus status,
            Long studentId,
            Pageable pageable) {
        
        return appealRepository.findByConditions(status, studentId, pageable);
    }

    @Override
    @Transactional
    public DisciplineAppeal reviewAppeal(
            Long id,
            AppealResult result,
            String comment,
            Long reviewerId) {
        
        log.info("审核申诉: id={}, result={}, reviewerId={}", id, result, reviewerId);
        
        DisciplineAppeal appeal = findById(id);
        
        // 检查申诉状态
        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new BusinessException(ErrorCode.APPEAL_ALREADY_REVIEWED);
        }
        
        // 查询审核人
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 更新申诉
        appeal.setReviewResult(result);
        appeal.setReviewComment(comment);
        appeal.setReviewedBy(reviewer);
        appeal.setReviewedAt(LocalDateTime.now());
        appeal.setStatus(AppealStatus.APPROVED);
        
        DisciplineAppeal saved = appealRepository.save(appeal);
        
        // 根据审核结果更新处分状态
        StudentDiscipline discipline = appeal.getDiscipline();
        
        if (result == AppealResult.ACCEPT) {
            // 接受申诉，撤销处分
            discipline.setStatus(DisciplineStatus.REMOVED);
            discipline.setRemovedDate(LocalDate.now());
            discipline.setRemovedReason("申诉通过");
            discipline.setRemovedBy(reviewer);
        } else {
            // 拒绝申诉，恢复原状态
            discipline.setStatus(DisciplineStatus.ACTIVE);
        }
        
        disciplineRepository.save(discipline);
        
        return saved;
    }

    @Override
    @Transactional
    public void cancelAppeal(Long id) {
        log.info("撤销申诉: id={}", id);
        
        DisciplineAppeal appeal = findById(id);
        
        // 检查申诉状态
        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new BusinessException(ErrorCode.APPEAL_CANNOT_CANCEL);
        }
        
        appeal.setStatus(AppealStatus.CANCELLED);
        appealRepository.save(appeal);
        
        // 恢复处分状态
        StudentDiscipline discipline = appeal.getDiscipline();
        discipline.setStatus(DisciplineStatus.ACTIVE);
        disciplineRepository.save(discipline);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingAppeal(Long disciplineId) {
        return appealRepository.hasPendingAppeal(disciplineId);
    }
}

