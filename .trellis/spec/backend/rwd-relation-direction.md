# RWD Object Query Relation Direction

> **Rule**: When using `DtpRwdObjectQueryParam` with relation filters, the combination of `setRelationFrom/setRelationTo` and `setObjFrom/setObjTo` MUST follow the physical relation direction defined in RWD.

---

## Relation Direction Mapping

Given a relation definition: `from=A →[RelationType]→ to=B`

| Query Target | API Call | Meaning |
|-------------|----------|---------|
| Query **B** (to-side objects) | `.setRelationTo(new DtpRwdObjectRelationQueryParam(TYPE).setObjFrom(aIds))` | "Find objects on the to-side, where from-side matches aIds" |
| Query **A** (from-side objects) | `.setRelationFrom(new DtpRwdObjectRelationQueryParam(TYPE).setObjTo(bIds))` | "Find objects on the from-side, where to-side matches bIds" |

---

## ❌ FORBIDDEN: Mismatched Direction

```java
// ❌ WRONG — relationFrom + objFrom is an illegal combination
.setRelationFrom(new DtpRwdObjectRelationQueryParam(TYPE).setObjFrom(aIds))

// ❌ WRONG — relationTo + objTo is an illegal combination
.setRelationTo(new DtpRwdObjectRelationQueryParam(TYPE).setObjTo(bIds))
```

---

## ❌ FORBIDDEN: Missing objTo/objFrom

```java
// ❌ WRONG — objTo is null, invalid() returns true, queryObject returns null
.setRelationFrom(new DtpRwdObjectRelationQueryParam(TYPE))

// ❌ WRONG — objFrom is null, invalid() returns true, queryObject returns null
.setRelationTo(new DtpRwdObjectRelationQueryParam(TYPE))
```

`DtpRwdObjectQueryParam.invalid()` enforces:
- If `relationFrom` is set, `objTo` MUST be non-null
- If `relationTo` is set, `objFrom` MUST be non-null

If you need "has any relation of this type" without filtering by specific IDs, you must provide the full set of possible IDs (e.g., allSpaceIds for EQUIPMENT_SERVICE_SPACE).

---

## Real Example

```java
// Relation: ACATAH →[ACAirNetwork/Supply]→ ACVTSF
// Query ACVTSF (to-side), from-side is ACATAH
DtpRwdObjectQueryParam supplyParam = new DtpRwdObjectQueryParam()
        .setClassCodeEnum(DtpRwdClassCodeEnum.ACVTSF)
        .setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.AC_AIR_NETWORK_SUPPLY).setObjFrom(acatahIds));

// Relation: ACATFU →[FreshAirNetwork/Fresh]→ ACVVER
// Query ACVVER (to-side), from-side is ACATFU
DtpRwdObjectQueryParam freshParam = new DtpRwdObjectQueryParam()
        .setClassCodeEnum(DtpRwdClassCodeEnum.ACVVER)
        .setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.FRESH_AIR_NETWORK).setObjFrom(acatfuIds));

// Relation: Equipment →[EQUIPMENT_SERVICE_SPACE]→ Space
// Query Equipment (from-side), to-side is Space
DtpRwdObjectQueryParam equipParam = new DtpRwdObjectQueryParam()
        .setClassCodeEnum(DtpRwdClassCodeEnum.ACVTSF)
        .setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(allSpaceIds));
```

---

## Pitfall: Nested Conditional Blocks

When a method queries multiple relation types with different source ID sets (e.g., acatahIds and acatfuIds), each relation query MUST be in its own independent `if` block. Do NOT nest them inside a parent condition:

```java
// ❌ WRONG — FreshAirNetwork query skipped when acatahIds is empty even if acatfuIds is not
if (CollectionUtils.isNotEmpty(acatahIds)) {
    // ACAirNetwork/Supply query...
    if (CollectionUtils.isNotEmpty(acatfuIds)) {
        // FreshAirNetwork/Fresh query — WRONGLY NESTED!
    }
}

// ✅ CORRECT — independent conditions
if (CollectionUtils.isNotEmpty(acatahIds)) {
    // ACAirNetwork/Supply query...
}
if (CollectionUtils.isNotEmpty(acatfuIds)) {
    // FreshAirNetwork/Fresh query...
}
```

---

## Pitfall: Caller-side Parameter Reuse Contamination

When a method shares the same `DtpRwdObjectQueryParam` across multiple service calls, any service that **directly mutates the shared parameter** (e.g., setting `relationFrom` or `relationTo`) leaves a persistent side effect. That side effect contaminates all subsequent uses of the same object reference.

```java
// ❌ WRONG — FLOOR query mutates the shared param, contamination propagates to pump query
DtpRwdObjectQueryParam sharedParam = new DtpRwdObjectQueryParam()
        .setClassCode(wsstlu, wsstor, wsstcp, wsstos);

// listEquipmentInByFloorIds writes relationFrom(EQUIPMENT_IN_FLOOR) into sharedParam (no copy)
list<DtpRwdEquipmentDTO> equipments = dtpRwdClientService.listEquipmentInByFloorIds(floorIds, sharedParam);

// sharedParam now has a residual relationFrom(EQUIPMENT_IN_FLOOR)
// Providing it to the pump query → both relationFrom + relationTo are dispatched
updateRelatedPumps(equipments, sharedParam);  // contamination!
```

```java
// ✅ CORRECT — each downstream call uses its own clean parameter built from the needed values
List<SewageEquipVO> listEquipments(..., @Nullable String... withColumns) {
    // Main query parameter — not reused for later queries
    DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
            .setClassCode(wsstlu, wsstor, wsstcp, wsstos)
            .setWithColumns(withColumnList);

    List<DtpRwdEquipmentDTO> equipments = dtpRwdClientService.listEquipmentInByFloorIds(floorIds, queryParam);

    // Relation query: construct a fresh, independent parameter with only what the pump query needs
    DtpRwdObjectQueryParam relationParam = new DtpRwdObjectQueryParam()
            .setClassCode(WSSTSP)
            .addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
            .setWithColumns(withColumnList);
    // relationParam has NO relationFrom/relationTo — the callee sets those internally
    dtpRwdClientService.listSubmersibleSewagePumpByEquipmentIds(projectId, ids, relationParam);
}
```

**Key principle**: If a parameter is meant for a specific downstream query, construct it locally with only the fields that query needs. Do not reuse a parameter that has passed through a service that may have mutated it. When passing only filter values (e.g., `List<String> withColumns`) instead of a mutable query object, the contamination risk is eliminated entirely.
