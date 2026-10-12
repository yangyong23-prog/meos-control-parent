package com.persagy.apccontrol.basic.model.dto.kitchen;

import com.persagy.apccontrol.basic.model.vo.kitchen.KitchenRangeHoodDemandTimeItemVO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 厨房排油烟需求时间节点DTO
 *
 * <p>空间(space)与设备(equipment)共用同一DTO: 入参传递 {@code id} 与 {@code relEquipment},
 * 查询后需求时间({@code demandTimes})原位回填到对应节点下。</p>
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
@Data
@Schema(title = "厨房排油烟需求时间节点DTO")
public class KitchenRangeHoodDemandTimeDTO {

    /**
     * 对象ID, 空间ID或设备ID
     *
     * @since v7.261001.0
     */
    @NotBlank(message = "{key_validation_id_not_blank}")
    @Schema(description = "对象ID, 空间ID或设备ID")
    private String id;

    /**
     * 需求时间, 查询结果回填, 入参时无需传递
     *
     * @since v7.261001.0
     */
    @Schema(description = "需求时间, 查询结果回填")
    private List<KitchenRangeHoodDemandTimeItemVO> demandTimes;

    /**
     * 关联设备, 仅空间节点使用; 设备节点该字段为空
     *
     * @since v7.261001.0
     */
    @Valid
    @Schema(description = "关联设备, 仅空间节点使用")
    private List<KitchenRangeHoodDemandTimeDTO> relEquipment;

    /**
     * 获取关联设备列表, 为空时返回空列表
     *
     * @return 关联设备列表, 永不为 {@literal null}
     * @since v7.261001.0
     */
    public @NonNull List<KitchenRangeHoodDemandTimeDTO> listRelEquipments() {
        return Objects.isNull(this.relEquipment) ? new ArrayList<>() : this.relEquipment;
    }

    /**
     * 将合并后的需求时间区间回填为需求时间, 空区间回填为空列表
     *
     * @param mergedIntervals 合并后的需求时间区间列表, 可为 {@literal null}
     * @since v7.261001.0
     */
    public void fillDemandTimes(
            @Nullable Collection<DemandTimeIntervalValueObject> mergedIntervals) {
        if (CollectionUtils.isEmpty(mergedIntervals)) {
            this.demandTimes = new ArrayList<>();
            return;
        }
        List<KitchenRangeHoodDemandTimeItemVO> items = new ArrayList<>(mergedIntervals.size());
        for (DemandTimeIntervalValueObject interval : mergedIntervals) {
            items.add(KitchenRangeHoodDemandTimeItemVO.of(interval));
        }
        this.demandTimes = items;
    }

    /**
     * 填充空需求时间(模板缺失场景), 保证响应结构完整
     *
     * @since v7.261001.0
     */
    public void fillEmptyDemandTimes() {
        this.fillDemandTimes(Collections.emptyList());
        this.listRelEquipments().forEach(KitchenRangeHoodDemandTimeDTO::fillEmptyDemandTimes);
    }
}
