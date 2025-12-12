package com.mailSystem.demo.service;

import com.mailSystem.demo.dal.ContactFileAccess;
import com.mailSystem.demo.dto.ContactDTO;
import com.mailSystem.demo.model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ContactFileAccess contactFileAccess;

    public ContactDTO addContact(String userEmail, ContactDTO contactDTO) {
        Contact contact = dtoToContact(contactDTO);
        contact = contactFileAccess.saveContact(userEmail, contact);
        return contactToDto(contact);
    }

    public List<ContactDTO> getContacts(String userEmail) {
        return contactFileAccess.loadUserContacts(userEmail).stream()
                .map(this::contactToDto)
                .collect(Collectors.toList());
    }

    public ContactDTO updateContact(String userEmail, ContactDTO contactDTO) {
        Contact contact = dtoToContact(contactDTO);
        contact = contactFileAccess.saveContact(userEmail, contact);
        return contactToDto(contact);
    }

    public void deleteContact(String userEmail, String contactId) {
        contactFileAccess.deleteContact(userEmail, contactId);
    }

    public List<ContactDTO> searchContactByName(String userEmail, String name) {
        return contactFileAccess.searchByName(userEmail, name).stream()
                .map(this::contactToDto)
                .collect(Collectors.toList());
    }

    public List<ContactDTO> searchContactByEmail(String userEmail, String email) {
        return contactFileAccess.searchByEmail(userEmail, email).stream()
                .map(this::contactToDto)
                .collect(Collectors.toList());
    }

    public List<ContactDTO> searchContacts(String userEmail, String query, String searchType) {
        List<Contact> results;
        
        switch (searchType.toLowerCase()) {
            case "name":
                results = contactFileAccess.searchByName(userEmail, query);
                break;
            case "email":
                results = contactFileAccess.searchByEmail(userEmail, query);
                break;
            case "default":
            case "both":
            default:
                // Search both name and email simultaneously
                List<Contact> nameResults = contactFileAccess.searchByName(userEmail, query);
                List<Contact> emailResults = contactFileAccess.searchByEmail(userEmail, query);
                
                // Combine results and remove duplicates
                results = nameResults.stream()
                    .collect(Collectors.toList());
                
                emailResults.stream()
                    .filter(contact -> !results.contains(contact))
                    .forEach(results::add);
                break;
        }
        
        return results.stream()
                .map(this::contactToDto)
                .collect(Collectors.toList());
    }

    private Contact dtoToContact(ContactDTO dto) {
        Contact contact = new Contact();
        contact.setId(dto.getId());
        contact.setUserId(dto.getUserId());
        contact.setName(dto.getName());
        contact.setEmails(dto.getEmails());
        return contact;
    }

    private ContactDTO contactToDto(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setUserId(contact.getUserId());
        dto.setName(contact.getName());
        dto.setEmails(contact.getEmails());
        return dto;
    }
}