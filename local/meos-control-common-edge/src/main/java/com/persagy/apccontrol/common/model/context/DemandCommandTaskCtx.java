package com.persagy.apccontrol.common.model.context;

import cn.hutool.core.map.MapUtil;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 照明需求计算任务上下文
 */
@Data
public class DemandCommandTaskCtx {


    //开
    public static final String OPEN = "1";
    //关
    public static final String CLOSE = "2";

    //反馈调节
    public static final String FEEDBACK = "3";
    //不分开启
    public static final String OTHER = "99";

    //--------------运行前填充开始-----------------------

    @Schema(description = "场景 ID")
    private Long sceneId;

    @Schema(description = " 需求模版类型")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "需求模版 ID")
    private Long demandTemplateId;

    @Schema(description = "当日全量场景配置信息 只有配置变更场景才填充")
    private Map<DemandTemplateTypeEnum, List<LightingSceneDTO>> nowDayAllCfg;

    //--------------干预相关参数-----------------------
    @Schema(description = "是否是自动需求 默认是  true 非自动就干预")
    private Boolean ai;
    @Schema(description = "干预截止时间")
    private LocalDateTime timeDuration;

    @Schema(description = "干预配置")
    private Map<String, List<DemandLightingFuncTypeSettingDTO>> cfList;

    //--------------运行前填充结束-----------------------

    // ------运行中填充-----
    @Schema(description = "是否是无需求场景")
    private Boolean isNoScene;

    @Schema(description = "执行时间-场景开始")
    private LocalDateTime time;

    @Schema(description = "当前时间")
    private LocalDateTime nowTime;

    @Schema(description = "执行批次号")
    private String batchNumber;


    @Schema(description = "所有功能类型开启状态的回路 ID")
    private Set<String> openFuncTypeCircuitIds;



    //---------------运行结果--------------------------------
    @Schema(description = "生成的指令列表")
    private List<EquipRecord> equipRecordLis;


    @Schema(description = "回路的指令存储 回路会服务多个空间 MAP")
    private Map<String, List<EquipRecord>> circuitEquipRecordMap;


    /**
     * 按照场景配置构建
     *
     * @param scene
     */
    public DemandCommandTaskCtx(LightingSceneDTO scene) {

        this.templateType = scene.getDemandTemplateType();
        this.demandTemplateId = scene.getRelDemandTemplateId();
        this.sceneId = scene.getSceneId();
        this.isNoScene = scene.isNoScene();
        this.time = scene.getBeginDateTime();
        this.batchNumber = ChangeContent.buildBatchNumber(time);
        this.ai = true;

        //无场景 对场景 ID 特殊处理 用执行时间的时间戳表示用于统一处理
        if (isNoScene) {
            ZoneId zoneId = DateUtils.getZoneId();
            this.sceneId = time.atZone(zoneId).toInstant().toEpochMilli();
        }

        init();

    }

    /**
     * 需求干预构造器
     *
     * @param type
     * @param time
     * @param timeDuration
     * @param cfList
     */
    public DemandCommandTaskCtx(RunningMonitorSecondTypeEnum type, LocalDateTime time, LocalDateTime timeDuration, Map<String, List<DemandLightingFuncTypeSettingDTO>> cfList) {
        this.cfList = cfList;
        this.ai = false;
        this.templateType = DemandTemplateTypeEnum.getByCode(type.value());
        this.time = time;
        this.timeDuration = timeDuration;

        init();
    }

    public void init() {
        this.nowTime = DateUtils.now();
        this.equipRecordLis = new ArrayList<>();
        this.openFuncTypeCircuitIds = new HashSet<>();
        this.circuitEquipRecordMap = new HashMap<>();
        this.batchNumber = ChangeContent.buildBatchNumber(DateUtils.now());
    }

    /**
     * 校验场景必要字段是否存在
     *
     * @return
     */
    public boolean check() {
        boolean dt = templateType == null;
        boolean b = time == null;
        return dt || b;
    }

    /**
     * 添加配置里面所有开启的回路状态
     *
     * @param allCfg
     */
    public void addOpenCircuitIds(Map<String, List<DemandLightingFuncTypeSettingDTO>> allCfg) {

        if (MapUtil.isEmpty(allCfg)) {
            return;
        }
        openFuncTypeCircuitIds = new HashSet<>();

        allCfg.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            v.forEach(type -> {
                if (DemandLightingTypeSettingModeEnum.ALL_OPEN.equals(type.getSettingMode())) {
                    if (CollectionUtils.isEmpty(type.getCircuitSettings())) {
                        return;
                    }
                    type.getCircuitSettings().forEach(hl -> {
                        openFuncTypeCircuitIds.add(hl.getCircuitId());
                    });
                }
            });
        });

    }

    /**
     * 添加回路 MAP 指令用于判断 重复回路
     *
     * @param e
     */
    public void addCircuitEquipRecordMap(EquipRecord e) {
        if (MapUtil.isEmpty(circuitEquipRecordMap)) {
            circuitEquipRecordMap = new HashMap<>();
        }

        if (circuitEquipRecordMap.containsKey(e.getObjInfoIndexes())) {
            circuitEquipRecordMap.get(e.getObjInfoIndexes()).add(e);
        } else {
            List<EquipRecord> l = new ArrayList<>();
            l.add(e);
            circuitEquipRecordMap.put(e.getObjInfoIndexes(), l);
        }
    }

    /**
     * 构建回路的指令
     */
    public void buildCircuitEquipRecordLis() {

        if (MapUtil.isEmpty(circuitEquipRecordMap)) {
            return;
        }
        circuitEquipRecordMap.forEach((k, v) -> {

            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            if (v.size() == 1) {
                //直接添加
                equipRecordLis.addAll(v);
            } else {
                //先取出 模版数据
                EquipRecord mb = v.get(0);
                //判断是否既有开也有关 如果是下发一个开.否则下发原始指令
                for (EquipRecord e : v) {
                    if (OPEN.equals(e.getChangeContent().getInfoValue())) {
                        //把模版数据替换为开
                        mb = e;
                        break;
                    }
                }
                equipRecordLis.add(mb);
            }
        });
    }




    /**
     * 获取已经生成的指令 对象 ID 列表
     *
     * @return
     */
    public List<String> getEquipRecordIds() {
        List<String> eIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(equipRecordLis)) {
            return eIds;
        }
        equipRecordLis.forEach(i -> eIds.add(i.getObjId()));
        return eIds;
    }


    /**
     * 判断当前指令时间是否是未来时间
     *
     * @return
     */
    public boolean isFutureTime() {
        return time.isAfter(nowTime);
    }


    /**
     * 获取 模版场景 ID
     *
     * @return
     */
    public String getTemplateSceneId() {
        return templateType.getValue() + sceneId;
    }
}
