package com.persagy.apccontrol.basic.model.dto;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * 查询指定对象的信息点
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 14:32
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "多对象信息点配置")
public class BatchObjectInfoCodeConfigDTO implements BaseCheckParamProvider {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @NotEmpty(message = "{key_1e01fb8a22dc41ba8a356ef7b00651ce}")
    @Schema(description = "对象集合")
    private List<ObjectInfoCodeConfigDTO> object;

    @Schema(description = "是否查询信息点规则")
    private Boolean queryInfoCodeRule;

    @Schema(description = "是否查询信息点定义")
    private Boolean queryInfoCodeDefine;

    @Schema(description = "强制干预截止时间的信息点")
    private Set<String> queryInfoCodeRecoverTime;

    @Override
    public void checkAndSet() {
        if (BooleanUtils.isTrue(queryInfoCodeRule) || BooleanUtils.isTrue(queryInfoCodeDefine)) {
            if (object.stream().anyMatch(item ->
                    StringUtils.isAnyBlank(item.getClassCode(), item.getObjectId())
                            || CollectionUtils.isEmpty(item.getInfoCodes()))) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_7aa826f9115941818dcc887d36bde998"));
            }
        }
    }
}
