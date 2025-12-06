package com.mailSystem.demo.service.Filter;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
@Component
public class SubjectFilterCriteria implements EmailFilterCriteria {
    @Override
    public Set<Mail> meet(Set<Mail> mails, String subject) {
        if (mails == null || subject == null) {
            return mails;
        }
        return mails.stream()
                .filter(mail -> mail.getSubject() != null &&
                        mail.getSubject().toLowerCase().contains(subject.toLowerCase()))
                .collect(toSet());
    }
}

