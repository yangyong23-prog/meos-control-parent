package com.persagy.apccontrol.basic.utils.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InternationalMvcConfig implements WebMvcConfigurer{

    private final MessageSource messageSource;

    public InternationalMvcConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @Bean
    public LocaleContextFilter languageContextFilter() {
        return new LocaleContextFilter();
    }

    //    @Bean
//    public LocaleResolver localeResolver() {
//        return new PersagyLocaleResolver();
//    }
//
    @Primary
    @Bean
    public LocalValidatorFactoryBean validatorBean(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }

    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
