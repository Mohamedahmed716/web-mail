package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.model.User;
import com.mailSystem.demo.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class TrashService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    public List<Mail> getTrash(String userEmail) {
        return fileAccessLayer.loadMails(userEmail, Constants.TRASH);
    }

    public void moveToTrash(String userEmail, String mailId, String fromFolder) {
        Mail mail = fileAccessLayer.getMailById(userEmail, fromFolder, mailId);
        if (mail == null) return;

        // 1. Store the original folder name
        mail.setParentFolder(fromFolder);

        // 2. Set the current folder to Trash
        mail.setFolder(Constants.TRASH);

        // 3. Set the trash entry date (Crucial for 30-day auto-delete)
        mail.setTrashEntryDate(new Date());

        // 4. Save into Trash
        fileAccessLayer.saveMailToFolder(userEmail, Constants.TRASH, mail);

        // 5. Remove from original folder
        fileAccessLayer.deleteMail(userEmail, fromFolder, mailId);
    }

    public void restoreFromTrash(String userEmail, String mailId) {
        Mail mail = fileAccessLayer.getMailById(userEmail, Constants.TRASH, mailId);
        if (mail == null) return;

        // Determine the target folder. Default to INBOX if originalFolder is null or invalid.
        String targetFolder = mail.getParentFolder();
        if (targetFolder == null || targetFolder.trim().isEmpty()) {
            targetFolder = mail.getFirstFolder();
        }

        // Clear the trash-related data
        mail.setParentFolder(null); // Clear the history
        mail.setTrashEntryDate(null); // Clear the deletion date

        // 1. Restore to the determined target folder
        mail.setFolder(targetFolder);

        // 2. Save into the target folder
        fileAccessLayer.saveMailToFolder(userEmail, targetFolder, mail);

        // 3. Remove from the Trash folder
        fileAccessLayer.deleteMail(userEmail, Constants.TRASH, mailId);
    }

    public void deleteForever(String userEmail, String mailId) {
        fileAccessLayer.deleteMail(userEmail, Constants.TRASH, mailId);
    }
    public void bulkMoveToTrash(String userEmail, List<String> mailIds, String fromFolder) {
        Date trashDate = new Date(); // Record the current date/time

        for (String mailId : mailIds) {
            Mail mail = fileAccessLayer.getMailById(userEmail, fromFolder, mailId);
            if (mail == null) continue;

            // 1. Set the folder to TRASH
            mail.setFolder(Constants.TRASH);

            // 2. Set the trash entry date
            mail.setTrashEntryDate(trashDate);

            // 3. Save the mail to the Trash folder (This will also handle moving the file)
            fileAccessLayer.saveMailToFolder(userEmail, Constants.TRASH, mail);

            // 4. Remove from original folder
            fileAccessLayer.deleteMail(userEmail, fromFolder, mailId);
        }
    }
    public void cleanupExpiredTrash() {
        System.out.println("--- Starting Trash Cleanup Job ---");

        // 1. Calculate the cutoff date (30 days ago)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        Date cutoffDate = cal.getTime();

        // 2. Load all users (assuming FileAccessLayer has this method)
        // If not, you will need to add a loadAllUsers method to FileAccessLayer
        List<User> allUsers = fileAccessLayer.loadAllUsers();

        for (User user : allUsers) {
            String userEmail = user.getEmail();
            // Load ALL emails from the TRASH folder for this user
            List<Mail> trashMails = fileAccessLayer.loadMails(userEmail, Constants.TRASH);

            int deletedCount = 0;

            for (Mail mail : trashMails) {
                Date entryDate = mail.getTrashEntryDate();

                // Check if the mail has an entry date and if it is older than the 30-day cutoff
                if (entryDate != null && entryDate.before(cutoffDate)) {
                    // Reuse the existing, working delete logic
                    fileAccessLayer.deleteMail(userEmail, Constants.TRASH, mail.getId());
                    deletedCount++;
                }
            }
            if (deletedCount > 0) {
                System.out.println("[Cleanup] Deleted " + deletedCount + " expired emails for user: " + userEmail);
            }
        }
        System.out.println("--- Trash Cleanup Job Finished ---");
    }
}
