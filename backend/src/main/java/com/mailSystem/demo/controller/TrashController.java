package com.mailSystem.demo.controller;

import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.TrashService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trash")
public class TrashController {

    private final TrashService trashService;

    public TrashController(TrashService trashService) {
        this.trashService = trashService;
    }

    private String getUser(String token) {
        if (!UserContext.isValid(token)) throw new RuntimeException("Invalid user");
        return UserContext.getUser(token);
    }

    @GetMapping
    public List<Mail> getTrash(@RequestHeader("Authorization") String token) {
        return trashService.getTrash(getUser(token));
    }

    @PostMapping("/move/{mailId}/{fromFolder}")
    public void moveToTrash(
            @RequestHeader("Authorization") String token,
            @PathVariable String mailId,
            @PathVariable String fromFolder
    ) {
        trashService.moveToTrash(getUser(token), mailId, fromFolder);
    }

    @PostMapping("/restore/{mailId}")
    public void restore(@RequestHeader("Authorization") String token,
                        @PathVariable String mailId) {
        trashService.restoreFromTrash(getUser(token), mailId);
    }

    @DeleteMapping("/deleteForever/{mailId}")
    public void deleteForever(@RequestHeader("Authorization") String token,
                              @PathVariable String mailId) {
        trashService.deleteForever(getUser(token), mailId);
    }
    @PostMapping("/bulk/move/{fromFolder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkMoveToTrash(
            @RequestHeader("Authorization") String token,
            @PathVariable String fromFolder,
            @RequestBody List<String> mailIds // List of IDs to trash
    ) {
        if (mailIds == null || mailIds.isEmpty()) {
            throw new IllegalArgumentException("Mail IDs list cannot be empty");
        }

        // This assumes all mails are being trashed from the same folder
        trashService.bulkMoveToTrash(getUser(token), mailIds, fromFolder);
    }


    // NEW ENDPOINT: BULK DELETE FOREVER (Permanent Deletion)
    @DeleteMapping("/bulk/deleteForever")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkDeleteForever(
            @RequestHeader("Authorization") String token,
            @RequestBody List<String> mailIds
    ) {
        if (mailIds == null || mailIds.isEmpty()) return;

        String userEmail = getUser(token);

        for (String mailId : mailIds) {
            // Reuses the efficient single delete logic
            trashService.deleteForever(userEmail, mailId);
        }
    }
}
