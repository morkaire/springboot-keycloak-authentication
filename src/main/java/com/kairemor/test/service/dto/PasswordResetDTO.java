package com.kairemor.test.service.dto;

import javax.validation.constraints.NotEmpty;

public class PasswordResetDTO {

    @NotEmpty
    private String oldPassword;

    @NotEmpty
    private String newPassword;

    @NotEmpty
    private String confirmPassword;

    public PasswordResetDTO() {}

    public PasswordResetDTO(String oldPassword, String newPassword, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() {
        return this.oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public String toString() {
        return "PasswordResetDTO{" +
            "oldPassword='" + oldPassword + '\'' +
            ", newPassword='" + newPassword + '\'' +
            ", confirmPassword='" + confirmPassword + '\'' +
            '}';
    }
}
