package com.university.sms.system.dto;

import com.university.sms.common.util.MaskUtils;
import com.university.sms.system.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private String statusText;
    private List<String> roles;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;

    public static UserDTO fromUser(User user, List<String> roles) {
        return fromUser(user, roles, true);
    }

    public static UserDTO fromUser(User user, List<String> roles, boolean mask) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPhone(mask ? MaskUtils.maskPhone(user.getPhone()) : user.getPhone());
        dto.setEmail(mask ? MaskUtils.maskEmail(user.getEmail()) : user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setStatusText(getStatusText(user.getStatus()));
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(roles);
        return dto;
    }

    public static String getStatusText(Integer status) {
        if (status == null) return "";
        return status == 1 ? "启用" : "禁用";
    }
}
