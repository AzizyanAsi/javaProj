package net.idonow.service.common;

import jakarta.mail.MessagingException;
import net.idonow.entity.User;
import net.idonow.entity.system.SystemUser;

public interface MailSenderService {
    void sendPasswordResetMessage(User user, String token) throws MessagingException;
    void sendPasswordResetMessageToSystemUser(SystemUser user, String token) throws MessagingException;

    void sendEmailVerificationMessage(User user, String token) throws MessagingException;

    void sendEmailUpdateNotification(String firstName, String email) throws MessagingException;
}

