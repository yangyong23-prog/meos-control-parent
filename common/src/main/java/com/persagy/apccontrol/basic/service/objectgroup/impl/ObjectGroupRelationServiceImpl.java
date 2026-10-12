package com.persagy.apccontrol.basic.service.objectgroup.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.ObjectGroupRelationMapper;
import com.persagy.apccontrol.basic.model.entity.objectgroup.ObjectGroupRelation;
import com.persagy.apccontrol.basic.service.objectgroup.ObjectGroupRelationService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ObjectGroupRelationServiceImpl extends ServiceImpl<ObjectGroupRelationMapper, ObjectGroupRelation> implements ObjectGroupRelationService {


    /**
     * 批量保存
     *
     * @param list 列表
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    @Override
    public Boolean batchSave(Collection<ObjectGroupRelation> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        return this.saveBatch(list);
    }

    /**
     * 按编组id删除
     *
     * @param groupId 组id
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    @Override
    public Boolean deleteByGroupId(Long groupId) {
        //删除关联设备
        return this.remove(new LambdaQueryWrapper<ObjectGroupRelation>().eq(ObjectGroupRelation::getGroupId, groupId));
    }

    /**
     * 根据编组id查询关联对象
     *
     * @param groupId 组id
     * @return {@code List<ObjectGroupRelation> }
     * @since 7.250402.0
     */
    @Override
    public List<ObjectGroupRelation> listByGroupId(Long groupId) {
        LambdaQueryWrapper<ObjectGroupRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectGroupRelation::getGroupId, groupId);
        queryWrapper.eq(ObjectGroupRelation::getProjectId, ContextUtil.getProjectId());
        return list(queryWrapper);
    }

    /**
     * 通过编组id批量查询，并封装成map返回
     *
     * @param groupIds 编组id列表
     * @return {@code Map<Long, List<ObjectGroupRelation>> }key->groupId value->关联对象列表
     * @since 7.250402.0
     */
    @Override
    public Map<Long, List<ObjectGroupRelation>> mapGroupRelationsByGroupIds(Collection<@NonNull Long> groupIds) {
        LambdaQueryWrapper<ObjectGroupRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ObjectGroupRelation::getGroupId, groupIds);
        queryWrapper.eq(ObjectGroupRelation::getProjectId, ContextUtil.getProjectId());
        List<ObjectGroupRelation> list = list(queryWrapper);
        return list.stream().collect(Collectors.groupingBy(ObjectGroupRelation::getGroupId));
    }


    @Override
    public String logTag() {
        return "对象编组关系类";
    }
}
