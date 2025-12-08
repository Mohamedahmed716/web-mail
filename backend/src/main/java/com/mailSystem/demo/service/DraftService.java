package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class DraftService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    public void saveDraft(String sender, List<String> receivers, String subject, String body, int priority, List<MultipartFile> attachments, String id) throws IOException {

        // 1. Save physical attachments to disk
        // (We save them even for drafts so they are there when you open the draft later)
        List<String> attachmentNames = new ArrayList<>();
        if (attachments != null) {
            for (MultipartFile file : attachments) {
                fileAccessLayer.saveAttachment(file, sender);
                attachmentNames.add(file.getOriginalFilename());
            }
        }

        // 2. Prepare Mail Object
        Mail mail = new Mail();
        // Use existing ID if provided (Update Draft), else generate new
        mail.setId((id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString());

        mail.setSender(sender);
        mail.setReceivers(receivers); // Can be empty for drafts
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setPriority(priority);
        mail.setAttachments(attachmentNames);
        mail.setTimestamp(new Date());

        // CRITICAL: Set folder to DRAFTS. FAL will only save locally.
        mail.setFolder(Constants.DRAFTS);

        // 3. Save (Local only)
        fileAccessLayer.saveMail(mail);
    }

    public boolean deleteDraft(String userEmail, String id) {
        return fileAccessLayer.deleteMail(userEmail, Constants.DRAFTS, id);
    }

    public List<Mail> loadDrafts(String userEmail) {
        return fileAccessLayer.loadMails(userEmail, Constants.DRAFTS);
    }
}
