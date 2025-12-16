package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.sort.ISortStrategy;
import com.mailSystem.demo.service.sort.SortByDate;
import com.mailSystem.demo.service.sort.SortByPriority;
import com.mailSystem.demo.service.sort.SortFactory;
import com.mailSystem.demo.utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriorityInboxService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    @Autowired
    private InboxSearchService searchService;

    /**
     * Get priority inbox emails with pagination and sorting
     * Priority inbox shows the same emails as inbox but sorted by priority
     */
    public InboxResponse getPriorityInboxEmails(String email, int page, int size, String sortType) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, "Inbox");

        // Always sort by priority for priority inbox, regardless of sortType parameter
        ISortStrategy sortStrategy = new SortByPriority();
        sortStrategy.sort(allMails);
        SortFactory.getStrategy(sortType).sort(allMails);


        int total = allMails.size();
        List<Mail> pagedList = Pagination.slice(allMails, page, size);

        return new InboxResponse(pagedList, total);
    }

    /**
     * Get all priority inbox emails as Set (for filtering)
     * Same emails as inbox, just for priority-focused view
     */
    public Set<Mail> getUserPriorityInboxEmails(String email) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, "Inbox");
        return new HashSet<>(allMails);
    }

    /**
     * Search priority inbox emails using Search Design Pattern
     */
    public InboxResponse searchPriorityInbox(String email, String query, int page, int size) {
        // Get all priority inbox emails
        Set<Mail> allMails = getUserPriorityInboxEmails(email);

        // Apply global search using Search Design Pattern
        Set<Mail> filteredMails = searchService.searchEmails(allMails, query);

        // Convert to list and sort by priority
        List<Mail> mailList = new ArrayList<>(filteredMails);
        ISortStrategy sortStrategy = new SortByPriority();
        sortStrategy.sort(mailList);

        // Paginate
        int total = mailList.size();
        List<Mail> pagedList = Pagination.slice(mailList, page, size);

        return new InboxResponse(pagedList, total);
    }

    /**
     * Filter priority inbox emails using Filter Design Pattern
     */
    public InboxResponse filterPriorityInbox(String email, EmailFilterDTO filters, int page, int size) {
        // Get all priority inbox emails
        Set<Mail> allMails = getUserPriorityInboxEmails(email);

        // Apply filters using Filter Design Pattern
        Set<Mail> filteredMails = searchService.filterEmails(allMails, filters);

        // Convert to list and sort by priority
        List<Mail> mailList = new ArrayList<>(filteredMails);
        ISortStrategy sortStrategy = new SortByPriority();
        sortStrategy.sort(mailList);

        // Paginate
        int total = mailList.size();
        List<Mail> pagedList = Pagination.slice(mailList, page, size);

        return new InboxResponse(pagedList, total);
    }

    /**
     * Mark an email as read
     */
    public boolean markAsRead(String userEmail, String mailId) {
        return fileAccessLayer.updateMailReadStatus(userEmail, "Inbox", mailId, true);
    }
}