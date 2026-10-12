package com.persagy.apccontrol.basic.service.objectgroup;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.entity.objectgroup.ObjectGroupRelation;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ObjectGroupRelationService extends IService<ObjectGroupRelation>, LogComponent {

    /**
     * 批量保存
     *
     * @param list 列表
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    Boolean batchSave(@NotNull Collection<ObjectGroupRelation> list);

    /**
     * 按编组id删除
     *
     * @param groupId 组id
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    Boolean deleteByGroupId(@NotNull Long groupId);

    /**
     * 根据编组id查询关联对象
     *
     * @param groupId 组id
     * @return {@code List<ObjectGroupRelation> }
     * @since 7.250402.0
     */
    List<ObjectGroupRelation> listByGroupId(@NotNull Long groupId);

    /**
     * 通过编组id批量查询，并封装成map返回
     *
     * @param groupIds 编组id列表
     * @return {@code Map<Long, List<ObjectGroupRelation>> }key->groupId value->关联对象列表
     * @since 7.250402.0
     */
    Map<Long, List<ObjectGroupRelation>> mapGroupRelationsByGroupIds(@NotNull Collection<@NonNull Long> groupIds);
}
