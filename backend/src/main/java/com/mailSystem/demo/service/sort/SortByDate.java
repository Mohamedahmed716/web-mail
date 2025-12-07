package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortByDate  implements ISortStrategy{

    @Override
    public List<Mail> sort(List<Mail> emails) {
        emails.sort(Comparator.comparing(Mail::getTimestamp).reversed());
        return emails;
    }
}
