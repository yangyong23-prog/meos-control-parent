# API URL 规范

> **Why**: 统一的 URL 规范让 API 自文档化，开发者无需查阅文档即可推断用法。
> **What**: RESTful 风格，复数名词。

---

## URL 结构

### 标准格式

```
/{module}/{resource}/[{id}[/{sub-resource}]]
```

**示例**:

| 操作  | URL                             |
|-----|---------------------------------|
| 列表  | `/admin/pointConfigs`           |
| 单个  | `/admin/pointConfigs/{id}`      |
| 子资源 | `/admin/pointConfigs/{id}/tags` |

### 为什么使用复数名词

**原因**: RESTful 风格约定俗成，`/users` 表示用户资源集合，比 `/user` 更清晰表达语义。

---

## 标准 CRUD URL

### URL 与方法对照

| 操作   | HTTP 方法 | URL                          | 说明       |
|------|---------|------------------------------|----------|
| 列表   | GET     | `/admin/pointConfigs`        | 分页/条件查询  |
| 获取   | GET     | `/admin/pointConfigs/{id}`   | 获取单个     |
| 新增   | POST    | `/admin/pointConfigs`        | 创建资源     |
| 更新   | PUT     | `/admin/pointConfigs/{id}`   | 全量更新     |
| 删除   | DELETE  | `/admin/pointConfigs/{id}`   | 删除       |
| 批量新增 | POST    | `/admin/pointConfigs/batch`  | 批量创建     |
| 批量更新 | PUT     | `/admin/pointConfigs/batch`  | 批量更新     |
| 导入   | POST    | `/admin/pointConfigs/import` | Excel 导入 |
| 导出   | GET     | `/admin/pointConfigs/export` | Excel 导出 |

---

## 业务操作 URL

### 自定义动作

对于非 CRUD 操作，使用动作动词：

```
/admin/pointConfigs/{id}/enable      # 启用
/admin/pointConfigs/{id}/disable     # 禁用
/admin/pointConfigs/{id}/copy        # 复制
/admin/pointConfigs/validate         # 校验
```

### 命名规则

| 规则   | 正确               | 错误                 |
|------|------------------|--------------------|
| 动宾结构 | `/enable`        | `/doEnable`        |
| 蛇形命名 | `/point-configs` | `/pointConfigs`    |
| 避免动词 | `/users/search`  | `/users/findUsers` |

---

## 模块前缀

### 环境隔离

| 环境  | 前缀       | 示例                      |
|-----|----------|-------------------------|
| 管理端 | `/admin` | `/admin/pointConfigs`   |
| 用户端 | `/user`  | `/user/profile`         |
| 公开  | 无前缀      | `/public/announcements` |

---

## 常见错误

### ❌ 错误示例

```bash
# 单数名词
GET /admin/pointConfig      # 应为 /admin/pointConfigs

# 动词代替资源
GET /admin/getPointConfigs   # 应为 GET /admin/pointConfigs

# 蛇形命名
GET /admin/point-configs    # 应为 /admin/pointConfigs
```

### ✅ 正确示例

```bash
GET /admin/pointConfigs
GET /admin/pointConfigs/{id}
POST /admin/pointConfigs
PUT /admin/pointConfigs/{id}
DELETE /admin/pointConfigs/{id}
```

---

## DO/DON'T 速查

| DO                  | DON'T                  |
|---------------------|------------------------|
| 复数名词                | 单数名词                   |
| `GET /users/{id}`   | `GET /getUserById?id=` |
| `/enable` 动作        | `/doEnable` 多余动词       |
| `/point-configs` 蛇形 | `/pointConfigs` 驼峰     |
