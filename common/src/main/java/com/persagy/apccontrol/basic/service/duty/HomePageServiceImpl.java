package com.persagy.apccontrol.basic.service.duty;


import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.duty.BasicsCardVO;
import com.persagy.apccontrol.basic.service.duty.strategy.HomePageCardDataHandler;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 值班首页业务接口
 */
@Service
@Slf4j
public class HomePageServiceImpl<T extends BasicsCardVO> implements HomePageService<T> {

    private static final String LOG_TAG = "值班首页业务接口";

    @Resource
    private List<HomePageCardDataHandler<T>> homePageCardDataHandler;

    @Override
    public List<T> homePage(HomePageParam param) {
        List<T> list = new ArrayList<>();
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        homePageCardDataHandler.forEach(handler -> {
            if (CollectionUtil.isNotEmpty(param.getSystemList()) &&
                    !param.getSystemList().contains(handler.getRunningMonitorType())) {
                return;
            }
            // 并发执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        list.add(handler.execute(param));
                    },
                    BusinessThreadPoolExecutor.ttlExecutor());
            futures.add(future);
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return list;
    }
}
