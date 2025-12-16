package com.mailSystem.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private long totalRecords;
    private int currentPage;
    private int pageSize;
    private int totalPages;

    public PaginatedResponse(List<T> data, long totalRecords, int currentPage, int pageSize) {
        this.data = data;
        this.totalRecords = totalRecords;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    }
}