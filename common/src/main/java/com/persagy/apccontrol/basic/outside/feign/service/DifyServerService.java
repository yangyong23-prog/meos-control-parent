package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.dto.lighting.DifyDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DifyResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyDetailResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyItemInfoListResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyTopItemInfoListResponse;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.EnergyTypeEnum;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/26 11:33
　　 * @description: 查询能耗预算相关数据
*/
public interface DifyServerService extends BaseFeignClientService {



    /**
     *
     * @param difyDTO
     * @return
     */
    void webhook(DifyDTO difyDTO);


}
