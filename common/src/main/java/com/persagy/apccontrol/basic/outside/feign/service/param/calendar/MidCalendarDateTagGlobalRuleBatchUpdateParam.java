package com.persagy.apccontrol.basic.outside.feign.service.param.calendar;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Objects;

/**
 * 工作历日期标签全局规则批量更新参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidCalendarDateTagGlobalRuleBatchUpdateParam implements BaseCheckParamProvider {

    /**
     * 工作历ID
     */
    @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private String calendarId;

    /**
     * 是否异步实例化每天规则, 默认同步
     */
    private Boolean asyncInit;

    /**
     * 工休日标签
     */
    private MidCalendarGlobalDateWorkDateTagRuleUpdateParam workDayTagRule;

    /**
     * 空调工况标签
     */
    private Collection<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> acSeasonTagRules;
    /**
     * 空调季标签
     */
    private Collection<MidCalendarGlobalDateAirSeasonTagRuleUpdateParam> airSeasonTagRules;
    /**
     * 店庆日标签
     */
    private MidCalendarGlobalAnniversaryRuleUpdateParam anniversaryRule;

    /**
     * 节假日
     */
    private MidCalendarGlobalHolidayRuleUpdateParam holidayRule;


    @Override
    public void checkAndSet() {
        if (isInvalidParam()) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_776066e0dcab47e1be8c0f3d443741f9"));
        }
        Validator validator = SpringUtil.getBean(Validator.class);
        if (Objects.nonNull(workDayTagRule)) {
            workDayTagRule.setCalendarId(calendarId);
            validator.validate(workDayTagRule);
        }
        if (CollectionUtils.isNotEmpty(acSeasonTagRules)) {
            for (MidCalendarGlobalDateAcSeasonTagRuleUpdateParam acSeasonTagRule : acSeasonTagRules) {
                acSeasonTagRule.setCalendarId(calendarId);
                validator.validate(acSeasonTagRule);
            }
        }
        if (CollectionUtils.isNotEmpty(airSeasonTagRules)) {
            for (MidCalendarGlobalDateAirSeasonTagRuleUpdateParam acSeasonTagRule : airSeasonTagRules) {
                acSeasonTagRule.setCalendarId(calendarId);
                validator.validate(acSeasonTagRule);
            }
        }
        if (Objects.nonNull(anniversaryRule)) {
            anniversaryRule.setCalendarId(calendarId);
            validator.validate(anniversaryRule);
        }
    }

    /**
     * 是否为无效参数
     *
     * @return 是否为无效参数
     * @since v6.0.0
     */
    private boolean isInvalidParam() {
        return ObjectUtils.isEmpty(workDayTagRule)
                && CollectionUtils.isEmpty(acSeasonTagRules)
                && ObjectUtils.isEmpty(anniversaryRule);
    }
}
