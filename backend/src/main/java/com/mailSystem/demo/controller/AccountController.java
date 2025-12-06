package com.mailSystem.demo.controller;

import com.mailSystem.demo.model.User;
import com.mailSystem.demo.service.AccountService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = accountService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (user != null) {
            String token = UUID.randomUUID().toString();
            UserContext.addSession(token, user.getEmail());
            Map<String, Object> response = new HashMap<>();
            user.setPassword(null);
            response.put("user", user);
            response.put("token", token);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            User createdUser = accountService.register(user);
            createdUser.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (UserContext.isValid(token)) {
            UserContext.removeSession(token);
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.badRequest().body("Invalid session");
    }
}