package com.mailSystem.demo.service.search;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Search strategy for subject field
 */
@Component
public class SubjectSearchStrategy implements IEmailSearchStrategy {
    
    @Override
    public Set<Mail> search(Set<Mail> emails, String query) {
        if (query == null || query.trim().isEmpty()) {
            return emails;
        }
        
        String queryLower = query.toLowerCase();
        return emails.stream()
                .filter(mail -> mail.getSubject() != null &&
                        mail.getSubject().toLowerCase().contains(queryLower))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getStrategyName() {
        return "SUBJECT_SEARCH";
    }
}