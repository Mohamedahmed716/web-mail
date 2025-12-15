package com.mailSystem.demo.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.Filter.EmailFilterManager;
import com.mailSystem.demo.service.search.EmailSearchManager;

@Service
public class InboxSearchService {

    @Autowired
    private EmailFilterManager filterManager;

    @Autowired
    private EmailSearchManager searchManager;

    /**
     * Apply multiple filter criteria to a set of emails using Filter Design Pattern
     */
    public Set<Mail> filterEmails(Set<Mail> emails, EmailFilterDTO filters) {
        if (emails == null || emails.isEmpty()) {
            return emails;
        }

        Set<Mail> result = emails;

        // Use Filter Design Pattern for advanced filtering
        result = filterManager.applyFilters(result, filters);

        // Apply global search if search query is provided
        if (filters.getSearchQuery() != null && !filters.getSearchQuery().isEmpty()) {
            result = searchManager.globalSearch(result, filters.getSearchQuery());
        }

        return result;
    }

    /**
     * Perform global search across all email fields using Search Design Pattern
     */
    public Set<Mail> searchEmails(Set<Mail> emails, String query) {
        if (emails == null || emails.isEmpty() || query == null || query.trim().isEmpty()) {
            return emails;
        }

        return searchManager.globalSearch(emails, query);
    }

    /**
     * Perform search using specific strategy
     */
    public Set<Mail> searchEmails(Set<Mail> emails, String query, String searchStrategy) {
        if (emails == null || emails.isEmpty() || query == null || query.trim().isEmpty()) {
            return emails;
        }

        return searchManager.search(emails, query, searchStrategy);
    }

    // ========== LEGACY METHODS FOR BACKWARD COMPATIBILITY ==========
    // These methods are kept for existing code that might still use them

    private Set<Mail> filterBySender(Set<Mail> emails, String sender) {
        String senderLower = sender.toLowerCase();
        return emails.stream()
                .filter(mail -> mail.getSender() != null &&
                        mail.getSender().toLowerCase().contains(senderLower))
                .collect(Collectors.toSet());
    }

    private Set<Mail> filterByReceiver(Set<Mail> emails, String receiver) {
        String receiverLower = receiver.toLowerCase();
        return emails.stream()
                .filter(mail -> mail.getReceivers() != null &&
                        mail.getReceivers().stream()
                                .anyMatch(r -> r != null &&
                                        r.toLowerCase().contains(receiverLower)))
                .collect(Collectors.toSet());
    }

    private Set<Mail> filterBySubject(Set<Mail> emails, String subject) {
        String subjectLower = subject.toLowerCase();
        return emails.stream()
                .filter(mail -> mail.getSubject() != null &&
                        mail.getSubject().toLowerCase().contains(subjectLower))
                .collect(Collectors.toSet());
    }

    private Set<Mail> filterByHasWords(Set<Mail> emails, String words) {
        String wordsLower = words.toLowerCase();
        return emails.stream()
                .filter(mail -> {
                    String searchableText = (mail.getSubject() + " " + mail.getBody()).toLowerCase();
                    return searchableText.contains(wordsLower);
                })
                .collect(Collectors.toSet());
    }

    private Set<Mail> filterByDoesntHave(Set<Mail> emails, String words) {
        String wordsLower = words.toLowerCase();
        return emails.stream()
                .filter(mail -> {
                    String searchableText = (mail.getSubject() + " " + mail.getBody()).toLowerCase();
                    return !searchableText.contains(wordsLower);
                })
                .collect(Collectors.toSet());
    }

    private Set<Mail> filterByDate(Set<Mail> emails, String dateRange) {
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

    private Set<Mail> filterBySearchQuery(Set<Mail> emails, String query) {
        String queryLower = query.toLowerCase();
        return emails.stream()
                .filter(mail -> {
                    String sender = mail.getSender() != null ? mail.getSender().toLowerCase() : "";
                    String subject = mail.getSubject() != null ? mail.getSubject().toLowerCase() : "";
                    String body = mail.getBody() != null ? mail.getBody().toLowerCase() : "";

                    return sender.contains(queryLower) ||
                            subject.contains(queryLower) ||
                            body.contains(queryLower);
                })
                .collect(Collectors.toSet());
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