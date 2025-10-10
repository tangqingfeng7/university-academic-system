package com.university.academic.controller;

import com.university.academic.entity.*;
import com.university.academic.repository.*;
import com.university.academic.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 测试数据控制器（仅用于开发测试）
 *
 * @author university
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "测试数据管理", description = "仅用于开发测试环境")
public class TestDataController {

    private final StudentStatusChangeRepository statusChangeRepository;
    private final StatusChangeApprovalRepository approvalRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @PostMapping("/status-changes/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "生成学籍异动测试数据", description = "生成8条测试数据（4条待审批，4条已处理）")
    public Result<String> generateStatusChangeTestData() {
        log.info("开始生成学籍异动测试数据");

        try {
            // 清理旧的测试数据（可选）
            // statusChangeRepository.deleteAll();
            // approvalRepository.deleteAll();

            // 检查是否有学生数据
            long studentCount = studentRepository.count();
            if (studentCount == 0) {
                return Result.error("请先添加学生数据！");
            }

            // 获取前10个学生
            var students = studentRepository.findAll();
            if (students.size() < 8) {
                return Result.error("需要至少8个学生数据，当前只有 " + students.size() + " 个");
            }

            // 获取审批人（假设ID=2是教师，ID=1是管理员）
            User teacher = userRepository.findById(2L).orElse(null);
            User admin = userRepository.findById(1L).orElse(null);

            if (teacher == null || admin == null) {
                return Result.error("请确保用户ID 1(管理员) 和 2(教师) 存在！");
            }

            int count = 0;

            // 1. 休学申请（待审批）
            StudentStatusChange application1 = new StudentStatusChange();
            application1.setStudent(students.get(0));
            application1.setType(ChangeType.SUSPENSION);
            application1.setReason("因身体健康原因需要休学治疗，医院建议休息半年。已附上医院诊断证明和病历资料。");
            application1.setStartDate(LocalDate.of(2024, 9, 1));
            application1.setEndDate(LocalDate.of(2025, 2, 28));
            application1.setAttachmentUrl("/uploads/medical-certificate-001.pdf");
            application1.setStatus(ApprovalStatus.PENDING);
            application1.setCurrentApproverId(teacher.getId());
            application1.setApprovalLevel(1);
            application1.setDeadline(LocalDateTime.now().plusDays(7));
            application1.setIsOverdue(false);
            application1.setDeleted(false);
            statusChangeRepository.save(application1);
            count++;

            // 2. 转专业申请（待审批）
            StudentStatusChange application2 = new StudentStatusChange();
            application2.setStudent(students.get(1));
            application2.setType(ChangeType.TRANSFER);
            application2.setReason("对计算机科学专业更感兴趣，且相关课程成绩优秀。希望能够转入计算机科学与技术专业深造。");
            application2.setTargetMajorId(2L);
            application2.setAttachmentUrl("/uploads/transcript-002.pdf");
            application2.setStatus(ApprovalStatus.PENDING);
            application2.setCurrentApproverId(teacher.getId());
            application2.setApprovalLevel(1);
            application2.setDeadline(LocalDateTime.now().plusDays(14));
            application2.setIsOverdue(false);
            application2.setDeleted(false);
            statusChangeRepository.save(application2);
            count++;

            // 3. 复学申请（待审批）
            StudentStatusChange application3 = new StudentStatusChange();
            application3.setStudent(students.get(2));
            application3.setType(ChangeType.RESUMPTION);
            application3.setReason("休学期满，身体状况已恢复，申请复学继续学业。附上医院康复证明。");
            application3.setStartDate(LocalDate.of(2024, 9, 1));
            application3.setAttachmentUrl("/uploads/health-certificate-003.pdf");
            application3.setStatus(ApprovalStatus.PENDING);
            application3.setCurrentApproverId(teacher.getId());
            application3.setApprovalLevel(1);
            application3.setDeadline(LocalDateTime.now().plusDays(7));
            application3.setIsOverdue(false);
            application3.setDeleted(false);
            statusChangeRepository.save(application3);
            count++;

            // 4. 退学申请（待审批）
            StudentStatusChange application4 = new StudentStatusChange();
            application4.setStudent(students.get(3));
            application4.setType(ChangeType.WITHDRAWAL);
            application4.setReason("已获得国外大学录取通知书，决定出国深造。附上录取通知书复印件。");
            application4.setStartDate(LocalDate.of(2024, 9, 1));
            application4.setAttachmentUrl("/uploads/admission-letter-004.pdf");
            application4.setStatus(ApprovalStatus.PENDING);
            application4.setCurrentApproverId(teacher.getId());
            application4.setApprovalLevel(1);
            application4.setDeadline(LocalDateTime.now().plusDays(7));
            application4.setIsOverdue(false);
            application4.setDeleted(false);
            statusChangeRepository.save(application4);
            count++;

            // 5. 休学申请（已批准）
            StudentStatusChange application5 = new StudentStatusChange();
            application5.setStudent(students.get(4));
            application5.setType(ChangeType.SUSPENSION);
            application5.setReason("因家庭原因需要休学一年处理家事。");
            application5.setStartDate(LocalDate.of(2024, 3, 1));
            application5.setEndDate(LocalDate.of(2025, 2, 28));
            application5.setStatus(ApprovalStatus.APPROVED);
            application5.setApprovalLevel(3);
            application5.setDeleted(false);
            application5.setCreatedAt(LocalDateTime.now().minusDays(90));
            application5.setUpdatedAt(LocalDateTime.now().minusDays(80));
            statusChangeRepository.save(application5);
            
            // 添加审批记录
            createApprovalRecord(application5.getId(), 1, teacher.getId(), ApprovalAction.APPROVE, 
                    "了解学生家庭情况，同意休学申请。", LocalDateTime.now().minusDays(89));
            createApprovalRecord(application5.getId(), 2, admin.getId(), ApprovalAction.APPROVE, 
                    "同意该生休学申请。", LocalDateTime.now().minusDays(85));
            createApprovalRecord(application5.getId(), 3, admin.getId(), ApprovalAction.APPROVE, 
                    "批准休学申请，请办理相关手续。", LocalDateTime.now().minusDays(80));
            count++;

            // 6. 转专业申请（已拒绝）
            StudentStatusChange application6 = new StudentStatusChange();
            application6.setStudent(students.get(5));
            application6.setType(ChangeType.TRANSFER);
            application6.setReason("希望转入金融专业学习。");
            application6.setTargetMajorId(4L);
            application6.setStatus(ApprovalStatus.REJECTED);
            application6.setApprovalLevel(1);
            application6.setDeleted(false);
            application6.setCreatedAt(LocalDateTime.now().minusDays(60));
            application6.setUpdatedAt(LocalDateTime.now().minusDays(55));
            statusChangeRepository.save(application6);
            
            createApprovalRecord(application6.getId(), 1, teacher.getId(), ApprovalAction.REJECT, 
                    "学生现专业成绩不理想，不建议转专业。建议先提升当前专业成绩。", LocalDateTime.now().minusDays(55));
            count++;

            // 7. 复学申请（已批准）
            StudentStatusChange application7 = new StudentStatusChange();
            application7.setStudent(students.get(6));
            application7.setType(ChangeType.RESUMPTION);
            application7.setReason("休学期满，申请复学。");
            application7.setStartDate(LocalDate.of(2024, 9, 1));
            application7.setStatus(ApprovalStatus.APPROVED);
            application7.setApprovalLevel(3);
            application7.setDeleted(false);
            application7.setCreatedAt(LocalDateTime.now().minusDays(30));
            application7.setUpdatedAt(LocalDateTime.now().minusDays(20));
            statusChangeRepository.save(application7);
            
            createApprovalRecord(application7.getId(), 1, teacher.getId(), ApprovalAction.APPROVE, 
                    "该生休学期满，同意复学。", LocalDateTime.now().minusDays(29));
            createApprovalRecord(application7.getId(), 2, admin.getId(), ApprovalAction.APPROVE, 
                    "审核通过，同意复学。", LocalDateTime.now().minusDays(25));
            createApprovalRecord(application7.getId(), 3, admin.getId(), ApprovalAction.APPROVE, 
                    "批准复学申请。", LocalDateTime.now().minusDays(20));
            count++;

            // 8. 休学申请（已取消）
            StudentStatusChange application8 = new StudentStatusChange();
            application8.setStudent(students.get(7));
            application8.setType(ChangeType.SUSPENSION);
            application8.setReason("因个人原因申请休学。");
            application8.setStartDate(LocalDate.of(2024, 9, 1));
            application8.setEndDate(LocalDate.of(2025, 8, 31));
            application8.setStatus(ApprovalStatus.CANCELLED);
            application8.setApprovalLevel(1);
            application8.setDeleted(false);
            application8.setCreatedAt(LocalDateTime.now().minusDays(45));
            application8.setUpdatedAt(LocalDateTime.now().minusDays(44));
            statusChangeRepository.save(application8);
            count++;

            log.info("成功生成{}条学籍异动测试数据", count);
            return Result.success("成功生成 " + count + " 条测试数据！包括：4条待审批，3条已处理，1条已取消");

        } catch (Exception e) {
            log.error("生成测试数据失败", e);
            return Result.error("生成测试数据失败: " + e.getMessage());
        }
    }

    private void createApprovalRecord(Long statusChangeId, Integer level, Long approverId, 
                                     ApprovalAction action, String comment, LocalDateTime approvedAt) {
        StatusChangeApproval approval = new StatusChangeApproval();
        
        // 设置关联
        StudentStatusChange statusChange = new StudentStatusChange();
        statusChange.setId(statusChangeId);
        approval.setStatusChange(statusChange);
        
        User approver = new User();
        approver.setId(approverId);
        approval.setApprover(approver);
        
        approval.setApprovalLevel(level);
        approval.setAction(action);
        approval.setComment(comment);
        approval.setApprovedAt(approvedAt);
        approval.setDeleted(false);
        approval.setCreatedAt(approvedAt);
        approval.setUpdatedAt(approvedAt);
        
        approvalRepository.save(approval);
    }

    @DeleteMapping("/status-changes/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "清除学籍异动测试数据", description = "删除所有学籍异动记录")
    public Result<String> clearStatusChangeTestData() {
        log.info("开始清除学籍异动测试数据");
        
        try {
            long approvalCount = approvalRepository.count();
            long statusChangeCount = statusChangeRepository.count();
            
            approvalRepository.deleteAll();
            statusChangeRepository.deleteAll();
            
            log.info("清除完成：删除{}条审批记录，{}条异动记录", approvalCount, statusChangeCount);
            return Result.success("清除完成：删除" + approvalCount + "条审批记录，" + statusChangeCount + "条异动记录");
            
        } catch (Exception e) {
            log.error("清除测试数据失败", e);
            return Result.error("清除测试数据失败: " + e.getMessage());
        }
    }
}

