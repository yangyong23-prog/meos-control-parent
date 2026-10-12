package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarScheduleTagTypeEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 工作历营业时间查询参数
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
public class MidCalendarWorkTimeQueryParam implements BaseCheckParamProvider {

    /**
     * 工作历ID
     */
    private String workId;

    /**
     * 对象ID
     */
    private String objId;

    /**
     * 开始时间, 闭区间
     */
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate workDateStart;

    /**
     * 结束时间, 闭区间
     */
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate workDateEnd;

    /**
     * 字典类型
     */
    private MidCalendarScheduleTagTypeEnum dictType = MidCalendarScheduleTagTypeEnum.OPENING;

    @Override
    public void checkAndSet() {
        if (StringUtils.isAllBlank(this.workId, this.objId)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_9952c9fb79e347fca1f71bc32b004741"));
        }
        if (!this.workDateEnd.isAfter(this.workDateStart)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_305ce37c62c74d558cc9a1925b43e0dc"));
        }
    }
}
