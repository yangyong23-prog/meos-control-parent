package com.persagy.apccontrol.basic.utils.i18n;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.framework.common.i18n.I18nProperties;
import com.persagy.framework.common.i18n.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class LocaleMessageUtil {

//    public static String getMessage(String code, Object...args) {
//        if(Objects.isNull(LocaleContext.getLocaleContext())){
//            LocaleContext.setLocaleContext(Locale.CHINESE);
//        }
//        if(!StringUtils.hasText(code)){
//            return code;
//        }
//        MessageSource messageSource = Ctx.getBean(MessageSource.class);
//        try {
//            return messageSource.getMessage(code, args, LocaleContext.getLocaleContext());
//        }catch (Exception e){
//            //找不到就原路返回
//            return code;
//        }
//    }


    public static String getMessage(String code, Object...args) {
        if(Objects.isNull(LocaleContext.getLocaleContext())){
            // 从paas的环境配置中取
            String i18n = SpringUtil.getBean(I18nProperties.class).getI18n();
            final Locale locale = Locale.forLanguageTag(i18n);
            LocaleContext.setLocaleContext(locale);
        }
        if(!StringUtils.hasText(code)){
            return code;
        }
        try {
            final Properties properties = I18nUtil.loadI18nProp(LocaleContext.getLocaleContext().getLanguage());
            if (Objects.isNull(properties)) {
                return code;
            }
            if (!properties.containsKey(code)) {
                return code;
            }
            return MessageFormat.format(properties.getProperty(code), args);
        }catch (Exception e){
            //找不到就原路返回
            return code;
        }
    }

}
