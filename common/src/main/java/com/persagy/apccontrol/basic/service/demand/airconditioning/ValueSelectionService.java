package com.persagy.apccontrol.basic.service.demand.airconditioning;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigLogDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.DemandValueSelectTypeQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.ValueSelectQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect.DemandValueSelectTypeConfigQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.airconditioning.DemandValueSelectTypeVO;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectElecQueryParam;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectInsertParam;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectQueryParamByPage;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface ValueSelectionService {

    List<BasicsConfigTypeEnum> TYPE_LIST = Lists.newArrayList(
            BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST,
            BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_ENERGY,
            BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS,
            BasicsConfigTypeEnum.VALUE_SELECT_QUALITY_FIRST);

    /**
     * 查询价值选择列表
     *
     * @param param 查询参数
     * @return 价值选择列表
     * @since v6.0.0
     */
    List<DemandValueSelectTypeVO> listValueSelectType(@NonNull DemandValueSelectTypeQueryParam param);

    /**
     * 根据类型查询配置
     *
     * @param param 查询参数
     * @return 类型配置
     * @since 7.250102.0
     */
    List<ValueSelectConfigDto> listConfigsByType(@NonNull @Valid DemandValueSelectTypeConfigQueryParam param);
    
    
    /**
     * 根据当前电价或者碳因子，获取对应的温度、CO2满足率
     *
     * @param param
     * @return
     */
    JSONObject currentTempAndCarbonRate(@NotNull ValueSelectQueryParam param);

    /**
     * 查询当前应用的价值选择配置
     *
     * @param param 查询参数
     * @return 价值选择配置
     */
    RcBasicsConfig currentApplyConfig(@NonNull ValueSelectQueryParam param);

    /**
     * 保存配置
     *
     * @param param
     * @return
     */
    boolean saveConfig(@NotNull ValueSelectInsertParam param);

    /**
     * 查询价值选择配置日志
     *
     * @param param
     * @return
     */
    List<ValueSelectConfigLogDto> listLog(@NotNull ValueSelectQueryParamByPage param);

    /**
     * 电价数据查询
     *
     * @param param
     * @return
     */
    ValueSelectionTrendTableDto listElectricityData(@NotNull ValueSelectElecQueryParam param);


    /**
     * 查询碳因子数据
     *
     * @param param
     * @return
     */
    ValueSelectionTrendTableDto listCarbonData(@NotNull ValueSelectElecQueryParam param);

}
