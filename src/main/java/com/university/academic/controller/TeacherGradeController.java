package com.university.academic.controller;

import com.university.academic.dto.GradeDTO;
import com.university.academic.dto.GradeInputRequest;
import com.university.academic.entity.Grade;
import com.university.academic.service.GradeService;
import com.university.academic.util.DtoConverter;
import com.university.academic.util.ExcelUtil;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教师成绩管理控制器
 * 提供成绩录入、查询、提交、发布接口（教师端）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/grades")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherGradeController {

    private final GradeService gradeService;
    private final DtoConverter dtoConverter;
    private final ExcelUtil excelUtil;

    /**
     * 查询开课班级的学生成绩列表
     */
    @GetMapping("/offering/{offeringId}")
    public Result<List<GradeDTO>> getOfferingGrades(@PathVariable Long offeringId) {
        log.info("查询开课班级成绩: offeringId={}", offeringId);

        List<Grade> grades = gradeService.findByOffering(offeringId);
        List<GradeDTO> gradeDTOList = grades.stream()
                .map(dtoConverter::toGradeDTO)
                .collect(Collectors.toList());

        return Result.success(gradeDTOList);
    }

    /**
     * 录入单个学生成绩
     */
    @PutMapping("/{courseSelectionId}")
    public Result<GradeDTO> inputGrade(@PathVariable Long courseSelectionId,
                                       @Valid @RequestBody GradeInputRequest request) {
        log.info("录入成绩: courseSelectionId={}", courseSelectionId);

        Grade grade = gradeService.inputGrade(
                courseSelectionId,
                request.getRegularScore(),
                request.getMidtermScore(),
                request.getFinalScore()
        );

        GradeDTO gradeDTO = dtoConverter.toGradeDTO(grade);
        return Result.success("成绩录入成功", gradeDTO);
    }

    /**
     * 批量录入成绩
     */
    @PostMapping("/batch")
    public Result<List<GradeDTO>> batchInputGrades(
            @RequestBody List<Map<String, Object>> gradeInputList) {
        log.info("批量录入成绩: 数量={}", gradeInputList.size());

        List<GradeService.GradeInput> inputs = gradeInputList.stream()
                .map(map -> GradeService.GradeInput.builder()
                        .courseSelectionId(((Number) map.get("courseSelectionId")).longValue())
                        .regularScore(map.get("regularScore") != null ? 
                                new java.math.BigDecimal(map.get("regularScore").toString()) : null)
                        .midtermScore(map.get("midtermScore") != null ? 
                                new java.math.BigDecimal(map.get("midtermScore").toString()) : null)
                        .finalScore(map.get("finalScore") != null ? 
                                new java.math.BigDecimal(map.get("finalScore").toString()) : null)
                        .build())
                .collect(Collectors.toList());

        List<Grade> grades = gradeService.batchInputGrades(inputs);
        List<GradeDTO> gradeDTOList = grades.stream()
                .map(dtoConverter::toGradeDTO)
                .collect(Collectors.toList());

        return Result.success("批量录入成绩成功", gradeDTOList);
    }

    /**
     * 提交成绩
     */
    @PutMapping("/offering/{offeringId}/submit")
    public Result<String> submitGrades(@PathVariable Long offeringId) {
        log.info("提交成绩: offeringId={}", offeringId);

        gradeService.submitGrades(offeringId);

        return Result.success("成绩提交成功");
    }

    /**
     * 发布成绩（管理员操作，暂时放在教师端）
     */
    @PutMapping("/offering/{offeringId}/publish")
    public Result<String> publishGrades(@PathVariable Long offeringId) {
        log.info("发布成绩: offeringId={}", offeringId);

        gradeService.publishGrades(offeringId);

        return Result.success("成绩发布成功");
    }

    /**
     * 批量导入成绩（Excel）
     */
    @PostMapping("/offering/{offeringId}/import")
    public Result<Map<String, Object>> importGrades(
            @PathVariable Long offeringId,
            @RequestParam("file") MultipartFile file) {
        log.info("导入成绩数据: offeringId={}, filename={}", offeringId, file.getOriginalFilename());

        try {
            // 解析Excel文件
            List<ExcelUtil.GradeImportDTO> importData = excelUtil.importGrades(file.getInputStream());

            List<Grade> successList = new ArrayList<>();
            List<Map<String, Object>> errorList = new ArrayList<>();

            // 处理每一条数据
            for (ExcelUtil.GradeImportDTO importDto : importData) {
                if (!importDto.isValid()) {
                    // 记录错误
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", importDto.getRowNumber());
                    error.put("studentNo", importDto.getStudentNo());
                    error.put("studentName", importDto.getStudentName());
                    error.put("error", importDto.getErrorMessage());
                    errorList.add(error);
                    continue;
                }

                try {
                    // 根据学号和开课计划ID查找选课记录
                    Long courseSelectionId = gradeService.findCourseSelectionIdByStudentNoAndOffering(
                            importDto.getStudentNo(), offeringId);

                    if (courseSelectionId == null) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("row", importDto.getRowNumber());
                        error.put("studentNo", importDto.getStudentNo());
                        error.put("studentName", importDto.getStudentName());
                        error.put("error", "学生未选该课程");
                        errorList.add(error);
                        continue;
                    }

                    // 录入成绩
                    Grade grade = gradeService.inputGrade(
                            courseSelectionId,
                            importDto.getRegularScore(),
                            importDto.getMidtermScore(),
                            importDto.getFinalScore()
                    );

                    successList.add(grade);
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", importDto.getRowNumber());
                    error.put("studentNo", importDto.getStudentNo());
                    error.put("studentName", importDto.getStudentName());
                    error.put("error", e.getMessage());
                    errorList.add(error);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", importData.size());
            result.put("successCount", successList.size());
            result.put("errorCount", errorList.size());
            result.put("errors", errorList);

            return Result.success("导入完成", result);
        } catch (IOException e) {
            log.error("导入成绩数据失败", e);
            return Result.error("文件读取失败");
        }
    }
}

