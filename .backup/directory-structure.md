# 目录结构

> **Why**: 统一目录结构让代码组织可预测，开发者能快速定位。
> **What**: 模块布局、包结构、数据对象。

---

## 模块布局

```
meos-control-parent/
├── common/basic/              # 共享（云+边）
├── saas/                      # 云端部署
│   ├── meos-control-common/
│   ├── meos-control-compute/
│   └── meos-control-server/   # 云端入口
└── local/                     # 边端部署
    ├── meos-control-common-edge/
    ├── meos-control-compute-edge/
    └── meos-control-server-edge/  # 边端入口
```

---

## 包结构

### 按业务分层

| 层级         | 包路径                         | 说明           |
|------------|-----------------------------|--------------|
| Controller | `controller/{business}/`    | REST 接口      |
| Service 接口 | `service/{business}/`       | 业务逻辑接口       |
| Service 实现 | `service/{business}/impl/`  | 业务逻辑实现       |
| Mapper     | `mapper/{business}/`        | MyBatis 数据访问 |
| Mapper XML | `resources/mapper/`         | MyBatis XML  |
| Entity     | `model/entity/`             | 实体类（DO）      |
| DTO        | `model/dto/`                | 数据传输对象       |
| VO         | `model/vo/`                 | 视图对象         |
| Param      | `model/param/{business}/`   | 查询参数         |
| Context    | `model/context/{business}/` | 上下文对象        |
| Structure  | `structure/{business}/`     | DDD 领域组件     |
| Resource   | `resource/{business}/`      | 资源层          |

### 工具与配置

| 包路径          | 说明            |
|--------------|---------------|
| `outside/`   | 外部客户端（Feign）  |
| `convert/`   | MapStruct 转换器 |
| `config/`    | 配置类           |
| `constant/`  | 常量            |
| `exception/` | 异常定义          |
| `utils/`     | 工具类           |
| `validate/`  | 校验类           |
| `sse/`       | 服务端推送         |
| `task/`      | 定时任务          |
| `kafka/`     | Kafka 消息      |

---

## 禁止的包

**老代码包（禁止新代码）**: `handler/`、`po/`、`page/`

---

## 数据对象

### 命名后缀

| 类型   | 后缀        | 示例               |
|------|-----------|------------------|
| 请求   | `Request` | `UserRequest`    |
| 响应   | `VO`      | `UserVO`         |
| 传输   | `DTO`     | `UserDTO`        |
| 查询参数 | `Param`   | `UserQueryParam` |

### 对象映射

使用 MapStruct 进行 Entity ↔ Request/VO 转换：

```java

@Mapper(componentModel = "spring")
public interface UserConvert {
    UserVO toVO(UserDO entity);

    UserDO toEntity(UserRequest request);
}
```

转换器放在 `convert/` 目录。

---

## 新功能组织示例

```
controller/pointConfig/
└── PointConfigController.java
service/pointConfig/
├── PointConfigService.java
└── impl/PointConfigServiceImpl.java
mapper/pointConfig/PointConfigMapper.java
model/
├── dto/PointConfigDTO.java
├── vo/PointConfigVO.java
├── entity/PointConfigDO.java
└── param/pointConfig/PointConfigQueryParam.java
resources/mapper/pointConfig/PointConfigMapper.xml
```
