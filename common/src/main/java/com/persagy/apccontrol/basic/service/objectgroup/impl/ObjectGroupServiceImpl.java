package com.persagy.apccontrol.basic.service.objectgroup.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.ObjectGroupMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.objectgroup.ObjectGroup;
import com.persagy.apccontrol.basic.model.entity.objectgroup.ObjectGroupRelation;
import com.persagy.apccontrol.basic.model.enumtype.objectgroup.ObjectGroupTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.model.param.objectgroup.ObjectGroupParam;
import com.persagy.apccontrol.basic.model.param.objectgroup.ObjectGroupRelationParam;
import com.persagy.apccontrol.basic.model.param.objectgroup.QueryObjectGroupNameParam;
import com.persagy.apccontrol.basic.model.param.objectgroup.QueryObjectGroupParam;
import com.persagy.apccontrol.basic.model.vo.objectgroup.ObjectGroupObjectVO;
import com.persagy.apccontrol.basic.model.vo.objectgroup.ObjectGroupVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.basic.service.objectgroup.ObjectGroupRelationService;
import com.persagy.apccontrol.basic.service.objectgroup.ObjectGroupService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ObjectGroupServiceImpl extends ServiceImpl<ObjectGroupMapper, ObjectGroup> implements ObjectGroupService {

    private final ObjectGroupRelationService objectGroupRelationService;
    private final DtpRwdClientService dtpRwdClientService;
    private final PaasDataTransferClientService paasDataTransferClientService;


    /**
     * 查询组
     *
     * @param param 查询参数
     * @return {@code List<ObjectGroupVO> }
     * @since 7.250402.0
     */
    @Override
    public List<ObjectGroupVO> queryGroup(QueryObjectGroupParam param) {
        ObjectGroupTypeEnum groupType = param.getGroupType();
        LambdaQueryWrapper<ObjectGroup> qw = new LambdaQueryWrapper<>();
        qw.eq(ObjectGroup::getGroupType, groupType);
        qw.eq(ObjectGroup::getProjectId, ContextUtil.getProjectId());
        List<ObjectGroup> list = list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<ObjectGroupVO> objectGroupVOList = new ArrayList<>();
        List<Long> groupIds = list.stream().map(ObjectGroup::getId).collect(Collectors.toList());
        Map<Long, List<ObjectGroupRelation>> groupIdRelationMap = objectGroupRelationService.mapGroupRelationsByGroupIds(groupIds);
        for (ObjectGroup objectGroup : list) {
            Long id = objectGroup.getId();
            ObjectGroupVO objectGroupVO = new ObjectGroupVO();
            objectGroupVO.setGroupName(objectGroup.getGroupName());
            objectGroupVO.setId(objectGroup.getId());
            objectGroupVO.setSequence(objectGroup.getSequence());
            objectGroupVO.setLayout(objectGroup.getLayout());
            objectGroupVO.setGroupType(objectGroup.getGroupType());
            List<ObjectGroupRelation> relationList = groupIdRelationMap.get(id);
            if (CollectionUtils.isNotEmpty(relationList)) {
                fillRelationObject(objectGroupVO, relationList, objectGroup.getGroupType());
            }
            objectGroupVOList.add(objectGroupVO);
        }
        //如果有一个Sequence不为空，就按Sequence排序
        boolean anySequenceNotEmpty = objectGroupVOList.stream().anyMatch(v -> !Objects.isNull(v.getSequence()));
        List<ObjectGroupVO> resultList;
        if (anySequenceNotEmpty) {
            resultList = new LinkedList<>();
            //拿到顺序不为空的集合，然后按顺序排序，然后把顺序为空的集合加到后面，然后返回
            List<ObjectGroupVO> sequenceNotEmptyList = objectGroupVOList.stream().filter(v -> !Objects.isNull(v.getSequence())).collect(Collectors.toList());
            List<ObjectGroupVO> sequenceEmptyList = objectGroupVOList.stream().filter(v -> Objects.isNull(v.getSequence())).collect(Collectors.toList());
            sequenceNotEmptyList.sort(Comparator.comparing(ObjectGroupVO::getSequence));
            resultList.addAll(sequenceNotEmptyList);
            resultList.addAll(sequenceEmptyList);
        } else {
            //按名称排序后返回
            resultList = objectGroupVOList.stream().sorted(Comparator.comparing(ObjectGroupVO::getGroupName)).collect(Collectors.toList());
        }
        return resultList;
    }

    /**
     * 填充关联对象
     *
     * @param relationList 关联对象列表
     * @since 7.250402.0
     */
    private void fillRelationObject(ObjectGroupVO objectGroupVO, List<ObjectGroupRelation> relationList, ObjectGroupTypeEnum groupType) {
        Set<String> objectSet = relationList.stream().map(ObjectGroupRelation::getObjectId).collect(Collectors.toSet());
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setId(objectSet);
        param.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, DtpRwdEquipmentDTO.Fields.floorId);
        if (CollectionUtils.isNotEmpty(groupType.getAddInfoCodes())) {
            param.addWithColumnWithDefault(groupType.getAddInfoCodes());
        }
        //查询设备
        List<DtpRwdEquipmentDTO> equipmentList = dtpRwdClientService.listEquipment(param);
        Set<String> floorSet = equipmentList.stream().map(DtpRwdEquipmentDTO::getFloorId).collect(Collectors.toSet());
        Map<String, DtpRwdEquipmentDTO> mapObjectIdAndEquipment = equipmentList.stream().collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, Function.identity()));
        //查询楼层名称
        Map<String, String> mapObjectIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(false, floorSet);
        List<ObjectGroupObjectVO> objectVOList = new ArrayList<>();
        for (ObjectGroupRelation relation : relationList) {
            ObjectGroupObjectVO objectVO = new ObjectGroupObjectVO();
            String objectId = relation.getObjectId();
            DtpRwdEquipmentDTO dtpRwdEquipmentDTO = mapObjectIdAndEquipment.get(objectId);
            if (Objects.isNull(dtpRwdEquipmentDTO)) {
                continue;
            }
            BeanUtils.copyProperties(dtpRwdEquipmentDTO, objectVO);
            String floorId = dtpRwdEquipmentDTO.getFloorId();
            String floorName = mapObjectIdAndLocalName.get(floorId);
            objectVO.setFloorName(floorName);
            objectVO.setSequence(relation.getSequence());
            objectVOList.add(objectVO);
        }
        //排序
        boolean anySequenceNotEmpty = objectVOList.stream().anyMatch(v -> !Objects.isNull(v.getSequence()));
        List<ObjectGroupObjectVO> resultList = new LinkedList<>();
        if (anySequenceNotEmpty) {
            //拿到顺序不为空的集合，然后按顺序排序，然后把顺序为空的集合加到后面，然后返回
            List<ObjectGroupObjectVO> sequenceNotEmptyList = objectVOList.stream().filter(v -> !Objects.isNull(v.getSequence())).collect(Collectors.toList());
            List<ObjectGroupObjectVO> sequenceEmptyList = objectVOList.stream().filter(v -> Objects.isNull(v.getSequence())).collect(Collectors.toList());
            sequenceNotEmptyList.sort(Comparator.comparing(ObjectGroupObjectVO::getSequence));
            CollectionUtils.addAll(resultList, sequenceNotEmptyList);
            CollectionUtils.addAll(resultList, sequenceEmptyList);
        }
        objectGroupVO.setObjects(resultList);
    }

    /**
     * 是否存在编组名
     *
     * @param param 查询参数
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    @Override
    public Boolean hasExistsGroupName(QueryObjectGroupNameParam param) {
        LambdaQueryWrapper<ObjectGroup> qw = new LambdaQueryWrapper<>();
        qw.eq(ObjectGroup::getGroupName, param.getGroupName());
        qw.eq(ObjectGroup::getProjectId, ContextUtil.getProjectId());
        qw.eq(Objects.nonNull(param.getGroupType()), ObjectGroup::getGroupType, param.getGroupType());
        qw.ne(Objects.nonNull(param.getId()), ObjectGroup::getId, param.getId());
        return CollectionUtils.isNotEmpty(this.list(qw));
    }

    /**
     * 删除编组
     *
     * @param id     编组id
     * @param isEdge
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteGroup(@NotBlank Long id, Boolean isEdge) {
        ObjectGroup objectGroup = this.getById(id);
        if (Objects.isNull(objectGroup)) {
            return true;
        }
        if (!isEdge && objectGroup.getGroupType() == ObjectGroupTypeEnum.VEDIO_MONITORING) {
            throw new ApplicationException(RequestParamErrorEnum.INPUT_ILLEGAL.value(), LocaleMessageUtil.getMessage("key_4b5cb8d6ed174eb8ac93d7e378c17afc"), LocaleMessageUtil.getMessage("key_4b5cb8d6ed174eb8ac93d7e378c17afc"));
        }
        List<ObjectGroupRelation> objectGroupRelations = objectGroupRelationService.listByGroupId(id);
        //删除关联设备
        boolean deleteRelation = objectGroupRelationService.deleteByGroupId(id);
        if (isEdge && deleteRelation && objectGroup.getGroupType().getIsSyncCloud()) {
            //同步至云端
            List<Pair<ObjectGroupRelation, Long>> relationIds = objectGroupRelations.stream()
                    .map(e -> Pair.of(new ObjectGroupRelation(), e.getId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(relationIds)) {
                sendCloud(PaasDataTransferParam.ofDel(relationIds));
            }
        }
        boolean delObjectGroup = this.removeById(id);
        if (isEdge && delObjectGroup && objectGroup.getGroupType().getIsSyncCloud()) {
            //同步至云端
            sendCloud(PaasDataTransferParam.ofDel(Pair.of(new ObjectGroup(), id)));
        }
        return delObjectGroup;
    }

    /**
     * 编辑编组
     *
     * @param param  参数
     * @param isEdge
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    @Override
    public Boolean editGroup(@NonNull ObjectGroupParam param, Boolean isEdge) {
        if (!isEdge && param.getGroupType() == ObjectGroupTypeEnum.VEDIO_MONITORING) {
            throw new ApplicationException(RequestParamErrorEnum.INPUT_ILLEGAL.value(), LocaleMessageUtil.getMessage("key_77a53811be1042db96a40cab4c17cad5"), LocaleMessageUtil.getMessage("key_77a53811be1042db96a40cab4c17cad5"));
        }
        Long id = param.getId();
        //id为空时为新增
        if (Objects.isNull(id)) {
            return saveObjectGroup(param, isEdge);
        }
        return updateObjectGroup(param, isEdge);
    }

    /**
     * 保存编组
     *
     * @param param 参数
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    private Boolean saveObjectGroup(ObjectGroupParam param, Boolean isEdge) {
        String groupName = param.getGroupName();
        Boolean has = hasExistsGroupName(new QueryObjectGroupNameParam().setGroupName(groupName).setGroupType(param.getGroupType()));
        if (has) {
            throw new ApplicationException(RequestParamErrorEnum.INPUT_ILLEGAL, LocaleMessageUtil.getMessage("key_167494d67ecb4bc481485f93961c3d2c"));
        }
        List<ObjectGroupRelationParam> objects = param.getObjects();
        if (objects.size() > 50) {
            throw new ApplicationException(RequestParamErrorEnum.INPUT_ILLEGAL, LocaleMessageUtil.getMessage("key_097320fd0acd4aa4bab4471e46364a7c"));
        }
        String projectId = ContextUtil.getProjectId();
        ObjectGroup objectGroup = new ObjectGroup();
        objectGroup.setGroupName(groupName);
        objectGroup.setGroupType(param.getGroupType());
        objectGroup.setLayout(param.getLayout());
        objectGroup.setSequence(param.getSequence());
        objectGroup.setProjectId(projectId);
        boolean save = this.save(objectGroup);
        if (save) {
            if (CollectionUtils.isNotEmpty(objects)) {
                List<ObjectGroupRelation> objectList = buildObjectGroupRelations(objects, objectGroup.getId());
                Boolean saveRelation = objectGroupRelationService.batchSave(objectList);
                if (saveRelation && isEdge && param.getGroupType().getIsSyncCloud()) {
                    //同步至云端
                    sendCloud(PaasDataTransferParam.of(DataTypeEnum.ADD, objectList));
                }
            }
            if (isEdge && param.getGroupType().getIsSyncCloud()) {
                //同步至云端
                sendCloud(PaasDataTransferParam.of(DataTypeEnum.ADD, objectGroup));
            }
        }
        return save;
    }

    @NotNull
    private List<ObjectGroupRelation> buildObjectGroupRelations(List<ObjectGroupRelationParam> objects, Long groupId) {
        List<ObjectGroupRelation> objectList = new ArrayList<>();
        for (ObjectGroupRelationParam object : objects) {
            ObjectGroupRelation objectGroupRelation = new ObjectGroupRelation();
            objectGroupRelation.setGroupId(groupId);
            objectGroupRelation.setObjectId(object.getId());
            objectGroupRelation.setSequence(object.getSequence());
            objectGroupRelation.setProjectId(ContextUtil.getProjectId());
            objectList.add(objectGroupRelation);
        }
        return objectList;
    }

    /**
     * 更新对象编组
     *
     * @param param 参数
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    private Boolean updateObjectGroup(ObjectGroupParam param, Boolean isEdge) {
        String groupName = param.getGroupName();
        Long id = param.getId();
        ObjectGroup data = getById(id);
        //如果传入的名称和之前的名称不相同，则验证名称是否存在
        if (!data.getGroupName().equals(groupName)) {
            Boolean has = hasExistsGroupName(new QueryObjectGroupNameParam().setGroupName(groupName).setGroupType(param.getGroupType()));
            if (has) {
                throw new ApplicationException(RequestParamErrorEnum.INPUT_ILLEGAL, LocaleMessageUtil.getMessage("key_167494d67ecb4bc481485f93961c3d2c"));
            }
        }
        List<ObjectGroupRelationParam> objects = param.getObjects();
        if (objects.size() > 50) {
            throw new ApplicationException(RequestParamErrorEnum.INPUT_ILLEGAL, LocaleMessageUtil.getMessage("key_097320fd0acd4aa4bab4471e46364a7c"));
        }
        data.setGroupName(groupName);
        data.setLayout(param.getLayout());
        data.setSequence(param.getSequence());
        boolean update = this.updateById(data);
        if (update) {
            List<ObjectGroupRelation> objectGroupRelations = objectGroupRelationService.listByGroupId(id);
            if (CollectionUtils.isNotEmpty(objectGroupRelations)) {
                //先删除，再插入
                Boolean delByGroupId = objectGroupRelationService.deleteByGroupId(id);
                if (delByGroupId && isEdge && data.getGroupType().getIsSyncCloud()) {
                    //同步至云端
                    List<Pair<ObjectGroupRelation, Long>> relationIds = objectGroupRelations.stream()
                            .map(e -> Pair.of(new ObjectGroupRelation(), e.getId())).collect(Collectors.toList());
                    sendCloud(PaasDataTransferParam.ofDel(relationIds));
                }
                if (CollectionUtils.isNotEmpty(objects)) {
                    List<ObjectGroupRelation> objectList = buildObjectGroupRelations(objects, id);
                    Boolean saveRelation = objectGroupRelationService.batchSave(objectList);
                    if (saveRelation && isEdge && data.getGroupType().getIsSyncCloud()) {
                        //同步至云端
                        sendCloud(PaasDataTransferParam.of(DataTypeEnum.ADD, objectList));
                    }
                }
            }
            if (isEdge && data.getGroupType().getIsSyncCloud()) {
                //同步至云端
                sendCloud(PaasDataTransferParam.of(DataTypeEnum.EDIT, data));
            }
        }
        return update;

    }

    /**
     * 发送
     *
     * @param data 数据
     * @since 7.250403.0
     */
    private <T extends BaseAssignIdDO> void sendCloud(List<PaasDataTransferParam<T>> data) {
        CompletableFuture.runAsync(() -> paasDataTransferClientService.syncDbDataToCloud(data), BusinessThreadPoolExecutor.ttlExecutor());
    }

    @Override
    public String logTag() {
        return "对象编组";
    }
}
