package com.ecommerce.E_commerce.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        logger.info("Attempting to send email from: {} to: {}, subject: {}", fromAddress, to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            logger.info("Email sent successfully from: {} to: {}", fromAddress, to);
        } catch (Exception e) {
            logger.error("Failed to send email from: {} to: {}, subject: {}", fromAddress, to, subject, e);
            throw e;
        }
    }
}
