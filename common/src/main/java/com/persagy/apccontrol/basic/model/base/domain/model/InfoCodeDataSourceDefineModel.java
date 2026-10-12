package com.persagy.apccontrol.basic.model.base.domain.model;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;


import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 信息点数据源定义模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeDataSourceDefineModel implements ValueObject {

    /**
     * 空实例
     */
    public static final InfoCodeDataSourceDefineModel EMPTY =
            new InfoCodeDataSourceDefineModel("", LocaleMessageUtil.getMessage("key_a09d40c0ec674d9583b1c6b8c7f22ee1"));

    /**
     * 信息点值编码
     */
    @NonNull
    private String code;

    /**
     * 信息点值名称
     */
    @NonNull
    private String name;

    /**
     * 信息点值别名
     */
    private String aliasName;

    /**
     * 父级编码
     */
    private String parentCode;

    // ------------------------- 拓展字段 -------------------------

    /**
     * 空间功能类型下关联空间
     */
    private List<DtpRwdSpaceDTO> spaces;

    public InfoCodeDataSourceDefineModel() {

    }

    public InfoCodeDataSourceDefineModel(@NonNull String code, @NonNull String name) {
        this.code = code;
        this.name = name;
    }
}
