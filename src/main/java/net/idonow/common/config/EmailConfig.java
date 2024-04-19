package net.idonow.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.emails")
public class EmailConfig {

    private String supportEmail;
    private String serviceEmail;
    private MessageConfig messageConfig;

    @Autowired
    public void setMessageConfig(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    @Bean
    public SpringTemplateEngine thymeleafTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        ITemplateResolver templateResolver = defaultTemplateResolver();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setTemplateEngineMessageSource(messageConfig.messageSource());
        return templateEngine;
    }

    @Bean
    public ITemplateResolver defaultTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("mail-templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }
}
