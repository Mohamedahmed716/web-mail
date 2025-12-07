package com.mailSystem.demo.service.sort;


import com.mailSystem.demo.model.Mail;

import java.util.List;


public interface ISortStrategy {
    List<Mail> sort(List<Mail> emails);
}
