package com.kairemor.test.service.dto;

import javax.validation.constraints.NotEmpty;

public class PasswordForgotDTO {

    @NotEmpty
    private String email;

    @NotEmpty
    private String newPassword;

    @NotEmpty
    private String confirmPassword;

    public PasswordForgotDTO() {
    }

    public PasswordForgotDTO(String email, String newPassword, String confirmPassword) {
        this.email = email;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
