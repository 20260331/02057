package com.university.sms.common.response;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "数据冲突"),

    // 服务端错误 5xx
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 1xxx
    PARAM_ERROR(1001, "参数校验失败"),
    DATA_NOT_EXIST(1002, "数据不存在"),
    DATA_ALREADY_EXIST(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败"),

    // 认证授权错误 2xxx
    LOGIN_FAILED(2001, "用户名或密码错误"),
    ACCOUNT_LOCKED(2002, "账户已被锁定"),
    ACCOUNT_DISABLED(2003, "账户已被禁用"),
    TOKEN_EXPIRED(2004, "登录已过期，请重新登录"),
    TOKEN_INVALID(2005, "无效的令牌"),
    PERMISSION_DENIED(2006, "权限不足"),

    // 学生模块错误 3xxx
    STUDENT_NOT_FOUND(3001, "学生信息不存在"),
    STUDENT_INFO_MODIFY_DENIED(3002, "无法修改关键信息"),
    STUDENT_EXPORT_FAILED(3003, "学生数据导出失败"),
    STUDENT_IMPORT_FAILED(3004, "学生数据导入失败"),

    // 选课模块错误 4xxx
    COURSE_NOT_FOUND(4001, "课程不存在"),
    COURSE_FULL(4002, "课程已满"),
    COURSE_TIME_CONFLICT(4003, "课程时间冲突"),
    COURSE_PREREQUISITE_NOT_MET(4004, "未满足先修课程要求"),
    COURSE_SELECTION_CLOSED(4005, "选课时间已结束"),
    COURSE_ALREADY_SELECTED(4006, "已选择该课程"),

    // 成绩模块错误 5xxx
    GRADE_NOT_FOUND(5001, "成绩记录不存在"),
    GRADE_INVALID_SCORE(5002, "成绩分数无效"),
    GRADE_ALREADY_APPROVED(5003, "成绩已审批，无法修改"),
    GRADE_SUBMIT_FAILED(5004, "成绩提交失败"),

    // 请假模块错误 6xxx
    LEAVE_NOT_FOUND(6001, "请假记录不存在"),
    LEAVE_DATE_INVALID(6002, "请假日期无效"),
    LEAVE_ALREADY_APPROVED(6003, "请假已审批"),
    LEAVE_APPROVAL_FAILED(6004, "请假审批失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
