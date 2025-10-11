package com.university.academic.listener;

import com.university.academic.event.GradePublishEvent;
import com.university.academic.service.CreditCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 成绩公布事件监听器
 * 用于在成绩公布后自动更新学生学分汇总
 *
 * @author Academic System Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GradePublishListener {

    private final CreditCalculationService creditCalculationService;

    /**
     * 监听成绩公布事件（单个学生）
     * 在事务提交后异步执行学分更新
     *
     * @param event 成绩公布事件
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGradePublish(GradePublishEvent event) {
        log.info("监听到成绩公布事件: studentId={}, gradeId={}", 
                event.getStudentId(), event.getGradeId());

        try {
            // 清除缓存
            creditCalculationService.evictStudentCreditCache(event.getStudentId());
            
            // 重新计算并更新学分
            creditCalculationService.updateStudentCreditSummary(event.getStudentId());
            
            log.info("学分更新完成: studentId={}", event.getStudentId());
        } catch (Exception e) {
            log.error("学分更新失败: studentId={}, error={}", 
                    event.getStudentId(), e.getMessage(), e);
        }
    }

    /**
     * 监听批量成绩公布事件
     * 批量更新学生学分
     *
     * @param studentIds 学生ID列表
     */
    @Async
    @EventListener
    public void handleBatchGradePublish(List<Long> studentIds) {
        log.info("监听到批量成绩公布事件: count={}", studentIds.size());

        try {
            // 批量更新学分
            creditCalculationService.batchUpdateCreditSummary(studentIds);
            
            log.info("批量学分更新完成: count={}", studentIds.size());
        } catch (Exception e) {
            log.error("批量学分更新失败: error={}", e.getMessage(), e);
        }
    }
}

