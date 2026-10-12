package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

/**
 * 信息点取值逻辑基础模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@FieldNameConstants
public abstract class BaseInfoCodeTackValueLogicModel implements LogComponent {

    /**
     * 是否执行
     */
    private boolean execute = true;

    /**
     * 过滤项目ID, 存在此过滤时, 如果项目ID不在此过滤中, 则移除数据
     */
    private Set<String> filterProjectIds;

    /**
     * 是否支持壳配置
     */
    private Boolean canShellConfigured;

    /**
     * 是否匹配项目
     *
     * @return 是否匹配项目
     * @since 7.250602.0
     */
    @JsonIgnore
    public boolean matchProject() {
        if (CollectionUtils.isEmpty(this.filterProjectIds)) {
            return true;
        }
        return this.filterProjectIds.contains(ContextUtil.getProjectId());
    }
}
