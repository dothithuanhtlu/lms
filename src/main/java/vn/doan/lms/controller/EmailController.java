package vn.doan.lms.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import vn.doan.lms.domain.dto.user_dto.EmailAccountDTO;
import vn.doan.lms.domain.dto.user_dto.EmailAccounttUpdateDTO;
import vn.doan.lms.service.implements_class.EmailService;

@RestController
@AllArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send-email")
    public String sendEmail(@RequestBody EmailAccountDTO emailAccountDTO) {
        this.emailService.sendEmailFromTemplateSync(emailAccountDTO);
        return "ok";
    }

    @PostMapping("/send-email-update")
    public String sendEmailUpdate(@RequestBody EmailAccounttUpdateDTO emailAccounttUpdateDTO) {
        this.emailService.sendEmailFromTemplateUpdateSync(emailAccounttUpdateDTO);
        return "ok";
    }
}
