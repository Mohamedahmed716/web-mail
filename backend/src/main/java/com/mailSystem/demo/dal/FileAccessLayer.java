package com.mailSystem.demo.dal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mailSystem.demo.model.User;
import com.mailSystem.demo.utils.Constants;
import org.springframework.stereotype.Component;

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
    public List<com.mailSystem.demo.model.Mail> loadMails(String email, String folderName) {
        File folder = new File(Constants.DATA_DIR + "/" + email + "/" + folderName);
        List<com.mailSystem.demo.model.Mail> mails = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        try {
                            com.mailSystem.demo.model.Mail mail = JsonMapper.getInstance().readValue(file, com.mailSystem.demo.model.Mail.class);
                            mails.add(mail);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
}