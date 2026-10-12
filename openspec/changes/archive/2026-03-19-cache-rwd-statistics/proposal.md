# Proposal: Cache Rwd Statistics

## Summary

添加一个 REST 接口，用于统计 `cache_rwd_object` 表中按 `running_monitor_type` 分组的缓存数量。

## Motivation

当前系统已有缓存构建和状态查询接口，但缺少对缓存数据的统计功能。运维人员需要了解各监测类型的缓存数据分布情况，以便监控缓存使用状态和排查问题。

## Goals

- 提供按 `runningMonitorType` 分组的缓存数量统计
- 返回缓存总数量
- 使用 GET 方法，无需请求参数

## Non-goals

- 不提供时间范围过滤
- 不返回无数据的监测类型
- 不提供其他维度（如 objectType）的统计

## Scope

- 影响模块：`CacheRwdController`、`CacheRwdService`、`CacheRwdDomainService`、`CacheRwdObjectRepository`
- 版本：`7.260302.0`
