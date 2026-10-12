package com.persagy.apccontrol.basic.model.enumtype;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wxf
 * @description 冷机分组
 * @date 11:12 2025/5/14
**/
@Getter
@AllArgsConstructor
public enum SysGroupEnum implements BaseEnum<String>, IEnum<String> {

    BASE("BASE", "key_df1c6864f48c48ba9153f7a7c20a1cde"),
    BASE_DUAL("BASE_DUAL", "key_3c148b71552d4c78b868e26e33cca6a7"),
    ACCCWH("ACCCWH", "key_ee686c611edc4689962aca0fa6c311d6"),
    ACCCWH_DUAL("ACCCWH_DUAL", "key_4ad53b4f02504bbcae0c19c4a8961b2a"),
    ACCCGH("ACCCGH", "key_f028c7f72fe045528ec4927807eb5c41"),
    ACCCGH_DUAL("ACCCGH_DUAL", "key_4e98919ce76e4bdb9bdd131286efb299"),
    ACAHC("ACAHC", "key_c1a021cbdd9349dcaab876979c5607b5"),
    ACCCAC("ACCCAC", "key_8a91347e56504548aa1f16a6645d0fe7"),
    ACCCCC("ACCCCC", "key_01f0714e723f4d049c6e5d914fcc3ab8"),
    ACCCSC("ACCCSC", "key_672682f898d7461a98ff3ef7ffffa290"),
    ACSCG("ACSCG", "key_672682f898d7461a98ff3ef7ffffa291"),
    ;

    private String code;

    private String desc;

    @Override
    public String getValue() {
        return code;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return code;
    }
    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SysGroupEnum analyze(String value) {
        try {
            return BaseEnum.analyze(value, SysGroupEnum.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
