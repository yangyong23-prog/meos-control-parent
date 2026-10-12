# InfoCode RWD Take Value

## Scenario: Composite source key binding

### 1. Scope / Trigger

- Trigger: updating `InfoCodeRwdObjectDetailByTemplateContext` source matching, root-object binding, or relation-based RWD queries.
- Scope: RWD object detail take-value configs that use `source`, `queryParam`, `takeValueExpression`, `sourceValueExpression`, `relationFrom`, or `relationTo`.

### 2. Signatures

- Main context: `InfoCodeRwdObjectDetailByTemplateContext#fillSourceMap()`
- Main context: `InfoCodeRwdObjectDetailByTemplateContext#fillObjects()`
- Binding output: `rootObjectIdAndSource: Map<rootObjectId, Map<sourceKey, Set<Object>>>`

### 3. Contracts

- Direct placeholder references such as `${sourceKey}` must keep priority when matching source keys.
- If direct placeholder matching finds no keys, SpEL root references may be detected from `#root['sourceKey']`, `#root["sourceKey"]`, or `#root.sourceKey`.
- When `${objectId}` is the source key for a relation query, `rootObjectIdAndSource` stores the returned object's `id`, not the value from `takeValueExpression`.
- `takeValueExpression` still controls the value written to `sourceMap`; do not reuse that value as the relation matching id unless the expression is explicitly `id`.

### 4. Validation & Error Matrix

- Direct `${sourceKey}` exists -> use the existing direct-placeholder behavior.
- Only SpEL root references exist -> detect matching source keys from `#root`.
- Relation query returns matching `relObjIds` -> bind the source object only to matched root object ids.
- Relation query has no matching `relObjIds` -> fall back to the existing broadcast behavior.

### 5. Good/Base/Bad Cases

- Good: `${#unionCollection(#root['A'], #root['B'])}` detects both `A` and `B` when they exist in `sourceMap`.
- Base: `${A}` keeps the old literal placeholder path and does not depend on SpEL parsing.
- Bad: storing `localName` from `takeValueExpression` in `rootObjectIdAndSource` for an objectId relation source; downstream `relObjIds` matching expects ids.

### 6. Tests Required

- Assert composite SpEL root extraction for map and property forms.
- Assert normal placeholders are not treated as SpEL root references.
- Assert relation source binding writes only matched root object ids.
- Assert no mutation happens when relation ids do not match root object ids.

### 7. Wrong vs Correct

#### Wrong

```java
MapExecutor.dynamicPut(rootObjectIdAndSource, rootObjectId, key,
        tempValue, HashMap::new, LinkedHashSet::new);
```

#### Correct

```java
MapExecutor.dynamicPut(rootObjectIdAndSource, rootObjectId, key,
        objectId, HashMap::new, LinkedHashSet::new);
```
