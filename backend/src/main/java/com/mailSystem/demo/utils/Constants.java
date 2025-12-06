package com.mailSystem.demo.utils;

public class Constants {
    // fixed domain for mail
    public static final String DOMAIN = "@wegmail.com";

    // Saves user home directory
    public static final String DATA_DIR = System.getProperty("user.home") + "/MailServerData";
    public static final String USERS_FILE = "index.json";

    public static final String INBOX = "Inbox";
    public static final String SENT = "Sent";
    public static final String TRASH = "Trash";
    public static final String DRAFTS = "Drafts";
    public static final String CONTACTS = "Contacts";
}