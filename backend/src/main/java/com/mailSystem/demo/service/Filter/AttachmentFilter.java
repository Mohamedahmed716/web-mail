package com.mailSystem.demo.service.Filter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mailSystem.demo.model.Mail;

/**
 * Attachment Filter - Filters emails based on attachment presence
 * Implements Filter Design Pattern
 */
@Component
public class AttachmentFilter implements IEmailFilter {
    
    @Override
    public String getFilterName() {
        return "ATTACHMENT_FILTER";
    }
    
    @Override
    public Set<Mail> apply(Set<Mail> emails, Object criteria) {
        if (emails == null || emails.isEmpty() || criteria == null) {
            return emails;
        }
        
        boolean hasAttachment = Boolean.parseBoolean(criteria.toString());
        
        return emails.stream()
                .filter(mail -> {
                    boolean emailHasAttachment = mail.getAttachmentNames() != null && 
                                               !mail.getAttachmentNames().isEmpty();
                    return emailHasAttachment == hasAttachment;
                })
                .collect(Collectors.toSet());
    }
}