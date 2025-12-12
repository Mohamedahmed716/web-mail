package com.mailSystem.demo.service.search;

import com.mailSystem.demo.model.Mail;
import java.util.Set;

/**
 * Search Strategy Pattern Interface
 * Defines the contract for different search strategies
 */
public interface IEmailSearchStrategy {
    /**
     * Search emails based on the strategy
     * @param emails The emails to search through
     * @param query The search query
     * @return Set of emails matching the search criteria
     */
    Set<Mail> search(Set<Mail> emails, String query);
    
    /**
     * Get the strategy name for identification
     * @return Strategy name
     */
    String getStrategyName();
}