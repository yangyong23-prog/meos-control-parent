package com.persagy.apccontrol.common.outside.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 孪生数据源切换（switch.opt.twinSource）：读 meos-hub 的孪生当前值。
 *
 * <p>用 meos-hub 保留的旧 iot-hub 兼容接口 {@code POST /edge2cloud/point/getPointTwin}：免鉴权、只读，
 * 请求 {@code {projectId, pointSet:[{objectId, infoCodeSet}]}}，返回 {@code R<List<CloudObjInfoCodeStatus>>}。
 * 必须显式传 infoCodeSet：空集合时 meos-hub 只返回直采绑点，不返回 TWC 点和规则点。
 * 已知限制：该接口不返回“规则合成点”（只有孪生规则、没有绑点的计算点），已向 meos-hub 团队提需求。
 *
 * <p>地址：{@code inner-url.meos-hub.service}（如 {@code http://meos-hub-server-edge:8080}），
 * 上下文路径 {@code opt.meos-hub.context-path}（默认 {@code /meos-hub-server-edge}）。
 * 返回原样的 JsonNode，不绑定 meos-hub 的类型。
 */
@FeignClient(name = "meos-hub-server-edge", contextId = "optMeosHubTwin", url = "${inner-url.meos-hub.service:}",
        path = "${opt.meos-hub.context-path:/meos-hub-server-edge}")
@RefreshScope
public interface OptMeosHubTwinClient {

    @PostMapping("/edge2cloud/point/getPointTwin")
    JsonNode getPointTwin(@RequestBody Map<String, Object> body);
}
