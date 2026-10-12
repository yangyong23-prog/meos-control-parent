## 1. 枚举新增

- [x] 1.1 在 ShellConfigItemEnum.java 的 GRAPH_HSEC_ACATFU_LIST 枚举项之后，新增 GRAPH_HSEC_ACAHG 枚举项：`GRAPH_HSEC_ACAHG("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acahg", "空调机组控制组显隐配置",  "/", "show", -1, null, null, "", "", null, "{\"classCode\":\"acahg\"}", null)`
- [x] 1.2 在 GRAPH_HSEC_ACAHG 之后，新增 GRAPH_HSEC_ACAHG_LIST 枚举项：`GRAPH_HSEC_ACAHG_LIST("hsec", "group_hsec_show", "半剖图配置", "graph_hsec_acahg_list", "添加需隐藏设备",  "/", "", -1, null, null, "", "", null, "{\"classCode\":\"acahg\"}", null)`

## 2. 验证

- [x] 2.1 编译验证：执行 mvn compile 确认无编译错误
- [x] 2.2 功能验证：确认 ShellConfigItemEnum.getByCode("graph_hsec_acahg") 和 getByCode("graph_hsec_acahg_list") 能正确返回对应枚举实例
