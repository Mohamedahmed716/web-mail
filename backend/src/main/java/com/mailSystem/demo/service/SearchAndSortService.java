package com.mailSystem.demo.service;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchAndSortService {

    /**
     * Search emails by a specific field type
     * @param mails List of emails to search through
     * @param searchTerm The term to search for
     * @param searchType Type of search: SUBJECT, SENDER, CONTENT, or ALL
     * @return Filtered list of emails matching the search criteria
     */
    public List<Mail> searchEmails(List<Mail> mails, String searchTerm, String searchType) {
        if (mails == null || mails.isEmpty() || searchTerm == null || searchTerm.trim().isEmpty()) {
            return mails != null ? mails : new ArrayList<>();
        }

        String lowerSearchTerm = searchTerm.toLowerCase();

        switch (searchType.toUpperCase()) {
            case "SUBJECT":
                return searchBySubject(mails, lowerSearchTerm);

            case "SENDER":
                return searchBySender(mails, lowerSearchTerm);

            case "RECEIVER":
            case "TO":
                return searchByReceiver(mails, lowerSearchTerm);

            case "CONTENT":
                return searchByContent(mails, lowerSearchTerm);

            case "ALL":
            default:
                return searchInAllFields(mails, lowerSearchTerm);
        }
    }

    private List<Mail> searchBySubject(List<Mail> mails, String searchTerm) {
        return mails.stream()
                .filter(mail -> mail.getSubject() != null &&
                        mail.getSubject().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    private List<Mail> searchBySender(List<Mail> mails, String searchTerm) {
        return mails.stream()
                .filter(mail -> mail.getSender() != null &&
                        mail.getSender().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    private List<Mail> searchByReceiver(List<Mail> mails, String searchTerm) {
        return mails.stream()
                .filter(mail -> mail.getReceivers() != null &&
                        mail.getReceivers().stream()
                                .anyMatch(r -> r.toLowerCase().contains(searchTerm)))
                .collect(Collectors.toList());
    }

    private List<Mail> searchByContent(List<Mail> mails, String searchTerm) {
        return mails.stream()
                .filter(mail -> mail.getBody() != null &&
                        mail.getBody().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Search in all fields (subject, sender, receiver, content)
     * Uses OR logic - returns emails matching ANY field
     */
    private List<Mail> searchInAllFields(List<Mail> mails, String searchTerm) {
        Set<Mail> results = new HashSet<>();

        results.addAll(searchBySubject(mails, searchTerm));
        results.addAll(searchBySender(mails, searchTerm));
        results.addAll(searchByReceiver(mails, searchTerm));
        results.addAll(searchByContent(mails, searchTerm));

        return new ArrayList<>(results);
    }

    /**
     * Advanced search with multiple criteria (AND logic)
     * All criteria must match
     */
    public List<Mail> advancedSearch(List<Mail> mails, String subject, String sender,
                                     String receiver, String content) {
        List<Mail> results = new ArrayList<>(mails);

        if (subject != null && !subject.trim().isEmpty()) {
            results = searchBySubject(results, subject.toLowerCase());
        }

        if (sender != null && !sender.trim().isEmpty()) {
            results = searchBySender(results, sender.toLowerCase());
        }

        if (receiver != null && !receiver.trim().isEmpty()) {
            results = searchByReceiver(results, receiver.toLowerCase());
        }

        if (content != null && !content.trim().isEmpty()) {
            results = searchByContent(results, content.toLowerCase());
        }

        return results;
    }

    /**
     * Sort emails by specified attribute
     * @param mails List of emails to sort
     * @param sortBy Attribute to sort by: DATE, SENDER, SUBJECT, PRIORITY
     * @param ascending True for ascending order, false for descending
     * @return Sorted list of emails
     */
    public List<Mail> sortEmails(List<Mail> mails, String sortBy, boolean ascending) {
        if (mails == null || mails.isEmpty()) {
            return new ArrayList<>();
        }

        List<Mail> mailList = new ArrayList<>(mails);
        Comparator<Mail> comparator = getComparator(sortBy);

        if (!ascending) {
            comparator = comparator.reversed();
        }

        mailList.sort(comparator);
        return mailList;
    }

    /**
     * Get comparator based on sort type
     */
    private Comparator<Mail> getComparator(String sortBy) {
        switch (sortBy.toUpperCase()) {
            case "DATE":
            case "TIMESTAMP":
                return Comparator.comparing(
                        Mail::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );

            case "SENDER":
            case "FROM":
                return Comparator.comparing(
                        Mail::getSender,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                );

            case "SUBJECT":
                return Comparator.comparing(
                        Mail::getSubject,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                );

            case "PRIORITY":
                return Comparator.comparing(
                        Mail::getPriority,
                        Comparator.nullsLast(Comparator.reverseOrder())
                );

            case "IMPORTANCE":
                return Comparator.comparing(
                        Mail::getImportance,
                        Comparator.nullsLast(Comparator.reverseOrder())
                );

            default:
                // Default to date sorting (newest first)
                return Comparator.comparing(
                        Mail::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())
                );
        }
    }

    /**
     * Search and sort in one operation
     */
    public List<Mail> searchAndSort(List<Mail> mails, String searchTerm, String searchType,
                                    String sortBy, boolean ascending) {
        List<Mail> searchResults = searchEmails(mails, searchTerm, searchType);
        return sortEmails(searchResults, sortBy, ascending);
    }

    /**
     * Filter emails by date range
     */
    public List<Mail> filterByDateRange(List<Mail> mails, Date startDate, Date endDate) {
        if (mails == null || mails.isEmpty()) {
            return mails != null ? mails : new ArrayList<>();
        }

        return mails.stream()
                .filter(mail -> {
                    if (mail.getTimestamp() == null) {
                        return false;
                    }
                    Date mailDate = mail.getTimestamp();
                    boolean afterStart = startDate == null || !mailDate.before(startDate);
                    boolean beforeEnd = endDate == null || !mailDate.after(endDate);
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());
    }

    /**
     * Filter emails by priority
     */
    public List<Mail> filterByPriority(List<Mail> mails, Integer minPriority, Integer maxPriority) {
        if (mails == null || mails.isEmpty()) {
            return mails != null ? mails : new ArrayList<>();
        }

        return mails.stream()
                .filter(mail -> {
                    Integer priority = mail.getPriority();
                    if (priority == null) {
                        return false;
                    }
                    boolean aboveMin = minPriority == null || priority >= minPriority;
                    boolean belowMax = maxPriority == null || priority <= maxPriority;
                    return aboveMin && belowMax;
                })
                .collect(Collectors.toList());
    }

    /**
     * Filter by importance level
     */
    public List<Mail> filterByImportance(List<Mail> mails, Integer minImportance) {
        if (mails == null || mails.isEmpty()) {
            return mails != null ? mails : new ArrayList<>();
        }

        return mails.stream()
                .filter(mail -> mail.getImportance() != null &&
                        mail.getImportance() >= (minImportance != null ? minImportance : 0))
                .collect(Collectors.toList());
    }

    /**
     * Get email statistics
     */
    public Map<String, Object> getEmailStatistics(List<Mail> mails) {
        Map<String, Object> stats = new HashMap<>();

        if (mails == null || mails.isEmpty()) {
            stats.put("total", 0);
            return stats;
        }

        stats.put("total", mails.size());

        // Count by priority
        Map<Integer, Long> priorityCount = mails.stream()
                .filter(m -> m.getPriority() != null)
                .collect(Collectors.groupingBy(Mail::getPriority, Collectors.counting()));
        stats.put("byPriority", priorityCount);

        // Count by sender
        Map<String, Long> senderCount = mails.stream()
                .filter(m -> m.getSender() != null)
                .collect(Collectors.groupingBy(Mail::getSender, Collectors.counting()));
        stats.put("topSenders", senderCount);

        // Count by importance
        Map<Integer, Long> importanceCount = mails.stream()
                .filter(m -> m.getImportance() != null)
                .collect(Collectors.groupingBy(Mail::getImportance, Collectors.counting()));
        stats.put("byImportance", importanceCount);

        return stats;
    }
}