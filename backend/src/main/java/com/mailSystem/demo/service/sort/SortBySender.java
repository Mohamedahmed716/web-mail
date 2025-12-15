package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortBySender implements ISortStrategy {

    private final boolean ascending; // true = A-Z, false = Z-A

    public SortBySender(boolean ascending) {
        this.ascending = ascending;
    }

    public SortBySender() {
        this(true);
    }

    @Override
    public List<Mail> sort(List<Mail> emails) {
        Comparator<Mail> senderComparator = Comparator.comparing(
                Mail::getSender,
                String.CASE_INSENSITIVE_ORDER
        );
        senderComparator = senderComparator.thenComparing(Mail::getTimestamp);

        if (!ascending) {
            senderComparator = senderComparator.reversed();
        }

        emails.sort(senderComparator);
        return emails;
    }
}