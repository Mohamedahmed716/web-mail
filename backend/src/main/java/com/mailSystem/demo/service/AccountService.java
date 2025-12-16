package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.FileAccessLayer;
import com.mailSystem.demo.model.User;
import com.mailSystem.demo.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private FileAccessLayer fileAccessLayer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User login(String email, String password) {
        List<User> users = fileAccessLayer.loadAllUsers();

        String searchEmail = email;
        if (searchEmail != null && !searchEmail.contains("@")) {
            searchEmail = searchEmail + Constants.DOMAIN;
        }
        final String finalEmail = searchEmail;

        // Find User by Email
        User foundUser = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(finalEmail))
                .findFirst()
                .orElse(null);

        if (foundUser == null)
            return null;

        if (passwordEncoder.matches(password, foundUser.getPassword())) {
            return foundUser;
        }

        return null; // Password wrong
    }

    public User register(User user) {
        List<User> users = fileAccessLayer.loadAllUsers();

        if (user.getEmail() != null && !user.getEmail().contains("@")) {
            user.setEmail(user.getEmail() + Constants.DOMAIN);
        }

        if (users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()))) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        user.setId(UUID.randomUUID().toString());
        fileAccessLayer.saveUser(user);

        return user;
    }

    public List<User> getAllAccounts() {
        return fileAccessLayer.loadAllUsers();
    }

    /**
     * Find a user by their email address
     */
    public User findByEmail(String email) {
        List<User> users = fileAccessLayer.loadAllUsers();

        String searchEmail = email;
        if (searchEmail != null && !searchEmail.contains("@")) {
            searchEmail = searchEmail + Constants.DOMAIN;
        }
        final String finalEmail = searchEmail;

        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(finalEmail))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verify security question answer (favorite movie)
     * Case-insensitive and trims whitespace
     */
    public boolean verifySecurityQuestion(String email, String favoriteMovieAnswer) {
        User user = findByEmail(email);
        if (user == null || user.getFavoriteMovie() == null) {
            return false;
        }

        String storedAnswer = user.getFavoriteMovie().trim().toLowerCase();
        String providedAnswer = favoriteMovieAnswer.trim().toLowerCase();

        return storedAnswer.equals(providedAnswer);
    }

    /**
     * Reset password for a user after security verification
     */
    public boolean resetPassword(String email, String newPassword) {
        User user = findByEmail(email);
        if (user == null) {
            return false;
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        return fileAccessLayer.updateUser(user);
    }
}