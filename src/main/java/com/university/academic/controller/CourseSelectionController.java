package com.university.academic.controller;

import com.university.academic.dto.CourseOfferingDTO;
import com.university.academic.dto.CourseSelectionDTO;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.CourseSelection;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.CourseSelectionService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生选课控制器
 * 提供选课相关接口（学生端）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class CourseSelectionController {

    private final CourseSelectionService selectionService;
    private final DtoConverter dtoConverter;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 获取可选课程列表
     */
    @GetMapping("/offerings")
    public Result<List<CourseOfferingDTO>> getAvailableOfferings() {
        log.info("查询可选课程列表");

        List<CourseOffering> offerings = selectionService.findAvailableOfferings();
        List<CourseOfferingDTO> offeringDTOList = offerings.stream()
                .map(dtoConverter::toCourseOfferingDTO)
                .collect(Collectors.toList());

        return Result.success(offeringDTOList);
    }

    /**
     * 获取我的选课列表
     */
    @GetMapping("/selections")
    public Result<List<CourseSelectionDTO>> getMySelections(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询我的选课列表: studentId={}", studentId);

        List<CourseSelection> selections = selectionService.findByStudentInActiveSemester(studentId);
        List<CourseSelectionDTO> selectionDTOList = selections.stream()
                .map(dtoConverter::toCourseSelectionDTO)
                .collect(Collectors.toList());

        return Result.success(selectionDTOList);
    }

    /**
     * 选课
     */
    @PostMapping("/selections")
    public Result<CourseSelectionDTO> selectCourse(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        Long offeringId = request.get("offeringId");
        
        log.info("学生选课: studentId={}, offeringId={}", studentId, offeringId);

        CourseSelection selection = selectionService.selectCourse(studentId, offeringId);
        
        // 重新查询完整的选课记录（包含所有关联实体）
        CourseSelection fullSelection = selectionService.findById(selection.getId());
        CourseSelectionDTO selectionDTO = dtoConverter.toCourseSelectionDTO(fullSelection);

        return Result.success("选课成功", selectionDTO);
    }

    /**
     * 退课
     */
    @DeleteMapping("/selections/{id}")
    public Result<String> dropCourse(@PathVariable Long id, Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生退课: studentId={}, selectionId={}", studentId, id);

        selectionService.dropCourse(studentId, id);

        return Result.success("退课成功");
    }

    /**
     * 获取选课统计信息
     */
    @GetMapping("/selections/statistics")
    public Result<Map<String, Object>> getSelectionStatistics(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询选课统计信息: studentId={}", studentId);

        List<CourseSelection> selections = selectionService.findByStudentInActiveSemester(studentId);
        
        int totalCourses = 0;
        int totalCredits = 0;
        
        for (CourseSelection selection : selections) {
            if (selection.getStatus() == CourseSelection.SelectionStatus.SELECTED) {
                totalCourses++;
                totalCredits += selection.getOffering().getCourse().getCredits();
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCourses", totalCourses);
        statistics.put("totalCredits", totalCredits);

        return Result.success(statistics);
    }
}

