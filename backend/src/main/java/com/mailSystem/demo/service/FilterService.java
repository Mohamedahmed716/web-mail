package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FilterFileAccess;
import com.mailSystem.demo.model.EmailFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mailSystem.demo.model.Mail;

import java.util.List;
import java.util.Optional;

@Service
public class FilterService {

    @Autowired
    private FilterFileAccess filterFileAccess;

    public EmailFilter addFilter(String userEmail, EmailFilter filter) {
        return filterFileAccess.saveFilter(userEmail, filter);
    }

    public EmailFilter updateFilter(String userEmail, EmailFilter filter) {
        Optional<EmailFilter> existing = filterFileAccess.findFilterById(userEmail, filter.getId());
        if (!existing.isPresent()) {
            throw new RuntimeException("Filter not found with ID: " + filter.getId());
        }
        return filterFileAccess.saveFilter(userEmail, filter);
    }

    public void deleteFilter(String userEmail, String filterId) {
        filterFileAccess.deleteFilter(userEmail, filterId);
    }

    public List<EmailFilter> getUserFilters(String userEmail) {
        return filterFileAccess.getActiveFilters(userEmail);
    }

    public List<EmailFilter> getAllUserFilters(String userEmail) {
        return filterFileAccess.loadUserFilters(userEmail);
    }

    public EmailFilter toggleFilterStatus(String userEmail, String filterId) {
        Optional<EmailFilter> filterOpt = filterFileAccess.findFilterById(userEmail, filterId);
        if (!filterOpt.isPresent()) {
            throw new RuntimeException("Filter not found with ID: " + filterId);
        }

        EmailFilter filter = filterOpt.get();
        filter.setActive(!filter.isActive());
        return filterFileAccess.saveFilter(userEmail, filter);
    }
    // Add this to FilterService.java

    public String applyFiltersToMail(Mail mail, String userEmail) {
        List<EmailFilter> filters = getUserFilters(userEmail);

        for (EmailFilter filter : filters) {
            if (matchesFilter(mail, filter)) {
                return filter.getTargetFolderId();
            }
        }

        return null; // No filter matched
    }

    private boolean matchesFilter(Mail mail, EmailFilter filter) {
        if (mail == null || filter == null || !filter.isActive()) {
            return false;
        }

        String filterValue = filter.getFilterValue().toLowerCase();

        switch (filter.getFilterType()) {
            case SUBJECT:
                return mail.getSubject() != null &&
                        mail.getSubject().toLowerCase().contains(filterValue);

            case SENDER:
                return mail.getSender() != null &&
                        mail.getSender().toLowerCase().contains(filterValue);

            case SUBJECT_AND_SENDER:
                boolean subjectMatch = mail.getSubject() != null &&
                        mail.getSubject().toLowerCase().contains(filterValue);
                boolean senderMatch = mail.getSender() != null &&
                        mail.getSender().toLowerCase().contains(filterValue);
                return subjectMatch && senderMatch;

            default:
                return false;
        }
    }
}