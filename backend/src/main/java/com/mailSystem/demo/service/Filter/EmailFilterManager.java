package com.mailSystem.demo.service.Filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.model.Mail;

/**
 * Filter Manager - Manages and applies multiple filters using Filter Design Pattern
 */
@Service
public class EmailFilterManager {
    
    private final Map<String, IEmailFilter> filters = new HashMap<>();
    
    @Autowired
    public EmailFilterManager(
            SenderFilter senderFilter,
            ReceiverFilter receiverFilter,
            IEmailFilter subjectFilter,
            ContentFilter contentFilter,
            DateRangeFilter dateRangeFilter,
            AttachmentFilter attachmentFilter) {
        
        // Register all filters
        registerFilter(senderFilter);
        registerFilter(receiverFilter);
        registerFilter(subjectFilter);
        registerFilter(contentFilter);
        registerFilter(dateRangeFilter);
        registerFilter(attachmentFilter);
    }
    
    /**
     * Register a new filter
     */
    public void registerFilter(IEmailFilter filter) {
        filters.put(filter.getFilterName(), filter);
    }
    
    /**
     * Apply multiple filters to emails based on EmailFilterDTO
     */
    public Set<Mail> applyFilters(Set<Mail> emails, EmailFilterDTO filterDTO) {
        if (emails == null || emails.isEmpty()) {
            return emails;
        }
        
        Set<Mail> result = emails;
        
        // Apply sender filter
        if (filterDTO.getFrom() != null && !filterDTO.getFrom().isEmpty()) {
            result = filters.get("SENDER_FILTER").apply(result, filterDTO.getFrom());
        }
        
        // Apply receiver filter
        if (filterDTO.getTo() != null && !filterDTO.getTo().isEmpty()) {
            result = filters.get("RECEIVER_FILTER").apply(result, filterDTO.getTo());
        }
        
        // Apply subject filter
        if (filterDTO.getSubject() != null && !filterDTO.getSubject().isEmpty()) {
            result = filters.get("SUBJECT_FILTER").apply(result, filterDTO.getSubject());
        }
        
        // Apply content filters (hasWords and doesntHave)
        if (filterDTO.getHasWords() != null && !filterDTO.getHasWords().isEmpty()) {
            result = filters.get("CONTENT_FILTER").apply(result, filterDTO.getHasWords());
        }
        
        if (filterDTO.getDoesntHave() != null && !filterDTO.getDoesntHave().isEmpty()) {
            // For "doesn't have", we need to exclude emails that contain the words
            Set<Mail> excludeSet = filters.get("CONTENT_FILTER").apply(result, filterDTO.getDoesntHave());
            result.removeAll(excludeSet);
        }
        
        // Apply date range filter
        if (filterDTO.getDateWithin() != null && !filterDTO.getDateWithin().isEmpty()) {
            result = filters.get("DATE_RANGE_FILTER").apply(result, filterDTO.getDateWithin());
        }
        
        // Apply attachment filter
        if (filterDTO.getHasAttachment() != null && !filterDTO.getHasAttachment().isEmpty()) {
            result = filters.get("ATTACHMENT_FILTER").apply(result, filterDTO.getHasAttachment());
        }
        
        return result;
    }
    
    /**
     * Get available filter names
     */
    public Set<String> getAvailableFilters() {
        return filters.keySet();
    }
}