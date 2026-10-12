package com.persagy.apccontrol.basic.controller;

import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.RedisCacheExpUtil;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.DebugBeanParam;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeInfoCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.translation.TranslationKit;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 调试控制器
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
public class DebugController {

    private final RedisCacheUtil redisCacheUtil;
    private final RedisCacheExpUtil redisCacheExpUtil;
    private final AbstractIotDataCacheServer iotDataCacheService;
    private final ProjectProperties projectProperties;
    private final DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;
    private final RedisCacheTemplate redisCacheTemplate;
    private final com.persagy.apccontrol.basic.outside.feign.service.WeatherServerService weatherServerService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;

    @Qualifier("CaffeineCacheManager")
    @Autowired
    private CacheManager caffeineCacheManager;

    @PostMapping("/beanDebug")
    public R<Object> beanDebug(@RequestBody DebugBeanParam param) {
        return R.success(param.process());
    }

    @PostMapping("/properties")
    public ProjectProperties properties() {
        return projectProperties;
    }

    @GetMapping("/translate")
    public R<Object> translationSql(@RequestParam String text) {
        return R.success(TranslationKit.BAIDU.translate(text));
    }

    @Operation(summary = "删除Redis缓存", tags = {VersionConstant.V5_6_0})
    @PostMapping("/deleteRedisCacheInfo")
    public R<Object> deleteRedisCacheInfo(@RequestBody Map<String, String> deleteRedisCacheInfo) {
        String type = deleteRedisCacheInfo.get("type");
        String key = deleteRedisCacheInfo.get("key");
        if ("string".equals(type)) {
            this.redisCacheUtil.del(key);
        } else if ("like".equals(type)) {
            return R.success(this.redisCacheExpUtil.scanKeys(key));
        }
        return R.success(key);
    }

    @Operation(summary = "获取REDIS缓存信息", tags = {VersionConstant.V5_6_0})
    @GetMapping("/queryRedisCacheInfo")
    public R<Object> queryRedisCacheInfo(@RequestParam Map<String, String> redisCacheDTO) {
        Object result = null;
        if ("run".equals(redisCacheDTO.get("type"))) {
            result = this.iotDataCacheService.queryIotRealDataVO(redisCacheDTO.get("groupCode"), redisCacheDTO.get("projectId"), redisCacheDTO.get("objectId"), redisCacheDTO.get("classCode"), redisCacheDTO.get("infoCode"));
        } else if ("control".equals(redisCacheDTO.get("type"))) {
            result = this.iotDataCacheService.queryIotCtrlDataVO(redisCacheDTO.get("groupCode"), redisCacheDTO.get("projectId"), redisCacheDTO.get("objectId"), redisCacheDTO.get("classCode"), redisCacheDTO.get("infoCode"));
        } else if ("string".equals(redisCacheDTO.get("type"))) {
            result = this.redisCacheUtil.get(redisCacheDTO.get("key"));
        } else if ("hash".equals(redisCacheDTO.get("type"))) {
            result = StrUtil.isBlank(redisCacheDTO.get("hKey")) ? this.redisCacheUtil.hGetAll(redisCacheDTO.get("key")) : this.redisCacheUtil.hGet(redisCacheDTO.get("key"), redisCacheDTO.get("hKey"));
        }
        return R.success(result == null ? new Object() : result);
    }

    @Operation(summary = "获取运行监测所有的本地缓存信息", tags = {VersionConstant.V5_6_0})
    @GetMapping("/queryAllLocalCacheInfo")
    public R<Map<String, Object>> queryAllLocalCacheInfo() {
        Map<String, Object> localCache = new HashMap<>();
        localCache.put("SYSTEM_INFO", LocalCacheConstant.SYSTEM_INFO);
        localCache.put("WEBSOCKET_CONFIG", LocalCacheConstant.WEBSOCKET_CONFIG);
        localCache.put("CUSTOM_FUNCTION", LocalCacheConstant.CUSTOM_FUNCTION);
        localCache.put("SYSTEM_RUN_PARAM_MAP", LocalCacheConstant.SYSTEM_RUN_PARAM_MAP);
        return R.success(localCache);
    }

    @Operation(summary = "获取Caffeine本地缓存信息", tags = {VersionConstant.V5_6_0})
    @GetMapping("/queryCaffeineCacheInfo")
    public R<Map<String, Object>> queryCaffeineCacheInfo(
            @RequestParam(required = false) String cacheName,
            @RequestParam(required = false) String key) {
        Map<String, Object> result = new HashMap<>();

        // 获取所有缓存名称
        Collection<String> allCacheNames = caffeineCacheManager.getCacheNames();
        result.put("totalCacheCount", allCacheNames.size());
        result.put("cacheNames", allCacheNames);

        Map<String, Object> cacheDetails = new LinkedHashMap<>();

        // 确定要查询的缓存列表
        Collection<String> targetCacheNames;
        if (StrUtil.isNotBlank(cacheName)) {
            // 传了cacheName参数，只查询指定的缓存
            targetCacheNames = Collections.singletonList(cacheName);
        } else {
            // 没传参数，查询所有缓存
            targetCacheNames = allCacheNames;
        }

        // 遍历查询缓存信息
        for (String name : targetCacheNames) {
            Cache cache = caffeineCacheManager.getCache(name);
            if (cache == null) {
                if (StrUtil.isNotBlank(cacheName)) {
                    cacheDetails.put(name, "缓存不存在");
                }
                continue;
            }

            Map<String, Object> cacheInfo = new LinkedHashMap<>();
            cacheInfo.put("cacheName", cache.getName());

            // 获取底层Caffeine缓存
            Object nativeCache = cache.getNativeCache();
            cacheInfo.put("nativeCacheType", nativeCache.getClass().getSimpleName());

            if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache =
                        (com.github.benmanes.caffeine.cache.Cache<Object, Object>) nativeCache;
                cacheInfo.put("estimatedSize", caffeineCache.estimatedSize());
                cacheInfo.put("stats", caffeineCache.stats().toString());
            }

            // 如果传了key参数，查询具体的缓存值
            if (StrUtil.isNotBlank(key)) {
                try {
                    Object value = cache.get(key);
                    cacheInfo.put("searchKey", key);
                    cacheInfo.put("value", value != null ? value : "缓存中不存在该key");
                } catch (Exception e) {
                    cacheInfo.put("searchKey", key);
                    cacheInfo.put("error", "查询失败: " + e.getMessage());
                }
            }

            cacheDetails.put(name, cacheInfo);
        }

        result.put("cacheDetails", cacheDetails);
        return R.success(result);
    }

    @PostMapping("/testListHasConfigSpaceIds")
    public Set<String> testListHasConfigSpaceIds(BaseDemandTemplateParam param) {
        return demandConfigGateway.listHasConfigSpaceIds(param, null);
    }

    @PostMapping("/delRwdCache")
    public void delRwdCache() {
        redisCacheTemplate.dataUpdatingDelCache();
    }

    @GetMapping("/test")
    public void test() {
        /*
        System.out.println(DateUtils.getLocalDateNow());
        System.out.println(DateUtils.getLocalDateTomorrow());
        System.out.println(DateUtils.get24WithHourDay());
        System.out.println(DateUtils.currentTimeOneHourAgo());
        System.out.println(DateUtils.getPastTime(3));
        System.out.println(DateUtils.getLastTime(3));
        System.out.println(DateUtils.getTheLastPrecisionMinutes(5));
        //System.out.println(weatherServerService.sunRiseAndSet(ContextUtil.getProjectId(), "2025-10-11", "2025-10-17"));
        System.out.println(DateUtils.getDateNow());

         */

        DtpKnowledgeInfoCodeByTagQueryParam param = new DtpKnowledgeInfoCodeByTagQueryParam();
        param.setClassCode(Arrays.asList("ACAC"));
        DtpKnowledgeInfoCodeByTagQueryParam.TagParam tagParam = new DtpKnowledgeInfoCodeByTagQueryParam.TagParam();
        tagParam.setParamType(0);
        tagParam.setTypeCode("x1stTag");
        tagParam.setTagCode(Arrays.asList("5"));
        param.setTagParams(Arrays.asList(tagParam));
        dtpKnowledgeClientService.listInfoByTag(param);
        System.out.println(DateUtils.now());
        DateUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
