package com.mailSystem.demo.service.filter;

import com.mailSystem.demo.model.Mail;
import java.util.Set;

/**
 * Filter Design Pattern Interface
 * Defines the contract for all email filters
 */
public interface IEmailFilter {
    /**
     * Apply the filter to a set of emails
     * @param emails The emails to filter
     * @param criteria The filter criteria
     * @return Filtered set of emails
     */
    Set<Mail> apply(Set<Mail> emails, Object criteria);
    
    /**
     * Get the filter name for identification
     * @return Filter name
     */
    String getFilterName();
}