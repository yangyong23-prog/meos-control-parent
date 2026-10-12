package com.persagy.apccontrol.common.outside.feign;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.InterfaceUrlConst;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectElecQueryParam;
import com.persagy.framework.common.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * 云端群控系统
 *
 * @author yuyao
 */
@FeignClient(name = "meos-control-server", url = "${external-url.meoi.meos-control.service:}")
public interface RcSaasClient {

    @GetMapping("callBack/syncBasicsConfig")
    R<List<RcBasicsConfig>> syncBasicsConfig(@RequestHeader String groupCode);

    /**
     * 获取2D系统图缓存数据
     *
     * @param groupCode
     * @param forwardVO
     * @return
     */
    @PostMapping(value = InterfaceUrlConst.MEOI_AIM_SERVER_COMMON)
    String meoiAimServerCommon(@RequestHeader(name = "groupCode") String groupCode, @RequestBody CommonServerForwardVO forwardVO);

    @PostMapping(value = InterfaceUrlConst.DTP_RWD_SERVER_COMMON)
    String dtpRwdServerCommon(@RequestHeader(name = "groupCode") String groupCode, @RequestBody CommonServerForwardVO forwardVO);

    /**
     * 获取价值方案满足率及其对应的IOT数据
     *
     * @param groupCode
     * @param dto       {
     *                  "projectId": "Pj7763494626",
     *                  "ipTime": "20230928170000"   不传，默认为当前最新
     *                  }
     * @return
     */
    @PostMapping(value = InterfaceUrlConst.VALUE_SELECT_TEMP_CARBON_RATE_BY_IOT)
    R<JSONObject> currentTempAndCarbonRate(@RequestHeader(name = "groupCode") String groupCode, @RequestBody JSONObject dto);
    
    /**
     * 价值选择方案对应的电价
     *
     * @param groupCode
     * @param param
     * @return
     */
    @PostMapping(value = "/valueSelection/listElectricityData")
    R<ValueSelectionTrendTableDto> listElectricityData(@RequestHeader(name = "groupCode") String groupCode, @RequestBody ValueSelectElecQueryParam param);

    /**
     * 价值选择方案对应的电价
     *
     * @param groupCode
     * @param param
     * @return
     */
    @PostMapping(value = "/valueSelection/listCarbonData")
    R<ValueSelectionTrendTableDto> listCarbonData(@RequestHeader(name = "groupCode") String groupCode, @RequestBody ValueSelectElecQueryParam param);

    /**
     * 巡检信息
     *
     * @param objectIdList 对象id
     * @return 对象id->巡检信息
     */
    @PostMapping(value = InterfaceUrlConst.TASK_INFO_MAP)
    R<Map<String, TaskInfoDTO>> mapTaskInfo(@RequestHeader(name = "projectId") String groupCode, @RequestBody List<String> objectIdList);
    
}
