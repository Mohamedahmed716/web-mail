package com.mailSystem.demo.service.Filter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mailSystem.demo.model.Mail;

/**
 * Filter emails by subject
 */
@Component
public class SubjectFilter implements IEmailFilter {
    
    @Override
    public Set<Mail> apply(Set<Mail> emails, Object criteria) {
        if (criteria == null || !(criteria instanceof String)) {
            return emails;
        }
        
        String subject = ((String) criteria).toLowerCase();
        if (subject.isEmpty()) {
            return emails;
        }
        
        return emails.stream()
                .filter(mail -> mail.getSubject() != null &&
                        mail.getSubject().toLowerCase().contains(subject))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getFilterName() {
        return "SUBJECT_FILTER";
    }
}