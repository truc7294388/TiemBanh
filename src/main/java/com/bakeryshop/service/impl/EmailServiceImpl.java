package com.bakeryshop.service.impl;

import com.bakeryshop.entity.Order;
import com.bakeryshop.entity.User;
import com.bakeryshop.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendVerificationEmail(User user) {
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("verificationUrl", 
            appUrl + "/api/auth/verify?token=" + user.getVerificationToken());

        String emailContent = templateEngine.process("email/verification", context);
        sendEmail(user.getEmail(), "Verify your email", emailContent);
    }

    @Override
    public void sendPasswordResetEmail(User user) {
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("resetUrl", 
            appUrl + "/reset-password?token=" + user.getResetPasswordToken());

        String emailContent = templateEngine.process("email/reset-password", context);
        sendEmail(user.getEmail(), "Reset your password", emailContent);
    }

    @Override
    public void sendOrderConfirmationEmail(Order order) {
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("user", order.getUser());

        String emailContent = templateEngine.process("email/order-confirmation", context);
        sendEmail(order.getUser().getEmail(), "Order Confirmation #" + order.getId(), emailContent);
    }

    @Override
    public void sendOrderCancellationEmail(Order order) {
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("user", order.getUser());

        String emailContent = templateEngine.process("email/order-cancellation", context);
        sendEmail(order.getUser().getEmail(), "Đơn hàng #" + order.getId() + " đã bị hủy", emailContent);
    }

    @Override
    public void sendOrderStatusUpdateEmail(Order order, String oldStatus, String newStatus) {
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("user", order.getUser());
        context.setVariable("oldStatus", oldStatus);
        context.setVariable("newStatus", newStatus);

        String emailContent = templateEngine.process("email/order-status-update", context);
        sendEmail(order.getUser().getEmail(), 
            "Cập nhật trạng thái đơn hàng #" + order.getId(), emailContent);
    }

    @Override
    public void sendPaymentStatusUpdateEmail(Order order, String oldStatus, String newStatus) {
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("user", order.getUser());
        context.setVariable("oldStatus", oldStatus);
        context.setVariable("newStatus", newStatus);

        String emailContent = templateEngine.process("email/payment-status-update", context);
        sendEmail(order.getUser().getEmail(), 
            "Cập nhật trạng thái thanh toán đơn hàng #" + order.getId(), emailContent);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
} 