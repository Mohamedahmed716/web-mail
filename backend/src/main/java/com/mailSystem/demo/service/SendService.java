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
public class SendService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    @Autowired
    private InboxSearchService searchService;

    public void sendEmail(String sender, List<String> receivers, String subject, String body, int priority, List<MultipartFile> attachments, String id) throws IOException {

        // 1. Save physical attachments to disk
        List<String> attachmentNames = new ArrayList<>();
        if (attachments != null) {
            for (MultipartFile file : attachments) {
                fileAccessLayer.saveAttachment(file, sender);
                attachmentNames.add(file.getOriginalFilename());
            }
        }

        // ID Logic: If an ID comes from the frontend, it might be a Draft we are now sending.
        // If it's empty/null, generate a new one.
        boolean wasDraft = (id != null && !id.isEmpty());
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

        // CRITICAL: Set folder to SENT. FAL will see this and distribute copies to receivers.
        mail.setFolder(Constants.SENT);

        // 3. Save (and Distribute)
        fileAccessLayer.saveMail(mail);

        // 4. Cleanup: If this was a draft, delete the old file from the Drafts folder
        // because we just moved it to Sent.
        if (wasDraft) {
            fileAccessLayer.deleteMail(sender, Constants.DRAFTS, id);
        }
    }

    /**
     * Load sent emails with pagination and sorting (original method)
     */
    public InboxResponse loadSent(String userEmail, int page, int size, String sortType) {
        List<Mail> allMails = fileAccessLayer.loadMails(userEmail, Constants.SENT);

        SortFactory.getStrategy(sortType).sort(allMails);


        int total = allMails.size();
        List<Mail> pagedList = Pagination.slice(allMails, page, size);

        return new InboxResponse(pagedList, total);
    }

    /**
     * Load sent emails (for backward compatibility)
     */
    public List<Mail> loadSent(String userEmail) {
        return fileAccessLayer.loadMails(userEmail, Constants.SENT);
    }

    /**
     * Get all sent emails as Set (for filtering)
     */
    public Set<Mail> getUserSentEmails(String email) {
        List<Mail> allMails = fileAccessLayer.loadMails(email, Constants.SENT);
        return new HashSet<>(allMails);
    }

    /**
     * Search sent emails using Search Design Pattern
     */
    public InboxResponse searchSent(String email, String query, int page, int size) {
        // Get all sent emails
        Set<Mail> allMails = getUserSentEmails(email);

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
     * Filter sent emails using Filter Design Pattern
     */
    public InboxResponse filterSent(String email, EmailFilterDTO filters, int page, int size) {
        // Get all sent emails
        Set<Mail> allMails = getUserSentEmails(email);

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