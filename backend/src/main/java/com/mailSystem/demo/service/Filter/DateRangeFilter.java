package com.mailSystem.demo.service.Filter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mailSystem.demo.model.Mail;

/**
 * Filter emails by date range
 */
@Component
public class DateRangeFilter implements IEmailFilter {
    
    @Override
    public Set<Mail> apply(Set<Mail> emails, Object criteria) {
        if (criteria == null || !(criteria instanceof String)) {
            return emails;
        }
        
        String dateRange = (String) criteria;
        LocalDateTime cutoffDate = calculateCutoffDate(dateRange);
        
        if (cutoffDate == null) {
            return emails;
        }
        
        Date cutoffDateAsDate = Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant());
        
        return emails.stream()
                .filter(mail -> mail.getTimestamp() != null &&
                        mail.getTimestamp().after(cutoffDateAsDate))
                .collect(Collectors.toSet());
    }
    
    @Override
    public String getFilterName() {
        return "DATE_RANGE_FILTER";
    }
    
    /**
     * Calculate cutoff date based on the date range string
     */
    private LocalDateTime calculateCutoffDate(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (dateRange) {
            case "1d":
                return now.minusDays(1);
            case "3d":
                return now.minusDays(3);
            case "1w":
                return now.minusWeeks(1);
            case "2w":
                return now.minusWeeks(2);
            case "1m":
                return now.minusMonths(1);
            case "3m":
                return now.minusMonths(3);
            case "6m":
                return now.minusMonths(6);
            case "1y":
                return now.minusYears(1);
            default:
                return null;
        }
    }
}