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
            // Search in sender
            if (mail.getSender() != null && 
                mail.getSender().toLowerCase().contains(queryLower)) {
                results.add(mail);
                continue;
            }
            
            // Search in receivers
            if (mail.getReceivers() != null && 
                mail.getReceivers().stream().anyMatch(receiver -> 
                    receiver != null && receiver.toLowerCase().contains(queryLower))) {
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
}