package vn.doan.lms.service.implements_class;

import java.nio.charset.StandardCharsets;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.user_dto.EmailAccountDTO;
import vn.doan.lms.domain.dto.user_dto.EmailAccounttUpdateDTO;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SEND EMAIL: " + e);
        }
    }

    public void sendEmailFromTemplateSync(EmailAccountDTO emailAccountDTO) {
        Context context = new Context();
        context.setVariable("fullName", emailAccountDTO.getFullName());
        context.setVariable("username", emailAccountDTO.getUserCode());
        context.setVariable("password", emailAccountDTO.getPassword());
        context.setVariable("role", emailAccountDTO.getRole());
        String to = emailAccountDTO.getEmail();
        String subject = "Thông tin tài khoản LMS của bạn";
        String templateName = "mailAccount";
        String htmlContent = templateEngine.process(templateName, context);

        this.sendEmailSync(to, subject, htmlContent, false, true);
    }

    public void sendEmailFromTemplateUpdateSync(EmailAccounttUpdateDTO emailAccounttUpdateDTO) {
        Context context = new Context();
        context.setVariable("fullName", emailAccounttUpdateDTO.getFullName());
        context.setVariable("role", emailAccounttUpdateDTO.getRole());
        String to = emailAccounttUpdateDTO.getEmail();
        String subject = "Thông tin tài khoản LMS của bạn - Cập nhật";
        String templateName = "mailAccountUpdate";
        String htmlContent = templateEngine.process(templateName, context);

        this.sendEmailSync(to, subject, htmlContent, false, true);
    }

}
