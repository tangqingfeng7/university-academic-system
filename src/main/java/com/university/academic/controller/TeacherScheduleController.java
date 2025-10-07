package com.university.academic.controller;

import com.university.academic.dto.ScheduleDTO;
import com.university.academic.dto.ScheduleItemDTO;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.ScheduleService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 教师课表控制器
 * 提供课表查询和导出接口（教师端）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/schedule")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherScheduleController {

    private final ScheduleService scheduleService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 获取我的课表
     */
    @GetMapping("/semester/{semesterId}")
    public Result<ScheduleDTO> getMySchedule(@PathVariable Long semesterId,
                                             Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师课表: teacherId={}, semesterId={}", teacherId, semesterId);

        ScheduleDTO schedule = scheduleService.getTeacherSchedule(teacherId, semesterId);
        return Result.success(schedule);
    }

    /**
     * 获取指定周次的课表
     */
    @GetMapping("/semester/{semesterId}/week/{weekNumber}")
    public Result<List<ScheduleItemDTO>> getWeeklySchedule(
            @PathVariable Long semesterId,
            @PathVariable Integer weekNumber,
            Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师周课表: teacherId={}, semesterId={}, weekNumber={}", 
                teacherId, semesterId, weekNumber);

        List<ScheduleItemDTO> items = scheduleService.getTeacherWeeklySchedule(
                teacherId, semesterId, weekNumber);
        return Result.success(items);
    }

    /**
     * 导出课表（文本格式）
     */
    @GetMapping("/semester/{semesterId}/export")
    public ResponseEntity<byte[]> exportSchedule(@PathVariable Long semesterId,
                                                 Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("导出教师课表: teacherId={}, semesterId={}", teacherId, semesterId);

        ScheduleDTO schedule = scheduleService.getTeacherSchedule(teacherId, semesterId);
        String text = scheduleService.formatScheduleToText(schedule);

        String filename = "课表_" + schedule.getSemesterName() + ".txt";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(text.getBytes(StandardCharsets.UTF_8));
    }
}

