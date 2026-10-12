## ADDED Requirements

### Requirement: RWD_OBJECT_DETAIL supports fallback query parameters
The `RWD_OBJECT_DETAIL` info code type SHALL support an optional list of fallback query parameters in its take-value logic model.

#### Scenario: Configuration with fallback query parameters
- **WHEN** a `RWD_OBJECT_DETAIL` config includes `fallbackQueryParams`
- **THEN** the system SHALL persist and deserialize the fallback parameters alongside the primary `queryParam`

### Requirement: Fallback executes only when primary query returns no results
The system SHALL execute the primary `queryParam` first. If the primary query returns one or more objects, the system SHALL use only the primary result. If the primary query returns no objects, the system SHALL try each fallback query parameter in order until one returns objects or all fallbacks are exhausted.

#### Scenario: Primary query has results
- **WHEN** the primary query returns objects
- **THEN** the system SHALL return only the primary query objects and SHALL NOT execute any fallback query

#### Scenario: Primary query is empty and fallback has results
- **WHEN** the primary query returns no objects and the first fallback query returns objects
- **THEN** the system SHALL return the first fallback query objects

#### Scenario: All queries are empty
- **WHEN** the primary query and all fallback queries return no objects
- **THEN** the system SHALL return an empty result set

### Requirement: Backward compatibility for configurations without fallback
If a `RWD_OBJECT_DETAIL` config does not include `fallbackQueryParams`, the system SHALL behave exactly as before the fallback feature was introduced.

#### Scenario: Config without fallback
- **WHEN** a `RWD_OBJECT_DETAIL` config has only `queryParam`
- **THEN** the system SHALL execute only the primary query and return its results

### Requirement: Import validation rejects invalid fallback parameters
The Excel import process SHALL validate each fallback query parameter. A fallback query parameter MUST contain at least one valid query dimension (for example `classCode`, `id`, or a relation parameter). Invalid fallback parameters SHALL cause the import to fail with a clear error message.

#### Scenario: Valid fallback parameters
- **WHEN** all `fallbackQueryParams` entries contain valid query conditions
- **THEN** the import SHALL succeed

#### Scenario: Empty fallback parameter
- **WHEN** a `fallbackQueryParams` entry contains no valid query conditions
- **THEN** the import SHALL fail and log the invalid row
