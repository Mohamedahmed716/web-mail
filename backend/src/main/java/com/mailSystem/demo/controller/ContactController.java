package com.mailSystem.demo.controller;

import com.mailSystem.demo.dto.ContactDTO;
import com.mailSystem.demo.service.ContactService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDTO> addContact(
            @RequestHeader("Authorization") String token,
            @RequestBody ContactDTO contactDTO) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        ContactDTO saved = contactService.addContact(userEmail, contactDTO);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<ContactDTO>> getContacts(@RequestHeader("Authorization") String token) {
        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        List<ContactDTO> contacts = contactService.getContacts(userEmail);
        return ResponseEntity.ok(contacts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactDTO> updateContact(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody ContactDTO contactDTO) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        contactDTO.setId(id);
        String userEmail = UserContext.getUser(token);
        ContactDTO updated = contactService.updateContact(userEmail, contactDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        contactService.deleteContact(userEmail, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContactDTO>> searchContacts(
            @RequestHeader("Authorization") String token,
            @RequestParam String query,
            @RequestParam(defaultValue = "default") String searchType) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        List<ContactDTO> results = contactService.searchContacts(userEmail, query, searchType);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<ContactDTO>> searchByName(
            @RequestHeader("Authorization") String token,
            @RequestParam String name) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        List<ContactDTO> results = contactService.searchContactByName(userEmail, name);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/email")
    public ResponseEntity<List<ContactDTO>> searchByEmail(
            @RequestHeader("Authorization") String token,
            @RequestParam String email) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        List<ContactDTO> results = contactService.searchContactByEmail(userEmail, email);
        return ResponseEntity.ok(results);
    }

}