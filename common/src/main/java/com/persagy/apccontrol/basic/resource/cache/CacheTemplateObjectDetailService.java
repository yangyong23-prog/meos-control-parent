package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailQueryDaoParam;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 缓存模板对象取值明细资源服务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheTemplateObjectDetailService {

    /**
     * 按完整业务键批量查询当前项目的模板对象取值明细。
     *
     * @param param 查询参数
     * @return 模板对象取值明细列表；业务键为空时返回空列表
     * @since v7.260701.0
     */
    @NonNull List<CacheTemplateObjectDetail> listByBusinessKeys(
            @NonNull CacheTemplateObjectDetailQueryDaoParam param);

    /**
     * 批量新增当前项目的模板对象取值明细。
     *
     * @param details 模板对象取值明细集合
     * @since v7.260701.0
     */
    void insertDetails(@NonNull Collection<CacheTemplateObjectDetail> details);

    /**
     * 删除当前项目单个模板键范围内的全部取值明细。
     *
     * @param templateKey 模板键
     * @since v7.260701.0
     */
    void removeByTemplateKey(@NonNull String templateKey);
}
