package com.persagy.apccontrol.common.outside;

import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationDeleteDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationUpdateDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmStartCalculationDTO;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description: 算法平台
 * @author: yancheng
 * @date: 2023/3/27
 * @version: V1.0
 */
public interface AlgorithmCalculationWebService {

    /**
     * 注册专家策略
     * @param algorithmStartCalculationDTO
     * @return
     */
    DmpResult<String> logonAlgorithmCalculation(@RequestBody AlgorithmStartCalculationDTO algorithmStartCalculationDTO);

    /**
     * 修改专家策略
     * @param algorithmCalculationUpdateDTO
     * @return
     */
    DmpResult<String> updateAlgorithmCalculation(@RequestBody AlgorithmCalculationUpdateDTO algorithmCalculationUpdateDTO);

    /**
     * 删除专家策略
     * @param algorithmCalculationDeleteDTO
     * @return
     */
    DmpResult<String> deleteAlgorithmCalculation(@RequestBody AlgorithmCalculationDeleteDTO algorithmCalculationDeleteDTO);
}
