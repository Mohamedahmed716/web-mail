package com.mailSystem.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact {
    private String id;
    private String userId;
    private String name;
    private Set<String> emails;

    public Contact(String name, Set<String> emails) {
        this.name = name;
        this.emails = emails != null ? new HashSet<>(emails) : new HashSet<>();
    }

    public void addEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (this.emails == null) {
                this.emails = new HashSet<>();
            }
            this.emails.add(email.trim().toLowerCase());
        }
    }

    public void removeEmail(String email) {
        if (this.emails != null && email != null) {
            this.emails.remove(email.trim().toLowerCase());
        }
    }

    public boolean hasEmail(String email) {
        if (this.emails == null || email == null) {
            return false;
        }
        return this.emails.contains(email.trim().toLowerCase());
    }

    public String getPrimaryEmail() {
        if (this.emails == null || this.emails.isEmpty()) {
            return null;
        }
        return this.emails.iterator().next();
    }

    public String getEmailsAsString() {
        if (this.emails == null || this.emails.isEmpty()) {
            return "";
        }
        return String.join(", ", this.emails);
    }

    public boolean isValid() {
        return this.name != null && !this.name.trim().isEmpty()
                && this.emails != null && !this.emails.isEmpty();
    }
}