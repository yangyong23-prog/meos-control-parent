package com.persagy.apccontrol.basic.service.duty.strategy;

import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ventilation.VentilationEquipStatisticsEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.duty.CardDateVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.ChildCardDataVO;
import com.persagy.apccontrol.basic.service.ventilation.VentilationIotService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.validate.utility.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@Slf4j
public class VentilationHomePageCardDataHandler extends AbstractHomePageCardDataHandler<CardDateVO<ChildCardDataVO>> {

    
    @Resource
    private VentilationIotService ventilationIotService;

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.VENTILATION;
    }

    @Override
    public CardDateVO<ChildCardDataVO> execute(HomePageParam param) {

        CardDateVO<ChildCardDataVO> resultVo = new CardDateVO<>();
        resultVo.setRunningMonitorType(getRunningMonitorType());
        
        Map<String, ObjectIdAndForceCodeQuery> queryMap = new HashMap<>();
        
        Map<VentilationEquipStatisticsEnum, Set<String>> groupId = ventilationIotService.queryEquipGroupId();
        if (MapUtils.isEmpty(groupId)) {
            return resultVo;
        }
        Set<String> objIds = new HashSet<>();
        groupId.forEach((k, v) -> {
            objIds.addAll(v);
            Map<String, ObjectIdAndForceCodeQuery> query =
                    ObjectIdAndForceCodeQuery.buildDutyHomePageQuery(k.getValue() + ":" + k.getReasonPhrase(), v,
                            null);
            queryMap.putAll(query);
        });
        
        //异常事件查询并行化：只需 objIds，不依赖聚合接口结果
        CompletableFuture<Integer> errorEventFuture = CompletableFuture.supplyAsync(
                () -> getExceptionEventCountByObjIds(objIds), BusinessThreadPoolExecutor.ttlExecutor());

        //主线程执行聚合接口（小卡片仍需 subStat）
        DutyHomePageGroupDTO dutyHomePageGroup = queryHomePageCardGroupIotData(queryMap);

        //聚合接口失败短路保护
        if (dutyHomePageGroup == null) {
            return resultVo;
        }

        //等待异常事件查询完成
        CompletableFuture.allOf(errorEventFuture).join();

        //内联构造大卡片标签（绕过父类 buildCardTagVO，只设 errorEventTotal）
        CardTagVO tag = new CardTagVO();
        tag.setErrorEventTotal(errorEventFuture.join());
        resultVo.setCardTag(tag);

        //填充小卡片信息
        //DTP 聚合接口对无统计数据的分组不返回 key，按入参分组补齐，保证小卡片齐全（缺失分组只展示设备总数，其余统计为 0）
        Map<String, DutyHomePageDTO> subStat = new LinkedHashMap<>();
        if (dutyHomePageGroup.getSubStat() != null) {
            subStat.putAll(dutyHomePageGroup.getSubStat());
        }
        queryMap.forEach((k, v) -> subStat.computeIfAbsent(k, key -> {
            DutyHomePageDTO emptyStat = new DutyHomePageDTO();
            emptyStat.setTotal(v.getObjectIds().size());
            return emptyStat;
        }));
        List<ChildCardDataVO> childCardList = buildChildCardDateVO(subStat);
        //默认都能跳转（产品已确认移除 queryCanJump）
//        childCardList.forEach(card -> {
//            if (card != null) {
//                card.setCanJump(true);
//            }
//        });
        //childCardList 排序，规则：按 VentilationEquipStatisticsEnum 枚举声明顺序
        // 1.通风风机-送风机(ACVTSF_SEND_FAN) 2.送风兼消防补风机(FFSCMF_SEND_FAN)
        // 3.通风风机-排风机(ACVTSF_RECEIVE_FAN) 4.排风兼机械排烟风机(FFSCAF_RECEIVE_FAN)
        // 5.普通窗(VENTILATION_FAN_CFCSWN) 6.排烟窗(VENTILATION_FAN_CFCSEW)
        if (!childCardList.isEmpty()) {
            Map<String, Integer> sortIndex = IntStream.range(0, VentilationEquipStatisticsEnum.values().length)
                    .boxed()
                    .collect(Collectors.toMap(i -> VentilationEquipStatisticsEnum.values()[i].getValue(), i -> i));
            childCardList.sort(Comparator.comparingInt(card ->
                    sortIndex.getOrDefault(card.getNameCode(), Integer.MAX_VALUE)));
        }
        resultVo.setChildCardList(childCardList);

        return resultVo;
    }

}
