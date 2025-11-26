package com.example.util;

import com.example.model.User;


public class Session {

    private static User currentUser;
    private static boolean guestMode = false;

    // ============================================================
    // LOGIN / SET USER
    // ============================================================
    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
        guestMode = false;
    }

    // ============================================================
    // GUEST MODE
    // ============================================================
    public static synchronized void setGuestMode() {
        currentUser = null;
        guestMode = true;
    }

    public static synchronized boolean isGuest() {
        return guestMode;
    }

    // ============================================================
    // GET USER
    // ============================================================
    public static synchronized User getCurrentUser() {
        return currentUser;
    }


    public static synchronized User getUserOrThrow() {
        if (currentUser == null && !guestMode) {
            throw new IllegalStateException("Користувач не увійшов у систему.");
        }
        return currentUser;
    }

    // ============================================================
    // STATUS
    // ============================================================
    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    // ============================================================
    // LOGOUT (clear session)
    // ============================================================
    public static synchronized void logout() {
        currentUser = null;
        guestMode = false;
    }

    public static synchronized void clear() {
        currentUser = null;
        guestMode = false;
    }
}
