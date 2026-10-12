package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyDetailResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyItemInfoListResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyTopItemInfoListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/22 15:31
　　 * @description: 能耗预算
*/
@FeignClient(name = "energy-budgetmanage-server", url = "${inner-url.energy.energy-budgetmanage.service:}")
public interface EnergyBudgetManageServerClient {


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/23 15:42
    　　 * @description: 能耗分项日数据列表
    */
    @PostMapping(value = "/Spring/MVC/entrance/unifier/DayEnergyDetail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    CustomerResponse<List<List<EnergyDetailResponse>>> dayEnergyDetail(
            @RequestHeader(name = "groupCode") String groupCode,
            @RequestHeader(name = "projectId") String projectId,
            @RequestPart("jsonString") String jsonString);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 17:14
    　　 * @description: 分项跨天数据列表
     */
    @PostMapping(value = "/Spring/MVC/entrance/unifier/WeekEnergyDetail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    CustomerResponse<List<List<EnergyDetailResponse>>> crossDayEnergyDetail(
            @RequestHeader(name = "groupCode") String groupCode, @RequestHeader(name = "projectId") String projectId,
            @RequestPart("jsonString") String jsonString);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 15:05
    　　 * @description: 分项信息列表
     */
    @PostMapping(value = "/Spring/MVC/entrance/unifier/ItemInfoList", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    CustomerResponse<List<List<EnergyItemInfoListResponse>>> itemInfoList(
            @RequestHeader(name = "groupCode") String groupCode, @RequestHeader(name = "projectId") String projectId,
            @RequestPart("jsonString") String jsonString);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/27 18:19
    　　 * @description: 查询顶级分项节点
    */
    @PostMapping(value = "/Spring/MVC/entrance/unifier/queryTopItemName", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    CustomerResponse<List<EnergyTopItemInfoListResponse>> queryTopItem(
            @RequestHeader(name = "groupCode") String groupCode, @RequestHeader(name = "projectId") String projectId,
            @RequestPart("jsonString") String jsonString);


}
