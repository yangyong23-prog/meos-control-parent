# Design: Cache Rwd Statistics

## API Design

### 接口定义

```
GET /cacheRwd/getStatistics
```

### 响应结构

```json
{
  "code": "0",
  "message": "success",
  "data": {
    "countByMonitorType": {
      "COLD_HEAT_SOURCE": 100,
      "LIGHTING": 200,
      "AIR_TERMINAL": 150
    },
    "totalCount": 450
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| countByMonitorType | Map&lt;RunningMonitorTypeEnum, Long&gt; | 按监测类型分组的缓存数量 |
| totalCount | Long | 缓存总数量 |

## Architecture

```
Controller Layer
    │
    │ GET /getStatistics
    ▼
Service Layer (CacheRwdService)
    │
    ▼
Domain Layer (CacheRwdDomainService)
    │
    ▼
Repository Layer (CacheRwdObjectRepository)
    │
    │ countGroupByMonitorType()
    ▼
Database (cache_rwd_object)
```

## Database Query

使用 MyBatis-Plus 的 `selectMaps` 进行分组统计：

```sql
SELECT running_monitor_type, COUNT(*) as count
FROM cache_rwd_object
WHERE project_id = ?
GROUP BY running_monitor_type
```

## Implementation Details

### 1. 新建 VO 类

`CacheRwdStatisticsVO`:
- `Map<RunningMonitorTypeEnum, Long> countByMonitorType`
- `Long totalCount`

### 2. Repository 层

`CacheRwdObjectRepository` 添加方法：
```java
Map<RunningMonitorTypeEnum, Long> countGroupByMonitorType();
```

### 3. Domain 层

`CacheRwdDomainService` 添加方法：
```java
CacheRwdStatisticsVO getStatistics();
```

### 4. Service 层

`CacheRwdService` 添加方法：
```java
CacheRwdStatisticsVO getStatistics();
```

### 5. Controller 层

`CacheRwdController` 添加接口：
```java
@GetMapping("/getStatistics")
public R<CacheRwdStatisticsVO> getStatistics()
```
