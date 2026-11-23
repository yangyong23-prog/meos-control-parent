# 边缘前端云端接口改由盒内转发（switch.opt.edgeCloudProxy）

2026-11-23 Blake 决定：边缘前端原先由浏览器直连云端的天气、电价、碳排请求，改走盒内后端转发。现场浏览器不需要访问外网，云端也不需要放行跨域。工作历日期标签一并处理：盒内有 mid-calendar，但 IP 入口没有它的路由。

## 接口

| meos-control 接口 | 转发到 | Nacos 地址键（沿用现有） | 4.91 当前指向 |
|---|---|---|---|
| `POST /opt/edgeCloudProxy/weather/hour24Forecast` | dtp-weather `/api/entrance/unifierJson/Hour24Forecast` | `url.cloud-dtp-weather-server` | 生产 |
| `POST /opt/edgeCloudProxy/persist/queryProjectElecPrice` | dtp-persist `/iot/ipcal/data/queryProjectElecPrice` | `url.cloud-dtp-persist-server` | QA |
| `POST /opt/edgeCloudProxy/electricity/carbonCategoryData` | dtp-electricity `/carbon/queryCategoryData` | `inner-url.bdtp.dtp-electricity.service` | 未配置，需配生产 |
| `POST /opt/edgeCloudProxy/calendar/getEveryDayDateTag` | mid-calendar `/v2.0/work/calendar/getEveryDayDateTag` | `inner-url.bdtp.mid-calendar.service` | 盒内 mid-calendar |

- 请求体原样转发，并以 query 参数附带 groupCode、projectId、userId=systemId（取自请求头上下文），与现有 Feign 客户端的做法一致。
- 响应体原样返回，不包 `R`，因此前端的解析逻辑和直连时完全相同。
- 开关关闭时，接口返回 `R.fail(A0402)`，不发任何外部请求。

## 前端

- `window.meosEnv.OPT_EDGE_CLOUD_PROXY = true` 时，四个调用改走 `${INTERFACE_PREFIX_MEOS}/opt/edgeCloudProxy/*`。
- 开关关闭或非边缘环境时，仍用原来的 `INTERFACE_PREFIX_BDTP_WEATHER / BDTP_PERSIST / BDTP_ELECTRICITY / MID_CALENDAR`。

## 上线顺序

1. 后端部署后，在 Nacos 打开 `switch.opt.edgeCloudProxy: true`。如果要用碳排，再补 `inner-url.bdtp.dtp-electricity.service`（生产 `https://www.ysbdtp.com/dtp-electricity-server`）。
2. 前端部署后，在 meosEnv 加 `OPT_EDGE_CLOUD_PROXY: true`。
3. 回退：关掉任一开关即可，前端会回到原前缀；在边缘环境下，原前缀会被 meosEnv 防回落拦截。
