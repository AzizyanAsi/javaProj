package net.idonow.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleUtils {

    private MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getLocalizedMessage(String messageKey) {
        return messageSource.getMessage(messageKey, null, getLocale());
    }

    public String getLocalizedMessage(String messageKey, Object[] args) {
        return messageSource.getMessage(messageKey, args, getLocale());
    }


    /* -- PRIVATE METHODS -- */

    private Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
