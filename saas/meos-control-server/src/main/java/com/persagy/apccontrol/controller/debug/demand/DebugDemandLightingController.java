package com.persagy.apccontrol.controller.debug.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.base.CacheDynamicKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.message.dtx.DtxPublishMessageEvent;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandConfigLightingComputeParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingComputeNowService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.lighitng.listener.LightingDtxPublishChangedListener;
import com.persagy.apccontrol.task.DemandConfigLightingComputeTaskHandler;
import com.persagy.apccontrol.task.JobTask;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

/**
 * 照明需求调试控制器
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@RestController
@RequestMapping(value = "/debug/demand/lighting")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DebugDemandLightingController {

    private final DemandLightingStrategy demandLightingStrategy;
    private final RedisCacheTemplate redisCacheTemplate;
    private final JobTask jobTask;
    private final DemandConfigLightingComputeTaskHandler demandConfigLightingComputeTaskHandler;
    private final DemandConfigLightingComputeNowService demandConfigLightingComputeNowService;

    @GetMapping("/dtxPublish")
    public R<Object> dtxPublish(
            @RequestParam(required = false) Boolean clearRwdCache,
            @RequestParam(required = false) Boolean updateDatabase,
            @RequestParam(required = false) Boolean computeResult) {
        DtxPublishMessageEvent.Message message = new DtxPublishMessageEvent.Message();
        message.setGroupCode(ContextUtil.getGroupCode());
        message.setProjectId(ContextUtil.getProjectId());
        LightingDtxPublishChangedListener listener = (LightingDtxPublishChangedListener) AopProxyUtils
                .getSingletonTarget(SpringUtil.getBean("lightingDtxPublishChangedListener"));
        if (clearRwdCache) {
            redisCacheTemplate.fuzzyMatchingDel(CommonConst.APPLICATION_NAME
                    + CacheKeyPrefix.SEPARATOR
                    + CacheDynamicKey.DYNAMIC_KEY_PREFIX
                    + "rwd");
            redisCacheTemplate.fuzzyMatchingDel(RedisCacheTemplate.BASE_CACHE_KEY_PREFIX);
        }
        return R.success(listener.execute(new DtxPublishMessageEvent(message),
                BooleanUtils.isTrue(updateDatabase), BooleanUtils.isTrue(computeResult)));
    }

    @GetMapping("/listTemplateType")
    public R<Object> listTemplateType(@RequestBody @Valid DemandTemplateTypeQueryParam param) {
        ContextUtil.setProjectId(param.getProjectId());
        return R.success(demandLightingStrategy.listTemplateTypes());
    }

    @GetMapping("/listCircuits")
    public R<Object> listCircuits(
            @RequestParam String projectId,
            @RequestParam DemandTemplateTypeEnum templateType,
            @RequestParam(required = false) Collection<String> circuitIds) {
        return R.success(demandLightingStrategy.listCircuits(projectId, templateType, circuitIds));
    }
    
    
    //重算全项目回路结果
    @PostMapping("/recalculationCircuitResult")
    public R<Object> recalculationCircuitResult(@RequestBody DemandConfigLightingComputeParam param) {
        //指定参数直接运行
        if (param != null && param.getTemplateId() != null) {
            demandConfigLightingComputeNowService.resultCompute(param);
            return R.success(true);
        }
        
        //传递了集团编码和项目 ID 只执行指定项目
        if (StringUtils.isNotBlank(ContextUtil.getGroupCode()) && StringUtils.isNotBlank(ContextUtil.getProjectId())) {
            ProjectDTO projectDTO = new ProjectDTO();
            projectDTO.setProjectId(ContextUtil.getProjectId());
            demandConfigLightingComputeTaskHandler.processAsync(ContextUtil.getGroupCode(), projectDTO);
        } else {
            jobTask.demandConfigLightingComputeTaskHandler();
        }
        return R.success(true);
    }
    
    //重算全项目回路结果
    @PostMapping("/recalculationCircuitResultQueryCfg")
    public R<Object> queryCfg(@RequestBody DemandConfigLightingComputeParam param) {
        return R.success(demandConfigLightingComputeNowService.queryCfg(param));
    }
}
