package com.mailSystem.demo.controller;

import com.mailSystem.demo.model.EmailFilter;
import com.mailSystem.demo.service.FilterService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
public class FilterController {

    @Autowired
    private FilterService filterService;

    @PostMapping
    public ResponseEntity<EmailFilter> addFilter(
            @RequestHeader("Authorization") String token,
            @RequestBody EmailFilter filter) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        EmailFilter saved = filterService.addFilter(userEmail, filter);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailFilter> updateFilter(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestBody EmailFilter filter) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        filter.setId(id);
        String userEmail = UserContext.getUser(token);
        EmailFilter updated = filterService.updateFilter(userEmail, filter);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilter(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        filterService.deleteFilter(userEmail, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<EmailFilter>> getUserFilters(@RequestHeader("Authorization") String token) {
        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        List<EmailFilter> filters = filterService.getUserFilters(userEmail);
        return ResponseEntity.ok(filters);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmailFilter>> getAllUserFilters(@RequestHeader("Authorization") String token) {
        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        List<EmailFilter> filters = filterService.getAllUserFilters(userEmail);
        return ResponseEntity.ok(filters);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<EmailFilter> toggleFilterStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {

        if (!UserContext.isValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = UserContext.getUser(token);
        EmailFilter filter = filterService.toggleFilterStatus(userEmail, id);
        return ResponseEntity.ok(filter);
    }
}