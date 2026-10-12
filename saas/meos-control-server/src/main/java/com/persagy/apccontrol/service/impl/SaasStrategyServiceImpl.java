package com.persagy.apccontrol.service.impl;


import com.persagy.apccontrol.basic.model.dto.strategy.ChillerUnitPlanStrategyDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyDayDTO;
import com.persagy.apccontrol.basic.model.dto.strategy.StrategyPlanDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.basic.structure.convert.BizStrategyConvert;
import com.persagy.apccontrol.controller.strategy.convert.SaasStrategyConvert;
import com.persagy.apccontrol.controller.strategy.param.ChillerFixedStrategyQueryParam;
import com.persagy.apccontrol.controller.strategy.vo.ChillerStrategyPlanVO;
import com.persagy.apccontrol.service.SaasStrategyService;
import com.persagy.validate.utility.validate.range.DataRangeValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/9/26 8:39 PM
 */
@Service
@Validated
@Slf4j
public class SaasStrategyServiceImpl implements SaasStrategyService {


    @Autowired
    private BizStrategyQueryService bizStrategyQueryService;
    @Autowired
    private BizStrategyConvert bizStrategyConvert;
    @Resource
    private SaasStrategyConvert strategyConvert;

    @Override
    public List<ChillerStrategyPlanVO> queryByParam(@NonNull ChillerFixedStrategyQueryParam param) {
        DataRangeValidator.date().allowFutureTime().validate(param.getBeginDateTime(), param.getEndDateTime());
        List<StrategyDTO> fixedStrategies = bizStrategyQueryService.listFixedStrategy(strategyConvert.toFixedQueryParam(param));
        if (CollectionUtils.isEmpty(fixedStrategies)) {
            return new ArrayList<>();
        }
        List<ChillerStrategyPlanVO> content = new ArrayList<>();
        for (StrategyDTO strategyDTO : fixedStrategies) {
            List<StrategyDayDTO> dayPlans = strategyDTO.getDayPlans();
            for (StrategyDayDTO dayDTO : dayPlans) {
                for (StrategyPlanDTO planDTO : dayDTO.getPlans()) {
                    ChillerUnitPlanStrategyDTO planStrategyDTO = bizStrategyConvert.parsePlanDTO(
                            ChillerUnitPlanStrategyDTO.class, planDTO.getResult());
                    ChillerStrategyPlanVO vo = strategyConvert.toChillerVo(planDTO, planStrategyDTO);
                    if (vo.getRunStatus().equals(RunStatusEnum.CLOSE)) {
                        // 关闭时，给算法提供默认值
                        vo.setChillerSNum(0);
                        vo.setChillerLNum(0);
                        vo.setChillWaterOutTempSet(new BigDecimal(-9998));
                    }
                    vo.setDate(dayDTO.getDate());
                    content.add(vo);
                }
            }
        }
        return content;
    }
}
