package com.persagy.apccontrol.basic.model.param.rwd;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * 所在空间的设备查询参数
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@Schema(title = "所在空间的设备查询参数")
public class EquipmentInSpaceQueryParam {

    @Schema(description = "运行监测二级类型, 存在时将根据二级类型过滤设备")
    private @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "空间ID")
    @NotBlank(message = "{key_c78c2ee89cbd491ea193306bff87f524}")
    private @NonNull String spaceId;

    @Schema(description = "设备ID, 为空时查询所有")
    private @Nullable Set<@NonNull String> equipmentIds;

    @Schema(description = "查询字段, 为空时查询默认字段")
    private @NonNull String @Nullable [] withColumns;

    @Schema(description = "过滤所在系统ID, 为空时不过滤")
    private @Nullable Set<@NonNull String> filterInSystemIds;

    @Schema(description = "过滤设备类, 为null时不过滤, 为空时过滤所有设备类")
    private @Nullable Set<@NonNull String> filterClassCodes;

    @Schema(description = "过滤可控设备类, 为null时不过滤, 为空时过滤所有设备类")
    private @Nullable Set<@NonNull String> filterControlledClassCodes;

    @Schema(description = "是否查询可运行状态")
    private @Nullable Boolean queryOperableRunningStatus;

    @Schema(description = "是否查询设备类名称")
    private @Nullable Boolean queryClassCodeName;

    @Schema(description = "是否查询专业名称")
    private @Nullable Boolean queryMajorName;

    @Schema(description = "拓展查询参数")
    private EquipmentInSpaceQueryParam additionalQueryParam;

}
