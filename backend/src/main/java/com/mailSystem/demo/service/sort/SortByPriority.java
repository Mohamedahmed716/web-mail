package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortByPriority implements ISortStrategy{

    @Override
    public List<Mail> sort(List<Mail> emails) {
        emails.sort(Comparator.comparing((Mail m) -> {
                    if (m.getPriority() == null) return 1;
                    return m.getPriority();
                }).reversed()
                .thenComparing(Comparator.comparing(Mail::getTimestamp).reversed()));
        return emails;
    }
}
