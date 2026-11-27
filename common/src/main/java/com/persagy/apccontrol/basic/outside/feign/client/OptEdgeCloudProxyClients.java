package com.persagy.apccontrol.basic.outside.feign.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * opt 分支（switch.opt.edgeCloudProxy，默认关）：边缘前端原先由浏览器直连云端的几个接口，改由盒内 meos-control 原样转发。
 * <p>
 * 地址沿用各服务已有的 Nacos 键（与现有 Feign 客户端相同），请求体和响应体都不做解析，保证前端拿到的结构与直连时一致。
 * 说明见 docs/opt/edge-cloud-proxy.md。
 *
 * @author opt
 */
public final class OptEdgeCloudProxyClients {

    private OptEdgeCloudProxyClients() {
    }

    /** dtp-weather：24 小时天气预报（地址键 url.cloud-dtp-weather-server，盒子上指向生产） */
    @FeignClient(name = "dtp-weather-server", contextId = "optEdgeWeatherProxy", url = "${url.cloud-dtp-weather-server:}")
    public interface Weather {

        @PostMapping("/api/entrance/unifierJson/Hour24Forecast")
        JsonNode hour24Forecast(@SpringQueryMap DtpRwdCommonParam param, @RequestBody JsonNode body);
    }

    /** dtp-persist：项目电价（地址键 url.cloud-dtp-persist-server，盒子上指向 QA） */
    @FeignClient(name = "dtp-persist-server", contextId = "optEdgePersistProxy", url = "${url.cloud-dtp-persist-server:}")
    public interface Persist {

        @PostMapping("/iot/ipcal/data/queryProjectElecPrice")
        JsonNode queryProjectElecPrice(@SpringQueryMap DtpRwdCommonParam param, @RequestBody JsonNode body);
    }

    /** dtp-electricity：碳排分类数据（地址键 inner-url.bdtp.dtp-electricity.service，盒子上指向生产） */
    @FeignClient(name = "dtp-electricity-server", contextId = "optEdgeElectricityProxy", url = "${inner-url.bdtp.dtp-electricity.service:}")
    public interface Electricity {

        @PostMapping("/carbon/queryCategoryData")
        JsonNode queryCarbonCategoryData(@SpringQueryMap DtpRwdCommonParam param, @RequestBody JsonNode body);
    }

    /** mid-calendar：每日日期标签（地址键 inner-url.bdtp.mid-calendar.service，盒子上是盒内 mid-calendar） */
    @FeignClient(name = "mid-calendar-server", contextId = "optEdgeCalendarProxy", url = "${inner-url.bdtp.mid-calendar.service:}", path = "/v2.0")
    public interface Calendar {

        @PostMapping("/work/calendar/getEveryDayDateTag")
        JsonNode getEveryDayDateTag(@SpringQueryMap DtpRwdCommonParam param, @RequestBody JsonNode body);
    }
}
