package com.mailSystem.demo.service.Filter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mailSystem.demo.model.Mail;

/**
 * Filter emails by receiver
 */
@Component
public class ReceiverFilter implements IEmailFilter {
    
    @Override
    public Set<Mail> apply(Set<Mail> emails, Object criteria) {
        if (criteria == null || !(criteria instanceof String)) {
            return emails;
        }
        
        String receiver = ((String) criteria).toLowerCase();
        if (receiver.isEmpty()) {
            return emails;
        }
        
        return emails.stream()
                .filter(mail -> mail.getReceivers() != null &&
                        mail.getReceivers().stream()
                                .anyMatch(r -> r != null &&
                                        r.toLowerCase().contains(receiver)))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getFilterName() {
        return "RECEIVER_FILTER";
    }
}