package com.mailSystem.demo.dto;

import com.mailSystem.demo.model.Mail;
import java.util.List;

public class InboxResponse {

    // دول المعلومتين اللي هنسفرهم للفرونت
    private List<Mail> data;       // شوية الإيميلات بتوع الصفحة دي
    private int totalRecords;      // العدد الكلي للإيميلات في الفولدر

    // Constructor
    public InboxResponse(List<Mail> data, int totalRecords) {
        this.data = data;
        this.totalRecords = totalRecords;
    }

    // Getters & Setters (مهمين جداً عشان التحويل لـ JSON يشتغل)
    public List<Mail> getData() { return data; }
    public void setData(List<Mail> data) { this.data = data; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
}