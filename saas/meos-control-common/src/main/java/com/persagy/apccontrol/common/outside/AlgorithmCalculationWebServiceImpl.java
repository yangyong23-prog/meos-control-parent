package com.persagy.apccontrol.common.outside;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationDeleteDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmCalculationUpdateDTO;
import com.persagy.apccontrol.basic.model.vo.AlgorithmStartCalculationDTO;
import com.persagy.apccontrol.basic.outside.feign.client.AlgorithmCalculationWebClient;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description: 算法平台
 * @author: yancheng
 * @date: 2023/3/27
 * @version: V1.0
 */
@Slf4j
@Service("algorithmCalculationWebService")
public class AlgorithmCalculationWebServiceImpl implements AlgorithmCalculationWebService{

    @Resource
    private AlgorithmCalculationWebClient algorithmCalculationWebClient;

    public static void handleResult(DmpResult<String> result) {
        String result1 = result.getResult();
        if (StringUtils.isBlank(result1) || !"success".equals(result1)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), result.getMessage() == null ? LocaleMessageUtil.getMessage("key_c6243bb5ab50449a867c8d12f93f6a81") : result.getMessage());
        }
    }

    @Override
    public DmpResult<String> logonAlgorithmCalculation(AlgorithmStartCalculationDTO algorithmStartCalculationDTO) {
        DmpResult<String> result = algorithmCalculationWebClient.logonAlgorithmCalculation(algorithmStartCalculationDTO);
        handleResult(result);
        return result;
    }

    @Override
    public DmpResult<String> updateAlgorithmCalculation(AlgorithmCalculationUpdateDTO algorithmCalculationUpdateDTO) {
        DmpResult<String> result = algorithmCalculationWebClient.updateAlgorithmCalculation(algorithmCalculationUpdateDTO);
        try {
            handleResult(result);
            return result;
        } catch (Exception e) {
            log.error("获取失败", e);
        }
        return null;
    }

    @Override
    public DmpResult<String> deleteAlgorithmCalculation(AlgorithmCalculationDeleteDTO algorithmCalculationDeleteDTO) {
        DmpResult<String> result = algorithmCalculationWebClient.deleteAlgorithmCalculation(algorithmCalculationDeleteDTO);
        try {
            handleResult(result);
            return result;
        } catch (Exception e) {
            log.error("获取失败", e);
        }
        return null;
    }
}
