package com.mailSystem.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mail {
    private String id;
    private String sender;
    private List<String> receivers;
    private String subject;
    private String body;
    private Date timestamp;
    private Integer priority; // 1-4 (4 = highest)
    private List<String> attachmentNames;
    private String folder; // Inbox, Sent, Trash, etc.
    private Date trashEntryDate;
    private String parentFolder;
    private Boolean isRead; // Default is false (unread)
    private String firstFolder;

    /**
     * Custom getter for isRead to handle null values from old JSON files.
     * Returns false if the value is null (backward compatibility).
     */
    public Boolean getIsRead() {
        return isRead != null ? isRead : false;
    }
}