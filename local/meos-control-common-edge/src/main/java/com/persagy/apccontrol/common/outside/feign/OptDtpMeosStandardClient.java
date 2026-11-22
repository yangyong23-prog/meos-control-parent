package com.persagy.apccontrol.common.outside.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * 运行标准过渡 B4：查询 dtp-meos 的运行标准解析结果（standard_compute_result / standard_compute_result_detail）。
 *
 * <p>对应 dtp-meos-server-edge {@code CalculateController}（2026-11-18 包已提供）。我方不直读这两张表。
 * 返回原样的 JsonNode，不绑定 dtp-meos 的实体与枚举，避免两边版本耦合。
 * 与 {@code DtpMeosServerClient} 同一服务，单独 contextId，不改动原客户端。
 */
@FeignClient(name = "dtp-meos-server-edge", contextId = "optDtpMeosStandard", url = "${inner-url.bdtp.dtp-meos.service:}")
@RefreshScope
public interface OptDtpMeosStandardClient {

    /**
     * 按日查询解析结果。入参字段：projectId（必填）、instanceIds、instanceGroup、instanceType、
     * standardDate 或 beginDate（含）/ endDate（不含）、standardTarget（TDB / CO2 / PRESS）。
     */
    @PostMapping("/calculate/listStandardComputeResultService")
    JsonNode listStandardComputeResult(@RequestHeader("projectId") String projectId, @RequestHeader("groupCode") String groupCode,
                                       @RequestBody Map<String, Object> body);

    /**
     * 按时间点查询解析详情。入参字段：projectId（必填）、instanceIds、instanceGroup、instanceType、
     * standardTime 或 beginTime（含）/ endTime（不含）、standardType。
     */
    @PostMapping("/calculate/listStandardComputeResultDetailService")
    JsonNode listStandardComputeResultDetail(@RequestHeader("projectId") String projectId, @RequestHeader("groupCode") String groupCode,
                                             @RequestBody Map<String, Object> body);
}
