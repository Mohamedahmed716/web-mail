package com.mailSystem.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            List<String> receivers = mapper.readValue(receiversJson, new TypeReference<List<String>>(){});

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
}