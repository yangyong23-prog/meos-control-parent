## ADDED Requirements

### Requirement: 气象监测站列表查询
系统必须提供查询项目中所有气象监测站（WESEMS）设备列表的能力，并按设备名称汉字升序排序。

#### Scenario: 项目有多个气象站时查询返回列表
- **WHEN** 调用气象站列表查询接口且项目有3个WESEMS设备
- **THEN** 返回包含3个设备的列表，按设备名称汉字升序排序

#### Scenario: 项目无气象站时查询返回空列表
- **WHEN** 调用气象站列表查询接口且项目没有任何WESEMS设备
- **THEN** 返回空列表`[]`

#### Scenario: 气象站按汉字升序排序
- **WHEN** 项目有气象站"北京站"、"上海站"、"广州站"
- **THEN** 返回顺序必须为：["北京站", "广州站", "上海站"]（按拼音升序）

#### Scenario: 查询结果包含设备ID和名称
- **WHEN** 调用气象站列表查询接口
- **THEN** 返回的每个设备对象必须包含：
  - `id`: 设备ID（如`WS001`）
  - `name`: 设备名称（如"北京气象监测站"）

### Requirement: 气象站查询数据源
气象站列表必须从RWD（资源目录服务）中查询，使用设备类编码`WESEMS`作为过滤条件。

#### Scenario: 使用RWD服务查询气象站
- **WHEN** 查询气象站列表
- **THEN** 必须调用`DtpRwdClientService.queryObjectList()`方法，传入classCode=`WESEMS`

#### Scenario: 查询异常时返回空列表
- **WHEN** RWD服务调用失败（网络异常、服务不可用等）
- **THEN** 捕获异常并返回空列表`[]`，记录ERROR级别日志
