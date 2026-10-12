package com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle;

import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeAggregateInitContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 信息点聚合根生命周期感知接口
 * <p>
 * 该接口定义了信息点聚合根在初始化、数据填充等各个生命周期阶段的回调方法，
 * 采用责任链模式，允许多个实现类按照优先级顺序依次处理。
 * </p>
 *
 * <h3>生命周期阶段</h3>
 * <ol>
 *   <li><b>上下文生命周期</b>：处理模板类型、信息点配置、信息点分组等基础数据</li>
 *   <li><b>聚合根生命周期</b>：处理上下文初始化前后、数据库数据处理、聚合根初始化等阶段</li>
 *   <li><b>填充信息点定义模型</b>：处理信息点定义模型填充前后的逻辑</li>
 *   <li><b>填充信息值模型</b>：处理信息点数据模型填充前后的逻辑</li>
 * </ol>
 *
 * <h3>执行顺序</h3>
 * <pre>
 * 1. handleTemplateTypes()       - 处理模板类型
 * 2. handleInfoCodeConfigs()     - 处理信息点配置
 * 3. handleInfoCodeGroups()      - 处理信息点分组配置
 * 4. beforeContextInit()         - 上下文初始化前
 * 5. handleDatabaseData()        - 数据库数据处理
 * 6. afterContextInit()          - 上下文初始化后
 * 7. afterAggregateInit()        - 聚合根初始化后
 * 8. beforeFillInfoCodeDefineModel() - 填充信息点定义模型前
 * 9. afterFillInfoCodeDefineModel()  - 填充信息点定义模型后
 * 10. beforeFillInfoCodeDataModel()  - 填充信息点数据模型前
 * 11. afterFillInfoCodeDataModel()   - 填充信息点数据模型后
 * </pre>
 *
 * <h3>使用方式</h3>
 * <p>
 * 实现此接口并注册为 Spring Bean，通过实现 {@link Ordered#getOrder()} 方法指定执行优先级，
 * 数字越小优先级越高，越先执行。
 * </p>
 * <p>
 * <b>关于作用域选择：</b>默认使用 {@code @Scope("singleton")} 单例作用域即可。
 * 仅在需要使用上下文链（即需要在不同生命周期方法之间共享实例状态）时，才使用 {@code @Scope("prototype")} 原型作用域。
 * 使用 prototype 作用域会在每次请求时创建新实例，虽然能够避免多线程环境下的状态污染，但会增加 Bean 创建的性能开销。
 * </p>
 * <p>
 * 在一次完整的信息点聚合根创建流程中，会获取所有的 {@code InfoCodeAggregateLifeAware} 实现类实例，
 * 并在整个执行过程中使用同一批实例对象。如果使用 prototype 作用域，同一个处理器实例可以安全地在不同的生命周期方法之间共享状态；
 * 如果使用 singleton 作用域，则应避免使用实例变量存储状态，以防止多线程并发问题。
 * </p>
 *
 * <h3>示例一：无状态处理器（推荐，使用单例）</h3>
 * <pre>{@code
 * @Component // 默认 singleton 作用域
 * public class StatelessInfoCodeHandler implements InfoCodeAggregateLifeAware {
 *
 *     @Autowired
 *     private SomeService someService;
 *
 *     @Override
 *     public void afterAggregateInit(@NonNull List<InfoCodeAggregate> aggregates) {
 *         // 无状态处理，直接使用方法参数和注入的服务
 *         aggregates.forEach(aggregate -> {
 *             someService.process(aggregate);
 *         });
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 100;
 *     }
 * }
 * }</pre>
 *
 * <h3>示例二：有状态处理器（需要上下文链时使用）</h3>
 * <pre>{@code
 * @Component
 * @Scope("prototype") // 需要在多个生命周期方法间共享状态时使用
 * public class StatefulInfoCodeHandler implements InfoCodeAggregateLifeAware {
 *
 *     // 使用实例变量在整个处理流程中共享状态
 *     private Map<String, Object> processingContext = new HashMap<>();
 *
 *     @Override
 *     public void beforeContextInit(@NonNull InfoCodeInitByObjectParam param) {
 *         // 在上下文初始化前记录状态
 *         processingContext.put("startTime", System.currentTimeMillis());
 *     }
 *
 *     @Override
 *     public void afterAggregateInit(@NonNull List<InfoCodeAggregate> aggregates) {
 *         // 访问之前方法中存储的状态
 *         long duration = System.currentTimeMillis() -
 *             (Long) processingContext.get("startTime");
 *         log.info("处理耗时: {}ms", duration);
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 100;
 *     }
 * }
 * }</pre>
 *
 * @author JiYinchuan
 * @see InfoCodeAggregate
 * @see InfoCodeAggregateInitContext
 * @see InfoCodeInitByObjectParam
 * @see Ordered
 * @since 5.240801.0
 */
public interface InfoCodeAggregateLifeAware extends Ordered {

    // ------------------------- 上下文生命周期 -------------------------

    /**
     * 处理模板类型
     *
     * @param param              请求参数
     * @param templateTypeModels 模板类型
     * @since 7.251202.0
     */
    default Collection<InfoCodeTemplateTypeModel> handleTemplateTypes(
            @NonNull InfoCodeInitByObjectParam param,
            @Nullable Collection<InfoCodeTemplateTypeModel> templateTypeModels) {
        return templateTypeModels;
    }

    /**
     * 处理信息点配置
     *
     * @param param           请求参数
     * @param infoCodeConfigs 信息点配置
     * @since 7.251202.0
     */
    default List<InfoCodeConfig> handleInfoCodeConfigs(
            @NonNull InfoCodeInitByObjectParam param,
            @Nullable List<InfoCodeConfig> infoCodeConfigs) {
        return infoCodeConfigs;
    }

    /**
     * 处理信息点分组配置
     *
     * @param param          请求参数
     * @param infoCodeGroups 信息点分组配置
     * @since 7.251202.0
     */
    default List<InfoCodeGroup> handleInfoCodeGroups(
            @NonNull InfoCodeInitByObjectParam param,
            @Nullable List<InfoCodeGroup> infoCodeGroups) {
        return infoCodeGroups;
    }

    // ------------------------- 聚合根生命周期 -------------------------

    /**
     * 上下文初始化前处理
     *
     * @param param 初始化参数
     * @since 5.240801.0
     */
    default void beforeContextInit(@NonNull InfoCodeInitByObjectParam param) {
    }

    /**
     * 数据库数据处理
     *
     * @param context 上下文
     * @since 7.251202.0
     */
    default void handleDatabaseData(@NonNull InfoCodeAggregateInitContext context) {
    }

    /**
     * 上下文初始化后处理
     *
     * @param context 上下文
     * @since 5.240801.0
     */
    default void afterContextInit(@NonNull InfoCodeAggregateInitContext context) {
    }

    /**
     * 聚合根初始化后处理
     *
     * @param aggregates 聚合根
     * @since 5.240801.0
     */
    default void afterAggregateInit(@NonNull List<InfoCodeAggregate> aggregates) {
    }

    // ------------------------- 填充信息点定义模型 -------------------------

    /**
     * 填充信息点定义模型前处理
     *
     * @param aggregates 聚合根
     * @since 5.240801.0
     */
    default void beforeFillInfoCodeDefineModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
    }

    /**
     * 填充信息点定义模型后处理
     *
     * @param aggregates 聚合根
     * @since 5.240801.0
     */
    default void afterFillInfoCodeDefineModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
    }

    // ------------------------- 填充信息值模型 -------------------------

    /**
     * 填充信息点定值模型前处理
     *
     * @param aggregates 聚合根
     * @since 5.240801.0
     */
    default void beforeFillInfoCodeDataModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
    }

    /**
     * 填充信息点定义值模型后处理
     *
     * @param aggregates 聚合根
     * @since 5.240801.0
     */
    default void afterFillInfoCodeDataModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
    }
}