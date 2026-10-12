package com.persagy.apccontrol.basic.model.param.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandTemplateRepository;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 需求管理基查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理基查询参数")
public class BaseDemandTemplateParam implements BaseCheckParamProvider {

    @Schema(description = "需求模版类型, 未传模版ID时必填")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "需求模版ID, 可任选其一参数, templateId > templateTag > date")
    private Long templateId;

    @Schema(description = "需求模版标签, 可任选其一参数, templateId > templateTag > date")
    private String templateTag;

    @Schema(description = "需求日期, 可任选其一参数, templateId > templateTag > date")
    private LocalDate date;

    /**
     * 查询并获取需求模版
     *
     * @return 需求模版
     * @since 5.241102.0
     */
    @Nullable
    public DemandTemplate queryAndGetTemplate() {
        checkAndSet();
        DemandTemplateRepository demandTemplateRepository = SpringUtil.getBean(DemandTemplateRepository.class);
        DemandTemplate template = null;
        if (Objects.nonNull(templateId)) {
            template = demandTemplateRepository.getByTemplateIdAndDate(false, templateId, date);
        } else if (Objects.nonNull(templateTag)) {
            template = demandTemplateRepository.getByTemplateByTemplateTypeTag(false, templateType, templateTag);
        } else if (Objects.nonNull(date)) {
            DemandCalendarQueryService demandCalendarQueryService = SpringUtil.getBean(DemandCalendarQueryService.class);
            DemandTemplateDateSettingVO dateSetting = demandCalendarQueryService.getDateSetting(templateType, date);
            if (Objects.nonNull(dateSetting)) {
                templateId = dateSetting.getRelTemplate().getTemplateId();
                template = demandTemplateRepository.getByTemplateIdAndDate(false, templateId, date);
            }
        }
        if (Objects.nonNull(template)) {
            templateType = template.getDemandTemplateType();
            templateId = template.getId();
            templateTag = template.getDemandTemplateTag();
        }
        return template;
    }

    @Override
    public void checkAndSet() {
        if (ObjectUtils.allNull(this.templateId, this.templateType)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_6f3542b7e45b4dd6979a07e9c1f9a592"));
        }
        if (ObjectUtils.allNull(this.templateId, this.templateTag, this.date)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_6f3d1809672141df9f77fd74b1efef0c"));
        }
    }
}
