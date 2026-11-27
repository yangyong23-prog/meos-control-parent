package com.persagy.apccontrol.basic.controller.outside;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.client.OptEdgeCloudProxyClients;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

/**
 * opt 分支（switch.opt.edgeCloudProxy，默认关）：边缘前端不再直连云端，天气、电价、碳排、工作历日期标签由本服务原样转发。
 * <p>
 * 开关关闭时接口返回失败，不发任何外部请求；前端只有在 window.meosEnv.OPT_EDGE_CLOUD_PROXY 打开时才会调用这里。
 * 返回值是下游的原始响应，不包 R，前端解析逻辑与直连时相同。说明见 docs/opt/edge-cloud-proxy.md。
 *
 * @author opt
 */
@Tag(name = "opt-边缘云端接口转发")
@RestController
@RequestMapping("/opt/edgeCloudProxy")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OptEdgeCloudProxyController implements LogComponent {

    private final OptEdgeCloudProxyClients.Weather weather;
    private final OptEdgeCloudProxyClients.Persist persist;
    private final OptEdgeCloudProxyClients.Electricity electricity;
    private final OptEdgeCloudProxyClients.Calendar calendar;

    @Operation(summary = "转发 dtp-weather 24 小时天气预报")
    @PostMapping("/weather/hour24Forecast")
    public Object hour24Forecast(@RequestBody JsonNode body) {
        return forward(() -> weather.hour24Forecast(DtpRwdCommonParam.instance(), body));
    }

    @Operation(summary = "转发 dtp-persist 项目电价")
    @PostMapping("/persist/queryProjectElecPrice")
    public Object queryProjectElecPrice(@RequestBody JsonNode body) {
        return forward(() -> persist.queryProjectElecPrice(DtpRwdCommonParam.instance(), body));
    }

    @Operation(summary = "转发 dtp-electricity 碳排分类数据")
    @PostMapping("/electricity/carbonCategoryData")
    public Object queryCarbonCategoryData(@RequestBody JsonNode body) {
        return forward(() -> electricity.queryCarbonCategoryData(DtpRwdCommonParam.instance(), body));
    }

    @Operation(summary = "转发 mid-calendar 每日日期标签")
    @PostMapping("/calendar/getEveryDayDateTag")
    public Object getEveryDayDateTag(@RequestBody JsonNode body) {
        return forward(() -> calendar.getEveryDayDateTag(DtpRwdCommonParam.instance(), body));
    }

    private Object forward(Supplier<JsonNode> call) {
        if (!OptSwitch.on(OptSwitch.EDGE_CLOUD_PROXY)) {
            return R.fail(ResponseCode.USER_ERROR_A0402.getCode(), "边缘云端接口转发未开启（switch.opt.edgeCloudProxy）");
        }
        return call.get();
    }

    @Override
    public String logTag() {
        return "边缘云端接口转发";
    }
}
