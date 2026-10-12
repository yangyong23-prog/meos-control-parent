# Controller 基础规范

> **Why**: Controller 是 API 的入口点，统一规范便于前端对接。
> **What**: 标准模板、参数校验、校验注解。

---

## 标准 Controller 模板

### CRUD 模板

```java

@RestController
@RequestMapping("/admin/pointConfigs")
@RequiredArgsConstructor
public class PointConfigController {

    private final PointConfigService pointConfigService;

    @GetMapping
    public R<List<PointConfigVO>> list(PointConfigQueryParam param) {
        return R.success(pointConfigService.listByParam(param));
    }

    @GetMapping("/{id}")
    public R<PointConfigVO> get(@PathVariable Long id) {
        return R.success(pointConfigService.getById(id));
    }

    @PostMapping
    public R<Void> add(@RequestBody @Valid PointConfigRequest request) {
        pointConfigService.add(request);
        return R.success();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                          @RequestBody @Valid PointConfigRequest request) {
        pointConfigService.update(id, request);
        return R.success();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        pointConfigService.delete(id);
        return R.success();
    }
}
```

---

## 参数校验

### 为什么必须 @Valid

```java
// ❌ 禁止：没有校验
@PostMapping
public R<Void> add(@RequestBody PointConfigRequest request) {
    // request.name 可能为 null
}

// ✅ 正确：@Valid 触发校验
@PostMapping
public R<Void> add(@RequestBody @Valid PointConfigRequest request) {
    pointConfigService.add(request);
}
```

**原因**: 没有 @Valid，Request 中的校验注解（@NotBlank 等）不会生效。

---

## 校验注解使用

### 标准 Request 模板

```java
public class PointConfigRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotNull(message = "类型不能为空")
    private Integer type;

    @Min(value = 0, message = "值必须大于等于0")
    @Max(value = 100, message = "值必须小于等于100")
    private Integer value;

    @Size(max = 200, message = "描述不能超过200字符")
    private String description;
}
```

### 常用校验注解

| 注解          | 用途       | 示例     |
|-------------|----------|--------|
| `@NotNull`  | 不能为 null | 数值、对象  |
| `@NotBlank` | 不能为空字符串  | 字符串    |
| `@NotEmpty` | 不能为空集合   | 集合、数组  |
| `@Min/@Max` | 数值范围     | 数值字段   |
| `@Size`     | 长度范围     | 字符串、集合 |
| `@Pattern`  | 正则匹配     | 手机号、邮箱 |
| `@Email`    | 邮箱格式     | 邮箱字段   |

---

## DO/DON'T 速查

| DO                            | DON'T                |
|-------------------------------|----------------------|
| `@RequestBody @Valid`         | 无校验注解                |
| `@RequestParam(defaultValue)` | required = true 无默认值 |
| `@PathVariable` 路径参数          | `@RequestParam` 路径参数 |
