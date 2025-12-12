package com.mailSystem.demo.service.search;

import com.mailSystem.demo.model.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Search Manager - Manages different search strategies using Strategy Design Pattern
 */
@Service
public class EmailSearchManager {
    
    private final Map<String, IEmailSearchStrategy> searchStrategies = new HashMap<>();
    private final IEmailSearchStrategy defaultStrategy;
    
    @Autowired
    public EmailSearchManager(
            SenderSearchStrategy senderSearchStrategy,
            ReceiverSearchStrategy receiverSearchStrategy,
            SubjectSearchStrategy subjectSearchStrategy,
            ContentSearchStrategy contentSearchStrategy,
            GlobalSearchStrategy globalSearchStrategy) {
        
        // Register all search strategies
        registerStrategy(senderSearchStrategy);
        registerStrategy(receiverSearchStrategy);
        registerStrategy(subjectSearchStrategy);
        registerStrategy(contentSearchStrategy);
        registerStrategy(globalSearchStrategy);
        
        // Set global search as default
        this.defaultStrategy = globalSearchStrategy;
    }
    
    /**
     * Register a new search strategy
     */
    public void registerStrategy(IEmailSearchStrategy strategy) {
        searchStrategies.put(strategy.getStrategyName(), strategy);
    }
    
    /**
     * Perform global search across all fields (default behavior)
     */
    public Set<Mail> globalSearch(Set<Mail> emails, String query) {
        return defaultStrategy.search(emails, query);
    }
    
    /**
     * Perform search using a specific strategy
     */
    public Set<Mail> search(Set<Mail> emails, String query, String strategyName) {
        IEmailSearchStrategy strategy = searchStrategies.get(strategyName);
        if (strategy == null) {
            // Fallback to global search if strategy not found
            return globalSearch(emails, query);
        }
        return strategy.search(emails, query);
    }
    
    /**
     * Get available search strategies
     */
    public Set<String> getAvailableStrategies() {
        return searchStrategies.keySet();
    }
}