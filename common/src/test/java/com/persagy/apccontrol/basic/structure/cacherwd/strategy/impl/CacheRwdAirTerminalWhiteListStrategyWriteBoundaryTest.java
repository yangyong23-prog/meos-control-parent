package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.function.BooleanSupplier;

/**
 * 末端白名单新对象模型写入边界测试。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheRwdAirTerminalWhiteListStrategyWriteBoundaryTest {

    /**
     * 验证白名单策略在写事务前查询完整对象，并把快照和本地回调传给写服务。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldPrepareWhitelistBeforeLocalAtomicCommit() {
        DtpRwdClientService rwdClientService = Mockito.mock(DtpRwdClientService.class);
        CacheObjectModelWriteService writeService =
                Mockito.mock(CacheObjectModelWriteService.class);
        CacheRwdAirTerminalWhiteListStrategy strategy =
                new CacheRwdAirTerminalWhiteListStrategy(
                        Mockito.mock(CacheRwdDomainService.class),
                        Mockito.mock(ShellConfigService.class),
                        Mockito.mock(CacheObjectModelStatusService.class),
                        rwdClientService,
                        Mockito.mock(CacheObjectRepository.class),
                        writeService);
        DtpRwdObjectDTO sourceObject = new DtpRwdObjectDTO();
        sourceObject.setId("space-source");
        Mockito.when(rwdClientService.listObject(
                        Mockito.any(DtpRwdObjectQueryParam.class)))
                .thenReturn(Collections.singletonList(sourceObject));
        Mockito.when(writeService.replaceModelCacheKey(
                        Mockito.anyString(), Mockito.anyCollection(),
                        Mockito.anyCollection(),
                        Mockito.any(BooleanSupplier.class)))
                .thenReturn(true);
        BooleanSupplier afterReplaceAction = () -> true;

        boolean result = strategy.replaceWhitelist(
                "whitelist-key", Collections.singletonList("configured-space"),
                afterReplaceAction);

        Assertions.assertTrue(result);
        InOrder inOrder = Mockito.inOrder(rwdClientService, writeService);
        inOrder.verify(rwdClientService).listObject(
                Mockito.any(DtpRwdObjectQueryParam.class));
        inOrder.verify(writeService).replaceModelCacheKey(
                "whitelist-key", Collections.singletonList(sourceObject),
                Collections.emptyList(), afterReplaceAction);
    }
}
