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
                        matchesEmailUsername(mail.getSender().toLowerCase(), sender))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getFilterName() {
        return "SENDER_FILTER";
    }
    
    /**
     * Helper method to match email username part only (ignore domain)
     */
    private boolean matchesEmailUsername(String email, String searchTerm) {
        // Always search only the username part (before @), ignore domain completely
        int atIndex = email.indexOf("@");
        if (atIndex > 0) {
            String username = email.substring(0, atIndex);
            return username.contains(searchTerm);
        }
        
        // If no @ found, search the whole string (shouldn't happen with valid emails)
        return email.contains(searchTerm);
    }
}