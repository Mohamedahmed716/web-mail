package com.mailSystem.demo.service.Filter;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
@Component
public class ContentCriteria implements EmailFilterCriteria {
    @Override
    public Set<Mail> meet(Set<Mail> mails, String content) {
        if (mails == null || content == null) {
            return mails;
        }
        return mails.stream()
                .filter(mail -> mail.getBody() != null &&
                        mail.getBody().toLowerCase().contains(content.toLowerCase()))
                .collect(toSet());
    }
}