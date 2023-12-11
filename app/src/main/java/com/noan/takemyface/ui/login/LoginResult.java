package com.noan.takemyface.ui.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult {
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private Integer error;
    private String errorStr;

    LoginResult(@Nullable Integer error) {
        this.error = error;
    }
    LoginResult(@Nullable String error) {
        this.errorStr = error;
    }
    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    @Nullable
    LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
    @Nullable
    String getErrorStr() {
        return errorStr;
    }
}