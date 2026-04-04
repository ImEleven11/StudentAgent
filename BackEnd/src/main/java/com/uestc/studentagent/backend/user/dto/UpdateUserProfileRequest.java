package com.uestc.studentagent.backend.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserProfileRequest {

    @Size(max = 64, message = "nickname is too long")
    private String nickname;

    @Size(max = 512, message = "avatar URL is too long")
    private String avatar;

    @Pattern(regexp = "^$|^[0-9+\\-() ]{6,32}$", message = "phone format is invalid")
    private String phone;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
