package com.mailSystem.demo.service.Filter;

import com.mailSystem.demo.model.Contact;

import java.util.Set;

public interface ContactCriteria {
    Set<Contact> meet(Set<Contact> data, String searched);

}
