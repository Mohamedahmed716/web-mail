package com.mailSystem.demo.controller;

import com.mailSystem.demo.service.InboxService;
import com.mailSystem.demo.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inbox")
@CrossOrigin
public class InboxController {

    @Autowired
    private InboxService inboxService;

    @GetMapping
    public ResponseEntity<?> getEmails(
            // 1. رجعنا نعتمد على التوكن بس (زي الكود القديم بتاعك بالظبط)
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DATE") String sort
    ) {

        // 2. التحقق من التوكن (زي ما كان)
        if (token == null || !UserContext.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid or missing token");
        }

        // 3. بنجيب الإيميل من التوكن (عشان منطلبوش من الفرونت)
        String email = UserContext.getUser(token);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found for this token");
        }

        // 4. التغيير الوحيد هنا:
        // السرفيس دلوقتي بترجع (InboxResponse) مش (List)
        // فاستخدمنا var عشان ياخد أي حاجة ترجع من السرفيس ويعديها للفرونت
        var response = inboxService.getInboxEmails(email, page, size, sort);

        return ResponseEntity.ok(response);
    }
}