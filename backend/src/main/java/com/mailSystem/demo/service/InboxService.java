package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.sort.ISortStrategy;
import com.mailSystem.demo.service.sort.SortByDate;
import com.mailSystem.demo.service.sort.SortByPriority;
import com.mailSystem.demo.utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class InboxService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    @Autowired
    private InboxSearchService searchService;

    /**
     * Get inbox emails with pagination and sorting (original method)
     */
    public InboxResponse getInboxEmails(String email, int page, int size, String sortType) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, "Inbox");

        ISortStrategy sortStrategy;
        if ("PRIORITY".equalsIgnoreCase(sortType)) {
            sortStrategy = new SortByPriority();
        } else {
            sortStrategy = new SortByDate();
        }
        sortStrategy.sort(allMails);

        int total = allMails.size();
        List<Mail> pagedList = Pagination.slice(allMails, page, size);

        return new InboxResponse(pagedList, total);
    }

    /**
     * Get all inbox emails as Set (for filtering)
     */
    public Set<Mail> getUserInboxEmails(String email) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, "Inbox");
        return new HashSet<>(allMails);
    }

    /**
     * Search inbox emails
     */
    public InboxResponse searchInbox(String email, String query, int page, int size) {
        // Get all inbox emails
        Set<Mail> allMails = getUserInboxEmails(email);

        // Create filter for search
        EmailFilterDTO filter = EmailFilterDTO.builder()
                .searchQuery(query)
                .build();

        // Apply search
        Set<Mail> filteredMails = searchService.filterEmails(allMails, filter);

        // Convert to list and sort by date
        List<Mail> mailList = new ArrayList<>(filteredMails);
        ISortStrategy sortStrategy = new SortByDate();
        sortStrategy.sort(mailList);

        // Paginate
        int total = mailList.size();
        List<Mail> pagedList = Pagination.slice(mailList, page, size);

        return new InboxResponse(pagedList, total);
    }

    /**
     * Filter inbox emails with multiple criteria
     */
    public InboxResponse filterInbox(String email, EmailFilterDTO filters, int page, int size) {
        // Get all inbox emails
        Set<Mail> allMails = getUserInboxEmails(email);

        // Apply all filters
        Set<Mail> filteredMails = searchService.filterEmails(allMails, filters);

        // Convert to list and sort by date
        List<Mail> mailList = new ArrayList<>(filteredMails);
        ISortStrategy sortStrategy = new SortByDate();
        sortStrategy.sort(mailList);

        // Paginate
        int total = mailList.size();
        List<Mail> pagedList = Pagination.slice(mailList, page, size);

        return new InboxResponse(pagedList, total);
    }
}