package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.contact.ContactRequestDTO;
import com.ecommerce.E_commerce.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @Value("${app.contact.admin.email}")
    private String adminEmail;

    @PostMapping
    public ResponseEntity<String> sendContactMessage(@Valid @RequestBody ContactRequestDTO request) {
        String subject = "Nowa wiadomość od: " + request.name();
        String content = String.format(
                "Otrzymano nową wiadomość z formularza kontaktowego: \n\n" +
                "Od: %s\n" +
                "Adres e-mail nadawcy: %s\n\n" +
                "Treść:\n %s",
                request.name(), request.email(), request.message()
        );

        emailService.sendSimpleMail(adminEmail, subject, content);

        return ResponseEntity.ok(content);
    }

}
