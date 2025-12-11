package com.mailSystem.demo.service.search;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Search strategy for receiver field
 */
@Component
public class ReceiverSearchStrategy implements IEmailSearchStrategy {
    
    @Override
    public Set<Mail> search(Set<Mail> emails, String query) {
        if (query == null || query.trim().isEmpty()) {
            return emails;
        }
        
        String queryLower = query.toLowerCase();
        return emails.stream()
                .filter(mail -> mail.getReceivers() != null &&
                        mail.getReceivers().stream()
                                .anyMatch(receiver -> receiver != null &&
                                        receiver.toLowerCase().contains(queryLower)))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getStrategyName() {
        return "RECEIVER_SEARCH";
    }
}