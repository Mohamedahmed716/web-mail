package com.mailSystem.demo.service.Filter;
import com.mailSystem.demo.model.Contact;
import org.springframework.stereotype.Component;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import java.util.Set;
@Component
public class NameCriteria implements ContactCriteria {
    public Set<Contact> meet(Set<Contact> data, String searched) {
        return data.stream().filter(contact -> contact.getName().contains(searched)).collect(toSet());
    }
}
