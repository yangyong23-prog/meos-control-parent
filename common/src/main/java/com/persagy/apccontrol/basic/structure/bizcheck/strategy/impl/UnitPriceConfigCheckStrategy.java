package com.persagy.apccontrol.basic.structure.bizcheck.strategy.impl;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectElecQueryParam;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.ValueSelectionService;
import com.persagy.apccontrol.basic.structure.bizcheck.context.BizCheckContext;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.UnitPriceConfigCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.strategy.BizCheckStrategy;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 单价配置检查策略实现
 * 检查当日运行的空调模板对应的价值选择为"费用最低"时，
 * 验证当日电价数据是否包含96个15分钟分精度数据
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UnitPriceConfigCheckStrategy implements BizCheckStrategy<UnitPriceConfigCheckItemDTO> {

    private final DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;
    private final ValueSelectionService valueSelectionService;

    @Override
    public BizCheckStrategyTypeEnum getStrategyType() {
        return BizCheckStrategyTypeEnum.UNIT_PRICE_CONFIG;
    }

    @Override
    public BizCheckItemDTO<UnitPriceConfigCheckItemDTO> execute(BizCheckContext context) throws ExecutionException {
        UnitPriceConfigCheckItemDTO checkResult = new UnitPriceConfigCheckItemDTO();
        BizCheckItemDTO<UnitPriceConfigCheckItemDTO> checkItemDTO = new BizCheckItemDTO<>();

        try {
            // 设置检查基本信息
            LocalDate checkDate = context.getCheckDate();
            checkResult.setExpectedDataCount(96); // 24小时 * 4个15分钟间隔 = 96个数据点

            // 1. 获取当日空调模板的价值选择类型
            String projectId = ContextUtil.getProjectId();
            BasicsConfigTypeEnum valueSelectType = demandConfigAirconditioningCustomService.getValueSelectType(projectId, checkDate);

            if (valueSelectType == null) {
                // 未配置价值选择，跳过检查
                checkResult.setNeedCheck(false);
                //checkResult.setValueSelectType("UNKNOWN");
                //checkResult.setValueSelectTypeName("未配置");
                //checkResult.setResultDescription("当日未配置空调模板或价值选择方案");
                checkResult.setStatus(BizCheckStatusEnum.EXCEPTION);
                checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
            } else {
                checkResult.setValueSelectType(valueSelectType.getCode());
                checkResult.setValueSelectTypeName(valueSelectType.getExplain());

                // 2. 判断是否需要执行检查（只有费用最低才需要检查）
                if (BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST.equals(valueSelectType)) {
                    checkResult.setNeedCheck(true);
                    
                    // 3. 查询当日电价数据
                    List<ValueSelectionTrendTableDto.ElecData> priceDataList = queryElectricityPriceData(checkDate);
                    
                    // 4. 验证数据完整性
                    validateElectricityData(checkResult, priceDataList, checkItemDTO);
                } else {
                    // 其他价值选择类型，跳过检查
                    checkResult.setNeedCheck(false);
                    //checkResult.setResultDescription("当前价值选择为：" + valueSelectType.getExplain() + "，无需检查电价数据");
                    checkResult.setStatus(BizCheckStatusEnum.NORMAL);
                    checkItemDTO.setStatus(BizCheckStatusEnum.COMPLETED);
                }
            }

            checkItemDTO.setCheckResult(checkResult);
            
        } catch (Exception e) {
            log.error("执行单价配置检查时发生异常", e);
            checkResult.setStatus(BizCheckStatusEnum.FAILED);
            //checkResult.setResultDescription("检查执行过程中发生异常：" + e.getMessage());
            checkItemDTO.setStatus(BizCheckStatusEnum.FAILED);
            checkItemDTO.setCheckResult(checkResult);
        }

        return checkItemDTO;
    }

    /**
     * 查询当日电价数据
     * 
     * @param checkDate 检查日期
     * @return 电价数据列表
     */
    private List<ValueSelectionTrendTableDto.ElecData> queryElectricityPriceData(LocalDate checkDate) {
        try {
            // 构造查询参数
            ValueSelectElecQueryParam param = new ValueSelectElecQueryParam();
            param.setProjectId(ContextUtil.getProjectId());
            param.setEnableSample(false);
            param.setStartTime(checkDate.atStartOfDay());
            param.setEndTime(checkDate.plusDays(1).atStartOfDay());
            
            // 使用价值选择服务查询电价数据
            ValueSelectionTrendTableDto priceData = valueSelectionService.listElectricityData(param);
            
            return priceData != null ? priceData.getElecDataList() : null;
            
        } catch (Exception e) {
            log.error("查询电价数据时发生异常，日期：{}", checkDate, e);
            throw new RuntimeException("查询电价数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证电价数据完整性
     * 
     * @param checkResult 检查结果对象
     * @param priceDataList 电价数据列表
     */
    private void validateElectricityData(UnitPriceConfigCheckItemDTO checkResult, List<ValueSelectionTrendTableDto.ElecData> priceDataList,BizCheckItemDTO<UnitPriceConfigCheckItemDTO> checkItemDTO) {
        int actualCount = CollectionUtils.isNotEmpty(priceDataList) ? priceDataList.size() : 0;
        checkResult.setActualDataCount(actualCount);

        // 验证数据完整性
        if (actualCount >= checkResult.getExpectedDataCount()) {
            checkResult.setStatus(BizCheckStatusEnum.NORMAL);
            checkItemDTO.setStatus(BizCheckStatusEnum.COMPLETED);
            //checkResult.setResultDescription(String.format(
            //    "电价数据检查通过：期望%d个数据点，实际获得%d个数据点",
            //    96, actualCount));
        } else {
            checkResult.setStatus(BizCheckStatusEnum.EXCEPTION);
            checkItemDTO.setStatus(BizCheckStatusEnum.REJECTED);
            //checkResult.setResultDescription(String.format(
            //    "电价数据不完整：期望%d个数据点，实际仅获得%d个数据点，缺少%d个数据点",
            //    96, actualCount, 96 - actualCount));
        }
    }

    @Override
    public String logTag() {
        return "单价配置检查";
    }
}