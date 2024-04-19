package net.idonow.service.common;

import net.idonow.controller.exception.common.InvalidRequestDataException;

public interface PhoneNumberVerificationService {
    boolean startVerification(String phoneNumber) throws InvalidRequestDataException;

    boolean confirmVerification(String phoneNumber, String token) throws InvalidRequestDataException;
}
