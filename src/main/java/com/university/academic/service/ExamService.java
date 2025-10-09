package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.dto.*;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试服务类
 * 提供考试管理的业务逻辑
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamRoomStudentRepository examRoomStudentRepository;
    private final ExamInvigilatorRepository examInvigilatorRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final SemesterRepository semesterRepository;
    private final NotificationService notificationService;

    // ==================== 5.1 考试CRUD方法 ====================

    /**
     * 创建考试
     */
    @Transactional
    public ExamDTO createExam(CreateExamRequest request) {
        log.info("创建考试: {}", request.getName());

        // 验证开课计划是否存在
        CourseOffering courseOffering = courseOfferingRepository.findById(request.getCourseOfferingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        // 创建考试实体
        Exam exam = Exam.builder()
                .name(request.getName())
                .type(request.getType())
                .courseOffering(courseOffering)
                .examTime(request.getExamTime())
                .duration(request.getDuration())
                .totalScore(request.getTotalScore())
                .description(request.getDescription())
                .status(ExamStatus.DRAFT)
                .build();

        exam = examRepository.save(exam);
        log.info("考试创建成功，ID: {}", exam.getId());

        return convertToDTO(exam);
    }

    /**
     * 更新考试
     */
    @CacheEvict(value = CacheConfig.CACHE_EXAMS, key = "#id")
    @Transactional
    public ExamDTO updateExam(Long id, UpdateExamRequest request) {
        log.info("更新考试，ID: {}", id);

        Exam exam = findById(id);

        // 验证考试状态为DRAFT
        if (!exam.isEditable()) {
            throw new BusinessException(ErrorCode.EXAM_NOT_EDITABLE);
        }

        // 更新考试信息
        exam.setName(request.getName());
        exam.setType(request.getType());
        exam.setExamTime(request.getExamTime());
        exam.setDuration(request.getDuration());
        exam.setTotalScore(request.getTotalScore());
        exam.setDescription(request.getDescription());

        exam = examRepository.save(exam);
        log.info("考试更新成功，ID: {}", id);

        return convertToDTO(exam);
    }

    /**
     * 删除考试
     */
    @CacheEvict(value = CacheConfig.CACHE_EXAMS, key = "#id")
    @Transactional
    public void deleteExam(Long id) {
        log.info("删除考试，ID: {}", id);

        Exam exam = findById(id);

        // 验证考试状态为DRAFT
        if (!exam.isEditable()) {
            throw new BusinessException(ErrorCode.EXAM_NOT_EDITABLE);
        }

        // 验证没有考场
        if (exam.hasRooms()) {
            throw new BusinessException(ErrorCode.EXAM_ROOM_HAS_STUDENTS);
        }

        examRepository.delete(exam);
        log.info("考试删除成功，ID: {}", id);
    }

    /**
     * 根据ID查询考试
     */
    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
    }

    /**
     * 根据ID查询考试详情（带关联数据）
     */
    @Cacheable(value = CacheConfig.CACHE_EXAMS, key = "#id")
    @Transactional(readOnly = true)
    public ExamDTO findExamById(Long id) {
        Exam exam = examRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
        
        // 主动触发懒加载，避免Session关闭后异常
        if (exam.getExamRooms() != null) {
            exam.getExamRooms().size(); // 触发加载
        }
        
        return convertToDTO(exam);
    }

    /**
     * 条件查询考试列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<ExamDTO> findWithFilters(Long semesterId, ExamStatus status, ExamType type, 
                                         String courseNo, String courseName, Pageable pageable) {
        log.info("查询考试列表，学期ID: {}, 状态: {}, 类型: {}, 课程编号: {}, 课程名称: {}", 
                semesterId, status, type, courseNo, courseName);

        Page<Exam> examPage = examRepository.findWithFilters(
            semesterId, status, type, courseNo, courseName, pageable);
        
        // examRooms 已经通过 JOIN FETCH 加载，无需手动触发
        return examPage.map(this::convertToDTO);
    }

    // ==================== 5.2 考试状态管理 ====================

    /**
     * 发布考试
     */
    @CacheEvict(value = CacheConfig.CACHE_EXAMS, key = "#id")
    @Transactional
    public void publishExam(Long id) {
        log.info("发布考试，ID: {}", id);

        Exam exam = findById(id);

        // 验证状态为DRAFT
        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_PUBLISHED);
        }

        // 验证至少有一个考场
        if (!exam.hasRooms()) {
            throw new BusinessException(ErrorCode.EXAM_NO_ROOMS);
        }

        // 更新状态为PUBLISHED
        exam.setStatus(ExamStatus.PUBLISHED);
        examRepository.save(exam);

        // 发送通知
        sendExamNotifications(exam);

        log.info("考试发布成功，ID: {}", id);
    }

    /**
     * 取消考试
     */
    @CacheEvict(value = CacheConfig.CACHE_EXAMS, key = "#id")
    @Transactional
    public void cancelExam(Long id) {
        log.info("取消考试，ID: {}", id);

        Exam exam = findById(id);

        // 验证考试未开始
        if (!exam.isCancellable()) {
            throw new BusinessException(ErrorCode.EXAM_NOT_CANCELLABLE);
        }

        // 检查考试是否已开始
        if (LocalDateTime.now().isAfter(exam.getExamTime())) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_STARTED);
        }

        // 更新状态为CANCELLED
        exam.setStatus(ExamStatus.CANCELLED);
        examRepository.save(exam);

        log.info("考试取消成功，ID: {}", id);
    }

    /**
     * 定时任务：自动更新考试状态
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateExamStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 将已发布且到达考试时间的考试更新为进行中
        List<Exam> publishedExams = examRepository.findByStatus(ExamStatus.PUBLISHED);
        for (Exam exam : publishedExams) {
            if (exam.getExamTime().isBefore(now) || exam.getExamTime().isEqual(now)) {
                exam.setStatus(ExamStatus.IN_PROGRESS);
                examRepository.save(exam);
                log.info("考试状态更新为进行中，ID: {}", exam.getId());
            }
        }

        // 将进行中且已结束的考试更新为已结束
        List<Exam> inProgressExams = examRepository.findByStatus(ExamStatus.IN_PROGRESS);
        for (Exam exam : inProgressExams) {
            LocalDateTime endTime = exam.getExamTime().plusMinutes(exam.getDuration());
            if (endTime.isBefore(now)) {
                exam.setStatus(ExamStatus.FINISHED);
                examRepository.save(exam);
                log.info("考试状态更新为已结束，ID: {}", exam.getId());
            }
        }
    }

    // ==================== 5.3 冲突检测方法 ====================

    /**
     * 检测学生在指定时间段是否有考试冲突
     */
    public boolean checkStudentTimeConflict(Long studentId, LocalDateTime examTime, Integer duration) {
        // 考试时间前后各留30分钟缓冲
        LocalDateTime startTime = examTime.minusMinutes(30);
        LocalDateTime endTime = examTime.plusMinutes(duration + 30);

        return examRoomStudentRepository.hasExamInTimeRange(studentId, startTime, endTime);
    }

    /**
     * 检测考试中所有学生的时间冲突
     */
    public List<StudentConflictDTO> detectStudentConflicts(Long examId) {
        log.info("检测考试学生冲突，考试ID: {}", examId);

        Exam exam = findById(examId);
        List<StudentConflictDTO> conflicts = new ArrayList<>();

        // 获取所有选课学生
        List<CourseSelection> selections = courseSelectionRepository
                .findByOfferingId(exam.getCourseOffering().getId());

        for (CourseSelection selection : selections) {
            Student student = selection.getStudent();
            
            // 检查该学生是否有时间冲突
            if (checkStudentTimeConflict(student.getId(), exam.getExamTime(), exam.getDuration())) {
                // 查找冲突的考试
                LocalDateTime startTime = exam.getExamTime().minusMinutes(30);
                LocalDateTime endTime = exam.getExamTime().plusMinutes(exam.getDuration() + 30);
                
                List<Exam> conflictExams = examRepository.findByTimeRange(startTime, endTime);
                
                for (Exam conflictExam : conflictExams) {
                    if (!conflictExam.getId().equals(examId)) {
                        // 检查学生是否参加这个冲突考试
                        boolean isInConflictExam = examRoomStudentRepository
                                .existsByStudentIdAndExamId(student.getId(), conflictExam.getId());
                        
                        if (isInConflictExam) {
                            StudentConflictDTO conflict = StudentConflictDTO.builder()
                                    .studentId(student.getId())
                                    .studentNo(student.getStudentNo())
                                    .studentName(student.getName())
                                    .conflictExamId(conflictExam.getId())
                                    .conflictExamName(conflictExam.getName())
                                    .conflictExamTime(conflictExam.getExamTime())
                                    .build();
                            conflicts.add(conflict);
                        }
                    }
                }
            }
        }

        log.info("检测到 {} 个学生冲突", conflicts.size());
        return conflicts;
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送考试通知
     * 向选课学生和任课教师发送考试发布通知
     */
    private void sendExamNotifications(Exam exam) {
        try {
            log.info("发送考试通知：考试ID: {}, 考试名称: {}, 考试时间: {}", 
                    exam.getId(), exam.getName(), exam.getExamTime());
            
            CourseOffering offering = exam.getCourseOffering();
            Course course = offering.getCourse();
            Teacher teacher = offering.getTeacher();
            
            // 构建通知内容
            String examTypeName = getExamTypeName(exam.getType());
            String notificationTitle = String.format("%s - %s通知", course.getName(), examTypeName);
            String notificationContent = String.format(
                "《%s》%s已发布。\n" +
                "考试时间：%s\n" +
                "考试时长：%d分钟\n" +
                "总分：%d分\n" +
                "请同学们按时参加考试，提前做好准备。",
                course.getName(),
                examTypeName,
                exam.getExamTime().toString().replace("T", " "),
                exam.getDuration(),
                exam.getTotalScore()
            );
            
            // 1. 向选课学生发送通知
            List<CourseSelection> selections = courseSelectionRepository.findByOfferingId(offering.getId());
            int studentNotificationCount = 0;
            for (CourseSelection selection : selections) {
                // 只向已选课（未退课）的学生发送通知
                if (selection.getStatus() == CourseSelection.SelectionStatus.SELECTED) {
                    Student student = selection.getStudent();
                    User studentUser = student.getUser();
                    
                    if (studentUser != null) {
                        try {
                            CreateNotificationRequest request = CreateNotificationRequest.builder()
                                .title(notificationTitle)
                                .content(notificationContent)
                                .type("EXAM")
                                .targetRole("STUDENT")
                                .build();
                            
                            notificationService.publishNotification(request, studentUser.getId());
                            studentNotificationCount++;
                        } catch (Exception e) {
                            log.error("向学生发送通知失败: studentId={}, studentNo={}", 
                                    student.getId(), student.getStudentNo(), e);
                        }
                    }
                }
            }
            log.info("成功向 {} 名学生发送考试通知", studentNotificationCount);
            
            // 2. 向任课教师发送通知
            User teacherUser = teacher.getUser();
            if (teacherUser != null) {
                try {
                    String teacherNotificationContent = String.format(
                        "您任教的课程《%s》%s已发布。\n" +
                        "考试时间：%s\n" +
                        "考试时长：%d分钟\n" +
                        "选课学生数：%d人",
                        course.getName(),
                        examTypeName,
                        exam.getExamTime().toString().replace("T", " "),
                        exam.getDuration(),
                        studentNotificationCount
                    );
                    
                    CreateNotificationRequest teacherRequest = CreateNotificationRequest.builder()
                        .title(notificationTitle)
                        .content(teacherNotificationContent)
                        .type("EXAM")
                        .targetRole("TEACHER")
                        .build();
                    
                    notificationService.publishNotification(teacherRequest, teacherUser.getId());
                    log.info("成功向任课教师发送考试通知: teacherId={}, teacherNo={}", 
                            teacher.getId(), teacher.getTeacherNo());
                } catch (Exception e) {
                    log.error("向教师发送通知失败: teacherId={}, teacherNo={}", 
                            teacher.getId(), teacher.getTeacherNo(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("发送考试通知失败", e);
        }
    }
    
    /**
     * 获取考试类型名称
     */
    private String getExamTypeName(ExamType type) {
        switch (type) {
            case MIDTERM:
                return "期中考试";
            case FINAL:
                return "期末考试";
            case MAKEUP:
                return "补考";
            case RETAKE:
                return "重修考试";
            default:
                return "考试";
        }
    }

    /**
     * 转换为DTO
     */
    private ExamDTO convertToDTO(Exam exam) {
        CourseOffering offering = exam.getCourseOffering();
        Course course = offering.getCourse();
        Teacher teacher = offering.getTeacher();
        Semester semester = offering.getSemester();

        return ExamDTO.builder()
                .id(exam.getId())
                .name(exam.getName())
                .type(exam.getType().name())
                .typeDescription(exam.getType().getDescription())
                .courseOfferingId(offering.getId())
                .semesterId(semester.getId())
                .semesterName(semester.getSemesterName())
                .courseId(course.getId())
                .courseNo(course.getCourseNo())
                .courseName(course.getName())
                .teacherId(teacher.getId())
                .teacherNo(teacher.getTeacherNo())
                .teacherName(teacher.getName())
                .examTime(exam.getExamTime())
                .duration(exam.getDuration())
                .totalScore(exam.getTotalScore())
                .status(exam.getStatus().name())
                .statusDescription(exam.getStatus().getDescription())
                .description(exam.getDescription())
                .totalRooms(exam.getTotalRooms())
                .totalStudents(exam.getTotalStudents())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }

    // ==================== 数据导出方法 ====================

    /**
     * 导出考试列表（Excel）
     */
    public byte[] exportExamList(Long semesterId, ExamStatus status) {
        log.info("导出考试列表: semesterId={}, status={}", semesterId, status);
        
        // 查询考试列表
        List<Exam> exams;
        if (semesterId != null || status != null) {
            exams = examRepository.findBySemesterAndStatus(
                semesterId, status, Pageable.unpaged()).getContent();
        } else {
            exams = examRepository.findAll();
        }
        
        // 准备导出数据
        String[] headers = {"考试名称", "考试类型", "课程编号", "课程名称", "教师", 
                           "学期", "考试时间", "时长(分钟)", "状态", "考场数", "学生数"};
        
        List<Object[]> dataList = new ArrayList<>();
        for (Exam exam : exams) {
            Object[] row = new Object[11];
            row[0] = exam.getName();
            row[1] = exam.getType() != null ? exam.getType().getDescription() : "";
            row[2] = exam.getCourseOffering().getCourse().getCourseNo();
            row[3] = exam.getCourseOffering().getCourse().getName();
            row[4] = exam.getCourseOffering().getTeacher().getName();
            row[5] = exam.getCourseOffering().getSemester().getSemesterName();
            row[6] = exam.getExamTime().toString();
            row[7] = exam.getDuration();
            row[8] = exam.getStatus() != null ? exam.getStatus().getDescription() : "";
            row[9] = exam.getTotalRooms();
            row[10] = exam.getTotalStudents();
            dataList.add(row);
        }
        
        // 使用ExcelUtil导出
        com.university.academic.util.ExcelUtil excelUtil = 
            new com.university.academic.util.ExcelUtil();
        return excelUtil.exportStatisticsReport("考试列表", headers, dataList);
    }

    /**
     * 导出考场安排（Excel）
     */
    public byte[] exportExamRoomArrangement(Long examId) {
        log.info("导出考场安排: examId={}", examId);
        
        Exam exam = findById(examId);
        
        // 查询考场和学生分配
        List<ExamRoom> rooms = exam.getExamRooms();
        
        String[] headers = {"考场名称", "考场地点", "学号", "姓名", "专业", "班级", "座位号"};
        List<Object[]> dataList = new ArrayList<>();
        
        for (ExamRoom room : rooms) {
            List<ExamRoomStudent> students = examRoomStudentRepository
                .findByExamRoomIdWithStudent(room.getId());
            
            for (ExamRoomStudent ers : students) {
                Student student = ers.getStudent();
                Object[] row = new Object[7];
                row[0] = room.getRoomName();
                row[1] = room.getLocation();
                row[2] = student.getStudentNo();
                row[3] = student.getName();
                row[4] = student.getMajor() != null ? student.getMajor().getName() : "";
                row[5] = student.getClassName() != null ? student.getClassName() : "";
                row[6] = ers.getSeatNumber();
                dataList.add(row);
            }
        }
        
        com.university.academic.util.ExcelUtil excelUtil = 
            new com.university.academic.util.ExcelUtil();
        return excelUtil.exportStatisticsReport(
            exam.getName() + " - 考场安排", headers, dataList);
    }

    /**
     * 导出监考安排（Excel）
     */
    public byte[] exportInvigilatorArrangement(Long examId) {
        log.info("导出监考安排: examId={}", examId);
        
        Exam exam = findById(examId);
        
        String[] headers = {"考场名称", "考场地点", "教师工号", "教师姓名", 
                           "所在部门", "监考类型"};
        List<Object[]> dataList = new ArrayList<>();
        
        for (ExamRoom room : exam.getExamRooms()) {
            for (ExamInvigilator invigilator : room.getInvigilators()) {
                Teacher teacher = invigilator.getTeacher();
                Object[] row = new Object[6];
                row[0] = room.getRoomName();
                row[1] = room.getLocation();
                row[2] = teacher.getTeacherNo();
                row[3] = teacher.getName();
                row[4] = teacher.getDepartment() != null ? 
                        teacher.getDepartment().getName() : "";
                row[5] = invigilator.getType() != null ? 
                        invigilator.getType().getDescription() : "";
                dataList.add(row);
            }
        }
        
        com.university.academic.util.ExcelUtil excelUtil = 
            new com.university.academic.util.ExcelUtil();
        return excelUtil.exportStatisticsReport(
            exam.getName() + " - 监考安排", headers, dataList);
    }

    /**
     * 导出学生个人考试安排（PDF）
     */
    public byte[] exportStudentExamSchedule(Long studentId) {
        log.info("导出学生考试安排: studentId={}", studentId);
        
        // 查询学生的所有已发布考试
        List<ExamRoomStudent> examRoomStudents = examRoomStudentRepository
            .findByStudentIdWithDetails(studentId);
        
        // 过滤已发布的考试
        List<ExamRoomStudent> publishedExams = examRoomStudents.stream()
            .filter(ers -> {
                ExamStatus status = ers.getExamRoom().getExam().getStatus();
                return status == ExamStatus.PUBLISHED 
                    || status == ExamStatus.IN_PROGRESS 
                    || status == ExamStatus.FINISHED;
            })
            .sorted((a, b) -> a.getExamRoom().getExam().getExamTime()
                .compareTo(b.getExamRoom().getExam().getExamTime()))
            .toList();
        
        if (publishedExams.isEmpty()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        
        // 获取学生信息
        Student student = publishedExams.get(0).getStudent();
        
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(
                com.itextpdf.text.PageSize.A4);
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 中文字体
            com.itextpdf.text.pdf.BaseFont baseFont = com.itextpdf.text.pdf.BaseFont
                .createFont("STSong-Light", "UniGB-UCS2-H", 
                    com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                baseFont, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(
                baseFont, 10, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                baseFont, 10, com.itextpdf.text.Font.BOLD);
            
            // 标题
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                "个人考试安排表", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            document.add(new com.itextpdf.text.Paragraph("\n"));
            
            // 学生信息
            com.itextpdf.text.pdf.PdfPTable infoTable = 
                new com.itextpdf.text.pdf.PdfPTable(4);
            infoTable.setWidthPercentage(100);
            
            addPdfCell(infoTable, "学号：" + student.getStudentNo(), contentFont);
            addPdfCell(infoTable, "姓名：" + student.getName(), contentFont);
            addPdfCell(infoTable, "专业：" + 
                (student.getMajor() != null ? student.getMajor().getName() : ""), contentFont);
            addPdfCell(infoTable, "班级：" + 
                (student.getClassName() != null ? student.getClassName() : ""), contentFont);
            
            document.add(infoTable);
            document.add(new com.itextpdf.text.Paragraph("\n"));
            
            // 考试安排表格
            com.itextpdf.text.pdf.PdfPTable examTable = 
                new com.itextpdf.text.pdf.PdfPTable(7);
            examTable.setWidthPercentage(100);
            float[] columnWidths = {0.5f, 1.5f, 1f, 1.5f, 1f, 1f, 0.7f};
            examTable.setWidths(columnWidths);
            
            // 表头
            addPdfHeaderCell(examTable, "序号", boldFont);
            addPdfHeaderCell(examTable, "考试名称", boldFont);
            addPdfHeaderCell(examTable, "课程", boldFont);
            addPdfHeaderCell(examTable, "考试时间", boldFont);
            addPdfHeaderCell(examTable, "考场", boldFont);
            addPdfHeaderCell(examTable, "地点", boldFont);
            addPdfHeaderCell(examTable, "座位", boldFont);
            
            // 数据行
            int index = 1;
            for (ExamRoomStudent ers : publishedExams) {
                Exam exam = ers.getExamRoom().getExam();
                ExamRoom room = ers.getExamRoom();
                
                addPdfDataCell(examTable, String.valueOf(index++), contentFont);
                addPdfDataCell(examTable, exam.getName(), contentFont);
                addPdfDataCell(examTable, exam.getCourseOffering().getCourse().getName(), 
                    contentFont);
                addPdfDataCell(examTable, exam.getExamTime().toString().replace("T", " "), 
                    contentFont);
                addPdfDataCell(examTable, room.getRoomName(), contentFont);
                addPdfDataCell(examTable, room.getLocation(), contentFont);
                addPdfDataCell(examTable, ers.getSeatNumber(), contentFont);
            }
            
            document.add(examTable);
            document.add(new com.itextpdf.text.Paragraph("\n\n"));
            
            // 页脚
            com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                "打印时间：" + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                contentFont);
            footer.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(footer);
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("导出学生考试安排PDF失败", e);
            throw new BusinessException(ErrorCode.EXPORT_ERROR);
        }
    }

    /**
     * 导出教师监考安排（PDF）
     */
    public byte[] exportTeacherInvigilationSchedule(Long teacherId) {
        log.info("导出教师监考安排: teacherId={}", teacherId);
        
        // 查询教师的所有监考任务
        List<ExamInvigilator> invigilations = examInvigilatorRepository
            .findByTeacherId(teacherId);
        
        // 排序
        invigilations = invigilations.stream()
            .sorted((a, b) -> a.getExamRoom().getExam().getExamTime()
                .compareTo(b.getExamRoom().getExam().getExamTime()))
            .toList();
        
        if (invigilations.isEmpty()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        
        // 获取教师信息
        Teacher teacher = invigilations.get(0).getTeacher();
        
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(
                com.itextpdf.text.PageSize.A4);
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // 中文字体
            com.itextpdf.text.pdf.BaseFont baseFont = com.itextpdf.text.pdf.BaseFont
                .createFont("STSong-Light", "UniGB-UCS2-H", 
                    com.itextpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                baseFont, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(
                baseFont, 10, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                baseFont, 10, com.itextpdf.text.Font.BOLD);
            
            // 标题
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                "监考任务安排表", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            document.add(new com.itextpdf.text.Paragraph("\n"));
            
            // 教师信息
            com.itextpdf.text.pdf.PdfPTable infoTable = 
                new com.itextpdf.text.pdf.PdfPTable(3);
            infoTable.setWidthPercentage(100);
            
            addPdfCell(infoTable, "教师工号：" + teacher.getTeacherNo(), contentFont);
            addPdfCell(infoTable, "姓名：" + teacher.getName(), contentFont);
            addPdfCell(infoTable, "部门：" + 
                (teacher.getDepartment() != null ? teacher.getDepartment().getName() : ""), 
                contentFont);
            
            document.add(infoTable);
            document.add(new com.itextpdf.text.Paragraph("\n"));
            
            // 监考安排表格
            com.itextpdf.text.pdf.PdfPTable examTable = 
                new com.itextpdf.text.pdf.PdfPTable(7);
            examTable.setWidthPercentage(100);
            float[] columnWidths = {0.5f, 1.5f, 1f, 1.5f, 1f, 1f, 0.8f};
            examTable.setWidths(columnWidths);
            
            // 表头
            addPdfHeaderCell(examTable, "序号", boldFont);
            addPdfHeaderCell(examTable, "考试名称", boldFont);
            addPdfHeaderCell(examTable, "课程", boldFont);
            addPdfHeaderCell(examTable, "考试时间", boldFont);
            addPdfHeaderCell(examTable, "考场", boldFont);
            addPdfHeaderCell(examTable, "地点", boldFont);
            addPdfHeaderCell(examTable, "监考类型", boldFont);
            
            // 数据行
            int index = 1;
            for (ExamInvigilator inv : invigilations) {
                Exam exam = inv.getExamRoom().getExam();
                ExamRoom room = inv.getExamRoom();
                
                addPdfDataCell(examTable, String.valueOf(index++), contentFont);
                addPdfDataCell(examTable, exam.getName(), contentFont);
                addPdfDataCell(examTable, exam.getCourseOffering().getCourse().getName(), 
                    contentFont);
                addPdfDataCell(examTable, exam.getExamTime().toString().replace("T", " "), 
                    contentFont);
                addPdfDataCell(examTable, room.getRoomName(), contentFont);
                addPdfDataCell(examTable, room.getLocation(), contentFont);
                addPdfDataCell(examTable, inv.getType() != null ? 
                    inv.getType().getDescription() : "", contentFont);
            }
            
            document.add(examTable);
            document.add(new com.itextpdf.text.Paragraph("\n\n"));
            
            // 页脚
            com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                "打印时间：" + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                contentFont);
            footer.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            document.add(footer);
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("导出教师监考安排PDF失败", e);
            throw new BusinessException(ErrorCode.EXPORT_ERROR);
        }
    }

    // PDF辅助方法
    private void addPdfCell(com.itextpdf.text.pdf.PdfPTable table, String content, 
            com.itextpdf.text.Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(content, font));
        cell.setBorder(com.itextpdf.text.Rectangle.BOX);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addPdfHeaderCell(com.itextpdf.text.pdf.PdfPTable table, String content, 
            com.itextpdf.text.Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(content, font));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addPdfDataCell(com.itextpdf.text.pdf.PdfPTable table, String content, 
            com.itextpdf.text.Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(content, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    // ==================== 统计功能 ====================

    /**
     * 获取考试统计数据
     */
    public com.university.academic.dto.ExamStatisticsDTO getExamStatistics(Long semesterId) {
        log.info("获取考试统计: semesterId={}", semesterId);
        
        // 查询学期信息
        Semester semester = null;
        if (semesterId != null) {
            semester = semesterRepository.findById(semesterId).orElse(null);
        }
        
        // 统计各状态考试数量
        Long totalExams = examRepository.countBySemesterId(semesterId);
        Long draftExams = examRepository.countBySemesterIdAndStatus(semesterId, ExamStatus.DRAFT);
        Long publishedExams = examRepository.countBySemesterIdAndStatus(semesterId, ExamStatus.PUBLISHED);
        Long inProgressExams = examRepository.countBySemesterIdAndStatus(semesterId, ExamStatus.IN_PROGRESS);
        Long finishedExams = examRepository.countBySemesterIdAndStatus(semesterId, ExamStatus.FINISHED);
        Long cancelledExams = examRepository.countBySemesterIdAndStatus(semesterId, ExamStatus.CANCELLED);
        
        // 统计考场和学生总数
        List<Exam> exams;
        if (semesterId != null) {
            exams = examRepository.findBySemesterAndStatus(semesterId, null, Pageable.unpaged()).getContent();
        } else {
            exams = examRepository.findAll();
        }
        
        long totalRooms = exams.stream()
            .mapToLong(Exam::getTotalRooms)
            .sum();
        
        long totalStudents = exams.stream()
            .mapToLong(Exam::getTotalStudents)
            .sum();
        
        return com.university.academic.dto.ExamStatisticsDTO.builder()
                .semesterId(semesterId)
                .semesterName(semester != null ? semester.getSemesterName() : "全部学期")
                .totalExams(totalExams)
                .draftExams(draftExams)
                .publishedExams(publishedExams)
                .inProgressExams(inProgressExams)
                .finishedExams(finishedExams)
                .cancelledExams(cancelledExams)
                .totalRooms(totalRooms)
                .totalStudents(totalStudents)
                .build();
    }

    // ==================== 5.8 获取可用学生 ====================

    /**
     * 获取考试的可用学生列表（选课学生）
     */
    @Transactional(readOnly = true)
    public List<StudentDTO> getAvailableStudents(Long examId) {
        log.info("获取考试可用学生，考试ID: {}", examId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        // 获取该课程的所有选课学生
        List<CourseSelection> selections = courseSelectionRepository
                .findByOfferingId(exam.getCourseOffering().getId());

        // 转换为StudentDTO
        List<StudentDTO> students = selections.stream()
                .map(selection -> {
                    Student student = selection.getStudent();
                    // 确保懒加载数据被加载
                    if (student.getMajor() != null) {
                        student.getMajor().getName();
                        if (student.getMajor().getDepartment() != null) {
                            student.getMajor().getDepartment().getName();
                        }
                    }
                    return convertToStudentDTO(student);
                })
                .sorted(Comparator.comparing(StudentDTO::getStudentNo))
                .collect(Collectors.toList());

        log.info("查询到 {} 个可用学生", students.size());
        return students;
    }

    /**
     * 将Student转换为StudentDTO
     */
    private StudentDTO convertToStudentDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gender(student.getGender() != null ? student.getGender().name() : null)
                .majorId(student.getMajor() != null ? student.getMajor().getId() : null)
                .majorName(student.getMajor() != null ? student.getMajor().getName() : null)
                .departmentId(student.getMajor() != null && student.getMajor().getDepartment() != null 
                        ? student.getMajor().getDepartment().getId() : null)
                .departmentName(student.getMajor() != null && student.getMajor().getDepartment() != null 
                        ? student.getMajor().getDepartment().getName() : null)
                .className(student.getClassName())
                .enrollmentYear(student.getEnrollmentYear())
                .phone(student.getPhone())
                .build();
    }

    /**
     * 学生冲突DTO（内部类）
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StudentConflictDTO {
        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long conflictExamId;
        private String conflictExamName;
        private LocalDateTime conflictExamTime;
    }
}

