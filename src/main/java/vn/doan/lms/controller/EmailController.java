package vn.doan.lms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import vn.doan.lms.service.implements_class.EmailService;

@RestController
@AllArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @GetMapping("/send-email")
    public String sendEmail() {
        // this.emailService.sendEmailSync("thuanhtlu0411@gmail.com", "test mail",
        // "<h1>Hello</h1>", false, true);
        this.emailService.sendEmailFromTemplateSync("thuanhtlu0411@gmail.com", "test mail", "mail");
        return "ok";
    }
}
