package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.sort.ISortStrategy;
import com.mailSystem.demo.service.sort.SortByDate;
import com.mailSystem.demo.service.sort.SortByPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InboxService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    public List<Mail> getInboxEmails(String email,int page , int size ,String sortType){
        List<Mail> allMails=fileAccessLayer.loadMails(email,"inbox");
        ISortStrategy sortStrategy;
        if("priority".equalsIgnoreCase(sortType)){
            sortStrategy=new SortByPriority();
        }
        else {
            sortStrategy=new SortByDate();
        }
        sortStrategy.sort(allMails);
        return paginate(allMails, page, size);
    }
    private List<Mail> paginate(List<Mail> mails, int page, int size) {
        int start = (page - 1) * size;
        int end = Math.min(start + size, mails.size());
        if (start >= mails.size() || start < 0) return new ArrayList<>();
        return mails.subList(start, end);
    }
}

