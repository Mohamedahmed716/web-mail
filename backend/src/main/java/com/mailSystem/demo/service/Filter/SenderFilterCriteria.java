package com.mailSystem.demo.service.Filter;
import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
@Component
public class SenderFilterCriteria implements EmailFilterCriteria {
    @Override
    public Set<Mail> meet(Set<Mail> mails, String sender) {
        if (mails == null || sender == null) {
            return mails;
        }
        return mails.stream()
                .filter(mail -> mail.getSender() != null &&
                        mail.getSender().toLowerCase().contains(sender.toLowerCase()))
                .collect(toSet());
    }
}