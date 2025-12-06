package com.mailSystem.demo.service.Filter;

import com.mailSystem.demo.model.Mail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
@Component
public class CompositeFilterCriteria implements EmailFilterCriteria {
    private List<EmailFilterCriteria> criteria = new ArrayList<>();

    public void addCriteria(EmailFilterCriteria criterion) {
        criteria.add(criterion);
    }

    public void removeCriteria(EmailFilterCriteria criterion) {
        criteria.remove(criterion);
    }

    public void clearCriteria() {
        criteria.clear();
    }

    @Override
    public Set<Mail> meet(Set<Mail> mails, String searchTerm) {
        Set<Mail> result = mails;
        for (EmailFilterCriteria criterion : criteria) {
            if (result.isEmpty()) {
                break;
            }
            result = criterion.meet(result, searchTerm);
        }
        return result;
    }
}
