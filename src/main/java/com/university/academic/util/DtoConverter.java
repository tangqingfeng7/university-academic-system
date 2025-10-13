package com.university.academic.util;

import com.university.academic.dto.*;
import com.university.academic.entity.*;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * DTO转换工具类
 * 负责Entity与DTO之间的转换
 *
 * @author Academic System Team
 */
@Component
@RequiredArgsConstructor
public class DtoConverter {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    /**
     * 获取用户的真实姓名（优先从Teacher/Student获取，否则返回用户名）
     */
    private String getUserRealName(User user) {
        if (user == null) {
            return null;
        }
        
        // 尝试从Teacher获取
        Teacher teacher = teacherRepository.findByUserId(user.getId()).orElse(null);
        if (teacher != null && teacher.getName() != null) {
            return teacher.getName();
        }
        
        // 尝试从Student获取
        Student student = studentRepository.findByUserId(user.getId()).orElse(null);
        if (student != null && student.getName() != null) {
            return student.getName();
        }
        
        // 都没有则返回用户名
        return user.getUsername();
    }

    /**
     * 将User实体转换为UserDTO
     *
     * @param user User实体
     * @return UserDTO
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .firstLogin(user.getFirstLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 将UserDTO转换为User实体（仅用于更新场景）
     *
     * @param userDTO UserDTO
     * @return User实体
     */
    public User toUserEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        if (userDTO.getRole() != null) {
            user.setRole(User.UserRole.valueOf(userDTO.getRole()));
        }
        user.setEnabled(userDTO.getEnabled());
        user.setFirstLogin(userDTO.getFirstLogin());

        return user;
    }

    /**
     * 将Department实体转换为DepartmentDTO
     *
     * @param department Department实体
     * @return DepartmentDTO
     */
    public DepartmentDTO toDepartmentDTO(Department department) {
        if (department == null) {
            return null;
        }

        return DepartmentDTO.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    /**
     * 将Major实体转换为MajorDTO
     *
     * @param major Major实体
     * @return MajorDTO
     */
    public MajorDTO toMajorDTO(Major major) {
        if (major == null) {
            return null;
        }

        return MajorDTO.builder()
                .id(major.getId())
                .code(major.getCode())
                .name(major.getName())
                .departmentId(major.getDepartment() != null ? major.getDepartment().getId() : null)
                .departmentName(major.getDepartment() != null ? major.getDepartment().getName() : null)
                .createdAt(major.getCreatedAt())
                .updatedAt(major.getUpdatedAt())
                .build();
    }

    /**
     * 将Student实体转换为StudentDTO
     *
     * @param student Student实体
     * @return StudentDTO
     */
    public StudentDTO toStudentDTO(Student student) {
        if (student == null) {
            return null;
        }

        return StudentDTO.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gender(student.getGender() != null ? student.getGender().name() : null)
                .birthDate(student.getBirthDate())
                .enrollmentYear(student.getEnrollmentYear())
                .majorId(student.getMajor() != null ? student.getMajor().getId() : null)
                .majorName(student.getMajor() != null ? student.getMajor().getName() : null)
                .departmentId(student.getMajor() != null && student.getMajor().getDepartment() != null 
                        ? student.getMajor().getDepartment().getId() : null)
                .departmentName(student.getMajor() != null && student.getMajor().getDepartment() != null 
                        ? student.getMajor().getDepartment().getName() : null)
                .className(student.getClassName())
                .phone(student.getPhone())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .username(student.getUser() != null ? student.getUser().getUsername() : null)
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    /**
     * 将Teacher实体转换为TeacherDTO
     *
     * @param teacher Teacher实体
     * @return TeacherDTO
     */
    public TeacherDTO toTeacherDTO(Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        return TeacherDTO.builder()
                .id(teacher.getId())
                .teacherNo(teacher.getTeacherNo())
                .name(teacher.getName())
                .gender(teacher.getGender() != null ? teacher.getGender().name() : null)
                .title(teacher.getTitle())
                .departmentId(teacher.getDepartment() != null ? teacher.getDepartment().getId() : null)
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .phone(teacher.getPhone())
                .email(teacher.getEmail())
                .userId(teacher.getUser() != null ? teacher.getUser().getId() : null)
                .username(teacher.getUser() != null ? teacher.getUser().getUsername() : null)
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    /**
     * 将Course实体转换为CourseDTO
     *
     * @param course Course实体
     * @return CourseDTO
     */
    public CourseDTO toCourseDTO(Course course) {
        if (course == null) {
            return null;
        }

        return CourseDTO.builder()
                .id(course.getId())
                .courseNo(course.getCourseNo())
                .name(course.getName())
                .credits(course.getCredits())
                .hours(course.getHours())
                .type(course.getType() != null ? course.getType().name() : null)
                .typeDescription(course.getType() != null ? course.getType().getDescription() : null)
                .departmentId(course.getDepartment() != null ? course.getDepartment().getId() : null)
                .departmentName(course.getDepartment() != null ? course.getDepartment().getName() : null)
                .description(course.getDescription())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    /**
     * 将Semester实体转换为SemesterDTO
     *
     * @param semester Semester实体
     * @return SemesterDTO
     */
    public SemesterDTO toSemesterDTO(Semester semester) {
        if (semester == null) {
            return null;
        }

        String semesterTypeName = semester.getSemesterType() == 1 ? "春季学期" : "秋季学期";

        return SemesterDTO.builder()
                .id(semester.getId())
                .academicYear(semester.getAcademicYear())
                .semesterType(semester.getSemesterType())
                .semesterTypeName(semesterTypeName)
                .semesterName(semester.getSemesterName())
                .semesterNameWithWeek(semester.getSemesterNameWithWeek())
                .currentWeek(semester.getCurrentWeek())
                .totalWeeks(semester.getTotalWeeks())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .courseSelectionStart(semester.getCourseSelectionStart())
                .courseSelectionEnd(semester.getCourseSelectionEnd())
                .courseSelectionEnabled(semester.getCourseSelectionEnabled())
                .courseSelectionStatus(semester.getCourseSelectionStatus())
                .courseSelectionAvailable(semester.isCourseSelectionAvailable())
                .active(semester.getActive())
                .createdAt(semester.getCreatedAt())
                .updatedAt(semester.getUpdatedAt())
                .build();
    }

    /**
     * 将CourseOffering实体转换为CourseOfferingDTO
     *
     * @param offering CourseOffering实体
     * @return CourseOfferingDTO
     */
    public CourseOfferingDTO toCourseOfferingDTO(CourseOffering offering) {
        if (offering == null) {
            return null;
        }

        return CourseOfferingDTO.builder()
                .id(offering.getId())
                .semesterId(offering.getSemester() != null ? offering.getSemester().getId() : null)
                .semesterName(offering.getSemester() != null ? offering.getSemester().getSemesterName() : null)
                .courseId(offering.getCourse() != null ? offering.getCourse().getId() : null)
                .courseNo(offering.getCourse() != null ? offering.getCourse().getCourseNo() : null)
                .courseName(offering.getCourse() != null ? offering.getCourse().getName() : null)
                .courseType(offering.getCourse() != null && offering.getCourse().getType() != null ? 
                        offering.getCourse().getType().name() : null)
                .credits(offering.getCourse() != null ? offering.getCourse().getCredits() : null)
                .teacherId(offering.getTeacher() != null ? offering.getTeacher().getId() : null)
                .teacherNo(offering.getTeacher() != null ? offering.getTeacher().getTeacherNo() : null)
                .teacherName(offering.getTeacher() != null ? offering.getTeacher().getName() : null)
                .schedule(offering.getSchedule())
                .location(offering.getLocation())
                .capacity(offering.getCapacity())
                .enrolled(offering.getEnrolled())
                .remainingCapacity(offering.getRemainingCapacity())
                .status(offering.getStatus() != null ? offering.getStatus().name() : null)
                .statusDescription(offering.getStatus() != null ? offering.getStatus().getDescription() : null)
                .version(offering.getVersion())
                .createdAt(offering.getCreatedAt())
                .updatedAt(offering.getUpdatedAt())
                .build();
    }

    /**
     * 将CourseSelection实体转换为CourseSelectionDTO
     *
     * @param selection CourseSelection实体
     * @return CourseSelectionDTO
     */
    public CourseSelectionDTO toCourseSelectionDTO(CourseSelection selection) {
        if (selection == null) {
            return null;
        }

        CourseOffering offering = selection.getOffering();
        
        return CourseSelectionDTO.builder()
                .id(selection.getId())
                .studentId(selection.getStudent() != null ? selection.getStudent().getId() : null)
                .studentNo(selection.getStudent() != null ? selection.getStudent().getStudentNo() : null)
                .studentName(selection.getStudent() != null ? selection.getStudent().getName() : null)
                .offeringId(offering != null ? offering.getId() : null)
                .semesterName(offering != null && offering.getSemester() != null ? 
                        offering.getSemester().getSemesterName() : null)
                .courseId(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getId() : null)
                .courseNo(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getCourseNo() : null)
                .courseName(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getName() : null)
                .credits(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getCredits() : null)
                .teacherName(offering != null && offering.getTeacher() != null ? 
                        offering.getTeacher().getName() : null)
                .schedule(offering != null ? offering.getSchedule() : null)
                .location(offering != null ? offering.getLocation() : null)
                .selectionTime(selection.getSelectionTime())
                .status(selection.getStatus() != null ? selection.getStatus().name() : null)
                .statusDescription(selection.getStatus() != null ? selection.getStatus().getDescription() : null)
                .createdAt(selection.getCreatedAt())
                .updatedAt(selection.getUpdatedAt())
                .build();
    }

    /**
     * 将Grade实体转换为GradeDTO
     *
     * @param grade Grade实体
     * @return GradeDTO
     */
    public GradeDTO toGradeDTO(Grade grade) {
        if (grade == null) {
            return null;
        }

        CourseSelection selection = grade.getCourseSelection();
        CourseOffering offering = selection != null ? selection.getOffering() : null;
        
        return GradeDTO.builder()
                .id(grade.getId())
                .courseSelectionId(selection != null ? selection.getId() : null)
                .studentId(selection != null && selection.getStudent() != null ? 
                        selection.getStudent().getId() : null)
                .studentNo(selection != null && selection.getStudent() != null ? 
                        selection.getStudent().getStudentNo() : null)
                .studentName(selection != null && selection.getStudent() != null ? 
                        selection.getStudent().getName() : null)
                .majorName(selection != null && selection.getStudent() != null && 
                        selection.getStudent().getMajor() != null ? 
                        selection.getStudent().getMajor().getName() : null)
                .className(selection != null && selection.getStudent() != null ? 
                        selection.getStudent().getClassName() : null)
                .courseId(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getId() : null)
                .courseNo(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getCourseNo() : null)
                .courseName(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getName() : null)
                .courseType(offering != null && offering.getCourse() != null && 
                        offering.getCourse().getType() != null ? 
                        offering.getCourse().getType().name() : null)
                .credits(offering != null && offering.getCourse() != null ? 
                        offering.getCourse().getCredits() : null)
                .semesterId(offering != null && offering.getSemester() != null ? 
                        offering.getSemester().getId() : null)
                .semesterName(offering != null && offering.getSemester() != null ? 
                        offering.getSemester().getSemesterName() : null)
                .teacherName(offering != null && offering.getTeacher() != null ? 
                        offering.getTeacher().getName() : null)
                .regularScore(grade.getRegularScore())
                .midtermScore(grade.getMidtermScore())
                .finalScore(grade.getFinalScore())
                .totalScore(grade.getTotalScore())
                .gradePoint(grade.getGradePoint())
                .passed(grade.isPassed())
                .status(grade.getStatus() != null ? grade.getStatus().name() : null)
                .statusDescription(grade.getStatus() != null ? grade.getStatus().getDescription() : null)
                .submittedAt(grade.getSubmittedAt())
                .createdAt(grade.getCreatedAt())
                .updatedAt(grade.getUpdatedAt())
                .build();
    }

    /**
     * 将Notification实体转换为NotificationDTO
     *
     * @param notification Notification实体
     * @return NotificationDTO
     */
    public NotificationDTO toNotificationDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType() != null ? notification.getType().name() : null)
                .typeDescription(notification.getType() != null ? 
                        notification.getType().getDescription() : null)
                .publisherId(notification.getPublisher() != null ? 
                        notification.getPublisher().getId() : null)
                .publisherName(notification.getPublisher() != null ? 
                        notification.getPublisher().getUsername() : null)
                .targetRole(notification.getTargetRole())
                .publishTime(notification.getPublishTime())
                .active(notification.getActive())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    // ==================== 考试模块转换方法 ====================

    /**
     * 将Exam实体转换为ExamDTO
     *
     * @param exam Exam实体
     * @return ExamDTO
     */
    public ExamDTO toExamDTO(Exam exam) {
        if (exam == null) {
            return null;
        }

        CourseOffering offering = exam.getCourseOffering();
        Course course = offering != null ? offering.getCourse() : null;
        Teacher teacher = offering != null ? offering.getTeacher() : null;
        Semester semester = offering != null ? offering.getSemester() : null;

        return ExamDTO.builder()
                .id(exam.getId())
                .name(exam.getName())
                .type(exam.getType() != null ? exam.getType().name() : null)
                .typeDescription(exam.getType() != null ? exam.getType().getDescription() : null)
                .courseOfferingId(offering != null ? offering.getId() : null)
                .semesterId(semester != null ? semester.getId() : null)
                .semesterName(semester != null ? semester.getSemesterName() : null)
                .courseId(course != null ? course.getId() : null)
                .courseNo(course != null ? course.getCourseNo() : null)
                .courseName(course != null ? course.getName() : null)
                .teacherId(teacher != null ? teacher.getId() : null)
                .teacherNo(teacher != null ? teacher.getTeacherNo() : null)
                .teacherName(teacher != null ? teacher.getName() : null)
                .examTime(exam.getExamTime())
                .duration(exam.getDuration())
                .totalScore(exam.getTotalScore())
                .status(exam.getStatus() != null ? exam.getStatus().name() : null)
                .statusDescription(exam.getStatus() != null ? exam.getStatus().getDescription() : null)
                .description(exam.getDescription())
                .totalRooms(exam.getTotalRooms())
                .totalStudents(exam.getTotalStudents())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }

    /**
     * 将ExamRoom实体转换为ExamRoomDTO
     *
     * @param examRoom ExamRoom实体
     * @return ExamRoomDTO
     */
    public ExamRoomDTO toExamRoomDTO(ExamRoom examRoom) {
        if (examRoom == null) {
            return null;
        }

        return ExamRoomDTO.builder()
                .id(examRoom.getId())
                .examId(examRoom.getExam() != null ? examRoom.getExam().getId() : null)
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
     * 将ExamRoomStudent实体转换为ExamRoomStudentDTO
     *
     * @param ers ExamRoomStudent实体
     * @return ExamRoomStudentDTO
     */
    public ExamRoomStudentDTO toExamRoomStudentDTO(ExamRoomStudent ers) {
        if (ers == null) {
            return null;
        }

        Student student = ers.getStudent();
        return ExamRoomStudentDTO.builder()
                .id(ers.getId())
                .examRoomId(ers.getExamRoom() != null ? ers.getExamRoom().getId() : null)
                .studentId(student != null ? student.getId() : null)
                .studentNo(student != null ? student.getStudentNo() : null)
                .studentName(student != null ? student.getName() : null)
                .majorName(student != null && student.getMajor() != null ? 
                        student.getMajor().getName() : null)
                .className(student != null ? student.getClassName() : null)
                .seatNumber(ers.getSeatNumber())
                .build();
    }

    /**
     * 将ExamInvigilator实体转换为ExamInvigilatorDTO
     *
     * @param invigilator ExamInvigilator实体
     * @return ExamInvigilatorDTO
     */
    public ExamInvigilatorDTO toExamInvigilatorDTO(ExamInvigilator invigilator) {
        if (invigilator == null) {
            return null;
        }

        Teacher teacher = invigilator.getTeacher();
        return ExamInvigilatorDTO.builder()
                .id(invigilator.getId())
                .examRoomId(invigilator.getExamRoom() != null ? 
                        invigilator.getExamRoom().getId() : null)
                .teacherId(teacher != null ? teacher.getId() : null)
                .teacherNo(teacher != null ? teacher.getTeacherNo() : null)
                .teacherName(teacher != null ? teacher.getName() : null)
                .departmentName(teacher != null && teacher.getDepartment() != null ? 
                        teacher.getDepartment().getName() : null)
                .type(invigilator.getType() != null ? invigilator.getType().name() : null)
                .typeDescription(invigilator.getType() != null ? 
                        invigilator.getType().getDescription() : null)
                .build();
    }

    /**
     * 将ExamRoomStudent实体转换为StudentExamDTO（学生端展示）
     *
     * @param ers ExamRoomStudent实体
     * @return StudentExamDTO
     */
    public StudentExamDTO toStudentExamDTO(ExamRoomStudent ers) {
        if (ers == null) {
            return null;
        }

        ExamRoom room = ers.getExamRoom();
        Exam exam = room != null ? room.getExam() : null;
        Course course = exam != null && exam.getCourseOffering() != null ? 
                exam.getCourseOffering().getCourse() : null;

        return StudentExamDTO.builder()
                .id(exam != null ? exam.getId() : null)
                .examName(exam != null ? exam.getName() : null)
                .courseNo(course != null ? course.getCourseNo() : null)
                .courseName(course != null ? course.getName() : null)
                .type(exam != null && exam.getType() != null ? exam.getType().name() : null)
                .typeDescription(exam != null && exam.getType() != null ? 
                        exam.getType().getDescription() : null)
                .examTime(exam != null ? exam.getExamTime() : null)
                .duration(exam != null ? exam.getDuration() : null)
                .endTime(exam != null ? exam.getExamTime().plusMinutes(exam.getDuration()) : null)
                .roomName(room != null ? room.getRoomName() : null)
                .location(room != null ? room.getLocation() : null)
                .seatNumber(ers.getSeatNumber())
                .status(exam != null && exam.getStatus() != null ? exam.getStatus().name() : null)
                .statusDescription(exam != null && exam.getStatus() != null ? 
                        exam.getStatus().getDescription() : null)
                .description(exam != null ? exam.getDescription() : null)
                .build();
    }

    /**
     * 将Exam实体转换为TeacherExamDTO（教师端展示）
     *
     * @param exam Exam实体
     * @return TeacherExamDTO
     */
    public TeacherExamDTO toTeacherExamDTO(Exam exam) {
        if (exam == null) {
            return null;
        }

        Course course = exam.getCourseOffering() != null ? 
                exam.getCourseOffering().getCourse() : null;

        return TeacherExamDTO.builder()
                .id(exam.getId())
                .examName(exam.getName())
                .courseNo(course != null ? course.getCourseNo() : null)
                .courseName(course != null ? course.getName() : null)
                .type(exam.getType() != null ? exam.getType().name() : null)
                .typeDescription(exam.getType() != null ? exam.getType().getDescription() : null)
                .examTime(exam.getExamTime())
                .duration(exam.getDuration())
                .status(exam.getStatus() != null ? exam.getStatus().name() : null)
                .statusDescription(exam.getStatus() != null ? exam.getStatus().getDescription() : null)
                .totalRooms(exam.getTotalRooms())
                .totalStudents(exam.getTotalStudents())
                .description(exam.getDescription())
                .build();
    }

    /**
     * 将ExamInvigilator实体转换为InvigilationDTO（教师端展示）
     *
     * @param invigilator ExamInvigilator实体
     * @return InvigilationDTO
     */
    public InvigilationDTO toInvigilationDTO(ExamInvigilator invigilator) {
        if (invigilator == null) {
            return null;
        }

        ExamRoom room = invigilator.getExamRoom();
        Exam exam = room != null ? room.getExam() : null;
        Course course = exam != null && exam.getCourseOffering() != null ? 
                exam.getCourseOffering().getCourse() : null;

        return InvigilationDTO.builder()
                .id(invigilator.getId())
                .examId(exam != null ? exam.getId() : null)
                .examName(exam != null ? exam.getName() : null)
                .courseNo(course != null ? course.getCourseNo() : null)
                .courseName(course != null ? course.getName() : null)
                .examType(exam != null && exam.getType() != null ? exam.getType().name() : null)
                .examTypeDescription(exam != null && exam.getType() != null ? 
                        exam.getType().getDescription() : null)
                .examTime(exam != null ? exam.getExamTime() : null)
                .duration(exam != null ? exam.getDuration() : null)
                .examRoomId(room != null ? room.getId() : null)
                .roomName(room != null ? room.getRoomName() : null)
                .location(room != null ? room.getLocation() : null)
                .invigilatorType(invigilator.getType() != null ? invigilator.getType().name() : null)
                .invigilatorTypeDescription(invigilator.getType() != null ? 
                        invigilator.getType().getDescription() : null)
                .examStatus(exam != null && exam.getStatus() != null ? exam.getStatus().name() : null)
                .statusDescription(exam != null && exam.getStatus() != null ? 
                        exam.getStatus().getDescription() : null)
                .studentCount(room != null ? room.getAssignedCount() : null)
                .roomCapacity(room != null ? room.getCapacity() : null)
                .build();
    }

    /**
     * 将CourseEvaluation实体转换为CourseEvaluationDTO
     *
     * @param evaluation CourseEvaluation实体
     * @return CourseEvaluationDTO
     */
    public CourseEvaluationDTO toCourseEvaluationDTO(com.university.academic.entity.CourseEvaluation evaluation) {
        if (evaluation == null) {
            return null;
        }

        com.university.academic.entity.Student student = evaluation.getStudent();
        com.university.academic.entity.CourseOffering offering = evaluation.getCourseOffering();
        com.university.academic.entity.Course course = offering != null ? offering.getCourse() : null;
        com.university.academic.entity.Teacher teacher = offering != null ? offering.getTeacher() : null;
        com.university.academic.entity.Semester semester = offering != null ? offering.getSemester() : null;

        return com.university.academic.dto.CourseEvaluationDTO.builder()
                .id(evaluation.getId())
                .studentId(student != null ? student.getId() : null)
                .studentNo(student != null ? student.getStudentNo() : null)
                .studentName(student != null ? student.getName() : null)
                .offeringId(offering != null ? offering.getId() : null)
                .courseId(course != null ? course.getId() : null)
                .courseNo(course != null ? course.getCourseNo() : null)
                .courseName(course != null ? course.getName() : null)
                .teacherId(teacher != null ? teacher.getId() : null)
                .teacherName(teacher != null ? teacher.getName() : null)
                .semesterId(evaluation.getSemesterId())
                .semesterName(semester != null ? semester.getSemesterName() : null)
                .rating(evaluation.getRating())
                .comment(evaluation.getComment())
                .anonymous(evaluation.getAnonymous())
                .status(evaluation.getStatus() != null ? evaluation.getStatus().name() : null)
                .statusDescription(evaluation.getStatus() != null ? 
                        evaluation.getStatus().getDescription() : null)
                .flagged(evaluation.getFlagged())
                .moderationNote(evaluation.getModerationNote())
                .createdAt(evaluation.getCreatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }

    /**
     * 将TeacherEvaluation实体转换为TeacherEvaluationDTO
     *
     * @param evaluation TeacherEvaluation实体
     * @return TeacherEvaluationDTO
     */
    public com.university.academic.dto.TeacherEvaluationDTO toTeacherEvaluationDTO(
            com.university.academic.entity.TeacherEvaluation evaluation) {
        if (evaluation == null) {
            return null;
        }

        com.university.academic.entity.Student student = evaluation.getStudent();
        com.university.academic.entity.Teacher teacher = evaluation.getTeacher();
        com.university.academic.entity.CourseOffering offering = evaluation.getCourseOffering();
        com.university.academic.entity.Course course = offering != null ? offering.getCourse() : null;

        // 计算综合评分
        Double overallRating = null;
        if (evaluation.getTeachingRating() != null && 
            evaluation.getAttitudeRating() != null && 
            evaluation.getContentRating() != null) {
            overallRating = (evaluation.getTeachingRating() + 
                           evaluation.getAttitudeRating() + 
                           evaluation.getContentRating()) / 3.0;
        }

        return com.university.academic.dto.TeacherEvaluationDTO.builder()
                .id(evaluation.getId())
                .studentId(student != null ? student.getId() : null)
                .studentNo(student != null ? student.getStudentNo() : null)
                .studentName(student != null ? student.getName() : null)
                .teacherId(teacher != null ? teacher.getId() : null)
                .teacherNo(teacher != null ? teacher.getTeacherNo() : null)
                .teacherName(teacher != null ? teacher.getName() : null)
                .offeringId(offering != null ? offering.getId() : null)
                .courseId(course != null ? course.getId() : null)
                .courseNo(course != null ? course.getCourseNo() : null)
                .courseName(course != null ? course.getName() : null)
                .teachingRating(evaluation.getTeachingRating())
                .attitudeRating(evaluation.getAttitudeRating())
                .contentRating(evaluation.getContentRating())
                .overallRating(overallRating)
                .comment(evaluation.getComment())
                .anonymous(evaluation.getAnonymous())
                .status(evaluation.getStatus() != null ? evaluation.getStatus().name() : null)
                .statusDescription(evaluation.getStatus() != null ? 
                        evaluation.getStatus().getDescription() : null)
                .flagged(evaluation.getFlagged())
                .moderationNote(evaluation.getModerationNote())
                .createdAt(evaluation.getCreatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }

    /**
     * 将EvaluationPeriod实体转换为EvaluationPeriodDTO
     *
     * @param period EvaluationPeriod实体
     * @return EvaluationPeriodDTO
     */
    public com.university.academic.dto.EvaluationPeriodDTO toEvaluationPeriodDTO(
            com.university.academic.entity.EvaluationPeriod period) {
        if (period == null) {
            return null;
        }

        com.university.academic.entity.Semester semester = period.getSemester();

        return com.university.academic.dto.EvaluationPeriodDTO.builder()
                .id(period.getId())
                .semesterId(semester != null ? semester.getId() : null)
                .semesterName(semester != null ? semester.getSemesterName() : null)
                .startTime(period.getStartTime())
                .endTime(period.getEndTime())
                .active(period.getActive())
                .description(period.getDescription())
                .inPeriod(period.isInPeriod())
                .expired(period.isExpired())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }
    
    /**
     * 将ScholarshipApplication实体转换为ScholarshipApplicationDTO
     * 静态方法用于在Service中直接调用
     *
     * @param application ScholarshipApplication实体
     * @return ScholarshipApplicationDTO
     */
    public static com.university.academic.dto.ScholarshipApplicationDTO toScholarshipApplicationDTO(
            com.university.academic.entity.ScholarshipApplication application) {
        if (application == null) {
            return null;
        }

        com.university.academic.entity.Scholarship scholarship = application.getScholarship();
        com.university.academic.entity.Student student = application.getStudent();
        
        // 构建奖学金DTO
        com.university.academic.dto.ScholarshipDTO scholarshipDTO = null;
        if (scholarship != null) {
            scholarshipDTO = com.university.academic.dto.ScholarshipDTO.builder()
                    .id(scholarship.getId())
                    .name(scholarship.getName())
                    .level(scholarship.getLevel())
                    .amount(scholarship.getAmount())
                    .quota(scholarship.getQuota())
                    .description(scholarship.getDescription())
                    .minGpa(scholarship.getMinGpa())
                    .minCredits(scholarship.getMinCredits())
                    .build();
        }
        
        // 构建学生DTO
        com.university.academic.dto.StudentDTO studentDTO = null;
        if (student != null) {
            studentDTO = com.university.academic.dto.StudentDTO.builder()
                    .id(student.getId())
                    .studentNo(student.getStudentNo())
                    .name(student.getName())
                    .build();
        }

        return com.university.academic.dto.ScholarshipApplicationDTO.builder()
                .id(application.getId())
                .scholarship(scholarshipDTO)
                .student(studentDTO)
                .academicYear(application.getAcademicYear())
                .gpa(application.getGpa())
                .totalCredits(application.getTotalCredits())
                .comprehensiveScore(application.getComprehensiveScore())
                .personalStatement(application.getPersonalStatement())
                .attachmentUrl(application.getAttachmentUrl())
                .status(application.getStatus())
                .statusDescription(application.getStatus() != null ? 
                        application.getStatus().getDescription() : null)
                .submittedAt(application.getSubmittedAt())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    /**
     * 将StudentDiscipline实体转换为StudentDisciplineDTO
     */
    public StudentDisciplineDTO toStudentDisciplineDTO(StudentDiscipline discipline) {
        if (discipline == null) {
            return null;
        }

        Student student = discipline.getStudent();
        
        return StudentDisciplineDTO.builder()
                .id(discipline.getId())
                .studentId(student != null ? student.getId() : null)
                .studentNo(student != null ? student.getStudentNo() : null)
                .studentName(student != null ? student.getName() : null)
                .majorName(student != null && student.getMajor() != null ? 
                        student.getMajor().getName() : null)
                .className(student != null ? student.getClassName() : null)
                .disciplineType(discipline.getDisciplineType() != null ? 
                        discipline.getDisciplineType().name() : null)
                .disciplineTypeDescription(discipline.getDisciplineType() != null ? 
                        discipline.getDisciplineType().getDescription() : null)
                .reason(discipline.getReason())
                .description(discipline.getDescription())
                .occurrenceDate(discipline.getOccurrenceDate())
                .punishmentDate(discipline.getPunishmentDate())
                .status(discipline.getStatus() != null ? discipline.getStatus().name() : null)
                .statusDescription(discipline.getStatus() != null ? 
                        discipline.getStatus().getDescription() : null)
                .approvalStatus(discipline.getApprovalStatus() != null ? 
                        discipline.getApprovalStatus().name() : null)
                .approvalStatusDescription(discipline.getApprovalStatus() != null ? 
                        discipline.getApprovalStatus().getDescription() : null)
                .canRemove(discipline.getCanRemove())
                .removedDate(discipline.getRemovedDate())
                .removedReason(discipline.getRemovedReason())
                .removedByName(getUserRealName(discipline.getRemovedBy()))
                .attachmentUrl(discipline.getAttachmentUrl())
                .reporterName(getUserRealName(discipline.getReporter()))
                .approverName(getUserRealName(discipline.getApprover()))
                .approvedAt(discipline.getApprovedAt())
                .approvalComment(discipline.getApprovalComment())
                .createdAt(discipline.getCreatedAt())
                .updatedAt(discipline.getUpdatedAt())
                .build();
    }

    /**
     * 将DisciplineAppeal实体转换为DisciplineAppealDTO
     */
    public DisciplineAppealDTO toDisciplineAppealDTO(DisciplineAppeal appeal) {
        if (appeal == null) {
            return null;
        }

        StudentDiscipline discipline = appeal.getDiscipline();
        Student student = appeal.getStudent();
        
        return DisciplineAppealDTO.builder()
                .id(appeal.getId())
                .disciplineId(discipline != null ? discipline.getId() : null)
                .disciplineTypeDescription(discipline != null && discipline.getDisciplineType() != null ? 
                        discipline.getDisciplineType().getDescription() : null)
                .disciplineReason(discipline != null ? discipline.getReason() : null)
                .studentId(student != null ? student.getId() : null)
                .studentNo(student != null ? student.getStudentNo() : null)
                .studentName(student != null ? student.getName() : null)
                .appealReason(appeal.getAppealReason())
                .evidence(appeal.getEvidence())
                .attachmentUrl(appeal.getAttachmentUrl())
                .status(appeal.getStatus() != null ? appeal.getStatus().name() : null)
                .statusDescription(appeal.getStatus() != null ? 
                        appeal.getStatus().getDescription() : null)
                .reviewResult(appeal.getReviewResult() != null ? 
                        appeal.getReviewResult().name() : null)
                .reviewResultDescription(appeal.getReviewResult() != null ? 
                        appeal.getReviewResult().getDescription() : null)
                .reviewComment(appeal.getReviewComment())
                .reviewedByName(appeal.getReviewedBy() != null ? 
                        appeal.getReviewedBy().getUsername() : null)
                .reviewedAt(appeal.getReviewedAt())
                .createdAt(appeal.getCreatedAt())
                .updatedAt(appeal.getUpdatedAt())
                .build();
    }
}

