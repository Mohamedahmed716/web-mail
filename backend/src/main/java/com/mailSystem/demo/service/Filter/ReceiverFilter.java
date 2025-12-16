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
                                        matchesEmailUsername(r.toLowerCase(), receiver)))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getFilterName() {
        return "RECEIVER_FILTER";
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