package com.mailSystem.demo.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailFilterDTO {
    private String from;           // Sender email
    private String to;             // Receiver email
    private String subject;        // Subject keywords
    private String hasWords;       // Words that must be in body/subject
    private String doesntHave;     // Words that must NOT be in body/subject
    private String dateWithin;     // Date range: 1d, 3d, 1w, 2w, 1m, 3m, 6m, 1y

    // Pagination
    private Integer page = 1;
    private Integer pageSize = 10;

    // Search query (for simple search bar)
    private String searchQuery;

    // Helper method to check if any filter is applied
    public boolean hasAnyFilter() {
        return (from != null && !from.isEmpty()) ||
                (to != null && !to.isEmpty()) ||
                (subject != null && !subject.isEmpty()) ||
                (hasWords != null && !hasWords.isEmpty()) ||
                (doesntHave != null && !doesntHave.isEmpty()) ||
                (searchQuery != null && !searchQuery.isEmpty());
    }
}