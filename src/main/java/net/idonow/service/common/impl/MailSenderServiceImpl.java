package net.idonow.service.common.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.idonow.common.config.AppConfig;
import net.idonow.common.config.EmailConfig;
import net.idonow.entity.User;
import net.idonow.entity.system.SystemUser;
import net.idonow.service.common.MailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MailSenderServiceImpl implements MailSenderService {

    private JavaMailSender mailSender;
    private SpringTemplateEngine templateEngine;
    private AppConfig appConfig;
    private EmailConfig emailConfig;

    @Autowired
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Autowired
    public void setTemplateEngine(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Autowired
    public void setEmailConfig(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    @Override
    public void sendPasswordResetMessage(User user, String token) throws MessagingException {
        Map<String, Object> templateModel = getDefaultTemplateModel();
        templateModel.put("token", token);
        templateModel.put("user", user);
        String subject = "Password reset";
        sendMessageUsingThymeleafTemplate(user.getEmail(), subject, "password-reset.html", templateModel);
    }

    @Override
    public void sendPasswordResetMessageToSystemUser(SystemUser user, String token) throws MessagingException {
        Map<String, Object> templateModel = getDefaultTemplateModel();
        templateModel.put("token", token);
        templateModel.put("user", user);
        String subject = "Password reset";
        sendMessageUsingThymeleafTemplate(user.getEmail(), subject, "password-reset.html", templateModel);
    }
    @Override
    public void sendEmailVerificationMessage(User user, String token) throws MessagingException {
        Map<String, Object> templateModel = getDefaultTemplateModel();
        templateModel.put("token", token);
        templateModel.put("user", user);
        String subject = "Email verification";
        sendMessageUsingThymeleafTemplate(user.getEmail(), subject, "email-verification.html", templateModel);
    }

    @Override
    public void sendEmailUpdateNotification(String firstName, String email) throws MessagingException {
        Map<String, Object> templateModel = getDefaultTemplateModel();
        templateModel.put("firstName", firstName);
        String subject = "Email update notification";
        sendMessageUsingThymeleafTemplate(email, subject, "email-update-notification.html", templateModel);

    }


    /* -- PRIVATE METHODS -- */

    private void sendMessageUsingThymeleafTemplate(String to, String subject, String templateFile,
                                                   Map<String, Object> templateModel) throws MessagingException {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = templateEngine.process(templateFile, thymeleafContext);

        sendHtmlMessage(to, subject, htmlBody);
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(emailConfig.getServiceEmail());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private Map<String, Object> getDefaultTemplateModel() {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("lang", appConfig.getAppDefaultLanguage());
        templateModel.put("supportEmailAddress", emailConfig.getSupportEmail());
        return templateModel;
    }

}
