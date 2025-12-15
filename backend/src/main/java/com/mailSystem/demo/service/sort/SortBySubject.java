package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortBySubject implements ISortStrategy {

    private final boolean ascending; // true = A-Z, false = Z-A

    public SortBySubject(boolean ascending) {
        this.ascending = ascending;
    }

    public SortBySubject() {
        this(true);
    }

    @Override
    public List<Mail> sort(List<Mail> emails) {
        Comparator<Mail> subjectComparator = Comparator.comparing(
                Mail::getSubject,
                String.CASE_INSENSITIVE_ORDER
        );
        subjectComparator = subjectComparator.thenComparing(Mail::getTimestamp);

        if (!ascending) {
            subjectComparator = subjectComparator.reversed();
        }

        emails.sort(subjectComparator);
        return emails;
    }
}