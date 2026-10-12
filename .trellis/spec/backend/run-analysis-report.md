# Run Analysis Report Contracts

## Scenario: Run Overview Optional Display Fields

### 1. Scope / Trigger

- Trigger: changing daily run analysis report `REPORT_CHAPTER_RUN_OVERVIEW` calculation or serialized chapter data.
- Applies to `RunAnalysisReportRunOverview#buildWithConfig` and `RunOverviewDTO` fields that are shown or hidden by report chapter configuration.

### 2. Signatures

- Strategy: `RunAnalysisReportRunOverview#buildWithConfig(RunAnalysisReportBuildParam, RunAnalysisReportConfig, Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>>)`
- Response DTO: `RunOverviewDTO`
- Nullable optional display fields:
  - `projectElectricity`
  - `dayBudgetUtilizationRate`
  - `spaceEnvironmentalQuality`
  - `systemRunningResults`

### 3. Contracts

- Top-level optional display fields use `null` as a frontend visibility signal: `null` means the section is hidden, while a non-null DTO/list means the section is shown and missing inner values render as `--`.
- `projectElectricity` is `null` when `REPORT_CHAPTER_ENERGY_ANALYSIS` is absent or its source data cannot be calculated.
- `dayBudgetUtilizationRate` is `null` when `REPORT_CHAPTER_ENERGY_BUDGET` is absent or its source data cannot be calculated.
- `spaceEnvironmentalQuality` is `null` only when `REPORT_CHAPTER_ENV_QUALITY` is absent. When environment quality is configured, return a non-null DTO even if configured spaces are empty/invalid or summary data is missing.
- `systemRunningResults` is `null` when no cooling system is configured, or selected IDs do not resolve to valid ACAC systems. The selected system list is the configuration source for this card.
- `weather`, `templateName`, and `valueSelectType` are default overview fields and must not be hidden only because a report chapter is absent; their inner values may still be `null` when source data is unavailable.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| Energy analysis chapter absent or source data cannot be calculated | `projectElectricity = null` |
| Energy budget chapter absent or source data cannot be calculated | `dayBudgetUtilizationRate = null` |
| Environment quality not configured in shell/report chapter config | `spaceEnvironmentalQuality = null` so frontend hides the section |
| Environment quality configured but valid spaces empty | `spaceEnvironmentalQuality != null`; frontend shows the section and renders missing inner values as `--` |
| Environment quality configured with valid spaces but no summary data | `spaceEnvironmentalQuality != null`; frontend shows the section and renders missing inner values as `--` |
| Cooling system run item chapter absent or empty | `systemRunningResults = null` |
| Cooling system run item chapter contains invalid/non-ACAC IDs only | `systemRunningResults = null` |
| Cooling system run item chapter contains selected valid ACAC IDs | `systemRunningResults` contains only those selected systems |

### 5. Good/Base/Bad Cases

- Good: unchecked optional display chapter writes `null` for its overview field so downstream UI can skip the section.
- Base: checked optional display chapter with valid source data calculates the field using existing logic.
- Base: checked environment quality chapter with valid spaces but no summary data returns a non-null DTO with nullable/default inner values.
- Bad: returning an empty DTO or empty list for a hidden overview section, because consumers can mistake it for displayable data.
- Bad: returning `null` for environment quality when the chapter is checked, because consumers use `null` to hide the card.

### 6. Tests Required

- Compile `common` after signature or nullable changes.
- Regression assertions should cover:
  - absent/unconfigured environment quality chapter -> `spaceEnvironmentalQuality == null`
  - configured environment quality chapter with empty/invalid spaces -> `spaceEnvironmentalQuality != null`
  - configured environment quality chapter with valid spaces but no summary data -> `spaceEnvironmentalQuality != null`
  - absent energy analysis chapter or no source data -> `projectElectricity == null`
  - absent energy budget chapter or no source data -> `dayBudgetUtilizationRate == null`
  - absent or empty cooling system run item chapter -> `systemRunningResults == null`
  - configured cooling system run item chapter with invalid systems only -> `systemRunningResults == null`
  - selected valid cooling systems -> result IDs are limited to selected IDs

### 7. Wrong vs Correct

#### Wrong

```java
SpaceEnvironmentalQualityDTO result = new SpaceEnvironmentalQualityDTO();
if (CollectionUtils.isEmpty(spaceIds)) {
    return null;
}
```

```java
if (spaceSummary == null) {
    return null;
}
```

```java
result.setTdbComplianceRate(spaceSummary.getSatisfyPercentage());
result.setTagCount(spaceSummary.getTagCount());
return result;
```

```java
if (CollectionUtils.isEmpty(configuredSystemIds)) {
    return Collections.emptyList();
}
```

#### Correct

```java
if (!hasChapterConfig(allConfigData, REPORT_CHAPTER_ENV_QUALITY)) {
    return null;
}

SpaceEnvironmentalQualityDTO result = new SpaceEnvironmentalQualityDTO();
if (CollectionUtils.isEmpty(spaceIds) || spaceSummary == null) {
    return SpaceEnvironmentalQualityDTO.empty();
}
result.setTdbComplianceRate(spaceSummary.getSatisfyPercentage());
result.setTagCount(Optional.ofNullable(spaceSummary.getTagCount()).orElseGet(Collections::emptyMap));
return result;
```

```java
if (CollectionUtils.isEmpty(configuredSystemIds)) {
    return null;
}
```

## Scenario: Exception Records Continuous Abnormal Merge

### 1. Scope / Trigger

- Trigger: changing daily run analysis report exception record calculation or serialized exception record data.
- Applies to `AbstractRunAnalysisReportStrategy#queryExceptionRecords(LocalDate, List<DtpRwdObjectDTO>, Map<String, List<InfoCodeDefineModel>>)` and private record assembly in `AbstractRunAnalysisReportStrategy#buildExceptionRecords`.
- Consumers include cooling system run, environment quality, and air conditioning control zone report sections that display `ExceptionRecordDTO`.

### 2. Signatures

- Strategy entry: `protected @NonNull List<ExceptionRecordDTO> queryExceptionRecords(@NonNull LocalDate date, @NonNull List<DtpRwdObjectDTO> objects, @NonNull Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines)`
- Assembly helper: `private @NonNull List<ExceptionRecordDTO> buildExceptionRecords(@NonNull List<DtpRwdObjectDTO> objects, @NonNull List<InfoCodeValueVO> allInfoCodeValues, @NonNull Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines)`
- Response DTO fields:
  - `triggerTime`
  - `recoverTime`
  - `object`
  - `infoCode`
  - `infoCodeValue`

### 3. Contracts

- Exception values are scanned independently by `objectId + infoCode`; do not merge or deduplicate across different info codes in this helper.
- A recover status is identified by data source name `正常`, `Normal`, `其他`, or `Other`.
- Recover status samples do not create exception records.
- The first non-recover sample in a continuous abnormal period creates one `ExceptionRecordDTO`.
- Continuous non-recover samples for the same `objectId + infoCode` keep the current exception record open even when the abnormal `infoCodeValue` changes; do not emit one record per 15-minute sampling point.
- If the same `objectId + infoCode` recovers later in the query window, set `recoverTime` to the first recover sample time and skip all intermediate abnormal samples.
- If the same `objectId + infoCode` does not recover before the query window ends, set `recoverTime = null` and stop scanning that info code group. Do not emit one unrecovered record per remaining sampling point.
- After a recovered period, a later non-recover sample starts a new exception record.
- Different objects or different info codes remain independent records even when timestamps overlap.

### 4. Validation & Error Matrix

| Condition | Behavior |
| --- | --- |
| All samples are recover statuses | Return no exception record for the group |
| Abnormal samples followed by recover status | One record; `triggerTime` is first abnormal time and `recoverTime` is first recover time |
| Abnormal samples continue until query window end | One record; `triggerTime` is first abnormal time and `recoverTime = null` |
| Abnormal value A -> abnormal value B before recovery | One record; abnormal value change does not recover the info code |
| Abnormal -> recover -> abnormal | Two records; the second record starts at the later abnormal time |
| Info code definition or data source is missing for an abnormal-looking sample | Treat it as non-recover under existing logic, then apply the same merge rules |
| `objectId` cannot resolve to a `DtpRwdObjectDTO` | Log and skip the sample |

### 5. Good/Base/Bad Cases

- Good: 23:30 and 23:45 samples for the same unrecovered chiller alarm produce one record at 23:30 with `recoverTime = null`.
- Base: 23:30 abnormal, 23:45 abnormal, 24:00 normal produces one record at 23:30 with `recoverTime = 24:00`.
- Base: 23:30 abnormal value A, 23:45 abnormal value B, 24:00 normal produces one record at 23:30 with `recoverTime = 24:00`.
- Base: 23:00 abnormal, 23:30 normal, 23:45 abnormal produces two records.
- Bad: emitting duplicate unrecovered rows at every 15-minute sampling point until the day ends.
- Bad: starting a new row when only the abnormal data source changes but no recover status appears.
- Bad: merging different `infoCode` alarms for the same object into one row only because their timestamps match.

### 6. Tests Required

- Compile `common` after changing exception record assembly.
- Regression assertions should cover:
  - continuous unrecovered abnormal samples for the same `objectId + infoCode` -> one record with `recoverTime == null`
  - abnormal samples followed by recover status -> one record with first recover time
  - abnormal after recover -> a new second record
  - same timestamp and same abnormal value duplicate samples -> one record
  - abnormal value change before recovery -> still one continuous record
  - overlapping different info codes remain separate records

### 7. Wrong vs Correct

#### Wrong

```java
for (int i = 0; i < infoCodeValues.size(); i++) {
    InfoCodeValueVO value = infoCodeValues.get(i);
    if (!isRecoverStatus(value)) {
        exceptionRecords.add(toRecord(value, null));
    }
}
```

#### Correct

```java
for (int i = 0; i < infoCodeValues.size(); i++) {
    InfoCodeValueVO value = infoCodeValues.get(i);
    if (isRecoverStatus(value)) {
        continue;
    }

    ExceptionRecordDTO record = toRecord(value);
    for (int j = i + 1; j < infoCodeValues.size(); j++) {
        InfoCodeValueVO next = infoCodeValues.get(j);
        if (isRecoverStatus(next)) {
            record.setRecoverTime(next.getDataTime());
            i = j;
            break;
        }
    }
    exceptionRecords.add(record);
    if (record.getRecoverTime() == null) {
        break;
    }
}
```
