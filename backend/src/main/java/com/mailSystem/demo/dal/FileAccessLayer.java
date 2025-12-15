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

        System.out.println("DEBUG: Looking in folder: " + folder.getAbsolutePath());
        System.out.println("DEBUG: Does folder exist? " + folder.exists());
        // -------------------------------

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            System.out.println("DEBUG: Found " + (files != null ? files.length : 0) + " files.");
            // ------------------

            if (files != null) {
                for (File file : files) {
                    System.out.println("DEBUG: Checking file: " + file.getName());
                    // ------------------

                    if (file.getName().endsWith(".json")) {
                        try {
                            Mail mail = JsonMapper.getInstance().readValue(file, Mail.class);

                            mail.setFolder(folderName);
                            mails.add(mail);
                        } catch (IOException e) {
                            e.printStackTrace();
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
        String originalFolder = mail.getFolder();
        String sender = mail.getSender();

        try {
            mail.setFolder(Constants.INBOX); // Change to Inbox for receivers

            for (String receiver : mail.getReceivers()) {
                File receiverFolder = new File(Constants.DATA_DIR + "/" + receiver + "/" + Constants.INBOX);
                if (!receiverFolder.exists()) receiverFolder.mkdirs();

                File receiverFile = new File(receiverFolder, mail.getId() + ".json");
                JsonMapper.getInstance().writeValue(receiverFile, mail);

                copyAttachments(sender, receiver, mail.getAttachmentNames());
            }
        } finally {
            // ALWAYS reset to original folder, even if an error occurs
            mail.setFolder(originalFolder);
        }
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
    public Mail getMailById(String userEmail, String folderName, String mailId) {
        File file = new File(Constants.DATA_DIR + "/" + userEmail + "/" + folderName + "/" + mailId + ".json");
        if (!file.exists()) return null;

        try {
            return JsonMapper.getInstance().readValue(file, Mail.class);
        } catch (Exception e) {
            return null;
        }
    }

    //copy attachments from sender to receiver
    private void copyAttachments(String senderEmail, String receiverEmail, List<String> attachmentNames) {
        if (attachmentNames == null || attachmentNames.isEmpty()) return;

        File senderAttachDir = new File(Constants.DATA_DIR + "/" + senderEmail + "/Attachments");
        File receiverAttachDir = new File(Constants.DATA_DIR + "/" + receiverEmail + "/Attachments");

        if (!receiverAttachDir.exists()) {
            receiverAttachDir.mkdirs();
        }

        for (String fileName : attachmentNames) {
            if (fileName.startsWith("http") || fileName.startsWith("https")) continue;

            File sourceFile = new File(senderAttachDir, fileName);
            File destFile = new File(receiverAttachDir, fileName);

            if (sourceFile.exists()) {
                try {
                    java.nio.file.Files.copy(
                            sourceFile.toPath(),
                            destFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void saveMailToFolder(String userEmail, String folderName, Mail mail) {
        try {
            File folder = new File(Constants.DATA_DIR + "/" + userEmail + "/" + folderName);
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, mail.getId() + ".json");
            JsonMapper.getInstance().writeValue(file, mail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Helper to get the path to the custom folders metadata file
    private File getFoldersFile(String userEmail) {
        String userPath = Constants.DATA_DIR + "/" + userEmail;
        return new File(userPath, Constants.FOLDERS_FILE);
    }

    // 1. READ: Load all custom folder names for a user
    public List<String> loadUserFolders(String userEmail) {
        File file = getFoldersFile(userEmail);
        try {
            if (!file.exists()) {
                // If file doesn't exist, return an empty list
                return new ArrayList<>();
            }
            // Uses TypeReference for deserializing List<String>
            return JsonMapper.getInstance().readValue(file, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load user folders for: " + userEmail, e);
        }
    }

    // 2. WRITE: Save the complete list of custom folder names
    // Used by FolderService after C, R, or D operations
    public void saveAllUserFolders(String userEmail, List<String> folders) {
        File file = getFoldersFile(userEmail);
        try {
            JsonMapper.getInstance().writeValue(file, folders);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user folders for: " + userEmail, e);
        }
    }

    // 3. CREATE/ADD: Creates the physical directory and updates the metadata file
    public String addFolder(String userEmail, String folderName) {
        // 3a. Sanitize folderName (important for file paths!)
        String sanitizedName = folderName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");

        // 3b. Create the physical directory on disk
        File folderDir = new File(Constants.DATA_DIR + "/" + userEmail + "/" + sanitizedName);

        if (folderDir.exists()) {
            // This also handles accidental creation of system folders (e.g., if user tries to create a folder named "Inbox")
            throw new IllegalArgumentException("Folder already exists: " + folderName);
        }

        // Ensure the base user directory exists
        new File(Constants.DATA_DIR + "/" + userEmail).mkdirs();

        if (!folderDir.mkdirs()) {
            throw new RuntimeException("Failed to create folder directory on disk.");
        }

        // 3c. Update the metadata file
        List<String> folders = loadUserFolders(userEmail);
        if (!folders.contains(sanitizedName)) {
            folders.add(sanitizedName);
            saveAllUserFolders(userEmail, folders);
        }

        return sanitizedName;
    }

}