package com.persagy.apccontrol.basic.utils.i18n;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Locale;
import java.util.function.Supplier;

public class LocaleContext {

    /**
     *  语言上下文
     */
    public static final ThreadLocal<Locale> LOCALE_CONTEXT = new TransmittableThreadLocal<>();

    public static final ThreadLocal<String> ACCEPT_LANGUAGE_CONTEXT = new TransmittableThreadLocal<>();

    public static void setLocaleContext(Locale locale) {
        LOCALE_CONTEXT.set(locale);
    }

    public static Locale getLocaleContext() {
        return LOCALE_CONTEXT.get();
    }

    public static void setAcceptLanguageContext(String language) {
        ACCEPT_LANGUAGE_CONTEXT.set(language);
    }

    public static String getAcceptLanguageContext() {
        return ACCEPT_LANGUAGE_CONTEXT.get();
    }

    public static void remove(){
        LOCALE_CONTEXT.remove();
        ACCEPT_LANGUAGE_CONTEXT.remove();
    }

    public static <T> T withLocale(Locale locale, Supplier<T> supplier) {
        Locale old = LOCALE_CONTEXT.get();
        LOCALE_CONTEXT.set(locale);
        try {
            return supplier.get();
        } finally {
            LOCALE_CONTEXT.set(old);
        }
    }

    public static <T> T withLocaleAndAccept(Locale locale,String acceptLanguage,Supplier<T> supplier){
        Locale old = LOCALE_CONTEXT.get();
        String oldAcceptLanguage = ACCEPT_LANGUAGE_CONTEXT.get();
        LOCALE_CONTEXT.set(locale);
        ACCEPT_LANGUAGE_CONTEXT.set(acceptLanguage);
        try {
            return supplier.get();
        } finally {
            LOCALE_CONTEXT.set(old);
            ACCEPT_LANGUAGE_CONTEXT.set(oldAcceptLanguage);
        }
    }
}
