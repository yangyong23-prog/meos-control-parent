## ADDED Requirements

### Requirement: 空调机组控制组显隐配置枚举
系统 SHALL 在 ShellConfigItemEnum 中提供 `GRAPH_HSEC_ACAHG` 枚举项，归属 scene="hsec"、group="group_hsec_show"，valSource 指定 classCode 为 "acahg"，defaultValues 为 "show"。

#### Scenario: 枚举项参数正确
- **WHEN** 系统加载 scene="hsec" 的壳配置列表
- **THEN** 列表中 SHALL 包含 code="graph_hsec_acahg"、name="空调机组控制组显隐配置" 的配置项，且 valSource 为 `{"classCode":"acahg"}`

#### Scenario: 默认值为显示
- **WHEN** 项目首次加载空调机组控制组显隐配置
- **THEN** 该配置项的 defaultValues SHALL 为 "show"

### Requirement: 空调机组控制组隐藏设备列表枚举
系统 SHALL 在 ShellConfigItemEnum 中提供 `GRAPH_HSEC_ACAHG_LIST` 枚举项，归属 scene="hsec"、group="group_hsec_show"，valSource 指定 classCode 为 "acahg"，defaultValues 为空字符串。

#### Scenario: 隐藏设备列表参数正确
- **WHEN** 系统加载 scene="hsec" 的壳配置列表
- **THEN** 列表中 SHALL 包含 code="graph_hsec_acahg_list"、name="添加需隐藏设备" 的配置项，且 valSource 为 `{"classCode":"acahg"}`

#### Scenario: 默认无隐藏设备
- **WHEN** 项目首次加载空调机组控制组隐藏设备列表
- **THEN** 该配置项的 defaultValues SHALL 为空字符串
