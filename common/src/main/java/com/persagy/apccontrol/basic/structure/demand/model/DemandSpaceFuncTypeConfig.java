package com.persagy.apccontrol.basic.structure.demand.model;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.utils.ValidateUtils;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 空间功能类型需求配置值对象
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
@FieldNameConstants
public class DemandSpaceFuncTypeConfig<T extends BaseDemandConfig> implements BaseCheckParamProvider {

    /**
     * 空间功能类型
     */
    @NotEmpty(message = "{key_da2f29612b404d2d9503d93e5ecbadeb}")
    private List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes;

    /**
     * 配置
     */
    @NotNull(message = "{key_d21c8347084b4bddb289570ec98a3899}")
    private T config;

    /**
     * 关联配置组ID
     */
    private Long relConfigGroupId;

    /**
     * 关联配置组名称
     */
    private String configGroupName;

    /**
     * 创建实例
     *
     * @param funcTypes 空间功能类型
     * @param config    配置
     * @param <T>       配置类型
     * @return 实例
     * @since 5.240902.0
     */
    public static <T extends BaseDemandConfig> DemandSpaceFuncTypeConfig<T> of(
            @NonNull List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes, @NonNull T config) {
        DemandSpaceFuncTypeConfig<T> result = new DemandSpaceFuncTypeConfig<>();
        result.setFuncTypes(funcTypes);
        result.setConfig(config);
        return result;
    }

    @Override
    public void checkAndSet() {
        ValidateUtils.validate(this);
        this.config.checkAndSet();
    }
}
