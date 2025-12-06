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
    private Integer priority;      // 1-5 (5 = highest)
    private Integer importance;    // 1-5 (5 = most important)
    private List<String> attachments;
    private String folder;         // Inbox, Sent, Trash, etc.
}