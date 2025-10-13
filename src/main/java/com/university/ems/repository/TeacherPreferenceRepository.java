package com.university.ems.repository;

import com.university.academic.entity.Teacher;
import com.university.ems.entity.TeacherPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 教师偏好Repository接口
 * 
 * @author Academic System Team
 */
@Repository
public interface TeacherPreferenceRepository extends JpaRepository<TeacherPreference, Long> {

    /**
     * 按教师查询偏好设置
     *
     * @param teacher 教师
     * @return 教师偏好
     */
    Optional<TeacherPreference> findByTeacher(Teacher teacher);

    /**
     * 按教师ID查询偏好设置
     *
     * @param teacherId 教师ID
     * @return 教师偏好
     */
    Optional<TeacherPreference> findByTeacherId(Long teacherId);

    /**
     * 检查教师是否已设置偏好
     *
     * @param teacherId 教师ID
     * @return 是否存在
     */
    boolean existsByTeacherId(Long teacherId);
}

