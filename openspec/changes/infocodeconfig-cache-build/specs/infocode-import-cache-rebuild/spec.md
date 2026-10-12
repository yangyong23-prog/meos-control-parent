## ADDED Requirements

### Requirement: Cache rebuild triggered after import

The system SHALL automatically trigger cache rebuild for all groups and projects after InfoCode configuration import
completes.

#### Scenario: Successful cache rebuild after import

- **WHEN** user imports InfoCode configuration via `importTableSheet` API
- **THEN** system SHALL rebuild cache for all groups and projects
- **AND** system SHALL wait for all cache rebuild operations to complete before returning
- **AND** system SHALL return OK status if all operations succeed

#### Scenario: Partial cache rebuild failure

- **WHEN** cache rebuild fails for some projects during import
- **THEN** system SHALL continue rebuilding cache for remaining projects
- **AND** system SHALL return failed project information in response
- **AND** system SHALL NOT fail the entire import operation

### Requirement: Event publishing controlled by parameter

The system SHALL support conditional event publishing in `InfoCodeTemplateTypeResource.add()` and `del()` methods.

#### Scenario: Add with event publishing enabled

- **WHEN** `add(config, true)` is called
- **THEN** system SHALL save the entity
- **AND** system SHALL publish `InfoCodeTemplateTypeSaveEvent`

#### Scenario: Add with event publishing disabled

- **WHEN** `add(config, false)` is called
- **THEN** system SHALL save the entity
- **AND** system SHALL NOT publish any event

#### Scenario: Delete with event publishing enabled

- **WHEN** `del(param, true)` is called
- **THEN** system SHALL delete the entities
- **AND** system SHALL publish `InfoCodeTemplateTypeRemoveEvent`

#### Scenario: Delete with event publishing disabled

- **WHEN** `del(param, false)` is called
- **THEN** system SHALL delete the entities
- **AND** system SHALL NOT publish any event

### Requirement: Cache rebuild executes across all groups

The system SHALL execute cache rebuild across all groups in parallel, with projects within each group executed
sequentially.

#### Scenario: Group parallel execution

- **WHEN** cache rebuild is triggered
- **THEN** system SHALL execute cache rebuild for different groups in parallel

#### Scenario: Project serial execution within group

- **WHEN** cache rebuild executes for a group
- **THEN** system SHALL execute cache rebuild for each project sequentially
- **AND** system SHALL set correct `groupCode` and `projectId` in context before each execution

### Requirement: Rebuild by query parameter

The system SHALL provide a method to rebuild cache based on query parameters.

#### Scenario: Rebuild with valid parameters

- **WHEN** `rebuildByParam(param)` is called with valid `InfoCodeTemplateTypeQueryDaoParam`
- **THEN** system SHALL query matching template types
- **AND** system SHALL delete old cache entries
- **AND** system SHALL add new cache entries
- **AND** system SHALL trigger event chain for downstream cache updates

#### Scenario: Rebuild with no matching templates

- **WHEN** `rebuildByParam(param)` finds no matching template types
- **THEN** system SHALL return without error
- **AND** system SHALL NOT modify any cache data
