package com.university.sms.common.util;

import cn.hutool.core.util.StrUtil;

/**
 * 数据脱敏工具类
 */
public class MaskUtils {

    private MaskUtils() {
    }

    /**
     * 身份证号脱敏，只显示后4位
     * 例如：******************1234
     */
    public static String maskIdNumber(String idNumber) {
        if (StrUtil.isBlank(idNumber)) {
            return idNumber;
        }
        if (idNumber.length() <= 4) {
            return idNumber;
        }
        int maskLength = idNumber.length() - 4;
        return "*".repeat(maskLength) + idNumber.substring(maskLength);
    }

    /**
     * 手机号脱敏，显示前3位和后4位
     * 例如：138****1234
     */
    public static String maskPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return phone;
        }
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏，显示前2位和@后面的域名
     * 例如：te***@example.com
     */
    public static String maskEmail(String email) {
        if (StrUtil.isBlank(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email;
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    /**
     * 姓名脱敏，只显示第一个字
     * 例如：张**
     */
    public static String maskName(String name) {
        if (StrUtil.isBlank(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 银行卡号脱敏，显示前4位和后4位
     * 例如：6222****1234
     */
    public static String maskBankCard(String bankCard) {
        if (StrUtil.isBlank(bankCard)) {
            return bankCard;
        }
        if (bankCard.length() <= 8) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }
}
