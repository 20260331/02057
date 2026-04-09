package com.university.sms.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库恢复任务状态（异步恢复用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestoreStatusDTO {

    /** 状态：IDLE-无任务, RUNNING-恢复中, SUCCESS-成功, FAILED-失败 */
    private String status;

    /** 附加信息（如失败原因、成功提示） */
    private String message;

    public static RestoreStatusDTO idle() {
        return new RestoreStatusDTO("IDLE", null);
    }

    public static RestoreStatusDTO running() {
        return new RestoreStatusDTO("RUNNING", "恢复进行中");
    }

    public static RestoreStatusDTO success(String msg) {
        return new RestoreStatusDTO("SUCCESS", msg != null ? msg : "恢复成功");
    }

    public static RestoreStatusDTO failed(String msg) {
        return new RestoreStatusDTO("FAILED", msg);
    }
}
