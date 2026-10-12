## MODIFIED Requirements

### Requirement: Import returns cache rebuild result

The system SHALL extend the return value of `importTableSheet` API to include cache rebuild results.

#### Scenario: All files and cache rebuild succeed

- **WHEN** user imports files and all cache rebuild operations succeed
- **THEN** system SHALL return `{"file1.xlsx": ["OK"], "file2.xlsx": ["OK"]}`

#### Scenario: Some files fail validation

- **WHEN** some files fail validation during import
- **THEN** system SHALL return error messages for failed files
- **AND** system SHALL still attempt cache rebuild for successfully imported files

#### Scenario: Cache rebuild partially fails

- **WHEN** cache rebuild fails for some projects
- **THEN** system SHALL return `_cacheRebuildFailures` key with failed project details
- **AND** format SHALL be `{"groupCode:projectId": "errorMessage"}`
