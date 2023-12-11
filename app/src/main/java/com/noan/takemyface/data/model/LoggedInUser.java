package com.noan.takemyface.data.model;

import okhttp3.CookieJar;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;

    public CookieJar getLoggedInCookie() {
        return loggedInCookie;
    }

    public void setLoggedInCookie(CookieJar loggedInCookie) {
        this.loggedInCookie = loggedInCookie;
    }

    private CookieJar loggedInCookie;

    public LoggedInUser() {}
    public LoggedInUser(String displayName) {
        this.displayName = displayName;
    }

    public LoggedInUser(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }


    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}