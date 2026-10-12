package com.persagy.apccontrol.basic.service;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDTO;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFixedQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 策略干预查询Service
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface BizStrategyQueryService {

    // ------------------------- 统计相关 -------------------------

    // ------------------------- 查询相关 -------------------------

    /**
     * 查询末级冷冻泵组
     *
     * @param param 查询参数
     * @return 末级冷冻泵组
     * @since v5.8.0
     */
    List<DtpRwdFreezePumpDTO> listLastFreezePumpGroups(@NonNull @Valid StrategyFreezeGroupObjectQueryParam param);

    /**
     * 查询固定策略DTO列表
     *
     * @param param 查询参数
     * @return 固定策略DTO列表
     * @since v5.6.0
     */
    List<StrategyDTO> listFixedStrategy(@NonNull @Valid StrategyFixedQueryParam param);

}
