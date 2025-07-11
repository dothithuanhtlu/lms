package vn.doan.lms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.user_dto.EmailAccountDTO;
import vn.doan.lms.service.implements_class.EmailService;

@RestController
@AllArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @GetMapping("/send-email")
    public String sendEmail(@RequestBody EmailAccountDTO emailAccountDTO) {
        this.emailService.sendEmailFromTemplateSync(emailAccountDTO);
        return "ok";
    }
}
