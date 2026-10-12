package com.persagy.apccontrol.basic.model.param.strategy;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 固定策略查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(title = "固定策略查询参数")
public class StrategyFixedQueryParam extends StrategyCommonParam {

    @Schema(description = "开始日期")
    @NotNull(message = "{key_6dc6cc33082c41d189980dd415b06083}")
    private LocalDate beginDate;

    @Schema(description = "结束日期")
    @NotNull(message = "{key_fe10d89294244a90a5a3d488db40d202}")
    private LocalDate endDate;

    /**
     * 创建中央供冷系统参数实例
     *
     * @param projectId              项目ID
     * @param centralCoolingSystemId 中央供冷系统ID
     * @param beginDate              开始日期
     * @param endDate                结束日期
     * @param businessType           业务类型
     * @return 中央供冷系统参数实例
     * @since v5.7.0
     */
    public static StrategyFixedQueryParam centralCoolingInstance(
            @NonNull String projectId, @NonNull String centralCoolingSystemId,
            @NonNull LocalDate beginDate, @NonNull LocalDate endDate,
            @NonNull StrategyBusinessTypeEnum businessType) {
        StrategyFixedQueryParam strategyFixedQueryParam = new StrategyFixedQueryParam();
        strategyFixedQueryParam.setBeginDate(beginDate);
        strategyFixedQueryParam.setEndDate(endDate);
        strategyFixedQueryParam.setProjectId(projectId);
        strategyFixedQueryParam.setSystemType(DtpRwdSystemClassCodeEnum.ACCC);
        strategyFixedQueryParam.setSystemId(centralCoolingSystemId);
        strategyFixedQueryParam.setBusinessType(businessType);
        strategyFixedQueryParam.setStrategyType(StrategyTypeEnum.FIXED_STRATEGY);
        return strategyFixedQueryParam;
    }

}
