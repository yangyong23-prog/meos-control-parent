# 数据库规范

> 数据库模式和约定。

---

## 概述

本项目使用 **MyBatis Plus** + **Druid** 连接池。所有 Mapper 继承 MyBatis Plus 基础 Mapper。

---

## MyBatis 配置

### XML Mapper 位置

**路径：** `src/main/resources/mapper/`

```
resources/
└── mapper/
    └── {business}/
        └── {Entity}Mapper.xml
```

### Mapper 模式

```java

@Mapper
public interface PointConfigMapper extends BaseMapper<PointConfigDO> {
}
```

---

## 查询模式

### LambdaQueryWrapper

```java
LambdaQueryWrapper<PointConfigDO> wrapper = new LambdaQueryWrapper<>();
wrapper.

eq(PointConfigDO::getId, id)
       .

like(PointConfigDO::getName, name)
       .

orderByDesc(PointConfigDO::getCreateTime);

List<PointConfigDO> list = mapper.selectList(wrapper);
```

---

## SQL 编写

### 禁止模式

**严禁使用 `${xxx}`** — SQL 注入风险，必须使用 `#{xxx}`

### 批量操作

```xml

<insert id="batchInsert">
    INSERT INTO table (name, value) VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.name}, #{item.value})
    </foreach>
</insert>
```

---

## 命名规范

### 表名

- 小写 + 下划线：`point_config`、`user_role`

### 列名

| 类型 | 规范              | 示例                  |
|----|-----------------|---------------------|
| ID | `{entity}_id`   | `user_id`           |
| 时间 | `{action}_time` | `create_time`       |
| 状态 | `status`        | `status`（1=启用，0=禁用） |

---

## 常见错误

1. **循环中单条操作** — N+1 查询问题
2. **忽略事务边界** — 部分更新
