package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.sort.ISortStrategy;
import com.mailSystem.demo.service.sort.SortByDate;
import com.mailSystem.demo.utils.Constants;
import com.mailSystem.demo.utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class FolderService {

    @Autowired
    private FileAccessLayer fileAccessLayer;
    private InboxSearchService searchService;
    Mail mail = new Mail();
    // Helper to add system folders for the frontend display
    private void addSystemFolders(List<String> folders) {
        if (!folders.contains(Constants.INBOX)) folders.add(0, Constants.INBOX);
        if (!folders.contains(Constants.SENT)) folders.add(1, Constants.SENT);
        if (!folders.contains(Constants.DRAFTS)) folders.add(2, Constants.DRAFTS);
        if (!folders.contains(Constants.TRASH)) folders.add(3, Constants.TRASH);
    }

    // 1. READ: Get all user folders (Custom + System)
    public List<String> getAllFolders(String userEmail) {
        List<String> folders = fileAccessLayer.loadUserFolders(userEmail);
        addSystemFolders(folders);
        return folders;
    }

    // 2. CREATE: Creates the physical folder and updates metadata
    // NOTE: Assumes FileAccessLayer.addFolder is implemented
    public String createFolder(String userEmail, String folderName) {
        return fileAccessLayer.addFolder(userEmail, folderName);
    }

    // 3. RENAME: Renames directory and updates metadata
    public void renameFolder(String userEmail, String oldName, String newName) {
        List<String> folders = fileAccessLayer.loadUserFolders(userEmail);
        String sanitizedNewName = newName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");

        if (!folders.contains(oldName)) {
            throw new IllegalArgumentException("Original folder not found.");
        }
        if (folders.contains(sanitizedNewName)) {
            throw new IllegalArgumentException("A folder with the new name already exists.");
        }

        // 3a. Rename the physical directory on disk
        File oldDir = new File(Constants.DATA_DIR + "/" + userEmail + "/" + oldName);
        File newDir = new File(Constants.DATA_DIR + "/" + userEmail + "/" + sanitizedNewName);
        if (!oldDir.renameTo(newDir)) {
            throw new RuntimeException("Failed to rename folder on disk. Check file locks.");
        }

        // 3b. Update the metadata file
        int index = folders.indexOf(oldName);
        folders.remove(index);
        folders.add(index, sanitizedNewName);
        // NOTE: Assumes FileAccessLayer.saveAllUserFolders is implemented
        fileAccessLayer.saveAllUserFolders(userEmail, folders);
    }

    // 4. DELETE: Removes folder, moves contents to INBOX, and deletes directory
    public void deleteFolder(String userEmail, String folderName) {
        // 4a. Remove folder name from metadata
        List<String> folders = fileAccessLayer.loadUserFolders(userEmail);
        if (!folders.remove(folderName)) {
            throw new IllegalArgumentException("Folder not found.");
        }
        fileAccessLayer.saveAllUserFolders(userEmail, folders);

        // 4b. Move all emails from this folder to the Trash
        List<Mail> mailsToMove = fileAccessLayer.loadMails(userEmail, folderName);
        for (Mail mail : mailsToMove) {
            mail.setFolder(Constants.TRASH);
            fileAccessLayer.saveMailToFolder(userEmail, Constants.TRASH, mail);
            fileAccessLayer.deleteMail(userEmail, folderName, mail.getId());
        }

        // 4c. Delete the empty folder directory itself
        File folderDir = new File(Constants.DATA_DIR + "/" + userEmail + "/" + folderName);
        if (folderDir.exists() && folderDir.isDirectory()) {
            if (folderDir.list().length == 0 && !folderDir.delete()) {
                System.err.println("Warning: Could not delete the empty physical directory: " + folderName);
            }
        }
    }
    public void singleMoveToFolder(String userEmail, String mailId, String fromFolder, String targetFolder) {
        if (fromFolder.equalsIgnoreCase(targetFolder)) {
            return;
        }

        Mail mail = fileAccessLayer.getMailById(userEmail, fromFolder, mailId);
        if (mail == null) return;

        mail.setFolder(targetFolder);
        mail.setOriginalFolder(targetFolder);
        if(targetFolder.equals(Constants.TRASH)){
              mail.setTrashEntryDate(new Date());}

        fileAccessLayer.saveMailToFolder(userEmail, targetFolder, mail);
        fileAccessLayer.deleteMail(userEmail, fromFolder, mailId);
    }
}