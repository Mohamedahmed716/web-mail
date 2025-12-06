package com.mailSystem.demo.service.Filter;

import com.mailSystem.demo.model.Mail;

import java.util.Set;

public interface EmailFilterCriteria {
    Set<Mail> meet(Set<Mail> mails, String criteria);
}
