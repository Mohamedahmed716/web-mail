package com.mailSystem.demo.dal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mailSystem.demo.model.Contact;
import com.mailSystem.demo.utils.Constants;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ContactFileAccess {

    private File getContactsFile(String userEmail) {
        String userPath = Constants.DATA_DIR + "/" + userEmail + "/" + Constants.CONTACTS;
        File dir = new File(userPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "contacts.json");
    }

    public List<Contact> loadUserContacts(String userEmail) {
        File file = getContactsFile(userEmail);
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return JsonMapper.getInstance().readValue(file, new TypeReference<List<Contact>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load contacts for user: " + userEmail, e);
        }
    }

    public Contact saveContact(String userEmail, Contact contact) {
        List<Contact> contacts = loadUserContacts(userEmail);

        if (contact.getId() == null) {
            contact.setId(UUID.randomUUID().toString());
        }
        contact.setUserId(userEmail);

        // Remove old version if updating
        contacts.removeIf(c -> c.getId().equals(contact.getId()));
        contacts.add(contact);

        saveAllContacts(userEmail, contacts);
        return contact;
    }

    public void deleteContact(String userEmail, String contactId) {
        List<Contact> contacts = loadUserContacts(userEmail);
        contacts.removeIf(c -> c.getId().equals(contactId));
        saveAllContacts(userEmail, contacts);
    }

    public Optional<Contact> findContactById(String userEmail, String contactId) {
        return loadUserContacts(userEmail).stream()
                .filter(c -> c.getId().equals(contactId))
                .findFirst();
    }

    private void saveAllContacts(String userEmail, List<Contact> contacts) {
        File file = getContactsFile(userEmail);
        try {
            JsonMapper.getInstance().writeValue(file, contacts);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save contacts for user: " + userEmail, e);
        }
    }

    public List<Contact> searchByName(String userEmail, String searchTerm) {
        return loadUserContacts(userEmail).stream()
                .filter(c -> c.getName() != null &&
                        c.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }
}