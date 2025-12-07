package com.mailSystem.demo.controller;

import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.InboxService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbox")
@CrossOrigin
public class InboxController {

    @Autowired
    private InboxService inboxService;
    @GetMapping
    public ResponseEntity<?> getEmails(
            @RequestHeader(value = "Authorization", required = false) String token,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DATE") String sort
    ) {


        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid or missing token");
        }

        String email = UserContext.getUser(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found for this token");
        }

        List<Mail> emails = inboxService.getInboxEmails(email, page, size, sort);

        return ResponseEntity.ok(emails);
    }
}