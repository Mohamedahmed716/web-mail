package com.mailSystem.demo.controller;

import com.mailSystem.demo.dto.EmailFilterDTO;
import com.mailSystem.demo.dto.InboxResponse;
import com.mailSystem.demo.service.TrashService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trash")
@CrossOrigin
public class TrashController {

    @Autowired
    private TrashService trashService;

    /**
     * Get trash emails with pagination
     */
    @GetMapping
    public ResponseEntity<?> getTrashEmails(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DATE") String sort) {

        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid or missing token");
        }

        String email = UserContext.getUser(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found for this token");
        }

        InboxResponse response = trashService.getTrashEmails(email, page, size, sort);
        return ResponseEntity.ok(response);
    }

    /**
     * Search trash emails with simple query
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTrashEmails(
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
            InboxResponse response = trashService.searchTrash(email, query, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching trash: " + e.getMessage());
        }
    }

    /**
     * Filter trash emails with multiple criteria
     */
    @PostMapping("/filter")
    public ResponseEntity<?> filterTrashEmails(
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
            InboxResponse response = trashService.filterTrash(email, filters, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering trash: " + e.getMessage());
        }
    }
}