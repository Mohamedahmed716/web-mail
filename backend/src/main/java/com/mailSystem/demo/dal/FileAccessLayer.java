package com.mailSystem.demo.dal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.model.User;
import com.mailSystem.demo.utils.Constants;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileAccessLayer {

    public FileAccessLayer() {
        initializeStorage();
    }

    private void initializeStorage() {
        File root = new File(Constants.DATA_DIR);
        if (!root.exists()) {
            root.mkdirs();
        }

        // Create the master user index file if missing
        File index = new File(root, Constants.USERS_FILE);
        if (!index.exists()) {
            try {
                JsonMapper.getInstance().writeValue(index, new ArrayList<User>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<User> loadAllUsers() {
        File file = new File(Constants.DATA_DIR, Constants.USERS_FILE);
        try {
            if (!file.exists())
                return new ArrayList<>();
            return JsonMapper.getInstance().readValue(file, new TypeReference<List<User>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users", e);
        }
    }


    public List<Mail> loadMails(String email, String folderName) {
        File folder = new File(Constants.DATA_DIR + "/" + email + "/" + folderName);
        List<Mail> mails = new ArrayList<>();

        // --- جمل طباعة للتجربة (Debug) ---
        System.out.println("DEBUG: Looking in folder: " + folder.getAbsolutePath());
        System.out.println("DEBUG: Does folder exist? " + folder.exists());
        // -------------------------------

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            // --- جملة طباعة ---
            System.out.println("DEBUG: Found " + (files != null ? files.length : 0) + " files.");
            // ------------------

            if (files != null) {
                for (File file : files) {
                    // --- جملة طباعة ---
                    System.out.println("DEBUG: Checking file: " + file.getName());
                    // ------------------

                    if (file.getName().endsWith(".json")) {
                        try {
                            Mail mail = JsonMapper.getInstance().readValue(file, Mail.class);
                            mails.add(mail);
                            System.out.println("DEBUG: Loaded mail successfully!");
                        } catch (IOException e) {
                            e.printStackTrace(); // لو فيه مشكلة في الـ JSON نفسه هتظهر هنا
                        }
                    } else {
                        System.out.println("DEBUG: Skipped (Not JSON)");
                    }
                }
            }
        }
        return mails;
    }

    public void saveUser(User user) {
        // Load existing
        List<User> users = loadAllUsers();

        users.add(user);

        // Write back to index.json
        File indexFile = new File(Constants.DATA_DIR, Constants.USERS_FILE);
        try {
            JsonMapper.getInstance().writeValue(indexFile, users);
            createUserDirectoryStructure(user.getEmail());

        } catch (IOException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    // Helper to create Inbox, Sent, Trash folders for a new user

    private void createUserDirectoryStructure(String email) {
        String userPath = Constants.DATA_DIR + "/" + email;
        new File(userPath).mkdirs();
        new File(userPath, Constants.INBOX).mkdirs();
        new File(userPath, Constants.SENT).mkdirs();
        new File(userPath, Constants.TRASH).mkdirs();
        new File(userPath, Constants.DRAFTS).mkdirs();
        new File(userPath, Constants.CONTACTS).mkdirs();
    }



    public void saveMail(Mail mail) throws IOException {
        String sender = mail.getSender();
        String folderName = mail.getFolder(); // "Sent", "Drafts", etc.

        // 1. Save to the Sender's specific folder
        File senderFolder = new File(Constants.DATA_DIR + "/" + sender + "/" + folderName);
        if (!senderFolder.exists()) senderFolder.mkdirs();

        // If ID exists, this OVERWRITES the file (Update Draft logic)
        File senderFile = new File(senderFolder, mail.getId() + ".json");
        JsonMapper.getInstance().writeValue(senderFile, mail);

        // 2. If this is a SEND operation, distribute to Receivers
        if (Constants.SENT.equalsIgnoreCase(folderName)) {
            distributeToReceivers(mail);
        }
    }

    // GENERIC DELETER (Point 3)
    // Works for Deleting a Draft OR moving to Trash
    public boolean deleteMail(String userEmail, String folderName, String mailId) {
        File file = new File(Constants.DATA_DIR + "/" + userEmail + "/" + folderName + "/" + mailId + ".json");
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    // Helper for distribution
    private void distributeToReceivers(Mail mail) throws IOException {
        // Temporarily set folder to Inbox so receivers see it correctly
        String originalFolder = mail.getFolder();
        mail.setFolder(Constants.INBOX);

        for (String receiver : mail.getReceivers()) {
            File receiverFolder = new File(Constants.DATA_DIR + "/" + receiver + "/" + Constants.INBOX);
            if (receiverFolder.exists()) {
                File receiverFile = new File(receiverFolder, mail.getId() + ".json");
                JsonMapper.getInstance().writeValue(receiverFile, mail);
            }
        }
        // Restore folder so the sender object isn't mutated unexpectedly
        mail.setFolder(originalFolder);
    }

    public void saveAttachment(MultipartFile file, String userEmail) throws IOException {
        File userDir = new File(Constants.DATA_DIR, userEmail);
        File attachDir = new File(userDir, "Attachments");

        if (!attachDir.exists()) {
            attachDir.mkdirs();
        }

        File dest = new File(attachDir, file.getOriginalFilename());

        file.transferTo(dest);
    }
}