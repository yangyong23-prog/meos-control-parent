package com.persagy.apccontrol.basic.opt;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.BaseQueryParamDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryEqParamDTO;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.outside.feign.wrapper.DtpRwdClientPageWrapper;
import com.persagy.apccontrol.basic.utils.DMPResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * opt 分支 B15（switch.opt.projectFromRwd，默认关）：项目名和城市编码改从 dtp-rwd 的项目对象读，不再调 mid-permission。
 * <ul>
 *     <li>按 projectId 查 dtp-rwd 对象（classCode=project，id=projectId）。项目名取 localName，为空取 name；城市编码取 infos.city
 *     （mid-permission 从 Boss 同步项目时写入的 cityCode，与 getProject 返回的是同一个值）；</li>
 *     <li>dtp-rwd 读不到（对象不存在、城市编码或名称为空、调用失败）时退回调一次原 getProject，结果同样缓存；</li>
 *     <li>结果放进程内存，{@code opt.project.refresh-minutes}（默认 360）分钟后下次访问时重读；重读两边都失败时继续用旧值；</li>
 *     <li>应用就绪后后台预读一次本站点项目（project.id）。</li>
 * </ul>
 * 从 dtp-rwd 得到的 ProjectDTO 只有 projectId、groupCode、projectName、projectLocalName、projectLocalId、cityCode；
 * 省、市、区名称和功能类型为空（只影响运行分析报告下载里的省/市/区三格，显示为空）。
 * 城市编码缓存同时满足 B5：天气等所有查城市编码的地方都经 MidPermissionClientService#getProject 走到这里。
 */
@Slf4j
@Component
public class OptProjectInfo {

    static final String CLASS_PROJECT = "project";

    private final ConcurrentMap<String, Entry> cache = new ConcurrentHashMap<>();
    private final BiFunction<String, String, JSONObject> rwdLoader;
    private final LongSupplier clock;

    @Value("${opt.project.refresh-minutes:360}")
    private long refreshMinutes = 360;

    @Lazy
    @Resource
    private MidPermissionClientService midPermissionClientService;
    @Lazy
    @Resource
    private ProjectCfg projectCfg;

    @Autowired
    public OptProjectInfo(DtpRwdClientPageWrapper dtpRwdClientPageWrapper) {
        this((groupCode, projectId) -> queryProjectObject(dtpRwdClientPageWrapper, groupCode, projectId), System::currentTimeMillis);
    }

    OptProjectInfo(BiFunction<String, String, JSONObject> rwdLoader, LongSupplier clock) {
        this.rwdLoader = rwdLoader;
        this.clock = clock;
    }

    /**
     * 取项目信息。
     *
     * @param groupCode 集团编码
     * @param projectId 项目 id
     * @param remote    dtp-rwd 读不到时的原逻辑（mid-permission getProject），按原 isThrows 语义抛异常或返回 null
     * @return 项目信息副本；两边都读不到且没有旧值时为 remote 的结果
     */
    public ProjectDTO get(String groupCode, String projectId, Function<String, ProjectDTO> remote) {
        if (StringUtils.isBlank(projectId)) {
            return remote.apply(projectId);
        }
        Entry entry = cache.get(projectId);
        if (entry != null && clock.getAsLong() - entry.loadedAt < refreshMillis()) {
            OptMetrics.inc("project.cache.hit");
            return copy(entry.project);
        }
        synchronized (this) {
            entry = cache.get(projectId);
            long now = clock.getAsLong();
            if (entry != null && now - entry.loadedAt < refreshMillis()) {
                OptMetrics.inc("project.cache.hit");
                return copy(entry.project);
            }
            ProjectDTO loaded = null;
            try {
                loaded = fromRwd(rwdLoader.apply(groupCode, projectId), groupCode, projectId);
            } catch (RuntimeException e) {
                log.warn("[opt-project] dtp-rwd 读项目对象失败 projectId={}：{}", projectId, e.toString());
            }
            if (loaded != null) {
                OptMetrics.inc("project.load.rwd");
            } else {
                OptMetrics.inc("project.load.midPermission");
                try {
                    loaded = remote.apply(projectId);
                } catch (RuntimeException e) {
                    if (entry != null) {
                        log.warn("[opt-project] 项目信息重读失败，继续用旧值 projectId={}：{}", projectId, e.toString());
                        return copy(entry.project);
                    }
                    throw e;
                }
            }
            if (loaded == null) {
                return entry == null ? null : copy(entry.project);
            }
            cache.put(projectId, new Entry(copy(loaded), now));
            return copy(loaded);
        }
    }

    /** 应用就绪后预读本站点项目；开关关闭或没有配置 project.id（云端）时不做任何事 */
    @EventListener(ApplicationReadyEvent.class)
    public void preload() {
        if (!OptSwitch.on(OptSwitch.PROJECT_FROM_RWD) || projectCfg == null || StringUtils.isBlank(projectCfg.getProjectId())) {
            return;
        }
        String projectId = projectCfg.getProjectId();
        Thread t = new Thread(() -> {
            try {
                midPermissionClientService.getProject(false, false, projectId);
            } catch (RuntimeException e) {
                log.warn("[opt-project] 预读项目信息失败 projectId={}：{}", projectId, e.toString());
            }
        }, "opt-project-preload");
        t.setDaemon(true);
        t.start();
    }

    /**
     * dtp-rwd 项目对象 → ProjectDTO。城市编码或名称为空时返回 null（视为读不到，走原逻辑）。
     */
    static ProjectDTO fromRwd(JSONObject obj, String groupCode, String projectId) {
        if (obj == null) {
            return null;
        }
        String city = null;
        Object infos = obj.get("infos");
        if (infos instanceof java.util.Map) {
            Object c = ((java.util.Map<?, ?>) infos).get("city");
            city = c == null ? null : c.toString();
        }
        if (StringUtils.isBlank(city)) {
            city = obj.getString("city");
        }
        String localName = obj.getString("localName");
        String name = StringUtils.isNotBlank(localName) ? localName : obj.getString("name");
        if (StringUtils.isBlank(city) || StringUtils.isBlank(name)) {
            return null;
        }
        ProjectDTO p = new ProjectDTO();
        p.setGroupCode(groupCode);
        p.setProjectId(projectId);
        p.setProjectLocalId(obj.getString("localId"));
        p.setProjectName(name);
        p.setProjectLocalName(name);
        p.setCityCode(city.trim());
        return p;
    }

    static JSONObject queryProjectObject(DtpRwdClientPageWrapper rwd, String groupCode, String projectId) {
        QueryEqParamDTO criteria = new QueryEqParamDTO();
        criteria.setClassCode(Collections.singletonList(CLASS_PROJECT));
        criteria.setId(Collections.singletonList(projectId));
        criteria.setValid(1);
        BaseQueryParamDTO paramDTO = new BaseQueryParamDTO();
        paramDTO.setPage(new DtpRwdPageQuery().setPage(1).setSize(10));
        paramDTO.setCriteria(criteria);
        DmpResult<List<JSONObject>> result = rwd.queryObjectList(groupCode, projectId, paramDTO);
        DMPResultUtil.handleResult(result);
        List<JSONObject> rows = result.getData();
        return rows == null || rows.isEmpty() ? null : rows.get(0);
    }

    private long refreshMillis() {
        return TimeUnit.MINUTES.toMillis(Math.max(refreshMinutes, 1));
    }

    private static ProjectDTO copy(ProjectDTO src) {
        if (src == null) {
            return null;
        }
        ProjectDTO p = new ProjectDTO();
        p.setGroupId(src.getGroupId());
        p.setGroupCode(src.getGroupCode());
        p.setGroupName(src.getGroupName());
        p.setProjectId(src.getProjectId());
        p.setProjectLocalId(src.getProjectLocalId());
        p.setProjectName(src.getProjectName());
        p.setProjectLocalName(src.getProjectLocalName());
        p.setFunctionTypeCode(src.getFunctionTypeCode());
        p.setFunctionTypeName(src.getFunctionTypeName());
        p.setCityCode(src.getCityCode());
        p.setProvinceName(src.getProvinceName());
        p.setCityName(src.getCityName());
        p.setPartitionName(src.getPartitionName());
        return p;
    }

    void setRefreshMinutesForTest(long minutes) {
        this.refreshMinutes = minutes;
    }

    private static final class Entry {
        final ProjectDTO project;
        final long loadedAt;

        Entry(ProjectDTO project, long loadedAt) {
            this.project = project;
            this.loadedAt = loadedAt;
        }
    }
}
