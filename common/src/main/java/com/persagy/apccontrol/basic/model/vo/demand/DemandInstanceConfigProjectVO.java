package com.persagy.apccontrol.basic.model.vo.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 需求管理-项目实例配置
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求管理-项目实例配置")
public class DemandInstanceConfigProjectVO extends DtpRwdObjectDTO {

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType = DemandInstanceTypeEnum.PROJECT;

    @Schema(description = "配置")
    private DemandInstanceConfigValueObject config;

    /**
     * 创建实例配置列表
     *
     * @param project         项目
     * @param buildings       建筑
     * @param floors          楼层
     * @param spaces          空间
     * @param instanceConfigs 实例配置
     * @return 实例配置列表
     * @since 5.241102.0
     */
    public static List<DemandInstanceConfigProjectVO> of(
            @Nullable DtpRwdObjectDTO project,
            @Nullable List<DtpRwdObjectDTO> buildings,
            @Nullable List<DtpRwdObjectDTO> floors,
            @Nullable List<DtpRwdObjectDTO> spaces,
            @NonNull List<DemandInstanceConfigEntity> instanceConfigs) {
        if (CollectionUtils.isEmpty(instanceConfigs)) {
            return Lists.newArrayList();
        }
        Map<String, DemandInstanceConfigValueObject> instanceIdAndConfig = instanceConfigs.stream().collect(
                Collectors.toMap(item -> item.getIdentifier().getInstance().getInstanceId(),
                        DemandInstanceConfigEntity::getConfig, (o1, o2) -> o1));
        DemandConverter converter = SpringUtil.getBean(DemandConverter.class);
        List<DemandInstanceConfigProjectVO> result = new ArrayList<>();
        if (Objects.nonNull(project)) {
            result.add(converter.toTreeInstanceConfigVo(project, instanceIdAndConfig.get(project.getId())));
        }
        if (CollectionUtils.isNotEmpty(buildings)) {
            result.addAll(buildings.stream()
                    .map(item -> converter.toTreeInstanceConfigVo(item, instanceIdAndConfig.get(item.getId())))
                    .collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(floors)) {
            result.addAll(floors.stream()
                    .map(item -> converter.toTreeInstanceConfigVo(item, instanceIdAndConfig.get(item.getId())))
                    .collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(spaces)) {
            result.addAll(spaces.stream()
                    .map(item -> converter.toTreeInstanceConfigVo(item, instanceIdAndConfig.get(item.getId())))
                    .collect(Collectors.toList()));
        }
        return result;
    }
}
