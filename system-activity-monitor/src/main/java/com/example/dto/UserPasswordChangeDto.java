package com.example.dto;

public class UserPasswordChangeDto {
    private String oldPassword;
    private String newPassword;

    public UserPasswordChangeDto(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }
}
