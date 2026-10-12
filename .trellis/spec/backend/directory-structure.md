# Directory Structure

> **Rule**: All classes MUST be in designated layer packages. Business subpackages are allowed and
> required when a feature would otherwise flatten several related classes into a shared root package;
> do not invent new architectural layers.

---

## Directory Layout

```
meos-control-parent/
├── common/basic/              # Shared (cloud + edge)
├── saas/                      # Cloud deployment
│   └── meos-control-server/   # Cloud entry
└── local/                     # Edge deployment
    └── meos-control-server-edge/  # Edge entry
```

---

## Package Structure

| Layer | Package | Example |
|-------|---------|---------|
| Controller | `controller/{business}/` | `controller/user/UserController.java` |
| Service | `service/{business}/impl/` | `service/user/impl/UserServiceImpl.java` |
| Mapper | `mapper/{business}/` | `mapper/user/UserMapper.java` |
| Entity | `model/entity/` | `model/entity/UserDO.java` |
| DTO | `model/dto/` | `model/dto/UserDTO.java` |
| VO | `model/vo/` | `model/vo/UserVO.java` |
| Param | `model/param/{business}/` | `model/param/user/UserQueryParam.java` |
| Convert | `convert/` | `convert/UserConvert.java` |
| Scheduled/XXL task | `task/{business}/` | `task/cacheobjectmodel/CacheObjectModelInitTaskHandler.java` |

---

## Task Packaging

- Keep shared task infrastructure such as `BaseXXJob`, generic handler contracts, and the central
  XXL-JOB registry directly under `task/`.
- Put three or more classes belonging to one task feature under `task/{business}/`; do not flatten
  feature-specific handlers, services, reports, and enums into the shared `task` root.
- Put feature-specific task parameters under `task/{business}/param/`.
- Test packages must mirror production packages so package-private contracts and file navigation stay
  predictable.

```text
# Wrong: feature classes pollute the shared task root
task/CacheObjectModelInitTaskHandler.java
task/CacheObjectModelStatusRestoreTask.java
task/CacheObjectModelTaskSupport.java

# Correct: shared infrastructure remains at root, feature classes are grouped
task/BaseXXJob.java
task/cacheobjectmodel/CacheObjectModelInitTaskHandler.java
task/cacheobjectmodel/CacheObjectModelStatusRestoreTask.java
task/cacheobjectmodel/param/CacheObjectModelTaskParam.java
```

---

## ❌ DO NOT CREATE

`handler/`、`po/`、`page/`

---

## Naming Suffix

| Type | Suffix | Example |
|------|--------|---------|
| Request | `Request` | `UserRequest` |
| Response | `VO` | `UserVO` |
| Transfer | `DTO` | `UserDTO` |
| Query Param | `Param` | `UserQueryParam` |
| Entity | `DO` | `UserDO` |
