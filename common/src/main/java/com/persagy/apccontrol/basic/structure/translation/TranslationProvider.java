package com.persagy.apccontrol.basic.structure.translation;

import com.persagy.apccontrol.basic.model.enumtype.LanguageEnum;
import org.jspecify.annotations.NonNull;

/**
 * 翻译提供者
 *
 * @author JiYinchuan
 * @since 7.250901.0
 */
public interface TranslationProvider {

    /**
     * 翻译
     *
     * @param text 待翻译的文本
     * @return 翻译结果
     */
    default String translate(@NonNull String text) {
        return this.translate(text, LanguageEnum.CHINA, LanguageEnum.ENGLISH);
    }

    /**
     * 翻译
     *
     * @param text         待翻译的文本
     * @param fromLanguage 源语言
     * @param toLanguage   目标语言
     * @return 翻译结果
     */
    String translate(@NonNull String text, @NonNull LanguageEnum fromLanguage, @NonNull LanguageEnum toLanguage);

}
