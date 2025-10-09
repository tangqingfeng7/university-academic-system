package com.university.academic.controller;

import com.university.academic.annotation.OperationLog;
import com.university.academic.dto.*;
import com.university.academic.entity.ExamStatus;
import com.university.academic.entity.ExamType;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ExamRepository;
import com.university.academic.service.ExamInvigilatorService;
import com.university.academic.service.ExamRoomService;
import com.university.academic.service.ExamService;
import com.university.academic.vo.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员端考试管理控制器
 * 提供考试、考场、监考管理接口
 *
 * @author Academic System Team
 */
@Tag(name = "考试管理-管理员", description = "管理员端考试、考场、监考管理接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminExamController {

    private final ExamService examService;
    private final ExamRoomService examRoomService;
    private final ExamInvigilatorService examInvigilatorService;
    private final ExamRepository examRepository;

    // ==================== 10.1 考试CRUD接口 ====================

    /**
     * 创建考试
     */
    @PostMapping
    @OperationLog(value = "创建考试", type = "CREATE")
    public Result<ExamDTO> createExam(@Valid @RequestBody CreateExamRequest request) {
        log.info("创建考试: name={}, courseOfferingId={}", request.getName(), request.getCourseOfferingId());
        ExamDTO examDTO = examService.createExam(request);
        return Result.success(examDTO);
    }

    /**
     * 分页查询考试列表
     */
    @GetMapping
    public Result<Map<String, Object>> getExamList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "examTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String courseNo,
            @RequestParam(required = false) String courseName) {

        log.info("查询考试列表: page={}, size={}, semesterId={}, status={}, type={}, courseNo={}, courseName={}", 
                page, size, semesterId, status, type, courseNo, courseName);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        ExamStatus examStatus = status != null ? ExamStatus.valueOf(status) : null;
        ExamType examType = type != null ? ExamType.valueOf(type) : null;
        Page<ExamDTO> examPage = examService.findWithFilters(
            semesterId, examStatus, examType, courseNo, courseName, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", examPage.getContent());
        response.put("totalElements", examPage.getTotalElements());
        response.put("totalPages", examPage.getTotalPages());
        response.put("currentPage", examPage.getNumber());
        response.put("pageSize", examPage.getSize());

        return Result.success(response);
    }

    /**
     * 查询考试详情
     */
    @GetMapping("/{id}")
    public Result<ExamDTO> getExamById(@PathVariable Long id) {
        log.info("查询考试详情: id={}", id);
        ExamDTO examDTO = examService.findExamById(id);
        return Result.success(examDTO);
    }

    /**
     * 更新考试
     */
    @PutMapping("/{id}")
    @OperationLog(value = "更新考试", type = "UPDATE")
    public Result<ExamDTO> updateExam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExamRequest request) {
        log.info("更新考试: id={}, name={}", id, request.getName());
        ExamDTO examDTO = examService.updateExam(id, request);
        return Result.success(examDTO);
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/{id}")
    @OperationLog(value = "删除考试", type = "DELETE")
    public Result<Void> deleteExam(@PathVariable Long id) {
        log.info("删除考试: id={}", id);
        examService.deleteExam(id);
        return Result.success();
    }

    // ==================== 10.2 考试操作接口 ====================

    /**
     * 发布考试
     */
    @PostMapping("/{id}/publish")
    @OperationLog(value = "发布考试", type = "UPDATE")
    public Result<Void> publishExam(@PathVariable Long id) {
        log.info("发布考试: id={}", id);
        examService.publishExam(id);
        return Result.success();
    }

    /**
     * 取消考试
     */
    @PostMapping("/{id}/cancel")
    @OperationLog(value = "取消考试", type = "UPDATE")
    public Result<Void> cancelExam(@PathVariable Long id) {
        log.info("取消考试: id={}", id);
        examService.cancelExam(id);
        return Result.success();
    }

    /**
     * 检测考试冲突
     */
    @GetMapping("/{id}/conflicts")
    public Result<List<ExamService.StudentConflictDTO>> detectConflicts(@PathVariable Long id) {
        log.info("检测考试冲突: id={}", id);
        List<ExamService.StudentConflictDTO> conflicts = examService.detectStudentConflicts(id);
        return Result.success(conflicts);
    }

    // ==================== 10.3 考场管理接口 ====================

    /**
     * 创建考场
     */
    @PostMapping("/{id}/rooms")
    @OperationLog(value = "创建考场", type = "CREATE")
    public Result<ExamRoomDTO> createExamRoom(
            @PathVariable Long id,
            @Valid @RequestBody CreateExamRoomRequest request) {
        log.info("创建考场: examId={}, roomName={}", id, request.getRoomName());
        request.setExamId(id);
        ExamRoomDTO roomDTO = examRoomService.createExamRoom(request);
        return Result.success(roomDTO);
    }

    /**
     * 批量创建考场（自动创建）
     */
    @PostMapping("/{id}/rooms/auto")
    @OperationLog(value = "批量创建考场", type = "CREATE")
    public Result<List<ExamRoomDTO>> autoCreateExamRooms(
            @PathVariable Long id,
            @RequestBody List<CreateExamRoomRequest> requests) {
        log.info("批量创建考场: examId={}, 数量={}", id, requests.size());

        // 设置考试ID
        requests.forEach(request -> request.setExamId(id));
        
        List<ExamRoomDTO> rooms = examRoomService.autoCreateExamRooms(id, requests);

        return Result.success("成功创建 " + rooms.size() + " 个考场", rooms);
    }

    /**
     * 查询考场列表
     */
    @GetMapping("/{id}/rooms")
    public Result<List<ExamRoomDTO>> getExamRooms(@PathVariable Long id) {
        log.info("查询考场列表: examId={}", id);
        List<ExamRoomDTO> rooms = examRoomService.findByExamId(id);
        return Result.success(rooms);
    }

    /**
     * 更新考场
     */
    @PutMapping("/rooms/{roomId}")
    @OperationLog(value = "更新考场", type = "UPDATE")
    public Result<ExamRoomDTO> updateExamRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateExamRoomRequest request) {
        log.info("更新考场: roomId={}", roomId);
        ExamRoomDTO roomDTO = examRoomService.updateExamRoom(roomId, request);
        return Result.success(roomDTO);
    }

    /**
     * 删除考场
     */
    @DeleteMapping("/rooms/{roomId}")
    @OperationLog(value = "删除考场", type = "DELETE")
    public Result<Void> deleteExamRoom(@PathVariable Long roomId) {
        log.info("删除考场: roomId={}", roomId);
        examRoomService.deleteExamRoom(roomId);
        return Result.success();
    }

    /**
     * 查询考场学生列表
     */
    @GetMapping("/rooms/{roomId}/students")
    public Result<List<ExamRoomStudentDTO>> getRoomStudents(@PathVariable Long roomId) {
        log.info("查询考场学生: roomId={}", roomId);
        List<ExamRoomStudentDTO> students = examRoomService.findStudentsByRoomId(roomId);
        return Result.success(students);
    }

    // ==================== 10.4 学生分配接口 ====================

    /**
     * 获取考试可用学生列表（选课学生）
     */
    @GetMapping("/{id}/available-students")
    public Result<List<StudentDTO>> getAvailableStudents(@PathVariable Long id) {
        log.info("获取考试可用学生: examId={}", id);
        
        // 验证考试是否存在
        examRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
        
        List<StudentDTO> students = examService.getAvailableStudents(id);
        log.info("查询到 {} 个可用学生", students.size());
        return Result.success(students);
    }

    /**
     * 分配学生到考场
     */
    @PostMapping("/rooms/{roomId}/students")
    @OperationLog(value = "分配学生到考场", type = "CREATE")
    public Result<Void> assignStudent(
            @PathVariable Long roomId,
            @RequestParam Long studentId,
            @RequestParam String seatNumber) {
        log.info("分配学生: roomId={}, studentId={}, seatNumber={}", roomId, studentId, seatNumber);
        examRoomService.assignStudentToRoom(roomId, studentId, seatNumber);
        return Result.success();
    }

    /**
     * 从考场移除学生
     */
    @DeleteMapping("/rooms/{roomId}/students/{studentId}")
    @OperationLog(value = "移除学生", type = "DELETE")
    public Result<Void> removeStudent(
            @PathVariable Long roomId,
            @PathVariable Long studentId) {
        log.info("移除学生: roomId={}, studentId={}", roomId, studentId);
        examRoomService.removeStudentFromRoom(roomId, studentId);
        return Result.success();
    }

    /**
     * 自动分配学生
     */
    @PostMapping("/{id}/students/auto")
    @OperationLog(value = "自动分配学生", type = "CREATE")
    public Result<Void> autoAssignStudents(@PathVariable Long id) {
        log.info("自动分配学生: examId={}", id);
        examRoomService.autoAssignStudents(id);
        return Result.success();
    }

    // ==================== 10.5 监考管理接口 ====================

    /**
     * 添加监考教师
     */
    @PostMapping("/rooms/{roomId}/invigilators")
    @OperationLog(value = "添加监考教师", type = "CREATE")
    public Result<ExamInvigilatorDTO> addInvigilator(
            @PathVariable Long roomId,
            @Valid @RequestBody AddInvigilatorRequest request) {
        log.info("添加监考: roomId={}, teacherId={}", roomId, request.getTeacherId());
        request.setExamRoomId(roomId);
        ExamInvigilatorDTO invigilatorDTO = examInvigilatorService.addInvigilator(request);
        return Result.success(invigilatorDTO);
    }

    /**
     * 移除监考教师
     */
    @DeleteMapping("/invigilators/{invigilatorId}")
    @OperationLog(value = "移除监考教师", type = "DELETE")
    public Result<Void> removeInvigilator(@PathVariable Long invigilatorId) {
        log.info("移除监考: invigilatorId={}", invigilatorId);
        examInvigilatorService.removeInvigilator(invigilatorId);
        return Result.success();
    }

    /**
     * 查询考场监考列表
     */
    @GetMapping("/rooms/{roomId}/invigilators")
    public Result<List<ExamInvigilatorDTO>> getRoomInvigilators(@PathVariable Long roomId) {
        log.info("查询考场监考: roomId={}", roomId);
        List<ExamInvigilatorDTO> invigilators = examInvigilatorService.findByExamRoomId(roomId);
        return Result.success(invigilators);
    }

    // ==================== 数据导出接口 ====================

    /**
     * 导出考试列表（Excel）
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportExamList(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String status) {
        log.info("导出考试列表: semesterId={}, status={}", semesterId, status);
        
        ExamStatus examStatus = status != null ? ExamStatus.valueOf(status) : null;
        byte[] excelData = examService.exportExamList(semesterId, examStatus);
        
        String filename = "考试列表_" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    /**
     * 导出考场安排（Excel）
     */
    @GetMapping("/{id}/export/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportExamRoomArrangement(@PathVariable Long id) {
        log.info("导出考场安排: examId={}", id);
        
        byte[] excelData = examService.exportExamRoomArrangement(id);
        
        String filename = "考场安排_" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    /**
     * 导出监考安排（Excel）
     */
    @GetMapping("/{id}/export/invigilators")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportInvigilatorArrangement(@PathVariable Long id) {
        log.info("导出监考安排: examId={}", id);
        
        byte[] excelData = examService.exportInvigilatorArrangement(id);
        
        String filename = "监考安排_" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }

    // ==================== 统计接口 ====================

    /**
     * 获取考试统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) Long semesterId) {
        log.info("查询考试统计: semesterId={}", semesterId);
        
        // 考试统计
        ExamStatisticsDTO examStats = examService.getExamStatistics(semesterId);
        
        // 考场统计
        com.university.academic.dto.RoomStatisticsDTO roomStats = 
            examRoomService.getRoomStatistics(semesterId);
        
        // 监考统计
        List<com.university.academic.dto.InvigilatorStatisticsDTO> invigilatorStats = 
            examInvigilatorService.getInvigilatorStatistics(semesterId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("examStatistics", examStats);
        response.put("roomStatistics", roomStats);
        response.put("invigilatorStatistics", invigilatorStats);
        
        return Result.success(response);
    }
}

