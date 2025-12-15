package com.mailSystem.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.SendService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/send")
public class SendController {

    @Autowired
    private SendService sendService;

    @PostMapping(value = "/sendEmail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendEmail(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("receivers") String receiversJson,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("id") String id,
            @RequestParam(value = "priority", defaultValue = "3") int priority,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String senderEmail = UserContext.getUser(token);

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> receivers = mapper.readValue(receiversJson, new TypeReference<List<String>>() {
            });

            if (receivers == null || receivers.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: At least one receiver is required.");
            }

            if (subject == null || subject.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Subject is required.");

            }

            if (priority < 1 || priority > 5) {
                return ResponseEntity.badRequest().body("Error: Priority must be between 1 (Low) and 5 (Critical).");
            }

            // 3. PROCESS
            sendService.sendEmail(senderEmail, receivers, subject, body, priority, attachments, id);

            return ResponseEntity.ok("Email sent successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Catches parsing errors (e.g., bad JSON format for receivers)
            return ResponseEntity.badRequest().body("Invalid Request: " + e.getMessage());
        }
    }

    /**
     * Get sent emails with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<?> getSentEmails(
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

        InboxResponse response = sendService.loadSent(email, page, size, sort);
        return ResponseEntity.ok(response);
    }

    /**
     * Load sent emails (original endpoint - maintains backward compatibility)
     */
    @GetMapping("/loadSent")
    public ResponseEntity<?> loadSent(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String senderEmail = UserContext.getUser(token);
        if (senderEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found for this token");
        }

        List<Mail> sent = sendService.loadSent(senderEmail);
        return ResponseEntity.ok(sent);
    }

    /**
     * Search sent emails with simple query
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchSentEmails(
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
            InboxResponse response = sendService.searchSent(email, query, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching emails: " + e.getMessage());
        }
    }

    /**
     * Filter sent emails with multiple criteria
     */
    @PostMapping("/filter")
    public ResponseEntity<?> filterSentEmails(
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
            InboxResponse response = sendService.filterSent(email, filters, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering emails: " + e.getMessage());
        }
    }
}