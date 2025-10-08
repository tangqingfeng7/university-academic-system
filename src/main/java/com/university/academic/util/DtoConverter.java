package com.university.academic.util;

import com.university.academic.dto.CourseDTO;
import com.university.academic.dto.CourseOfferingDTO;
import com.university.academic.dto.CourseSelectionDTO;
import com.university.academic.dto.DepartmentDTO;
import com.university.academic.dto.GradeDTO;
import com.university.academic.dto.MajorDTO;
import com.university.academic.dto.NotificationDTO;
import com.university.academic.dto.SemesterDTO;
import com.university.academic.dto.StudentDTO;
import com.university.academic.dto.TeacherDTO;
import com.university.academic.dto.UserDTO;
import com.university.academic.entity.Course;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.CourseSelection;
import com.university.academic.entity.Department;
import com.university.academic.entity.Grade;
import com.university.academic.entity.Major;
import com.university.academic.entity.Notification;
import com.university.academic.entity.Semester;
import com.university.academic.entity.Student;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import org.springframework.stereotype.Component;

/**
 * DTO转换工具类
 * 负责Entity与DTO之间的转换
 *
 * @author Academic System Team
 */
@Component
public class DtoConverter {

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
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .courseSelectionStart(semester.getCourseSelectionStart())
                .courseSelectionEnd(semester.getCourseSelectionEnd())
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
}

