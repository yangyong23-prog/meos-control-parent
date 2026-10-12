package com.persagy.apccontrol.common.saas.bizcheck.notify.render;

import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 渲染器注册中心：启动时收集所有 {@link NotifyTextRenderer} 实现，按 supports() 索引。
 *
 * @since 7.260601.0
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NotifyTextRendererRegistry {

    private final ApplicationContext applicationContext;

    private Map<BizCheckStrategyTypeEnum, NotifyTextRenderer> rendererMap;

    @PostConstruct
    public void init() {
        Map<String, NotifyTextRenderer> beans = applicationContext.getBeansOfType(NotifyTextRenderer.class);
        rendererMap = new HashMap<>();
        beans.values().forEach(r -> rendererMap.put(r.supports(), r));
        log.info("BizCheck通知渲染器注册完成, 数量={}", rendererMap.size());
    }

    /**
     * 按检查项类型路由到对应渲染器
     *
     * @param type 检查项类型
     * @return 渲染器，若无则返回 null
     */
    public NotifyTextRenderer dispatch(BizCheckStrategyTypeEnum type) {
        return rendererMap.get(type);
    }
}
