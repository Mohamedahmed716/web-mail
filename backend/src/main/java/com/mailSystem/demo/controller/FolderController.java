package com.mailSystem.demo.controller;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.FolderService;
import com.mailSystem.demo.service.TrashService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;
    private final TrashService trashService;
    private final FileAccessLayer fileAccessLayer; // NEW: Inject FileAccessLayer for mail loading

    @Autowired
    public FolderController(FolderService folderService, TrashService trashService, FileAccessLayer fileAccessLayer) {
        this.folderService = folderService;
        this.trashService = trashService;
        this.fileAccessLayer = fileAccessLayer; // Initialize FileAccessLayer
    }

    private String getUser(String token) {
        if (!UserContext.isValid(token)) throw new RuntimeException("Invalid user");
        return UserContext.getUser(token);
    }

    // ====================================================================
    // 1. GET /api/folders - Folder Metadata
    // ====================================================================
    @GetMapping
    public ResponseEntity<List<String>> getAllUserFolders(@RequestHeader("Authorization") String token) {
        List<String> folders = folderService.getAllFolders(getUser(token));
        return ResponseEntity.ok(folders);
    }

    // ====================================================================
    // 2. GET /api/folders/{folderName} - Folder Contents (NEW)
    // ====================================================================
    @GetMapping("/{folderName}")
    public ResponseEntity<List<Mail>> getMailByFolder(
            @RequestHeader("Authorization") String token,
            @PathVariable String folderName

    ) {
        String userEmail = getUser(token);

        // Use FileAccessLayer to load all mails for the dynamic folder name
        List<Mail> mails = fileAccessLayer.loadMails(userEmail, folderName);

        return ResponseEntity.ok(mails);
    }


    // ====================================================================
    // 3. Folder CRUD & Single Move Endpoints
    // ====================================================================

    // POST: Create a new custom folder
    @PostMapping
    public ResponseEntity<String> addFolder(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        String folderName = request.get("name");
        try {
            String createdName = folderService.createFolder(getUser(token), folderName);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdName);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // PUT: Rename an existing folder
    @PutMapping("/{oldName}")
    public ResponseEntity<String> renameFolder(@RequestHeader("Authorization") String token, @PathVariable String oldName, @RequestBody Map<String, String> request) {
        String newName = request.get("newName");
        try {
            folderService.renameFolder(getUser(token), oldName, newName);
            return ResponseEntity.ok("Folder renamed successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE: Delete a custom folder
    @DeleteMapping("/{folderName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(@RequestHeader("Authorization") String token, @PathVariable String folderName) {
        if (folderName.equalsIgnoreCase("Inbox") || folderName.equalsIgnoreCase("Sent") ||
                folderName.equalsIgnoreCase("Trash") || folderName.equalsIgnoreCase("Drafts")) {
            throw new IllegalArgumentException("Cannot delete system folders.");
        }
        folderService.deleteFolder(getUser(token), folderName);
    }

    // POST: Single Move Mail to Any Folder
    @PostMapping("/move/{mailId}/{targetFolder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveSingleMail(
            @RequestHeader("Authorization") String token,
            @PathVariable String mailId,
            @PathVariable String targetFolder,
            @RequestParam("sourceFolder") String sourceFolder
    ) {
        folderService.singleMoveToFolder(
                getUser(token),
                mailId,
                sourceFolder,
                targetFolder
        );
    }
}