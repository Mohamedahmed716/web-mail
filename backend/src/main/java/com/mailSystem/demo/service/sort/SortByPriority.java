package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortByPriority implements ISortStrategy {

    private final boolean ascending;

    public SortByPriority(boolean ascending) {
        this.ascending = ascending;
    }

    public SortByPriority() {
        this.ascending = false;
    }

    @Override
    public List<Mail> sort(List<Mail> emails) {
        Comparator<Mail> priorityComparator = Comparator.comparing((Mail m) -> {
            if (m.getPriority() == null) return 1;
            return m.getPriority();
        });


        if (!ascending) {
            priorityComparator = priorityComparator.reversed();
        }

        priorityComparator = priorityComparator.thenComparing(
                Comparator.comparing(Mail::getTimestamp).reversed()
        );

        emails.sort(priorityComparator);
        return emails;
    }
}
