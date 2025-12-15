package com.mailSystem.demo.service.Filter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mailSystem.demo.model.Mail;

/**
 * Filter emails by content (body)
 */
@Component
public class ContentFilter implements IEmailFilter {
    
    @Override
    public Set<Mail> apply(Set<Mail> emails, Object criteria) {
        if (criteria == null || !(criteria instanceof String)) {
            return emails;
        }
        
        String content = ((String) criteria).toLowerCase();
        if (content.isEmpty()) {
            return emails;
        }
        
        return emails.stream()
                .filter(mail -> mail.getBody() != null &&
                        mail.getBody().toLowerCase().contains(content))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getFilterName() {
        return "CONTENT_FILTER";
    }
}