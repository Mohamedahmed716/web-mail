package com.mailSystem.demo.controller;

import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.service.InboxService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inbox")
@CrossOrigin
public class InboxController {

    @Autowired
    private InboxService inboxService;

    /**
     * Get inbox emails with pagination (original endpoint)
     */
    @GetMapping
    public ResponseEntity<?> getEmails(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DATE_NEWEST") String sort) {

        System.out.println("🔥 DEBUG: Sort Parameter received: " + sort);

        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }

        String email = UserContext.getUser(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found for this token");
        }

        InboxResponse response = inboxService.getInboxEmails(email, page, size, sort);
        return ResponseEntity.ok(response);
    }

    /**
     * Search inbox emails with simple query
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchEmails(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }

        String email = UserContext.getUser(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found for this token");
        }

        try {
            InboxResponse response = inboxService.searchInbox(email, query, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching emails: " + e.getMessage());
        }
    }

    /**
     * Filter inbox emails with multiple criteria
     */
    @PostMapping("/filter")
    public ResponseEntity<?> filterEmails(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody EmailFilterDTO filters,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }

        String email = UserContext.getUser(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found for this token");
        }

        try {
            InboxResponse response = inboxService.filterInbox(email, filters, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering emails: " + e.getMessage());
        }
    }

    /**
     * Mark an email as read
     */
    @PutMapping("/{mailId}/read")
    public ResponseEntity<?> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String mailId) {

        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }

        String email = UserContext.getUser(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found for this token");
        }

        boolean success = inboxService.markAsRead(email, mailId);
        if (success) {
            return ResponseEntity.ok().body("Email marked as read");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Email not found");
        }
    }
}