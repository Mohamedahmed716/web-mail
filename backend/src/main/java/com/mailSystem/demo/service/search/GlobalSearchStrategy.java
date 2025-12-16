package com.mailSystem.demo.service.search;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Global search strategy - searches across all fields (sender, receiver, subject, content)
 */
@Component
public class GlobalSearchStrategy implements IEmailSearchStrategy {
    
    @Override
    public Set<Mail> search(Set<Mail> emails, String query) {
        if (query == null || query.trim().isEmpty()) {
            return emails;
        }
        
        String queryLower = query.toLowerCase();
        Set<Mail> results = new HashSet<>();
        
        for (Mail mail : emails) {
            // Search in sender (only username part, ignore domain)
            if (mail.getSender() != null && 
                matchesEmailUsername(mail.getSender().toLowerCase(), queryLower)) {
                results.add(mail);
                continue;
            }
            
            // Search in receivers (only username part, ignore domain)
            if (mail.getReceivers() != null && 
                mail.getReceivers().stream().anyMatch(receiver -> 
                    receiver != null && matchesEmailUsername(receiver.toLowerCase(), queryLower))) {
                results.add(mail);
                continue;
            }
            
            // Search in subject
            if (mail.getSubject() != null && 
                mail.getSubject().toLowerCase().contains(queryLower)) {
                results.add(mail);
                continue;
            }
            
            // Search in body/content
            if (mail.getBody() != null && 
                mail.getBody().toLowerCase().contains(queryLower)) {
                results.add(mail);
            }
        }
        
        return results;
    }
    
    @Override
    public String getStrategyName() {
        return "GLOBAL_SEARCH";
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