## ADDED Requirements

### Requirement: WSSTOR and WSSTLU list/detail pages query submersible sewage pumps via the new relation
For the equipment list page and equipment detail page, the `relSubmersibleBlowdownPump` and `submersibleSewagePump` info codes for WSSTOR and WSSTLU SHALL use `MechSubset/Eq2Eq` as the primary relation, querying pumps where the parent equipment is the source (`objFrom`) of the relation.

#### Scenario: New relation data exists for WSSTOR
- **WHEN** a WSSTOR equipment has submersible sewage pumps linked by `MechSubset/Eq2Eq`
- **THEN** the list/detail page SHALL display those pumps and SHALL NOT use the old `MechLocation/Inside` relation

#### Scenario: New relation data exists for WSSTLU
- **WHEN** a WSSTLU equipment has submersible sewage pumps linked by `MechSubset/Eq2Eq`
- **THEN** the list/detail page SHALL display those pumps and SHALL NOT use the old `MechLocation/Inside` relation

### Requirement: WSSTOR and WSSTLU queries fall back to the old relation when the new relation has no data
If the primary `MechSubset/Eq2Eq` query returns no objects, the system SHALL fall back to the old `MechLocation/Inside` relation for the same WSSTOR or WSSTLU equipment.

#### Scenario: Only old relation data exists for WSSTOR
- **WHEN** a WSSTOR equipment has no pumps linked by `MechSubset/Eq2Eq` but has pumps linked by `MechLocation/Inside`
- **THEN** the list/detail page SHALL display the pumps from the old relation

#### Scenario: Only old relation data exists for WSSTLU
- **WHEN** a WSSTLU equipment has no pumps linked by `MechSubset/Eq2Eq` but has pumps linked by `MechLocation/Inside`
- **THEN** the list/detail page SHALL display the pumps from the old relation

### Requirement: WSSSTSP reverse queries for WSSTOR and WSSTLU use the new relation
On the equipment list page, the WSSSTSP reverse info codes `relOilRemoveDevice` (WSSTOR) and `relLiftingUnit` (WSSTLU) SHALL use `relationFrom MechSubset/Eq2Eq objTo=${objectId}` as the primary query to find the parent equipment from a pump.

#### Scenario: WSSSTSP has a WSSTOR parent via new relation
- **WHEN** a WSSSTSP pump is the target of a `MechSubset/Eq2Eq` relation whose source is a WSSTOR
- **THEN** the pump list page SHALL display that WSSTOR as the associated oil removal device

#### Scenario: WSSSTSP has a WSSTLU parent via new relation
- **WHEN** a WSSSTSP pump is the target of a `MechSubset/Eq2Eq` relation whose source is a WSSTLU
- **THEN** the pump list page SHALL display that WSSTLU as the associated lifting unit

### Requirement: WSSSTSP reverse queries for WSSTOR and WSSTLU fall back to the old relation
If the primary new-relation query for `relOilRemoveDevice` or `relLiftingUnit` returns no objects, the system SHALL fall back to the old `MechLocation/Inside` relation query.

#### Scenario: WSSSTSP has only an old WSSTOR parent
- **WHEN** a WSSSTSP pump has no `MechSubset/Eq2Eq` WSSTOR parent but has a `MechLocation/Inside` WSSTOR parent
- **THEN** the pump list page SHALL display that WSSTOR via the fallback relation

#### Scenario: WSSSTSP has only an old WSSTLU parent
- **WHEN** a WSSSTSP pump has no `MechSubset/Eq2Eq` WSSTLU parent but has a `MechLocation/Inside` WSSTLU parent
- **THEN** the pump list page SHALL display that WSSTLU via the fallback relation

### Requirement: New relation query direction must match the reversed physical relation
The `MechSubset/Eq2Eq` relation direction is the reverse of `MechLocation/Inside`: the parent equipment (WSSTOR/WSSTLU) is the source (`objFrom`) and the pump (WSSSTSP) is the target (`objTo`). When querying pumps from a parent, the implementation SHALL use `relationTo + objFrom(parentId)`. When querying the parent from a pump, the implementation SHALL use `relationFrom + objTo(pumpId)`. This is the opposite of the old `MechLocation/Inside` direction where the pump was `objFrom` and the parent was `objTo`.

This constraint applies to both Java code (`DtpRwdClientServiceImpl.listSubmersibleSewagePumpByEquipmentIds`) and Excel template JSON configurations. The fallback queries using `MechLocation/Inside` SHALL retain the old direction (`relationFrom + objTo` for parent→pump, `relationTo + objFrom` for pump→parent).

See `.trellis/spec/backend/rwd-relation-direction.md` for the general relation direction rule.

#### Scenario: Java code queries pumps from parent equipment via new relation
- **WHEN** `listSubmersibleSewagePumpByEquipmentIds` queries pumps for WSSTOR/WSSTLU equipment using `MechSubset/Eq2Eq`
- **THEN** it SHALL call `setRelationTo(new DtpRwdObjectRelationQueryParam(EQ_IN_EQ).setObjFrom(equipmentIds))`

#### Scenario: Excel config queries pumps from parent equipment via new relation
- **WHEN** the Excel template configures `submersibleSewagePump` or `relSubmersibleBlowdownPump` for WSSTOR/WSSTLU with `MechSubset/Eq2Eq` as primary
- **THEN** the JSON SHALL use `"relationTo": {"graphCode": "MechSubset", "relCode": "Eq2Eq", "objFrom": ["${objectId}"]}`

#### Scenario: Excel config queries parent from pump via new relation
- **WHEN** the Excel template configures `relOilRemoveDevice` or `relLiftingUnit` for WSSSTSP with `MechSubset/Eq2Eq` as primary
- **THEN** the JSON SHALL use `"relationFrom": {"graphCode": "MechSubset", "relCode": "Eq2Eq", "objTo": ["${objectId}"]}`

#### Scenario: Fallback queries retain old relation direction
- **WHEN** a fallback query uses `MechLocation/Inside`
- **THEN** it SHALL use the old direction: `relationFrom + objTo` for parent→pump, `relationTo + objFrom` for pump→parent

### Requirement: WSSTCP and WSSTOS relationship queries remain unchanged
The `relSubmersibleBlowdownPump` and `submersibleSewagePump` info codes for WSSTCP and WSSTOS SHALL continue to use `MechLocation/Inside` without a fallback query.

#### Scenario: WSSTCP queries its pumps
- **WHEN** a WSSTCP equipment queries its associated submersible sewage pumps
- **THEN** the system SHALL use `MechLocation/Inside` as before

#### Scenario: WSSTOS queries its pumps
- **WHEN** a WSSTOS equipment queries its associated submersible sewage pumps
- **THEN** the system SHALL use `MechLocation/Inside` as before

### Requirement: Java callers use the new fallback-aware query method
`RunningMonitorSewageStrategy` and `WaterSupplyHomePageCardDataHandler` SHALL call the new `DtpRwdClientService.listSubmersibleSewagePumpByEquipmentIds` method, which internally applies the same new-relation-first fallback logic.

#### Scenario: Running monitor query with new relation data
- **WHEN** `RunningMonitorSewageStrategy` queries pumps for a batch of WSSTOR/WSSTLU equipment and new relation data exists
- **THEN** it SHALL return only the new relation pumps

#### Scenario: Running monitor query with only old relation data
- **WHEN** `RunningMonitorSewageStrategy` queries pumps for a batch of WSSTOR/WSSTLU equipment and no new relation data exists
- **THEN** it SHALL return the old relation pumps

#### Scenario: Water supply home page query
- **WHEN** `WaterSupplyHomePageCardDataHandler` queries pumps for WSSTOR/WSSTLU equipment
- **THEN** it SHALL use the new fallback-aware method and display the correct pump count

### Requirement: Dashboard grid chart equipment query uses the fallback-aware relation method
`/dashboardGridChart/queryEquipment` for SEWAGE SHALL return associated submersible sewage pumps via the same fallback logic, covering the right-side device statistics panel.

#### Scenario: Dashboard grid chart query with new relation data
- **WHEN** the dashboard grid chart queries pumps for WSSTOR/WSSTLU equipment and new relation data exists
- **THEN** `SewageEquipVO.relEquipment` SHALL contain only the new relation pumps

#### Scenario: Dashboard grid chart query with only old relation data
- **WHEN** the dashboard grid chart queries pumps for WSSTOR/WSSTLU equipment and no new relation data exists
- **THEN** `SewageEquipVO.relEquipment` SHALL fall back to the old relation pumps

### Requirement: Info code config by object endpoint returns fallback-aware relation data
`/equipment/infoCodeConfig/listInfoCodeConfigsByObject` SHALL return `RWD_OBJECT_DETAIL` relation info codes (e.g. `submersibleSewagePump` on WSSTOR/WSSTLU detail pages) using the new fallback mechanism configured in the Excel templates.

#### Scenario: Detail page relation info code with new relation data
- **WHEN** the endpoint returns the `submersibleSewagePump` info code for a WSSTOR/WSSTLU equipment and new relation data exists
- **THEN** it SHALL display only the new relation pumps

#### Scenario: Detail page relation info code with only old relation data
- **WHEN** the endpoint returns the `submersibleSewagePump` info code for a WSSTOR/WSSTLU equipment and no new relation data exists
- **THEN** it SHALL fall back to the old relation pumps
