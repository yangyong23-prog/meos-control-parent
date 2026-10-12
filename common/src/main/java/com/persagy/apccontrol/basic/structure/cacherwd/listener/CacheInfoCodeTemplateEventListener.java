package com.persagy.apccontrol.basic.structure.cacherwd.listener;

import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.infocodeconfig.envet.InfoCodeTemplateTypeRemoveEvent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.envet.InfoCodeTemplateTypeSaveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

/**
 * 缓存信息点模版事件监听器。
 *
 * <p>只在源信息点模板事务提交后接收一次领域事件；进入缓存模块后使用直接调用完成
 * 模板元数据和对象模型维护，不再继续发布缓存内部事件。</p>
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Slf4j
@Component
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheInfoCodeTemplateEventListener {

    /**
     * 模板元数据缓存仓储。
     *
     * @since 7.260302.0
     */
    private final CacheInfoCodeTemplateRepository cacheInfoCodeTemplateRepository;

    /**
     * 监听信息点模版类型保存事件
     *
     * @param event 信息点模版类型保存事件
     * @since 7.260302.0
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInfoCodeTemplateTypeSave(InfoCodeTemplateTypeSaveEvent event) {
        Long templateTypeId = (Long) event.getSource();
        this.cacheInfoCodeTemplateRepository.add(templateTypeId);
    }

    /**
     * 监听信息点模版类型移除事件
     *
     * @param event 信息点模版类型移除事件
     * @since 7.260302.0
     */
    @SuppressWarnings("unchecked")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void onInfoCodeTemplateTypeRemove(InfoCodeTemplateTypeRemoveEvent event) {
        Collection<Long> toBeDelTemplateTypeIds = (Collection<Long>) event.getSource();
        if (CollectionUtils.isEmpty(toBeDelTemplateTypeIds)) {
            return;
        }
        this.cacheInfoCodeTemplateRepository.del(toBeDelTemplateTypeIds);
    }
}

