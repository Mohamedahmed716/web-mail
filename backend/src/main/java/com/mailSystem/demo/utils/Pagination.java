package com.mailSystem.demo.utils;

import java.util.ArrayList;
import java.util.List;

public class Pagination {
    public static <T> List<T> slice(List<T> list, int page, int size) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        int totalItems = list.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);

        if (start >= totalItems || start < 0) {
            return new ArrayList<>();
        }

        return list.subList(start, end);
    }
}
