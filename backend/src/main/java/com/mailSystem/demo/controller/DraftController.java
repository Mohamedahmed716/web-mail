package com.mailSystem.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.DraftService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/draft")
@CrossOrigin
public class DraftController {
    @Autowired
    private DraftService draftService;

    @PostMapping(value = "/saveDraft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveDraft(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("receivers") String receiversJson,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("id") String id,
            @RequestParam(value = "priority", defaultValue = "3") int priority,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String senderEmail = UserContext.getUser(token);

        if (senderEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found for this token");
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> receivers = mapper.readValue(receiversJson, new TypeReference<List<String>>() {
            });

            draftService.saveDraft(senderEmail, receivers, subject, body, priority, attachments, id);

            return ResponseEntity.ok("Draft saved successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Catches parsing errors (e.g., bad JSON format for receivers)
            return ResponseEntity.badRequest().body("Invalid Request: " + e.getMessage());
        }
    }

    /**
     * Get draft emails with pagination (new endpoint)
     */
    @GetMapping
    public ResponseEntity<?> getDrafts(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DATE_NEWEST") String sort) {

        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }

        String email = UserContext.getUser(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found for this token");
        }

        InboxResponse response = draftService.getDraftEmails(email, page, size, sort);
        return ResponseEntity.ok(response);
    }

    /**
     * Search draft emails with simple query
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDrafts(
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
            InboxResponse response = draftService.searchDrafts(email, query, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching drafts: " + e.getMessage());
        }
    }

    /**
     * Filter draft emails with multiple criteria
     */
    @PostMapping("/filter")
    public ResponseEntity<?> filterDrafts(
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
            InboxResponse response = draftService.filterDrafts(email, filters, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering drafts: " + e.getMessage());
        }
    }

    @GetMapping("/loadDrafts")
    public ResponseEntity<?> loadDrafts(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String senderEmail = UserContext.getUser(token);
        if (senderEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found for this token");
        }

        List<Mail> drafts = draftService.loadDrafts(senderEmail);
        return ResponseEntity.ok(drafts);
    }
}
