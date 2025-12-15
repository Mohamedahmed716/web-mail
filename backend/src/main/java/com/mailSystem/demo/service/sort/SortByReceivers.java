package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortByReceivers implements ISortStrategy {
    private final boolean ascending; // true = A-Z, false = Z-A

    public SortByReceivers(boolean ascending) {
        this.ascending = ascending;
    }

    public SortByReceivers() {
        this(true);
    }

    @Override
    public List<Mail> sort(List<Mail> emails) {

        Comparator<Mail> receiverComparator = Comparator.comparing((Mail m) -> {
            List<String> list = m.getReceivers();

            if (list == null || list.isEmpty()) {
                return "";
            }

            return list.get(0);

        }, String.CASE_INSENSITIVE_ORDER);

        if (!ascending) {
            receiverComparator = receiverComparator.reversed();
        }

        emails.sort(receiverComparator);
        return emails;
    }
}
