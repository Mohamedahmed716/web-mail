package com.mailSystem.demo.utils;

import java.util.concurrent.ConcurrentHashMap;

public class UserContext {
    private static final ConcurrentHashMap<String, String> activeSessions = new ConcurrentHashMap<>();

    public static void addSession(String token, String email) {
        activeSessions.put(token, email);
    }

    public static void removeSession(String token) {
        activeSessions.remove(token);
    }

    public static boolean isValid(String token) {
        return token != null && activeSessions.containsKey(token);
    }

    public static String getUser(String token) {
        return activeSessions.get(token);
    }
}