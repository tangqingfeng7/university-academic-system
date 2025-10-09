package com.university.academic.service;

import com.university.academic.dto.*;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 监考服务类
 * 提供监考安排的业务逻辑
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamInvigilatorService {

    private final ExamInvigilatorRepository examInvigilatorRepository;
    private final ExamRoomRepository examRoomRepository;
    private final TeacherRepository teacherRepository;
    private final ExamRepository examRepository;

    /**
     * 添加监考教师
     */
    @Transactional
    public ExamInvigilatorDTO addInvigilator(AddInvigilatorRequest request) {
        log.info("添加监考教师，考场ID: {}, 教师ID: {}", request.getExamRoomId(), request.getTeacherId());

        // 验证考场ID不为空
        if (request.getExamRoomId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 验证考场存在
        ExamRoom examRoom = examRoomRepository.findById(request.getExamRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_ROOM_NOT_FOUND));

        // 验证教师存在
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        Exam exam = examRoom.getExam();

        // 检测教师时间冲突
        if (checkTeacherTimeConflict(request.getTeacherId(), exam.getExamTime(), exam.getDuration())) {
            throw new BusinessException(ErrorCode.TEACHER_INVIGILATION_CONFLICT);
        }

        // 创建监考记录
        ExamInvigilator invigilator = ExamInvigilator.builder()
                .examRoom(examRoom)
                .teacher(teacher)
                .type(request.getType())
                .build();

        invigilator = examInvigilatorRepository.save(invigilator);
        log.info("监考教师添加成功，ID: {}", invigilator.getId());

        return convertToDTO(invigilator);
    }

    /**
     * 移除监考教师
     */
    @Transactional
    public void removeInvigilator(Long invigilatorId) {
        log.info("移除监考教师，ID: {}", invigilatorId);

        ExamInvigilator invigilator = examInvigilatorRepository.findById(invigilatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVIGILATOR_NOT_FOUND));

        examInvigilatorRepository.delete(invigilator);
        log.info("监考教师移除成功，ID: {}", invigilatorId);
    }

    /**
     * 查询考场的监考安排
     */
    public List<ExamInvigilatorDTO> findByExamRoomId(Long examRoomId) {
        log.info("查询考场监考安排，考场ID: {}", examRoomId);

        List<ExamInvigilator> invigilators = examInvigilatorRepository
                .findByExamRoomIdWithTeacher(examRoomId);

        return invigilators.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询教师的监考任务
     */
    public List<InvigilationDTO> findByTeacherId(Long teacherId) {
        log.info("查询教师监考任务，教师ID: {}", teacherId);

        List<ExamInvigilator> invigilators = examInvigilatorRepository
                .findByTeacherId(teacherId);

        return invigilators.stream()
                .map(this::convertToInvigilationDTO)
                .collect(Collectors.toList());
    }

    /**
     * 检测教师时间冲突
     */
    public boolean checkTeacherTimeConflict(Long teacherId, LocalDateTime examTime, Integer duration) {
        LocalDateTime startTime = examTime;
        LocalDateTime endTime = examTime.plusMinutes(duration);

        // 检查监考冲突
        boolean hasInvigilation = examInvigilatorRepository
                .hasInvigilationInTimeRange(teacherId, startTime, endTime);

        if (hasInvigilation) {
            return true;
        }

        // 检查教师自己的考试
        List<Exam> teacherExams = examRepository
                .findByTeacherIdAndTimeRange(teacherId, startTime, endTime);

        return !teacherExams.isEmpty();
    }

    /**
     * 统计教师在学期内的监考次数
     */
    public long getTeacherInvigilationCount(Long teacherId, Long semesterId) {
        log.info("统计教师监考次数，教师ID: {}, 学期ID: {}", teacherId, semesterId);

        return examInvigilatorRepository.countByTeacherIdAndSemesterId(teacherId, semesterId);
    }

    /**
     * 转换为ExamInvigilatorDTO
     */
    private ExamInvigilatorDTO convertToDTO(ExamInvigilator invigilator) {
        Teacher teacher = invigilator.getTeacher();
        
        // 构建嵌套的teacher对象
        ExamInvigilatorDTO.TeacherInfo teacherInfo = ExamInvigilatorDTO.TeacherInfo.builder()
                .id(teacher.getId())
                .teacherNo(teacher.getTeacherNo())
                .name(teacher.getName())
                .phone(teacher.getPhone())
                .department(teacher.getDepartment() != null ? ExamInvigilatorDTO.DepartmentInfo.builder()
                        .id(teacher.getDepartment().getId())
                        .name(teacher.getDepartment().getName())
                        .build() : null)
                .build();
        
        return ExamInvigilatorDTO.builder()
                .id(invigilator.getId())
                .examRoomId(invigilator.getExamRoom().getId())
                .teacherId(teacher.getId())
                .teacherNo(teacher.getTeacherNo())
                .teacherName(teacher.getName())
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .type(invigilator.getType().name())
                .typeDescription(invigilator.getType().getDescription())
                .teacher(teacherInfo)
                .build();
    }

    /**
     * 转换为InvigilationDTO
     */
    private InvigilationDTO convertToInvigilationDTO(ExamInvigilator invigilator) {
        ExamRoom room = invigilator.getExamRoom();
        Exam exam = room.getExam();
        Course course = exam.getCourseOffering().getCourse();

        return InvigilationDTO.builder()
                .id(invigilator.getId())
                .examId(exam.getId())
                .examName(exam.getName())
                .courseNo(course.getCourseNo())
                .courseName(course.getName())
                .examType(exam.getType().name())
                .examTypeDescription(exam.getType().getDescription())
                .examTime(exam.getExamTime())
                .duration(exam.getDuration())
                .examRoomId(room.getId())
                .roomName(room.getRoomName())
                .location(room.getLocation())
                .invigilatorType(invigilator.getType().name())
                .invigilatorTypeDescription(invigilator.getType().getDescription())
                .examStatus(exam.getStatus().name())
                .statusDescription(exam.getStatus().getDescription())
                .studentCount(room.getAssignedCount())
                .build();
    }

    // ==================== 统计功能 ====================

    /**
     * 获取监考统计数据（按教师）
     */
    public List<com.university.academic.dto.InvigilatorStatisticsDTO> getInvigilatorStatistics(Long semesterId) {
        log.info("获取监考统计: semesterId={}", semesterId);
        
        // 查询所有监考记录
        List<ExamInvigilator> invigilations;
        if (semesterId != null) {
            invigilations = examInvigilatorRepository.findByTeacherIdAndSemesterId(null, semesterId);
            // 这个方法需要所有教师，所以我们需要另一种方式
            // 查询该学期的所有考试
            List<Exam> exams = examRepository.findBySemesterAndStatus(
                semesterId, null, org.springframework.data.domain.Pageable.unpaged()).getContent();
            invigilations = exams.stream()
                .flatMap(exam -> examInvigilatorRepository.findByExamId(exam.getId()).stream())
                .collect(java.util.stream.Collectors.toList());
        } else {
            invigilations = examInvigilatorRepository.findAll();
        }
        
        // 按教师分组统计
        java.util.Map<Long, List<ExamInvigilator>> groupedByTeacher = invigilations.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                inv -> inv.getTeacher().getId()));
        
        return groupedByTeacher.entrySet().stream()
            .map(entry -> {
                Teacher teacher = entry.getValue().get(0).getTeacher();
                List<ExamInvigilator> teacherInvigilations = entry.getValue();
                
                long totalCount = teacherInvigilations.size();
                long chiefCount = teacherInvigilations.stream()
                    .filter(inv -> inv.getType() == InvigilatorType.CHIEF)
                    .count();
                long assistantCount = teacherInvigilations.stream()
                    .filter(inv -> inv.getType() == InvigilatorType.ASSISTANT)
                    .count();
                
                return com.university.academic.dto.InvigilatorStatisticsDTO.builder()
                        .teacherId(teacher.getId())
                        .teacherNo(teacher.getTeacherNo())
                        .teacherName(teacher.getName())
                        .departmentName(teacher.getDepartment() != null ? 
                            teacher.getDepartment().getName() : "")
                        .totalCount(totalCount)
                        .chiefCount(chiefCount)
                        .assistantCount(assistantCount)
                        .build();
            })
            .sorted(java.util.Comparator.comparing(
                com.university.academic.dto.InvigilatorStatisticsDTO::getTotalCount).reversed())
            .collect(java.util.stream.Collectors.toList());
    }
}

