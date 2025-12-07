package com.mailSystem.demo.controller;

import com.mailSystem.demo.model.Mail;
import com.mailSystem.demo.service.SearchAndSortService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchAndSortService searchAndSortService;

    /**
     * Simple search in emails
     */
    @PostMapping("/emails")
    public ResponseEntity<List<Mail>> searchEmails(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Mail> mails,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "ALL") String searchType) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        List<Mail> results = searchAndSortService.searchEmails(mails, searchTerm, searchType);
        return ResponseEntity.ok(results);
    }

    /**
     * Advanced search with multiple criteria
     */
    @PostMapping("/advanced")
    public ResponseEntity<List<Mail>> advancedSearch(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Mail> mails,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String sender,
            @RequestParam(required = false) String receiver,
            @RequestParam(required = false) String content) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        List<Mail> results = searchAndSortService.advancedSearch(mails, subject, sender, receiver, content);
        return ResponseEntity.ok(results);
    }

    /**
     * Sort emails by attribute
     */
    @PostMapping("/sort")
    public ResponseEntity<List<Mail>> sortEmails(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Mail> mails,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        List<Mail> results = searchAndSortService.sortEmails(mails, sortBy, ascending);
        return ResponseEntity.ok(results);
    }

    /**
     * Search and sort combined
     */
    @PostMapping("/search-and-sort")
    public ResponseEntity<List<Mail>> searchAndSort(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Mail> mails,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "ALL") String searchType,
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        List<Mail> results = searchAndSortService.searchAndSort(
                mails, searchTerm, searchType, sortBy, ascending
        );
        return ResponseEntity.ok(results);
    }

    /**
     * Filter by date range
     */
    @PostMapping("/filter/date-range")
    public ResponseEntity<List<Mail>> filterByDateRange(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Mail> mails,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        Date start = startDate != null ? new Date(startDate) : null;
        Date end = endDate != null ? new Date(endDate) : null;

        List<Mail> results = searchAndSortService.filterByDateRange(mails, start, end);
        return ResponseEntity.ok(results);
    }

    /**
     * Filter by priority range
     */
    @PostMapping("/filter/priority")
    public ResponseEntity<List<Mail>> filterByPriority(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Mail> mails,
            @RequestParam(required = false) Integer minPriority,
            @RequestParam(required = false) Integer maxPriority) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        List<Mail> results = searchAndSortService.filterByPriority(mails, minPriority, maxPriority);
        return ResponseEntity.ok(results);
    }

    /**
     * Filter by importance level
     */
//    @PostMapping("/filter/importance")
//    public ResponseEntity<List<Mail>> filterByImportance(
//            @RequestHeader("Authorization") String token,
//            @RequestBody List<Mail> mails,
//            @RequestParam(required = false) Integer minImportance) {
//
//        if (!UserContext.isValid(token)) {
//            return ResponseEntity.status(401).build();
//        }
//
//        List<Mail> results = searchAndSortService.filterByImportance(mails, minImportance);
//        return ResponseEntity.ok(results);
//    }

    /**
     * Get statistics about emails
     */
    @PostMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEmailStatistics(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Mail> mails) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> stats = searchAndSortService.getEmailStatistics(mails);
        return ResponseEntity.ok(stats);
    }
}