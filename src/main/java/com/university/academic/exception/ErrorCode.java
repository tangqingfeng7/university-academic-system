package com.university.academic.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 统一定义系统中所有的业务错误码
 *
 * @author Academic System Team
 */
@Getter
public enum ErrorCode {

    // ========== 通用错误码 1000-1999 ==========
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统异常，请稍后重试"),
    PARAM_ERROR(400, "参数错误"),
    INVALID_PARAM(400, "参数无效"),
    INVALID_OPERATION(400, "无效的操作"),
    OPERATION_FAILED(400, "操作失败"),
    VALIDATION_ERROR(400, "数据验证失败"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "没有权限访问"),
    REQUEST_TIMEOUT(408, "请求超时"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ========== 认证相关错误码 2000-2099 ==========
    LOGIN_FAILED(2001, "用户名或密码错误"),
    ACCOUNT_DISABLED(2002, "账户已被禁用"),
    ACCOUNT_LOCKED(2003, "账户已被锁定，请稍后再试"),
    TOKEN_EXPIRED(2004, "登录已过期，请重新登录"),
    TOKEN_INVALID(2005, "无效的令牌"),
    PASSWORD_ERROR(2006, "密码错误"),
    OLD_PASSWORD_ERROR(2007, "原密码错误"),
    PASSWORD_SAME(2008, "新密码不能与原密码相同"),
    PASSWORD_SAME_AS_OLD(2008, "新密码不能与原密码相同"),
    PASSWORD_MISMATCH(2009, "两次输入的密码不一致"),
    FIRST_LOGIN_CHANGE_PASSWORD(2010, "首次登录，请修改密码"),
    USERNAME_ALREADY_EXISTS(2011, "用户名已存在"),
    USER_NOT_FOUND(2012, "用户不存在"),

    // ========== 学生管理错误码 3000-3099 ==========
    STUDENT_NOT_FOUND(3001, "学生不存在"),
    STUDENT_NO_ALREADY_EXISTS(3002, "学号已存在"),
    STUDENT_ALREADY_DELETED(3003, "学生已被删除"),
    STUDENT_HAS_COURSES(3004, "学生有在读课程，无法删除"),
    STUDENT_IMPORT_ERROR(3005, "学生数据导入失败"),
    STUDENT_EXPORT_ERROR(3006, "学生数据导出失败"),

    // ========== 教师管理错误码 3100-3149 ==========
    TEACHER_NOT_FOUND(3101, "教师不存在"),
    TEACHER_NO_ALREADY_EXISTS(3102, "工号已存在"),
    TEACHER_HAS_COURSES(3103, "教师有授课任务，无法删除"),

    // ========== 毕业审核模块错误码 3150-3199 ==========
    GRADUATION_REQUIREMENT_NOT_FOUND(3151, "未找到毕业要求"),
    GRADUATION_REQUIREMENT_ALREADY_EXISTS(3152, "该专业和入学年份的毕业要求已存在"),
    INVALID_CREDIT_REQUIREMENTS(3153, "学分要求不合理"),
    GRADUATION_AUDIT_NOT_FOUND(3154, "毕业审核记录不存在"),
    STUDENT_CREDIT_SUMMARY_NOT_FOUND(3155, "学生学分汇总不存在"),

    // ========== 课程管理错误码 3200-3299 ==========
    COURSE_NOT_FOUND(3201, "课程不存在"),
    COURSE_NO_ALREADY_EXISTS(3202, "课程编号已存在"),
    COURSE_HAS_OFFERINGS(3203, "课程已有开课计划，无法删除"),
    COURSE_HAS_STUDENTS(3204, "课程已有学生选课，无法删除"),
    PREREQUISITE_NOT_MET(3205, "未满足先修课程要求"),
    PREREQUISITE_CIRCULAR(3206, "先修课程设置存在循环依赖"),
    PREREQUISITE_SELF(3207, "不能将课程设置为自己的先修课程"),

    // ========== 院系专业错误码 3300-3399 ==========
    DEPARTMENT_NOT_FOUND(3301, "院系不存在"),
    DEPARTMENT_CODE_ALREADY_EXISTS(3302, "院系代码已存在"),
    DEPARTMENT_HAS_MAJORS(3303, "院系下有专业，无法删除"),
    MAJOR_NOT_FOUND(3304, "专业不存在"),
    MAJOR_CODE_ALREADY_EXISTS(3305, "专业代码已存在"),
    MAJOR_HAS_STUDENTS(3306, "专业下有学生，无法删除"),

    // ========== 学期管理错误码 3400-3499 ==========
    SEMESTER_NOT_FOUND(3401, "学期不存在"),
    SEMESTER_ALREADY_EXISTS(3402, "学期已存在"),
    SEMESTER_HAS_OFFERINGS(3403, "学期有开课计划，无法删除"),
    SEMESTER_NOT_ACTIVE(3404, "当前没有活动学期"),
    SEMESTER_DATE_ERROR(3405, "学期日期设置错误"),
    SEMESTER_IS_ACTIVE(3406, "当前活动学期不允许编辑，请先设置其他学期为活动学期"),

    // ========== 开课计划错误码 3500-3599 ==========
    OFFERING_NOT_FOUND(3501, "开课计划不存在"),
    OFFERING_ALREADY_PUBLISHED(3502, "开课计划已发布"),
    OFFERING_ALREADY_CANCELLED(3503, "开课计划已取消"),
    OFFERING_TIME_CONFLICT(3504, "上课时间冲突"),
    OFFERING_TEACHER_CONFLICT(3505, "教师时间冲突"),
    OFFERING_CLASSROOM_CONFLICT(3506, "教室时间冲突"),
    OFFERING_HAS_STUDENTS(3507, "开课计划已有学生选课"),
    OFFERING_CAPACITY_ERROR(3508, "容量设置错误"),

    // ========== 选课管理错误码 3600-3699 ==========
    SELECTION_NOT_FOUND(3601, "选课记录不存在"),
    SELECTION_ALREADY_EXISTS(3602, "已选过该课程"),
    SELECTION_PERIOD_NOT_START(3603, "选课未开始"),
    SELECTION_PERIOD_ENDED(3604, "选课已结束"),
    SELECTION_DISABLED(3605, "选课功能已关闭"),
    SELECTION_COURSE_FULL(3606, "课程已满"),
    SELECTION_TIME_CONFLICT(3607, "选课时间冲突"),
    SELECTION_CREDITS_EXCEED(3608, "超过学分上限"),
    SELECTION_COURSES_EXCEED(3609, "超过课程数量上限"),
    SELECTION_PREREQUISITE_NOT_MET(3610, "未满足先修课程要求"),
    SELECTION_ALREADY_DROPPED(3611, "已退选该课程"),
    SELECTION_DROP_DEADLINE_PASSED(3612, "已超过退课截止日期"),
    SELECTION_CANNOT_DROP(3613, "无法退选该课程"),

    // ========== 成绩管理错误码 3700-3799 ==========
    GRADE_NOT_FOUND(3701, "成绩记录不存在"),
    GRADE_ALREADY_EXISTS(3702, "成绩记录已存在"),
    GRADE_SCORE_INVALID(3703, "成绩分数无效（0-100）"),
    GRADE_ALREADY_SUBMITTED(3704, "成绩已提交，无法修改"),
    GRADE_ALREADY_PUBLISHED(3705, "成绩已公布"),
    GRADE_NOT_PUBLISHED(3706, "成绩未公布"),
    GRADE_IMPORT_ERROR(3707, "成绩导入失败"),
    GRADE_EXPORT_ERROR(3708, "成绩导出失败"),

    // ========== 通知管理错误码 3800-3899 ==========
    NOTIFICATION_NOT_FOUND(3801, "通知不存在"),
    NOTIFICATION_ALREADY_READ(3802, "通知已读"),

    // ========== 系统配置错误码 3900-3999 ==========
    CONFIG_NOT_FOUND(3901, "配置项不存在"),
    CONFIG_KEY_ALREADY_EXISTS(3902, "配置键已存在"),
    CONFIG_VALUE_INVALID(3903, "配置值无效"),

    // ========== 文件操作错误码 4000-4099 ==========
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(4002, "文件下载失败"),
    FILE_TYPE_NOT_SUPPORTED(4003, "不支持的文件类型"),
    FILE_SIZE_EXCEED(4004, "文件大小超过限制"),
    FILE_NOT_FOUND(4005, "文件不存在"),
    EXPORT_ERROR(4006, "数据导出失败"),
    FILE_NAME_INVALID(4007, "文件名无效"),

    // ========== 数据操作错误码 4100-4199 ==========
    DATA_NOT_FOUND(4101, "数据不存在"),
    DATA_ALREADY_EXISTS(4102, "数据已存在"),
    DATA_IN_USE(4103, "数据正在使用中，无法删除"),
    DATA_CONSTRAINT_VIOLATION(4104, "数据约束冲突"),
    OPTIMISTIC_LOCK_FAILURE(4105, "数据已被其他用户修改，请刷新后重试"),

    // ========== 考试管理错误码 4200-4299 ==========
    EXAM_NOT_FOUND(4201, "考试不存在"),
    EXAM_ALREADY_PUBLISHED(4202, "考试已发布，无法修改"),
    EXAM_ALREADY_STARTED(4203, "考试已开始，无法取消"),
    EXAM_NO_ROOMS(4204, "考试没有考场，无法发布"),
    EXAM_TIME_CONFLICT(4205, "考试时间冲突"),
    EXAM_NOT_EDITABLE(4206, "考试不可编辑"),
    EXAM_NOT_CANCELLABLE(4207, "考试不可取消"),

    // ========== 考场管理错误码 4300-4399 ==========
    EXAM_ROOM_NOT_FOUND(4301, "考场不存在"),
    EXAM_ROOM_FULL(4302, "考场已满"),
    EXAM_ROOM_TIME_CONFLICT(4303, "考场时间冲突"),
    EXAM_ROOM_HAS_STUDENTS(4304, "考场已有学生，无法删除"),
    EXAM_ROOM_CAPACITY_INSUFFICIENT(4305, "考场容量不足"),
    EXAM_ROOM_CAPACITY_ERROR(4306, "考场容量不能小于已分配人数"),

    // ========== 学生分配错误码 4400-4499 ==========
    STUDENT_NOT_ENROLLED(4401, "学生未选修该课程"),
    STUDENT_ALREADY_ASSIGNED(4402, "学生已分配到考场"),
    STUDENT_EXAM_CONFLICT(4403, "学生考试时间冲突"),
    STUDENT_ASSIGNMENT_NOT_FOUND(4404, "学生分配记录不存在"),

    // ========== 监考管理错误码 4500-4599 ==========
    INVIGILATOR_NOT_FOUND(4501, "监考安排不存在"),
    TEACHER_INVIGILATION_CONFLICT(4502, "教师监考时间冲突"),
    TEACHER_EXAM_CONFLICT(4503, "教师在该时间有自己的考试"),
    INVIGILATOR_ALREADY_EXISTS(4504, "该教师已安排监考"),

    // ========== 教学评价模块错误码 4600-4699 ==========
    EVALUATION_PERIOD_NOT_ACTIVE(4601, "评价周期未开放"),
    EVALUATION_ALREADY_SUBMITTED(4602, "已提交过评价"),
    EVALUATION_NOT_ELIGIBLE(4603, "不符合评价资格"),
    EVALUATION_PERIOD_NOT_FOUND(4604, "评价周期不存在"),
    EVALUATION_PERIOD_ALREADY_EXISTS(4605, "该学期已存在评价周期"),
    EVALUATION_PERIOD_CONFLICT(4606, "评价周期时间冲突"),
    EVALUATION_NOT_FOUND(4607, "评价记录不存在"),
    EVALUATION_PERIOD_EXPIRED(4608, "评价周期已结束"),
    ACTIVE_PERIOD_ALREADY_EXISTS(4609, "已存在活跃的评价周期"),
    
    // ========== 教室资源管理模块错误码 4700-4799 ==========
    CLASSROOM_NOT_FOUND(4701, "教室不存在"),
    CLASSROOM_ALREADY_EXISTS(4702, "教室编号已存在"),
    CLASSROOM_NOT_AVAILABLE(4703, "教室不可用"),
    CLASSROOM_TIME_CONFLICT(4704, "教室时间冲突"),
    CLASSROOM_CAPACITY_EXCEEDED(4705, "教室容量超限"),
    CLASSROOM_HAS_BOOKING(4706, "教室有借用记录，无法删除"),
    CLASSROOM_HAS_USAGE(4707, "教室有使用记录，无法删除"),
    INVALID_TIME_RANGE(4708, "无效的时间范围"),
    
    // ========== 教室借用管理错误码 4800-4899 ==========
    BOOKING_NOT_FOUND(4801, "借用记录不存在"),
    BOOKING_ALREADY_APPROVED(4802, "借用申请已审批"),
    BOOKING_ALREADY_REJECTED(4803, "借用申请已拒绝"),
    BOOKING_ALREADY_CANCELLED(4804, "借用申请已取消"),
    BOOKING_CANNOT_CANCEL(4805, "无法取消借用申请"),
    BOOKING_TIME_CONFLICT(4806, "借用时间冲突"),
    
    // ========== 学籍异动管理模块错误码 4900-4999 ==========
    STATUS_CHANGE_NOT_FOUND(4901, "学籍异动申请不存在"),
    STATUS_CHANGE_NOT_ALLOWED(4902, "不允许此类异动操作"),
    STATUS_CHANGE_ALREADY_EXISTS(4903, "已存在进行中的异动申请"),
    STATUS_CHANGE_ALREADY_PROCESSED(4904, "异动申请已处理，无法修改"),
    STATUS_CHANGE_CANNOT_CANCEL(4905, "无法取消该异动申请"),
    TRANSFER_REQUIREMENTS_NOT_MET(4906, "不符合转专业条件"),
    STUDENT_NOT_SUSPENDED(4907, "学生未处于休学状态"),
    STUDENT_ALREADY_SUSPENDED(4908, "学生已处于休学状态"),
    TARGET_MAJOR_NOT_FOUND(4909, "目标专业不存在"),
    INVALID_DATE_RANGE(4910, "日期范围无效"),
    SUSPENSION_PERIOD_INVALID(4911, "休学期限无效"),

    // ========== 学费缴纳模块错误码 5000-5099 ==========
    TUITION_STANDARD_NOT_FOUND(5001, "学费标准不存在"),
    TUITION_STANDARD_ALREADY_EXISTS(5002, "学费标准已存在"),
    TUITION_BILL_NOT_FOUND(5003, "学费账单不存在"),
    TUITION_BILL_ALREADY_EXISTS(5004, "学费账单已存在"),
    TUITION_BILL_ALREADY_PAID(5005, "学费账单已支付"),
    PAYMENT_NOT_FOUND(5006, "缴费记录不存在"),
    PAYMENT_FAILED(5007, "支付失败"),
    PAYMENT_AMOUNT_EXCEEDS_OUTSTANDING(5008, "缴费金额超过欠费金额"),
    PAYMENT_AMOUNT_INVALID(5009, "缴费金额无效"),
    PAYMENT_METHOD_NOT_SUPPORTED(5010, "不支持的支付方式"),
    
    // 退费相关错误码 5011-5020
    REFUND_APPLICATION_NOT_FOUND(5011, "退费申请不存在"),
    REFUND_AMOUNT_EXCEEDS_PAYMENT(5012, "退费金额超过原缴费金额"),
    REFUND_AMOUNT_INVALID(5013, "退费金额无效"),
    REFUND_APPLICATION_ALREADY_PROCESSED(5014, "退费申请已处理"),
    REFUND_APPLICATION_ALREADY_EXISTS(5015, "该缴费记录已存在退费申请"),
    PAYMENT_CANNOT_BE_REFUNDED(5016, "该缴费记录不支持退费"),
    REFUND_APPROVAL_PERMISSION_DENIED(5017, "无退费审批权限"),
    
    // ========== 奖学金评定模块错误码 5100-5199 ==========
    SCHOLARSHIP_NOT_FOUND(5101, "奖学金不存在"),
    SCHOLARSHIP_ALREADY_EXISTS(5102, "奖学金名称已存在"),
    SCHOLARSHIP_NOT_ACTIVE(5103, "奖学金未启用"),
    SCHOLARSHIP_HAS_APPLICATIONS(5104, "该奖学金已有申请记录，无法删除"),
    SCHOLARSHIP_APPLICATION_NOT_FOUND(5105, "奖学金申请不存在"),
    SCHOLARSHIP_APPLICATION_ALREADY_EXISTS(5106, "已提交过该奖学金申请"),
    SCHOLARSHIP_APPLICATION_NOT_ELIGIBLE(5107, "不符合申请条件"),
    SCHOLARSHIP_APPLICATION_PERIOD_CLOSED(5108, "申请期已关闭"),
    SCHOLARSHIP_AWARD_NOT_FOUND(5109, "获奖记录不存在"),
    SCHOLARSHIP_APPROVAL_NOT_FOUND(5110, "审批记录不存在"),
    SCHOLARSHIP_APPROVAL_PERMISSION_DENIED(5111, "无审批权限"),
    SCHOLARSHIP_ALREADY_APPROVED(5112, "该申请已审批过"),
    SCHOLARSHIP_APPLICATION_REJECTED(5113, "该申请已被拒绝"),
    SCHOLARSHIP_APPLICATION_STATUS_INVALID(5114, "申请状态不符合审批条件"),
    
    // ========== 排课优化模块错误码 5200-5299 ==========
    CONSTRAINT_NOT_FOUND(5201, "排课约束不存在"),
    CONSTRAINT_NAME_ALREADY_EXISTS(5202, "约束名称已存在"),
    CONSTRAINT_IN_USE(5203, "约束正在使用中，无法删除"),
    CONSTRAINT_TYPE_INVALID(5204, "约束类型无效"),
    CONSTRAINT_WEIGHT_INVALID(5205, "约束权重无效"),
    
    SOLUTION_NOT_FOUND(5211, "排课方案不存在"),
    SOLUTION_ALREADY_APPLIED(5212, "排课方案已应用"),
    SOLUTION_STATUS_INVALID(5213, "排课方案状态无效"),
    SOLUTION_CANNOT_MODIFY(5214, "排课方案不可修改"),
    
    TEACHER_PREFERENCE_NOT_FOUND(5221, "教师排课偏好不存在"),
    TEACHER_PREFERENCE_ALREADY_EXISTS(5222, "教师排课偏好已存在"),
    PREFERENCE_NOT_FOUND(5223, "偏好设置不存在"),
    
    SCHEDULING_CONFLICT(5231, "排课冲突"),
    SCHEDULING_FAILED(5232, "排课失败"),
    
    // ========== 学生处分管理模块错误码 5300-5399 ==========
    DISCIPLINE_NOT_FOUND(5301, "处分记录不存在"),
    DISCIPLINE_CANNOT_REMOVE(5302, "该处分不允许解除"),
    DISCIPLINE_ALREADY_REMOVED(5303, "处分已被解除"),
    
    APPEAL_NOT_FOUND(5311, "申诉记录不存在"),
    APPEAL_ALREADY_EXISTS(5312, "已存在待审核的申诉"),
    APPEAL_ALREADY_REVIEWED(5313, "申诉已被审核"),
    APPEAL_CANNOT_CANCEL(5314, "申诉无法撤销"),
    
    // ========== 考勤管理模块错误码 5400-5499 ==========
    ATTENDANCE_RECORD_NOT_FOUND(5401, "考勤记录不存在"),
    ATTENDANCE_ALREADY_SUBMITTED(5402, "考勤已提交，无法修改"),
    QRCODE_EXPIRED(5403, "二维码已过期"),
    LOCATION_OUT_OF_RANGE(5404, "不在签到范围内"),
    DUPLICATE_CHECKIN(5405, "重复签到"),
    INVALID_ATTENDANCE_TIME(5406, "签到时间已过"),
    ATTENDANCE_PERMISSION_DENIED(5407, "无权限操作此考勤"),
    STUDENT_NOT_IN_COURSE(5408, "学生不在选课名单中"),
    ATTENDANCE_METHOD_NOT_SUPPORTED(5409, "考勤方式不支持"),
    ATTENDANCE_REQUEST_ALREADY_PROCESSED(5410, "申请已处理，无法修改"),
    ATTENDANCE_DETAIL_NOT_FOUND(5411, "考勤明细不存在"),
    ATTENDANCE_CONFIG_NOT_FOUND(5412, "考勤配置不存在"),
    ATTENDANCE_WARNING_NOT_FOUND(5413, "考勤预警不存在"),
    ATTENDANCE_REQUEST_NOT_FOUND(5414, "考勤申请不存在"),
    ATTENDANCE_NOT_IN_PROGRESS(5415, "考勤未在进行中"),
    ATTENDANCE_ALREADY_CANCELLED(5416, "考勤已取消"),
    QRCODE_INVALID(5417, "二维码无效"),
    ATTENDANCE_TIME_NOT_VALID(5418, "不在考勤时间范围内"),
    CHECKIN_TOO_LATE(5419, "签到时间过晚"),
    OFFERING_NOT_STARTED(5420, "课程尚未开始"),
    
    // ========== 通用错误码 9000-9999 ==========
    EXPORT_FAILED(9001, "导出失败"),
    IMPORT_FAILED(9002, "导入失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取错误信息
     */
    public static String getMessageByCode(Integer code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessage();
            }
        }
        return "未知错误";
    }
}

