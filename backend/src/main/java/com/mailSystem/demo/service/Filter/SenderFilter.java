package com.mailSystem.demo.service.filter;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

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