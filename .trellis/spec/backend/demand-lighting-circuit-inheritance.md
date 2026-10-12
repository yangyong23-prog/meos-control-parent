# Demand Lighting Circuit Inheritance

> **Rule**: A newly delivered circuit (present in delivery data but absent from the saved config) inherits only the ON/OFF semantics of its function type. `FEEDBACK_ADJUSTMENT` is never inherited — a new circuit must be `NOT_CONFIGURED` until a human supplies its circuit-level feedback conditions.

## Background

- Since 7.260801.0, illuminance feedback conditions are configured per circuit (`feedbackAdjustSetting` on the circuit value object), not per space.
- A circuit running `FEEDBACK_ADJUSTMENT` without circuit-level conditions is dirty data: the edge side has no thresholds to evaluate or push.
- Product decision (confirmed 2026-08-04): after a config is saved, any circuit added later by delivery defaults to **not configured**.

## Decision Point

`DemandLightingCircuitSettingModeEnum.getSettingMode(parentSettingMode, circuitSettingMode)` is the single decision point for new-circuit inheritance. Inheritance applies only when `circuitSettingMode == null` (circuit not in saved config):

| Parent function-type mode | New circuit result |
|---|---|
| ALL_OPEN | OPEN |
| ALL_CLOSE | CLOSE |
| PARTIAL_OPEN | NOT_CONFIGURED |
| NOT_CONFIGURED | NOT_CONFIGURED |
| FEEDBACK_ADJUSTMENT | **NOT_CONFIGURED** (never inherited) |

A saved circuit always keeps its own stored mode via the first branch; inheritance must never override it.

## Application Points

- All dynamic circuit backfill paths (preview assembly, edit-page VO building, intervention compute merge) must resolve new circuits through `getSettingMode(...)`; do not hand-roll inheritance.
- `DemandLightingStrategyImpl.configBuild()` treats feedback-adjustment function types like any other: saved circuits are filtered and kept with their conditions; new circuits are appended as `NOT_CONFIGURED`. Do not reintroduce a special branch that clears and rebuilds `circuitSettings` — that drops saved circuit-level conditions.
- DTX publish-change handling stays subtract-only (remove deleted circuits, clean invalid configs). New circuits are never persisted by DTX handling; they appear through query-time backfill as `NOT_CONFIGURED`.
- An unconfigured circuit cancels its config source in result computation and must not participate in feedback-adjustment dispatch.

## Regression Anchor

`DemandLightingCircuitSettingModeEnumTest` pins the inheritance matrix. Update the test and this spec together if the matrix ever changes intentionally.
