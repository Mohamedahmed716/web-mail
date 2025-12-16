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

    /**
     * Step 1: Verify if email exists in the system
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        User user = accountService.findByEmail(email);
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("exists", true);
            response.put("message", "Email found. Please answer your security question.");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found in our system");
    }

    /**
     * Step 2: Verify security question answer (favorite movie)
     */
    @PostMapping("/verify-security-question")
    public ResponseEntity<?> verifySecurityQuestion(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String favoriteMovie = request.get("favoriteMovie");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (favoriteMovie == null || favoriteMovie.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Security answer is required");
        }

        boolean isValid = accountService.verifySecurityQuestion(email, favoriteMovie);
        if (isValid) {
            Map<String, Object> response = new HashMap<>();
            response.put("verified", true);
            response.put("message", "Identity verified. Please enter your new password.");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect answer. Access denied.");
    }

    /**
     * Step 3: Reset password after security verification
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String favoriteMovie = request.get("favoriteMovie");
        String newPassword = request.get("newPassword");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (favoriteMovie == null || favoriteMovie.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Security answer is required");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }

        // Re-verify security question before resetting password
        boolean isValid = accountService.verifySecurityQuestion(email, favoriteMovie);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Security verification failed");
        }

        boolean success = accountService.resetPassword(email, newPassword);
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successful. You can now login with your new password.");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password");
    }
}