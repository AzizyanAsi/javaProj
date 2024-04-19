package net.idonow.service.common.impl;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.rest.verify.v2.service.VerificationCreator;
import lombok.extern.slf4j.Slf4j;
import net.idonow.common.config.LimitsConfig;
import net.idonow.common.config.TwilioConfig;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.common.InvalidRequestDataException;
import net.idonow.service.common.PhoneNumberVerificationService;
import net.idonow.service.entity.VerificationTokenService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.idonow.common.cache.TemplateCacheNames.PHONE_NUMBER_VERIFICATION_INFO_CACHE_PREFIX;

@Slf4j
@Service
public class PhoneNumberVerificationServiceImpl implements PhoneNumberVerificationService {

    private final TwilioConfig twilioConfig;
    private final RedisTemplate<String, LocalDateTime> redisTemplate;
    private final VerificationTokenService verificationTokenService;
    private final LimitsConfig limitsConfig;
    private final LocaleUtils localeUtils;

    public PhoneNumberVerificationServiceImpl(TwilioConfig twilioConfig,
                                              RedisTemplate<String, LocalDateTime> redisTemplate,
                                              VerificationTokenService verificationTokenService,
                                              LimitsConfig limitsConfig,
                                              LocaleUtils localeUtils) {
        this.twilioConfig = twilioConfig;
        this.verificationTokenService = verificationTokenService;
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
        this.redisTemplate = redisTemplate;
        this.limitsConfig = limitsConfig;
        this.localeUtils = localeUtils;
    }

    @Override
    public boolean startVerification(String phoneNumber) {
        try {
            if (resendAllowed(phoneNumber)) {
                VerificationCreator verificationCreator = Verification.creator(twilioConfig.getVerificationServiceSid(), phoneNumber, Verification.Channel.SMS.toString());
                Verification verification = verificationCreator.create();
                storeVerificationInfo(phoneNumber);
                log.info("Verification code sent to phone number: '{}', status:'{}'", phoneNumber, verification.getStatus());
                return true;
            } else {
                throw new InvalidRequestDataException(
                        "The resend time has not been exceeded",
                        Map.of("phoneNumber", localeUtils.getLocalizedMessage("error.token-already-sent", new Integer[]{limitsConfig.getResendTokenAfterSeconds()})),
                        HttpStatus.CONFLICT);
            }
        } catch (ApiException apiException) {
            log.warn("Error(code={}) while trying to start verification for phone number '{}'", apiException.getCode(), phoneNumber);
            return false;
        }
    }

    @Override
    public boolean confirmVerification(String phoneNumber, String token) {
        try {
            VerificationCheck verificationCheck = VerificationCheck.creator(twilioConfig.getVerificationServiceSid())
                    .setTo(phoneNumber)
                    .setCode(token)
                    .create();
            if (verificationCheck.getStatus().compareTo(Verification.Status.APPROVED.toString()) == 0) {
                redisTemplate.opsForValue().getAndDelete(getKey(phoneNumber));
                log.info("Phone number '{}' successfully verified", phoneNumber);
                return true;
            }
            return false;
        } catch (ApiException apiException) {
            log.warn("Error(code={}) while trying to verify phone number '{}'", apiException.getCode(), phoneNumber);
            throw new ActionNotAllowedException("Verification check error");
        }
    }

    /*    PRIVATE METHODS     */

    private void storeVerificationInfo(String phoneNumber) {
        String key = getKey(phoneNumber);
        redisTemplate.opsForValue().set(key, LocalDateTime.now(), 10L, TimeUnit.MINUTES);
    }

    private boolean resendAllowed(String phoneNumber) {
        String key = getKey(phoneNumber);
        LocalDateTime lastSent = this.redisTemplate.opsForValue().get(key);
        if (lastSent != null) {
            return verificationTokenService.resendAllowed(lastSent);
        }
        return true;
    }

    private String getKey(String phoneNumber) {
        return PHONE_NUMBER_VERIFICATION_INFO_CACHE_PREFIX + phoneNumber;
    }
}
