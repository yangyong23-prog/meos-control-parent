# InfoCodeConfigEvent EventParam Placeholder Param

## Goal

Allow `INFO_CODE_CONFIG_EVENT` SSE subscriptions to pass placeholder parameters through `EventParam` so InfoCode data filling can use caller-provided placeholder values.

## Requirements

* Add a nullable placeholder parameter map to `InfoCodeConfigEvent.EventParam`.
* When building `InfoCodeConfigByObjectQueryParam`, pass the event placeholder map into `queryParam.setPlaceholders(...)`.
* Preserve existing `templateType` / `templateTypes`, object, group, and SSE filtering behavior.

## Acceptance Criteria

* [ ] SSE event param JSON can include `placeholders`.
* [ ] `InfoCodeConfigByObjectQueryParam#getPlaceholders()` receives the SSE `placeholders` map.
* [ ] Existing subscriptions without `placeholders` continue to work.
* [ ] Relevant compile/test check passes where feasible.

## Definition of Done

* Code follows backend specs for DTO/nullability and logging style.
* Quality check is run for the touched backend module where practical.
* No unrelated files are changed.

## Technical Approach

`InfoCodeConfigByObjectQueryParam` already exposes `placeholders`, and `InfoCodeConfigServiceImpl` already forwards non-empty placeholder maps to `BaseInfoCodeAnalyzeStrategy.PLACEHOLDERS`. This task only needs to expose the same field on the SSE event parameter and copy it into the query parameter used by `InfoCodeConfigEvent`.

## Decision

Context: InfoCode placeholder support already exists downstream, but `InfoCodeConfigEvent.EventParam` does not accept it.

Decision: Reuse the existing `Map<String, Object> placeholders` contract from `InfoCodeConfigByObjectQueryParam`.

Consequences: The SSE event stays aligned with the existing InfoCode API contract and does not introduce a second placeholder naming convention.

## Out of Scope

* Changing placeholder replacement logic.
* Adding new placeholder validation rules.
* Changing frontend subscription payload construction.

## Technical Notes

* Target event: `common/src/main/java/com/persagy/apccontrol/basic/sse/strategy/InfoCodeConfigEvent.java`
* Existing downstream contract: `common/src/main/java/com/persagy/apccontrol/basic/model/param/infocodeconfig/InfoCodeConfigByObjectQueryParam.java`
* Service forwarding path: `InfoCodeConfigServiceImpl#queryInfoCodeObjectGroupVo`
