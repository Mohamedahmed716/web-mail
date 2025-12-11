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

import java.util.*;

@Service
public class TrashService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    @Autowired
    private InboxSearchService searchService;

    /**
     * Get trash emails with pagination and sorting
     */
    public InboxResponse getTrashEmails(String email, int page, int size, String sortType) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, "Trash");

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
     * Get all trash emails as Set (for filtering)
     */
    public Set<Mail> getUserTrashEmails(String email) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, "Trash");
        return new HashSet<>(allMails);
    }

    /**
     * Search trash emails using Search Design Pattern
     */
    public InboxResponse searchTrash(String email, String query, int page, int size) {
        // Get all trash emails
        Set<Mail> allMails = getUserTrashEmails(email);

        // Apply global search using Search Design Pattern
        Set<Mail> filteredMails = searchService.searchEmails(allMails, query);

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
     * Filter trash emails using Filter Design Pattern
     */
    public InboxResponse filterTrash(String email, EmailFilterDTO filters, int page, int size) {
        // Get all trash emails
        Set<Mail> allMails = getUserTrashEmails(email);

        // Apply filters using Filter Design Pattern
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