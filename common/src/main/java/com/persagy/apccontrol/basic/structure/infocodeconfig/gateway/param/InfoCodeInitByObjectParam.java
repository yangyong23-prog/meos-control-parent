package com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotEmpty;
import java.util.Collection;

/**
 * 信息点配置-聚合根按对象初始化参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InfoCodeInitByObjectParam extends BaseInfoCodeInitParam {

    /**
     * 对象
     */
    @NotEmpty(message = "{key_c5e627a908f14af2aaaf42137a9158bc}")
    private Collection<DtpRwdObjectDTO> objects;

    /**
     * 老版本过滤SSE发布信息点
     *
     * @deprecated 切换到新字段 {@link #filterSsePublishInfoCode}
     */
    @Deprecated
    private Boolean filterSsePublishInfoCodeOld;

    /**
     * 过滤SSE发布信息点
     */
    private Boolean filterSsePublishInfoCode;

    /**
     * 根据模板类型创建带虚拟对象的查询参数
     *
     * @param templateTypeModel 模板类型
     * @return 带虚拟对象的查询参数
     * @since 7.260501.0
     */
    public static InfoCodeInitByObjectParam ofVirtualObject(@NonNull InfoCodeTemplateTypeModel templateTypeModel) {
        DtpRwdObjectDTO virtualObject = new DtpRwdObjectDTO();
        virtualObject.setId("virtual-object-" + templateTypeModel.getTemplateTypeId());
        virtualObject.setClassCode(templateTypeModel.getClassCode());
        InfoCodeInitByObjectParam objectParam = new InfoCodeInitByObjectParam();
        objectParam.setObjects(Lists.newArrayList(virtualObject));
        return objectParam;
    }
}
