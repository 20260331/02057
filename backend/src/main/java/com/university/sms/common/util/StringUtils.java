package com.university.sms.common.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * 手机号正则
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 身份证号正则（18位）
     */
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");

    /**
     * 学号正则（字母数字组合，8-20位）
     */
    private static final Pattern STUDENT_NO_PATTERN = Pattern.compile("^[A-Za-z0-9]{8,20}$");

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 验证手机号格式
     */
    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证身份证号格式
     */
    public static boolean isValidIdNumber(String idNumber) {
        if (isBlank(idNumber)) {
            return false;
        }
        return ID_NUMBER_PATTERN.matcher(idNumber).matches();
    }

    /**
     * 验证学号格式
     */
    public static boolean isValidStudentNo(String studentNo) {
        if (isBlank(studentNo)) {
            return false;
        }
        return STUDENT_NO_PATTERN.matcher(studentNo).matches();
    }

    /**
     * 生成UUID（无横线）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToUnderscore(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     */
    public static String underscoreToCamel(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    /**
     * 截取字符串，超出部分用省略号代替
     */
    public static String truncate(String str, int maxLength) {
        if (isBlank(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 安全获取字符串，null返回空字符串
     */
    public static String defaultIfNull(String str) {
        return str == null ? "" : str;
    }

    /**
     * 安全获取字符串，null或空返回默认值
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }
}
