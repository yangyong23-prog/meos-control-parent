package com.persagy.apccontrol.basic.model.param.rwd;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 对象查询参数
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
public class ObjectQueryParam implements BaseCheckParamProvider {

    /**
     * 运行监测类型
     */
    private @NonNull RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测类型
     */
    private @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondTypeEnum;

    /**
     * 取值逻辑
     */
    @NotNull(message = "{key_faa0b7f1363a4e4189e3d01657752030}")
    private @NonNull InfoCodeTakeValueLogicRwdObjectDetailModel takeValueLogic;

    /**
     * 开始时间
     */
    private @Nullable LocalDateTime startTime;

    /**
     * 结束时间
     */
    private @Nullable LocalDateTime endTime;

    @Override
    public void checkAndSet() {
        takeValueLogic.setQueryInfoCodeConfig(null);
    }

    /**
     * 转换模版类型模型
     *
     * @return 模版类型模型
     * @since 7.250403.0
     */
    public InfoCodeTemplateTypeModel toTemplateTypeModel() {
        InfoCodeTemplateTypeModel templateTypeModel = new InfoCodeTemplateTypeModel();
        templateTypeModel.setRunningMonitorType(ObjectUtils.defaultIfNull(
                this.runningMonitorType, RunningMonitorTypeEnum.COMMON));
        templateTypeModel.setRunningMonitorSecondType(this.runningMonitorSecondTypeEnum);
        templateTypeModel.setTemplateType(InfoCodeTemplateTypeEnum.DEFAULT);
        templateTypeModel.setObjectType(DtpRwdObjectTypeEnum.PROJECT);
        templateTypeModel.setClassCode(DtpRwdClassCodeEnum.PROJECT.value());
        return templateTypeModel;
    }
}
