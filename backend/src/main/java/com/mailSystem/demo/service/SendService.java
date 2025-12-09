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
public class SendService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    public void sendEmail(String sender, List<String> receivers, String subject, String body, int priority, List<MultipartFile> attachments, String id) throws IOException {

        // 1. Save physical attachments to disk
        List<String> attachmentNames = new ArrayList<>();
        if (attachments != null) {
            for (MultipartFile file : attachments) {
                fileAccessLayer.saveAttachment(file, sender);
                attachmentNames.add(file.getOriginalFilename());
            }
        }

        // 2. Prepare Mail Object
        Mail mail = new Mail();

        // ID Logic: If an ID comes from the frontend, it might be a Draft we are now sending.
        // If it's empty/null, generate a new one.
        boolean wasDraft = (id != null && !id.isEmpty());
        mail.setId(wasDraft ? id : UUID.randomUUID().toString());

        mail.setSender(sender);
        mail.setReceivers(receivers);
        mail.setSubject(subject);
        mail.setBody(body);
        mail.setPriority(priority);
        mail.setAttachmentNames(attachmentNames);
        mail.setTimestamp(new Date());

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

    public List<Mail> loadSent(String userEmail) {
        return fileAccessLayer.loadMails(userEmail, Constants.SENT);
    }
}