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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考场服务类
 * 提供考场管理和学生分配的业务逻辑
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamRoomService {

    private final ExamRoomRepository examRoomRepository;
    private final ExamRoomStudentRepository examRoomStudentRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final SemesterRepository semesterRepository;

    // ==================== 6.1 考场管理方法 ====================

    /**
     * 创建考场
     */
    @Transactional
    public ExamRoomDTO createExamRoom(CreateExamRoomRequest request) {
        log.info("创建考场: {}", request.getRoomName());

        // 验证考试是否存在
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        // 检测考场时间冲突
        if (checkRoomTimeConflict(request.getLocation(), exam.getExamTime(), exam.getDuration())) {
            throw new BusinessException(ErrorCode.EXAM_ROOM_TIME_CONFLICT);
        }

        // 创建考场实体
        ExamRoom examRoom = ExamRoom.builder()
                .exam(exam)
                .roomName(request.getRoomName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .assignedCount(0)
                .build();

        examRoom = examRoomRepository.save(examRoom);
        log.info("考场创建成功，ID: {}", examRoom.getId());

        return convertToDTO(examRoom);
    }

    /**
     * 更新考场
     */
    @Transactional
    public ExamRoomDTO updateExamRoom(Long id, UpdateExamRoomRequest request) {
        log.info("更新考场，ID: {}", id);

        ExamRoom examRoom = examRoomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_ROOM_NOT_FOUND));

        // 验证容量不能小于已分配人数
        if (request.getCapacity() < examRoom.getAssignedCount()) {
            throw new BusinessException(ErrorCode.EXAM_ROOM_CAPACITY_ERROR);
        }

        // 更新考场信息
        examRoom.setRoomName(request.getRoomName());
        examRoom.setLocation(request.getLocation());
        examRoom.setCapacity(request.getCapacity());

        examRoom = examRoomRepository.save(examRoom);
        log.info("考场更新成功，ID: {}", id);

        return convertToDTO(examRoom);
    }

    /**
     * 删除考场
     */
    @Transactional
    public void deleteExamRoom(Long id) {
        log.info("删除考场，ID: {}", id);

        ExamRoom examRoom = examRoomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_ROOM_NOT_FOUND));

        // 验证考场没有学生分配
        if (examRoom.getAssignedCount() > 0) {
            throw new BusinessException(ErrorCode.EXAM_ROOM_HAS_STUDENTS);
        }

        examRoomRepository.delete(examRoom);
        log.info("考场删除成功，ID: {}", id);
    }

    /**
     * 批量创建考场
     */
    @Transactional
    public List<ExamRoomDTO> autoCreateExamRooms(Long examId, List<CreateExamRoomRequest> requests) {
        log.info("批量创建考场，考试ID: {}，数量: {}", examId, requests.size());

        // 验证考试是否存在
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        List<ExamRoom> createdRooms = new java.util.ArrayList<>();
        
        for (CreateExamRoomRequest request : requests) {
            // 检测考场时间冲突
            if (checkRoomTimeConflict(request.getLocation(), exam.getExamTime(), exam.getDuration())) {
                log.warn("考场 {} 存在时间冲突，跳过创建", request.getRoomName());
                continue;
            }

            // 创建考场实体
            ExamRoom examRoom = ExamRoom.builder()
                    .exam(exam)
                    .roomName(request.getRoomName())
                    .location(request.getLocation())
                    .capacity(request.getCapacity())
                    .assignedCount(0)
                    .build();

            examRoom = examRoomRepository.save(examRoom);
            createdRooms.add(examRoom);
            log.info("考场创建成功: {}", examRoom.getRoomName());
        }

        log.info("批量创建完成，成功创建 {} 个考场", createdRooms.size());

        return createdRooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据考试ID查询所有考场
     */
    public List<ExamRoomDTO> findByExamId(Long examId) {
        log.info("查询考试考场列表，考试ID: {}", examId);

        List<ExamRoom> examRooms = examRoomRepository.findByExamId(examId);
        return examRooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 检测考场地点和时间冲突
     */
    public boolean checkRoomTimeConflict(String location, LocalDateTime examTime, Integer duration) {
        LocalDateTime startTime = examTime;
        LocalDateTime endTime = examTime.plusMinutes(duration);

        List<ExamRoom> conflictRooms = examRoomRepository
                .findByLocationAndTimeRange(location, startTime, endTime);

        return !conflictRooms.isEmpty();
    }

    // ==================== 6.2 学生分配方法 ====================

    /**
     * 分配学生到考场
     */
    @Transactional
    public void assignStudentToRoom(Long roomId, Long studentId, String seatNumber) {
        log.info("分配学生到考场，考场ID: {}, 学生ID: {}", roomId, studentId);

        // 查询考场
        ExamRoom examRoom = examRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_ROOM_NOT_FOUND));

        // 查询学生
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 验证学生选修该课程
        boolean isEnrolled = courseSelectionRepository.existsActiveSelection(
                studentId, examRoom.getExam().getCourseOffering().getId());
        if (!isEnrolled) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_ENROLLED);
        }

        // 验证学生未被分配
        if (examRoomStudentRepository.existsByStudentIdAndExamId(
                studentId, examRoom.getExam().getId())) {
            throw new BusinessException(ErrorCode.STUDENT_ALREADY_ASSIGNED);
        }

        // 验证考场容量
        if (examRoom.isFull()) {
            throw new BusinessException(ErrorCode.EXAM_ROOM_FULL);
        }

        // 创建分配记录
        ExamRoomStudent examRoomStudent = ExamRoomStudent.builder()
                .examRoom(examRoom)
                .student(student)
                .seatNumber(seatNumber)
                .build();

        examRoomStudentRepository.save(examRoomStudent);

        // 更新考场人数
        examRoom.incrementAssignedCount();
        examRoomRepository.save(examRoom);

        log.info("学生分配成功，学生ID: {}, 座位号: {}", studentId, seatNumber);
    }

    /**
     * 从考场移除学生
     */
    @Transactional
    public void removeStudentFromRoom(Long roomId, Long studentId) {
        log.info("从考场移除学生，考场ID: {}, 学生ID: {}", roomId, studentId);

        // 查询考场
        ExamRoom examRoom = examRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_ROOM_NOT_FOUND));

        // 删除分配记录
        examRoomStudentRepository.deleteByExamRoomIdAndStudentId(roomId, studentId);

        // 更新考场人数
        examRoom.decrementAssignedCount();
        examRoomRepository.save(examRoom);

        log.info("学生移除成功，学生ID: {}", studentId);
    }

    /**
     * 查询考场的所有学生
     */
    public List<ExamRoomStudentDTO> findStudentsByRoomId(Long roomId) {
        log.info("查询考场学生列表，考场ID: {}", roomId);

        List<ExamRoomStudent> students = examRoomStudentRepository
                .findByExamRoomIdWithStudent(roomId);

        return students.stream()
                .map(this::convertToStudentDTO)
                .collect(Collectors.toList());
    }

    // ==================== 6.3 自动分配功能 ====================

    /**
     * 自动分配学生到考场
     */
    @Transactional
    public void autoAssignStudents(Long examId) {
        log.info("开始自动分配学生，考试ID: {}", examId);

        // 查询考试
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        // 查询考场列表
        List<ExamRoom> rooms = examRoomRepository.findByExamId(examId);
        if (rooms.isEmpty()) {
            throw new BusinessException(ErrorCode.EXAM_NO_ROOMS);
        }

        // 获取选课学生列表，按学号排序
        List<Student> students = courseSelectionRepository
                .findByOfferingId(exam.getCourseOffering().getId())
                .stream()
                .map(CourseSelection::getStudent)
                .sorted(Comparator.comparing(Student::getStudentNo))
                .collect(Collectors.toList());

        // 验证考场容量是否足够
        int totalCapacity = rooms.stream().mapToInt(ExamRoom::getCapacity).sum();
        if (students.size() > totalCapacity) {
            throw new BusinessException(ErrorCode.EXAM_ROOM_CAPACITY_INSUFFICIENT);
        }

        // 清除现有分配（如果有）
        log.info("清除现有学生分配记录");
        for (ExamRoom room : rooms) {
            // 删除该考场的所有学生分配记录
            examRoomStudentRepository.deleteByExamRoomId(room.getId());
            room.setAssignedCount(0);
        }
        // 批量更新考场（清零已分配人数）
        examRoomRepository.saveAll(rooms);
        log.info("现有分配已清除");

        // 按顺序分配学生到考场 - 使用批量操作优化性能
        int studentIndex = 0;
        List<ExamRoomStudent> allAssignments = new ArrayList<>();
        
        for (ExamRoom room : rooms) {
            int assignCount = Math.min(room.getCapacity(), students.size() - studentIndex);
            
            for (int i = 0; i < assignCount; i++) {
                Student student = students.get(studentIndex);
                String seatNumber = generateSeatNumber(i);

                ExamRoomStudent ers = ExamRoomStudent.builder()
                        .examRoom(room)
                        .student(student)
                        .seatNumber(seatNumber)
                        .build();
                
                allAssignments.add(ers);
                studentIndex++;
            }

            // 更新考场人数
            room.setAssignedCount(assignCount);
            log.info("考场 {} 准备分配 {} 名学生", room.getRoomName(), assignCount);
        }

        // 批量保存所有学生分配记录
        log.info("批量保存 {} 条学生分配记录", allAssignments.size());
        examRoomStudentRepository.saveAll(allAssignments);
        
        // 批量更新所有考场的已分配人数
        examRoomRepository.saveAll(rooms);

        log.info("自动分配完成，共分配 {} 名学生到 {} 个考场", studentIndex, rooms.size());
    }

    /**
     * 生成座位号
     * 格式：A01, A02, ..., A30, B01, B02, ...
     */
    public String generateSeatNumber(int index) {
        int row = index / 30; // 每行30个座位
        int col = index % 30 + 1;
        char rowLetter = (char) ('A' + row);
        return String.format("%c%02d", rowLetter, col);
    }

    // ==================== 辅助方法 ====================

    /**
     * 转换为ExamRoomDTO
     */
    private ExamRoomDTO convertToDTO(ExamRoom examRoom) {
        return ExamRoomDTO.builder()
                .id(examRoom.getId())
                .examId(examRoom.getExam().getId())
                .roomName(examRoom.getRoomName())
                .location(examRoom.getLocation())
                .capacity(examRoom.getCapacity())
                .assignedCount(examRoom.getAssignedCount())
                .remainingCapacity(examRoom.getRemainingCapacity())
                .createdAt(examRoom.getCreatedAt())
                .updatedAt(examRoom.getUpdatedAt())
                .build();
    }

    /**
     * 转换为ExamRoomStudentDTO
     */
    private ExamRoomStudentDTO convertToStudentDTO(ExamRoomStudent ers) {
        Student student = ers.getStudent();
        
        // 构建嵌套的student对象
        ExamRoomStudentDTO.StudentInfo studentInfo = ExamRoomStudentDTO.StudentInfo.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .className(student.getClassName())
                .major(student.getMajor() != null ? ExamRoomStudentDTO.MajorInfo.builder()
                        .id(student.getMajor().getId())
                        .name(student.getMajor().getName())
                        .build() : null)
                .build();
        
        return ExamRoomStudentDTO.builder()
                .id(ers.getId())
                .examRoomId(ers.getExamRoom().getId())
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .majorName(student.getMajor() != null ? student.getMajor().getName() : null)
                .className(student.getClassName())
                .seatNumber(ers.getSeatNumber())
                .student(studentInfo)
                .build();
    }

    // ==================== 统计功能 ====================

    /**
     * 获取考场统计数据
     */
    public com.university.academic.dto.RoomStatisticsDTO getRoomStatistics(Long semesterId) {
        log.info("获取考场统计: semesterId={}", semesterId);
        
        // 查询学期信息
        Semester semester = null;
        if (semesterId != null) {
            semester = semesterRepository.findById(semesterId).orElse(null);
        }
        
        // 查询所有考场
        List<ExamRoom> rooms;
        if (semesterId != null) {
            // 查询该学期的所有考试的考场
            List<Exam> exams = examRepository.findBySemesterAndStatus(
                semesterId, null, org.springframework.data.domain.Pageable.unpaged()).getContent();
            rooms = exams.stream()
                .flatMap(exam -> examRoomRepository.findByExamId(exam.getId()).stream())
                .collect(Collectors.toList());
        } else {
            rooms = examRoomRepository.findAll();
        }
        
        long totalRooms = rooms.size();
        int totalCapacity = rooms.stream()
            .mapToInt(ExamRoom::getCapacity)
            .sum();
        int usedCapacity = rooms.stream()
            .mapToInt(ExamRoom::getAssignedCount)
            .sum();
        int remainingCapacity = totalCapacity - usedCapacity;
        double utilizationRate = totalCapacity > 0 ? 
            (double) usedCapacity / totalCapacity * 100 : 0.0;
        
        return com.university.academic.dto.RoomStatisticsDTO.builder()
                .semesterId(semesterId)
                .semesterName(semester != null ? semester.getSemesterName() : "全部学期")
                .totalRooms(totalRooms)
                .totalCapacity(totalCapacity)
                .usedCapacity(usedCapacity)
                .remainingCapacity(remainingCapacity)
                .utilizationRate(Math.round(utilizationRate * 100) / 100.0)
                .build();
    }
}

