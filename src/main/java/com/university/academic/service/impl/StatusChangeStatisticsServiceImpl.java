package com.university.academic.service.impl;

import com.university.academic.dto.StatusChangeQueryDTO;
import com.university.academic.dto.StatusChangeStatisticsDTO;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.MajorRepository;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.StudentStatusChangeRepository;
import com.university.academic.service.StatusChangeStatisticsService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学籍异动统计服务实现类
 *
 * @author university
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusChangeStatisticsServiceImpl implements StatusChangeStatisticsService {

    private final StudentStatusChangeRepository statusChangeRepository;
    private final StudentRepository studentRepository;
    private final MajorRepository majorRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> queryStatusChanges(StatusChangeQueryDTO queryDTO, Pageable pageable) {
        log.info("查询异动记录，条件: {}", queryDTO);

        Specification<StudentStatusChange> spec = (root, query, cb) -> {
            // 预加载关联对象（避免 LazyInitializationException）
            // 注意：只在非 COUNT 查询时使用 fetch，COUNT 查询不需要加载关联对象
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
                root.fetch("student", jakarta.persistence.criteria.JoinType.LEFT)
                    .fetch("major", jakarta.persistence.criteria.JoinType.LEFT);
            }
            
            List<Predicate> predicates = new ArrayList<>();

            // 软删除过滤
            predicates.add(cb.equal(root.get("deleted"), false));

            // 学生ID
            if (queryDTO.getStudentId() != null) {
                predicates.add(cb.equal(root.get("student").get("id"), queryDTO.getStudentId()));
            }

            // 学生姓名模糊查询
            if (queryDTO.getStudentName() != null && !queryDTO.getStudentName().trim().isEmpty()) {
                Join<StudentStatusChange, Student> studentJoin = root.join("student");
                predicates.add(cb.like(studentJoin.get("name"), "%" + queryDTO.getStudentName().trim() + "%"));
            }

            // 学号模糊查询
            if (queryDTO.getStudentNumber() != null && !queryDTO.getStudentNumber().trim().isEmpty()) {
                Join<StudentStatusChange, Student> studentJoin = root.join("student");
                predicates.add(cb.like(studentJoin.get("studentNo"), "%" + queryDTO.getStudentNumber().trim() + "%"));
            }

            // 异动类型
            if (queryDTO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), queryDTO.getType()));
            }

            // 审批状态
            if (queryDTO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
            }

            // 开始日期范围
            if (queryDTO.getStartDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), queryDTO.getStartDateFrom()));
            }
            if (queryDTO.getStartDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), queryDTO.getStartDateTo()));
            }

            // 创建时间范围
            if (queryDTO.getCreatedAtFrom() != null) {
                LocalDateTime fromDateTime = queryDTO.getCreatedAtFrom().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
            }
            if (queryDTO.getCreatedAtTo() != null) {
                LocalDateTime toDateTime = queryDTO.getCreatedAtTo().atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
            }

            // 目标专业ID（转专业）
            if (queryDTO.getTargetMajorId() != null) {
                predicates.add(cb.equal(root.get("targetMajorId"), queryDTO.getTargetMajorId()));
            }

            // 审批级别
            if (queryDTO.getApprovalLevel() != null) {
                predicates.add(cb.equal(root.get("approvalLevel"), queryDTO.getApprovalLevel()));
            }

            // 当前审批人
            if (queryDTO.getCurrentApproverId() != null) {
                predicates.add(cb.equal(root.get("currentApproverId"), queryDTO.getCurrentApproverId()));
            }

            // 是否超时
            if (queryDTO.getIsOverdue() != null) {
                predicates.add(cb.equal(root.get("isOverdue"), queryDTO.getIsOverdue()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return statusChangeRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public StatusChangeStatisticsDTO getStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("统计异动数据，时间段: {} - {}", startDate, endDate);

        LocalDate start = convertToLocalDate(startDate);
        LocalDate end = convertToLocalDate(endDate);

        // 总数统计
        Long totalCount = statusChangeRepository.countTotal(start, end);

        // 按类型统计
        Map<ChangeType, Long> countByType = new HashMap<>();
        List<Object[]> typeResults = statusChangeRepository.countByType(start, end);
        for (Object[] result : typeResults) {
            ChangeType type = (ChangeType) result[0];
            Long count = (Long) result[1];
            countByType.put(type, count);
        }

        // 按状态统计
        Map<ApprovalStatus, Long> countByStatus = new HashMap<>();
        List<Object[]> statusResults = statusChangeRepository.countByStatus(start, end);
        for (Object[] result : statusResults) {
            ApprovalStatus status = (ApprovalStatus) result[0];
            Long count = (Long) result[1];
            countByStatus.put(status, count);
        }

        // 按月份统计
        Map<String, Long> countByMonth = new HashMap<>();
        List<Object[]> monthResults = statusChangeRepository.countByMonth(start, end);
        for (Object[] result : monthResults) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            countByMonth.put(month, count);
        }

        // 转专业流向统计
        Map<String, Long> transferCountByMajor = new HashMap<>();
        List<Object[]> transferResults = statusChangeRepository.countTransferByMajor(start, end);
        for (Object[] result : transferResults) {
            String majorName = (String) result[0];
            Long count = (Long) result[1];
            transferCountByMajor.put(majorName, count);
        }

        // 超时申请数量
        Long overdueCount = statusChangeRepository.countOverdue(start, end);

        // 平均审批时长
        Double averageApprovalDays = statusChangeRepository.calculateAverageApprovalDays(start, end);
        if (averageApprovalDays == null) {
            averageApprovalDays = 0.0;
        }

        // 审批通过率
        Long approvedCount = countByStatus.getOrDefault(ApprovalStatus.APPROVED, 0L);
        Long rejectedCount = countByStatus.getOrDefault(ApprovalStatus.REJECTED, 0L);
        Long processedCount = approvedCount + rejectedCount;
        Double approvalRate = processedCount > 0 ? (approvedCount * 100.0 / processedCount) : 0.0;

        return StatusChangeStatisticsDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCount(totalCount)
                .countByType(countByType)
                .countByStatus(countByStatus)
                .countByMonth(countByMonth)
                .transferCountByMajor(transferCountByMajor)
                .pendingCount(countByStatus.getOrDefault(ApprovalStatus.PENDING, 0L))
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .cancelledCount(countByStatus.getOrDefault(ApprovalStatus.CANCELLED, 0L))
                .overdueCount(overdueCount)
                .suspensionCount(countByType.getOrDefault(ChangeType.SUSPENSION, 0L))
                .resumptionCount(countByType.getOrDefault(ChangeType.RESUMPTION, 0L))
                .transferCount(countByType.getOrDefault(ChangeType.TRANSFER, 0L))
                .withdrawalCount(countByType.getOrDefault(ChangeType.WITHDRAWAL, 0L))
                .averageApprovalDays(averageApprovalDays)
                .approvalRate(approvalRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StatusChangeStatisticsDTO getStatisticsByType(LocalDate startDate, LocalDate endDate) {
        log.info("按类型统计异动数据，时间段: {} - {}", startDate, endDate);

        LocalDate start = convertToLocalDate(startDate);
        LocalDate end = convertToLocalDate(endDate);

        Map<ChangeType, Long> countByType = new HashMap<>();
        List<Object[]> typeResults = statusChangeRepository.countByType(start, end);
        for (Object[] result : typeResults) {
            ChangeType type = (ChangeType) result[0];
            Long count = (Long) result[1];
            countByType.put(type, count);
        }

        return StatusChangeStatisticsDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .countByType(countByType)
                .suspensionCount(countByType.getOrDefault(ChangeType.SUSPENSION, 0L))
                .resumptionCount(countByType.getOrDefault(ChangeType.RESUMPTION, 0L))
                .transferCount(countByType.getOrDefault(ChangeType.TRANSFER, 0L))
                .withdrawalCount(countByType.getOrDefault(ChangeType.WITHDRAWAL, 0L))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StatusChangeStatisticsDTO getStatisticsByStatus(LocalDate startDate, LocalDate endDate) {
        log.info("按状态统计异动数据，时间段: {} - {}", startDate, endDate);

        LocalDate start = convertToLocalDate(startDate);
        LocalDate end = convertToLocalDate(endDate);

        Map<ApprovalStatus, Long> countByStatus = new HashMap<>();
        List<Object[]> statusResults = statusChangeRepository.countByStatus(start, end);
        for (Object[] result : statusResults) {
            ApprovalStatus status = (ApprovalStatus) result[0];
            Long count = (Long) result[1];
            countByStatus.put(status, count);
        }

        return StatusChangeStatisticsDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .countByStatus(countByStatus)
                .pendingCount(countByStatus.getOrDefault(ApprovalStatus.PENDING, 0L))
                .approvedCount(countByStatus.getOrDefault(ApprovalStatus.APPROVED, 0L))
                .rejectedCount(countByStatus.getOrDefault(ApprovalStatus.REJECTED, 0L))
                .cancelledCount(countByStatus.getOrDefault(ApprovalStatus.CANCELLED, 0L))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StatusChangeStatisticsDTO getStatisticsByMonth(LocalDate startDate, LocalDate endDate) {
        log.info("按月份统计异动趋势，时间段: {} - {}", startDate, endDate);

        LocalDate start = convertToLocalDate(startDate);
        LocalDate end = convertToLocalDate(endDate);

        Map<String, Long> countByMonth = new HashMap<>();
        List<Object[]> monthResults = statusChangeRepository.countByMonth(start, end);
        for (Object[] result : monthResults) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            countByMonth.put(month, count);
        }

        return StatusChangeStatisticsDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .countByMonth(countByMonth)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StatusChangeStatisticsDTO getTransferStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("统计转专业流向，时间段: {} - {}", startDate, endDate);

        LocalDate start = convertToLocalDate(startDate);
        LocalDate end = convertToLocalDate(endDate);

        Map<String, Long> transferCountByMajor = new HashMap<>();
        List<Object[]> transferResults = statusChangeRepository.countTransferByMajor(start, end);
        for (Object[] result : transferResults) {
            String majorName = (String) result[0];
            Long count = (Long) result[1];
            transferCountByMajor.put(majorName, count);
        }

        return StatusChangeStatisticsDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .transferCountByMajor(transferCountByMajor)
                .transferCount((long) transferCountByMajor.values().stream().mapToLong(Long::longValue).sum())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportStatusChanges(StatusChangeQueryDTO queryDTO) {
        log.info("导出异动记录，条件: {}", queryDTO);

        try {
            // 查询所有符合条件的记录（不分页）
            Specification<StudentStatusChange> spec = buildSpecification(queryDTO);
            List<StudentStatusChange> statusChanges = statusChangeRepository.findAll(spec);

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("学籍异动记录");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"序号", "学号", "姓名", "专业", "异动类型", "异动原因", "开始日期", "结束日期",
                    "目标专业", "审批状态", "审批级别", "是否超时", "创建时间"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;
            for (StudentStatusChange sc : statusChanges) {
                Row row = sheet.createRow(rowNum++);

                Student student = sc.getStudent();
                Major major = student.getMajor();

                int cellNum = 0;
                createCell(row, cellNum++, rowNum - 1, dataStyle);
                createCell(row, cellNum++, student.getStudentNo(), dataStyle);
                createCell(row, cellNum++, student.getName(), dataStyle);
                createCell(row, cellNum++, major != null ? major.getName() : "", dataStyle);
                createCell(row, cellNum++, getChangeTypeText(sc.getType()), dataStyle);
                createCell(row, cellNum++, sc.getReason(), dataStyle);
                createCell(row, cellNum++, sc.getStartDate() != null ? sc.getStartDate().format(DATE_FORMATTER) : "", dataStyle);
                createCell(row, cellNum++, sc.getEndDate() != null ? sc.getEndDate().format(DATE_FORMATTER) : "", dataStyle);

                String targetMajorName = "";
                if (sc.getTargetMajorId() != null) {
                    targetMajorName = majorRepository.findById(sc.getTargetMajorId())
                            .map(Major::getName)
                            .orElse("");
                }
                createCell(row, cellNum++, targetMajorName, dataStyle);

                createCell(row, cellNum++, getApprovalStatusText(sc.getStatus()), dataStyle);
                createCell(row, cellNum++, "第" + sc.getApprovalLevel() + "级", dataStyle);
                createCell(row, cellNum++, sc.getIsOverdue() ? "是" : "否", dataStyle);
                createCell(row, cellNum++, sc.getCreatedAt().format(DATETIME_FORMATTER), dataStyle);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("导出异动记录完成，共{}条", statusChanges.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出异动记录失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出Excel文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportStatisticsReport(LocalDate startDate, LocalDate endDate) {
        log.info("导出统计报告，时间段: {} - {}", startDate, endDate);

        try {
            StatusChangeStatisticsDTO statistics = getStatistics(startDate, endDate);

            Workbook workbook = new XSSFWorkbook();

            // 创建汇总页
            createSummarySheet(workbook, statistics);

            // 创建按类型统计页
            createTypeStatisticsSheet(workbook, statistics);

            // 创建按状态统计页
            createStatusStatisticsSheet(workbook, statistics);

            // 创建月度趋势页
            createMonthlyTrendSheet(workbook, statistics);

            // 创建转专业流向页
            if (statistics.getTransferCountByMajor() != null && !statistics.getTransferCountByMajor().isEmpty()) {
                createTransferSheet(workbook, statistics);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("导出统计报告完成");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出统计报告失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出统计报告失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> getPendingApplications(Long approverId, Pageable pageable) {
        log.info("查询审批人{}的待审批申请", approverId);
        return statusChangeRepository.findPendingByApproverId(approverId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentStatusChange> getOverdueApplications(Pageable pageable) {
        log.info("查询超时的异动申请");
        return statusChangeRepository.findOverdueApplications(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentStatusChange> getStudentStatusChanges(Long studentId) {
        log.info("查询学生{}的所有异动记录", studentId);

        if (!studentRepository.existsById(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        return statusChangeRepository.findAllByStudentId(studentId);
    }

    // ========== 私有辅助方法 ==========

    private Specification<StudentStatusChange> buildSpecification(StatusChangeQueryDTO queryDTO) {
        return (root, query, cb) -> {
            // 预加载关联对象（避免 LazyInitializationException）
            // 注意：只在非 COUNT 查询时使用 fetch，COUNT 查询不需要加载关联对象
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
                root.fetch("student", jakarta.persistence.criteria.JoinType.LEFT)
                    .fetch("major", jakarta.persistence.criteria.JoinType.LEFT);
            }
            
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (queryDTO.getStudentId() != null) {
                predicates.add(cb.equal(root.get("student").get("id"), queryDTO.getStudentId()));
            }
            if (queryDTO.getType() != null) {
                predicates.add(cb.equal(root.get("type"), queryDTO.getType()));
            }
            if (queryDTO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
            }
            if (queryDTO.getCreatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), queryDTO.getCreatedAtFrom().atStartOfDay()));
            }
            if (queryDTO.getCreatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), queryDTO.getCreatedAtTo().atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDate convertToLocalDate(LocalDate date) {
        return date;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }

    private String getChangeTypeText(ChangeType type) {
        return switch (type) {
            case SUSPENSION -> "休学";
            case RESUMPTION -> "复学";
            case TRANSFER -> "转专业";
            case WITHDRAWAL -> "退学";
        };
    }

    private String getApprovalStatusText(ApprovalStatus status) {
        return switch (status) {
            case PENDING -> "待审批";
            case APPROVED -> "已批准";
            case REJECTED -> "已拒绝";
            case CANCELLED -> "已取消";
        };
    }

    private void createSummarySheet(Workbook workbook, StatusChangeStatisticsDTO statistics) {
        Sheet sheet = workbook.createSheet("汇总统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        createCell(titleRow, 0, "学籍异动统计报告", headerStyle);

        // 时间范围
        rowNum++;
        Row periodRow = sheet.createRow(rowNum++);
        createCell(periodRow, 0, "统计时间段:", dataStyle);
        createCell(periodRow, 1, (statistics.getStartDate() != null ? statistics.getStartDate().format(DATE_FORMATTER) : "不限") +
                " 至 " + (statistics.getEndDate() != null ? statistics.getEndDate().format(DATE_FORMATTER) : "不限"), dataStyle);

        // 总体统计
        rowNum++;
        createCell(sheet.createRow(rowNum++), 0, "总申请数", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, statistics.getTotalCount(), dataStyle);

        rowNum++;
        createCell(sheet.createRow(rowNum++), 0, "待审批", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, statistics.getPendingCount(), dataStyle);

        createCell(sheet.createRow(rowNum++), 0, "已批准", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, statistics.getApprovedCount(), dataStyle);

        createCell(sheet.createRow(rowNum++), 0, "已拒绝", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, statistics.getRejectedCount(), dataStyle);

        createCell(sheet.createRow(rowNum++), 0, "已取消", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, statistics.getCancelledCount(), dataStyle);

        createCell(sheet.createRow(rowNum++), 0, "超时申请", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, statistics.getOverdueCount(), dataStyle);

        rowNum++;
        createCell(sheet.createRow(rowNum++), 0, "平均审批时长(天)", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, String.format("%.2f", statistics.getAverageApprovalDays()), dataStyle);

        createCell(sheet.createRow(rowNum++), 0, "审批通过率(%)", headerStyle);
        createCell(sheet.createRow(rowNum++), 0, String.format("%.2f", statistics.getApprovalRate()), dataStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTypeStatisticsSheet(Workbook workbook, StatusChangeStatisticsDTO statistics) {
        Sheet sheet = workbook.createSheet("按类型统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "异动类型", headerStyle);
        createCell(headerRow, 1, "数量", headerStyle);

        int rowNum = 1;
        if (statistics.getCountByType() != null) {
            for (Map.Entry<ChangeType, Long> entry : statistics.getCountByType().entrySet()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, getChangeTypeText(entry.getKey()), dataStyle);
                createCell(row, 1, entry.getValue(), dataStyle);
            }
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createStatusStatisticsSheet(Workbook workbook, StatusChangeStatisticsDTO statistics) {
        Sheet sheet = workbook.createSheet("按状态统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "审批状态", headerStyle);
        createCell(headerRow, 1, "数量", headerStyle);

        int rowNum = 1;
        if (statistics.getCountByStatus() != null) {
            for (Map.Entry<ApprovalStatus, Long> entry : statistics.getCountByStatus().entrySet()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, getApprovalStatusText(entry.getKey()), dataStyle);
                createCell(row, 1, entry.getValue(), dataStyle);
            }
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createMonthlyTrendSheet(Workbook workbook, StatusChangeStatisticsDTO statistics) {
        Sheet sheet = workbook.createSheet("月度趋势");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "月份", headerStyle);
        createCell(headerRow, 1, "数量", headerStyle);

        int rowNum = 1;
        if (statistics.getCountByMonth() != null) {
            // 按月份排序
            List<Map.Entry<String, Long>> sortedEntries = statistics.getCountByMonth().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            for (Map.Entry<String, Long> entry : sortedEntries) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, entry.getKey(), dataStyle);
                createCell(row, 1, entry.getValue(), dataStyle);
            }
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTransferSheet(Workbook workbook, StatusChangeStatisticsDTO statistics) {
        Sheet sheet = workbook.createSheet("转专业流向");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "目标专业", headerStyle);
        createCell(headerRow, 1, "转入人数", headerStyle);

        int rowNum = 1;
        if (statistics.getTransferCountByMajor() != null) {
            // 按数量降序排序
            List<Map.Entry<String, Long>> sortedEntries = statistics.getTransferCountByMajor().entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .collect(Collectors.toList());

            for (Map.Entry<String, Long> entry : sortedEntries) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, entry.getKey(), dataStyle);
                createCell(row, 1, entry.getValue(), dataStyle);
            }
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
}

