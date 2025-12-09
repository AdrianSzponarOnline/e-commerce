package com.ecommerce.E_commerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("$spring.mail.username")
    private String fromAddress;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            System.out.println("Email Sent Successfully from " + fromAddress + " to " + to);
        } catch (Exception e) {
            System.err.println("Email Sent Failed from " + fromAddress + " to " + to);
        }
    }
}
