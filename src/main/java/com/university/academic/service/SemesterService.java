package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.Semester;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学期服务类
 * 提供学期CRUD、活动学期管理等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    /**
     * 根据ID查询学期
     *
     * @param id 学期ID
     * @return 学期对象
     */
    @Cacheable(value = CacheConfig.CACHE_SEMESTERS, key = "#id")
    @Transactional(readOnly = true)
    public Semester findById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEMESTER_NOT_FOUND));
    }

    /**
     * 查询当前活动学期
     *
     * @return 当前活动学期
     */
    @Cacheable(value = CacheConfig.CACHE_ACTIVE_SEMESTER, key = "'active'")
    @Transactional(readOnly = true)
    public Semester findActiveSemester() {
        return semesterRepository.findByActiveTrue()
                .orElseThrow(() -> new BusinessException(ErrorCode.SEMESTER_NOT_ACTIVE));
    }

    /**
     * 查询所有学期
     *
     * @return 学期列表（按学年和学期类型降序排序）
     */
    @Transactional(readOnly = true)
    public List<Semester> findAll() {
        return semesterRepository.findAllOrderByDesc();
    }

    /**
     * 查询所有学期（按指定排序）
     *
     * @param sort 排序参数
     * @return 学期列表
     */
    @Transactional(readOnly = true)
    public List<Semester> findAll(Sort sort) {
        return semesterRepository.findAll(sort);
    }

    /**
     * 创建学期
     *
     * @param semester 学期对象
     * @return 创建后的学期对象
     */
    @CacheEvict(value = CacheConfig.CACHE_SEMESTERS, allEntries = true)
    @Transactional
    public Semester createSemester(Semester semester) {
        // 检查学年和学期类型是否已存在
        if (semesterRepository.existsByAcademicYearAndSemesterType(
                semester.getAcademicYear(), semester.getSemesterType())) {
            throw new BusinessException(ErrorCode.SEMESTER_ALREADY_EXISTS);
        }

        // 验证日期设置
        validateSemesterDates(semester);

        // 新创建的学期默认不是活动学期
        semester.setActive(false);

        Semester savedSemester = semesterRepository.save(semester);
        log.info("创建学期成功: {} - {} ({})",
                savedSemester.getId(),
                savedSemester.getSemesterName(),
                savedSemester.getActive() ? "活动" : "非活动");
        return savedSemester;
    }

    /**
     * 更新学期信息
     *
     * @param id       学期ID
     * @param semester 更新的学期信息
     * @return 更新后的学期对象
     */
    @CacheEvict(value = {CacheConfig.CACHE_SEMESTERS, CacheConfig.CACHE_ACTIVE_SEMESTER}, allEntries = true)
    @Transactional
    public Semester updateSemester(Long id, Semester semester) {
        Semester existingSemester = findById(id);

        // 如果要修改学年或学期类型，检查新组合是否已存在
        if (semester.getAcademicYear() != null && semester.getSemesterType() != null) {
            if (!existingSemester.getAcademicYear().equals(semester.getAcademicYear()) ||
                !existingSemester.getSemesterType().equals(semester.getSemesterType())) {
                if (semesterRepository.existsByAcademicYearAndSemesterType(
                        semester.getAcademicYear(), semester.getSemesterType())) {
                    throw new BusinessException(ErrorCode.SEMESTER_ALREADY_EXISTS);
                }
            }
        }

        // 更新字段
        if (semester.getAcademicYear() != null) {
            existingSemester.setAcademicYear(semester.getAcademicYear());
        }
        if (semester.getSemesterType() != null) {
            existingSemester.setSemesterType(semester.getSemesterType());
        }
        if (semester.getStartDate() != null) {
            existingSemester.setStartDate(semester.getStartDate());
        }
        if (semester.getEndDate() != null) {
            existingSemester.setEndDate(semester.getEndDate());
        }
        if (semester.getCourseSelectionStart() != null) {
            existingSemester.setCourseSelectionStart(semester.getCourseSelectionStart());
        }
        if (semester.getCourseSelectionEnd() != null) {
            existingSemester.setCourseSelectionEnd(semester.getCourseSelectionEnd());
        }

        // 验证日期设置
        validateSemesterDates(existingSemester);

        Semester updatedSemester = semesterRepository.save(existingSemester);
        log.info("更新学期成功: {}", updatedSemester.getSemesterName());
        return updatedSemester;
    }

    /**
     * 删除学期
     *
     * @param id 学期ID
     */
    @CacheEvict(value = {CacheConfig.CACHE_SEMESTERS, CacheConfig.CACHE_ACTIVE_SEMESTER}, allEntries = true)
    @Transactional
    public void deleteSemester(Long id) {
        Semester semester = findById(id);

        // 检查是否有开课计划
        if (semesterRepository.hasOfferings(id)) {
            throw new BusinessException(ErrorCode.SEMESTER_HAS_OFFERINGS);
        }

        // 如果是活动学期，不允许删除
        if (semester.getActive()) {
            throw new BusinessException(ErrorCode.SEMESTER_NOT_ACTIVE);
        }

        semesterRepository.delete(semester);
        log.info("删除学期成功: {}", semester.getSemesterName());
    }

    /**
     * 设置当前活动学期
     *
     * @param id 学期ID
     */
    @CacheEvict(value = {CacheConfig.CACHE_SEMESTERS, CacheConfig.CACHE_ACTIVE_SEMESTER}, allEntries = true)
    @Transactional
    public void setActiveSemester(Long id) {
        Semester semester = findById(id);

        // 先将所有学期设置为非活动状态
        semesterRepository.deactivateAllSemesters();

        // 设置指定学期为活动学期
        semester.setActive(true);
        semesterRepository.save(semester);

        log.info("设置活动学期成功: {}", semester.getSemesterName());
    }

    /**
     * 统计学期数量
     *
     * @return 学期总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return semesterRepository.count();
    }

    /**
     * 验证学期日期设置
     *
     * @param semester 学期对象
     */
    private void validateSemesterDates(Semester semester) {
        LocalDate startDate = semester.getStartDate();
        LocalDate endDate = semester.getEndDate();
        LocalDateTime selectionStart = semester.getCourseSelectionStart();
        LocalDateTime selectionEnd = semester.getCourseSelectionEnd();

        // 学期结束日期必须晚于开始日期
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.SEMESTER_DATE_ERROR);
        }

        // 选课结束时间必须晚于开始时间
        if (selectionEnd.isBefore(selectionStart)) {
            throw new BusinessException(ErrorCode.SEMESTER_DATE_ERROR);
        }

        // 选课时间应该在学期时间范围内（可以适当放宽）
        // 这里只做警告日志，不强制限制
        LocalDate selectionStartDate = selectionStart.toLocalDate();
        LocalDate selectionEndDate = selectionEnd.toLocalDate();
        
        if (selectionStartDate.isBefore(startDate.minusDays(30))) {
            log.warn("选课开始时间过早，在学期开始前30天以上: {}", semester.getSemesterName());
        }
        
        if (selectionEndDate.isAfter(endDate)) {
            log.warn("选课结束时间晚于学期结束时间: {}", semester.getSemesterName());
        }
    }

    /**
     * 检查是否存在活动学期
     *
     * @return true-存在，false-不存在
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSemester() {
        return semesterRepository.findByActiveTrue().isPresent();
    }
}

