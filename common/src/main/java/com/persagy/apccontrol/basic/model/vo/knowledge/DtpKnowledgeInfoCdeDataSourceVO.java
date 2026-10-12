package com.persagy.apccontrol.basic.model.vo.knowledge;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 数据字典-信息点数据源定义VO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DtpKnowledgeInfoCdeDataSourceVO {

    /**
     * 空实例
     */
    public static final DtpKnowledgeInfoCdeDataSourceVO EMPTY =
            new DtpKnowledgeInfoCdeDataSourceVO().setCode("").setName(LocaleMessageUtil.getMessage("key_a09d40c0ec674d9583b1c6b8c7f22ee1"));

    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "父级编码")
    private String parentCode;

}
