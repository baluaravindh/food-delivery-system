package com.balu.food_delivery_system.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String fullName) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to FoodExpress! 🍕");

            // Java 17 Text Block for HTML email
            String htmlBody = """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #e74c3c;">
                            Welcome to FoodExpress, %s! 🍕
                        </h2>
                        <p>Your account has been 
                           created successfully.</p>
                        <p>Start exploring restaurants 
                           and order your favourite food!</p>
                        <br/>
                        <p style="color: #888;">
                            Team FoodExpress
                        </p>
                    </body>
                    </html>
                    """.formatted(fullName);

            helper.setText(htmlBody, true);
            mailSender.send(message);

            log.info("Welcome email sent to: {}",
                    toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email"
                            + " to: {}, error: {}",
                    toEmail, e.getMessage());
        }
    }

    public void sendOrderConfirmationEmail(
            String toEmail,
            String fullName,
            Long orderId,
            BigDecimal amount) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Order #" + orderId + " Confirmed! 🎉");

            // Java 17 Text Block
            String htmlBody = """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2 style="color: #27ae60;">
                            Order Confirmed! 🎉
                        </h2>
                        <p>Hi %s,</p>
                        <p>Your order <strong>#%d</strong> 
                           has been confirmed.</p>
                        <p>Total Amount: 
                           <strong>₹%.2f</strong></p>
                        <p>We will notify you when 
                           your food is on the way!</p>
                        <br/>
                        <p style="color: #888;">
                            Team FoodExpress
                        </p>
                    </body>
                    </html>
                    """.formatted(fullName, orderId, amount);

            helper.setText(htmlBody, true);
            mailSender.send(message);

            log.info("Order confirmation email"
                            + " sent to: {} for order: {}",
                    toEmail, orderId);
        } catch (Exception e) {
            log.error("Failed to send order email" + " to: {}, error: {}",
                    toEmail, e.getMessage());
        }
    }
}
