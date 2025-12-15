package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.sort.ISortStrategy;
import com.mailSystem.demo.service.sort.SortByDate;
import com.mailSystem.demo.service.sort.SortByPriority;
import com.mailSystem.demo.service.sort.SortFactory;
import com.mailSystem.demo.utils.Constants;
import com.mailSystem.demo.utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class DraftService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    @Autowired
    private InboxSearchService searchService;

    public void saveDraft(String sender, List<String> receivers, String subject, String body, int priority, List<MultipartFile> attachments, String id) throws IOException {
        if (receivers.isEmpty() && subject.isEmpty() && body.isEmpty() && attachments == null) {
            if (id != null) {
                fileAccessLayer.deleteMail(sender, Constants.DRAFTS, id);
            }
            return;
        }
        // 1. Save physical attachments to disk
        // (We save them even for drafts so they are there when you open the draft later)
        List<String> attachmentNames = new ArrayList<>();
        if (attachments != null) {
            for (MultipartFile file : attachments) {
                fileAccessLayer.saveAttachment(file, sender);
                attachmentNames.add(file.getOriginalFilename());
            }
        }

        // 2. Prepare Mail Object using Builder Pattern
        Mail mail = Mail.builder()
                .id((id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString())
                .sender(sender)
                .receivers(receivers)
                .subject(subject)
                .body(body)
                .priority(priority)
                .attachmentNames(attachmentNames)
                .timestamp(new Date())
                // CRITICAL: Set folder to DRAFTS
                .folder(Constants.DRAFTS)
                .build();

        // 3. Save (Local only)
        fileAccessLayer.saveMail(mail);
    }

    public boolean deleteDraft(String userEmail, String id) {
        return fileAccessLayer.deleteMail(userEmail, Constants.DRAFTS, id);
    }

    /**
     * Get draft emails with pagination and sorting (new method)
     */
    public InboxResponse getDraftEmails(String email, int page, int size, String sortType) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, Constants.DRAFTS);

        SortFactory.getStrategy(sortType).sort(allMails);

        int total = allMails.size();
        List<Mail> pagedList = Pagination.slice(allMails, page, size);

        return new InboxResponse(pagedList, total);
    }

    /**
     * Get all draft emails as Set (for filtering)
     */
    public Set<Mail> getUserDraftEmails(String email) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, Constants.DRAFTS);
        return new HashSet<>(allMails);
    }

    /**
     * Search draft emails using Search Design Pattern
     */
    public InboxResponse searchDrafts(String email, String query, int page, int size) {
        // Get all draft emails
        Set<Mail> allMails = getUserDraftEmails(email);

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
     * Filter draft emails using Filter Design Pattern
     */
    public InboxResponse filterDrafts(String email, EmailFilterDTO filters, int page, int size) {
        // Get all draft emails
        Set<Mail> allMails = getUserDraftEmails(email);

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

    public List<Mail> loadDrafts(String userEmail) {
        return fileAccessLayer.loadMails(userEmail, Constants.DRAFTS);
    }
}
