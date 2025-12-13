package com.mailSystem.demo.service.Filter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mailSystem.demo.model.Mail;

/**
 * Filter emails by sender
 */
@Component
public class SenderFilter implements IEmailFilter {
    
    @Override
    public Set<Mail> apply(Set<Mail> emails, Object criteria) {
        if (criteria == null || !(criteria instanceof String)) {
            return emails;
        }
        
        String sender = ((String) criteria).toLowerCase();
        if (sender.isEmpty()) {
            return emails;
        }
        
        return emails.stream()
                .filter(mail -> mail.getSender() != null &&
                        mail.getSender().toLowerCase().contains(sender))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getFilterName() {
        return "SENDER_FILTER";
    }
}