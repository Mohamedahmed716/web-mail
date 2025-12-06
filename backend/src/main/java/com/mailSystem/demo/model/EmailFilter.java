package com.mailSystem.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailFilter {
    private String id;
    private String userId;
    private String filterName;
    private FilterType filterType;
    private String filterValue;
    private String targetFolderId;
    private boolean active;

    public EmailFilter(String userId, String filterName, FilterType filterType,
                       String filterValue, String targetFolderId) {
        this.userId = userId;
        this.filterName = filterName;
        this.filterType = filterType;
        this.filterValue = filterValue;
        this.targetFolderId = targetFolderId;
        this.active = true;
    }
}