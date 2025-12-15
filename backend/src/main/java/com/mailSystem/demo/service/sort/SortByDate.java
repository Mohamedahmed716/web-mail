package com.mailSystem.demo.service.sort;

import com.mailSystem.demo.model.Mail;

import java.util.Comparator;
import java.util.List;

public class SortByDate  implements ISortStrategy{

    private final boolean ascending;

    public SortByDate(boolean ascending) {
        this.ascending = ascending;
    }

    public SortByDate() {
        this.ascending=false;
    }
    @Override
    public List<Mail> sort(List<Mail> emails) {
        if(ascending){
            emails.sort(Comparator.comparing(Mail::getTimestamp));
        }
        else {
            emails.sort(Comparator.comparing(Mail::getTimestamp).reversed());}
        return emails;
    }
}
