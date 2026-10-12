package com.persagy.apccontrol.basic.model.param.demand;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;


/**
 * 需求管理-对象树查询参数
 *
 * @author yuyao
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-对象树查询参数")
public class DemandTreeQueryParam {

    @Schema(description = "需求模版标签, 有就必传,和需求模版类型其中必传一个")
    private String templateTag;

    @Schema(description = "需求模版类型, 有就必传,和需求模版标签其中必传一个")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "需求日期, 传入历史日期时按该天查询实例标签, 默认查询当前")
    private LocalDate date;

    @Schema(description = "场景时段ID,传递情况下 必传 需求模版标签")
    private Long sceneId;

    @Schema(description = "是否返回完整树 默认true")
    private Boolean allMsg = true;

    /**
     * 基础参数校验
     */
    public void check() {
        //场景id存在情况下 必须传递模版id
        if (sceneId == null) {
            ParameterCheck.isTrue(templateTag == null, LocaleMessageUtil.getMessage("key_2b1405aebbaa416d87ebcb22ae9ffc9c"));
        }
        ParameterCheck.isTrue(StringUtils.isEmpty(templateTag) && templateType == null, LocaleMessageUtil.getMessage("key_3910dc8276294b1589936126f8e9b218"));
    }

}
