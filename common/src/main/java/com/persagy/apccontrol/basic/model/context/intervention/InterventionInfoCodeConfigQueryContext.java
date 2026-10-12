package com.persagy.apccontrol.basic.model.context.intervention;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionConfigEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionInfoQueryParam;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Data
public class InterventionInfoCodeConfigQueryContext implements LogComponent {

    /**
     * 查询参数
     */
    private InterventionInfoQueryParam queryParam;


    /**
     * 对象对应的配置，配置列表包含空间、楼层、建筑、项目的配置，key为所查询的instanceId
     */
    private Map<String, List<InterventionConfig>> instanceAllConfig = Maps.newHashMap();

    /**
     * 所要查询对象的干预配置
     */
    private Map<String, InterventionConfig> instanceInterventionConfig = Maps.newHashMap();
    /**
     * 原始的时间段
     */
    private Map<String,List<TimeSlotEntity>> instanceTimeSlotMap = Maps.newHashMap();

    private Map<String,List<TimeSlotEntity>> instanceAggregationTimeSlotMap = Maps.newHashMap();


    public void fillParentInterventionConfig(@NonNull Function<InterventionInfoQueryParam, Map<String, List<InterventionConfig>>> function) {
        instanceAllConfig = function.apply(queryParam);
    }
    //按照空间、楼层、建筑、项目的配置 循序填充
    public void fillInstanceAllConfig(String instanceId,List<InterventionConfig> configs){
        if(CollectionUtils.isNotEmpty(configs)){
            List<InterventionConfig> singleInstanceConfig =   instanceAllConfig.get(instanceId);
            if(singleInstanceConfig==null){
                singleInstanceConfig = new ArrayList<>();
                instanceAllConfig.put(instanceId,singleInstanceConfig);
            }
            singleInstanceConfig.addAll(configs);
        }

    }

    public void sourceTimeSlot(){
        if(instanceAllConfig.isEmpty()){
            return;
        }
        instanceAllConfig.forEach((instanceId,allConfig)->{
            if(CollectionUtils.isNotEmpty(allConfig)){
                List<TimeSlotEntity> sourceConfigTimeSlot = new ArrayList<>();
                allConfig.forEach(config->{
                    TimeSlotEntity timeSlot = new TimeSlotEntity(config.getId(),config.getInstanceType(),
                            config.getBeginTime(),config.getEndTime());
                    sourceConfigTimeSlot.add(timeSlot);
                });
                instanceTimeSlotMap.put(instanceId,sourceConfigTimeSlot);
            }
        });
    }

    public void aggregationTimeSlot(){
        if(instanceTimeSlotMap.isEmpty()){
            return;
        }
        instanceTimeSlotMap.forEach((instanceId,timeSlotList)->{
            List<TimeSlotEntity> aggregationTimeSlot =  instanceAggregationTimeSlotMap.get(instanceId);
            if(aggregationTimeSlot==null){
                aggregationTimeSlot = new ArrayList<>();

            }
            List<TimeSlotEntity> finalAggregationTimeSlot = aggregationTimeSlot;
            timeSlotList.forEach(sourceTime->{
                List<TimeSlotEntity> addTimeSlot =  TimeSlotEntity.calculateNonOverlappingPartsWithExisting(finalAggregationTimeSlot,sourceTime);
                if(CollectionUtils.isNotEmpty(addTimeSlot)){
                    finalAggregationTimeSlot.addAll(addTimeSlot);
                }
            });
            instanceAggregationTimeSlotMap.put(instanceId,finalAggregationTimeSlot);
        });
    }

    private List<TimeSlotEntity> aggregationTime(List<TimeSlotEntity> sourceTimeSlots){
        if(CollectionUtils.isEmpty(sourceTimeSlots)){
            return new ArrayList<>();
        }
//        sourceTimeSlots.forEach(source);
        return null;


    }

    public void fillInstanceInterventionConfig(@NonNull Function<InterventionInfoQueryParam, Map<String, InterventionConfig>> function) {
        instanceInterventionConfig = function.apply(queryParam);
    }

//    /**
//     * 整合 对象级继承的
//     */
//    public void integrationObjectInterventionConfig() {
//
//        instanceInterventionConfig.forEach((key, value) -> {
//            // 没有配置才需要向上找父的配置，取父配置时只需要取第一个有配置
//            if (!CollectionUtils.isEmpty(instanceParentInterventionConfig.get(key))) {
//
//
//            }
//
//        });
//
//    }
//
//    /**
//     * 字段继承时 需要对配置(json格式)进行遍历
//     *
//     * @param fieldKeys 给定类型所需要的配置key列表
//     */
//    @SneakyThrows
//    public void integrationFieldInterventionConfig(List<String> fieldKeys) throws JsonProcessingException {
//
//        if (!CollectionUtils.isEmpty(fieldKeys)) {
//            //字段继承父级字段为空 对应的标识修改为 对应为1时标识改字段在父级找到
//            int[] parentFieldExistFlag = new int[fieldKeys.size()];
//            instanceInterventionConfig.values().stream().filter(p-> StringUtils.isNotEmpty(p.getConfigData())).forEach(instance -> {
//
//                String configData = instance.getConfigData();
//                JSONObject jsonObject = JSONObject.parseObject(configData);
//                JSONObject targetInterventionConfig =  new JSONObject();
//
//                AtomicInteger fillStopFlag  = new AtomicInteger(0);
//                if (jsonObject != null) {
//                    fieldKeys.forEach(key->{
//                        int flagTemp = 0;
//                        if(jsonObject.get(key)==null){
//                            instanceParentInterventionConfig.get(instance.getId());
//
//                        }else {
//                            targetInterventionConfig.put(key,jsonObject.get(key));
//                            flagTemp = flagTemp+1;
//                        }
//                        fillStopFlag.set(flagTemp);
//                    });
//                    jsonObject.keySet();
//                }
//
//
//            });
//
//        }
//
//    }

//   void test(@NonNull InterventionConfig interventionConfig,@NonNull List<String>fieldKeys){
//
//       String configData = interventionConfig.getConfigData();
//       //原始配置
//       JSONObject source = JSONObject.parseObject(configData);
//
//       int parentStopFlag = 0;
//       if(source==null){
//           source = new JSONObject();
//       }
//       while (parentStopFlag!=fieldKeys.size()){
//           //判断是否继续
//           for(String fieldKey:fieldKeys){
//               if(source.get(fieldKey)!=null){
//                   parentStopFlag= parentStopFlag+1;
//               }
//           }
//           List<InterventionConfig> parentConfigList = instanceParentInterventionConfig.get(interventionConfig.getId());
//           if(CollectionUtils.isEmpty(parentConfigList)){
//               break;
//           }
//           for(InterventionConfig config:parentConfigList){
//               config.getConfigData();
//           }
//           JSONObject targetInterventionConfig =  new JSONObject();
////           for (String key : target.keySet()) {
////               // 如果 source 中不存在该 key，则从 target 中获取值并添加到 source
////               if (!source.containsKey(key)) {
////                   source.put(key, target.get(key));
////               }
////           }
//
//       }
//
//
//
//
////       fieldKeys.forEach(key->{
////               int flagTemp = 0;
////               if(jsonObject.get(key)==null){
////                    List<InterventionConfig> parentConfigList =  instanceParentInterventionConfig.get(interventionConfig.getId());
////                    if(!CollectionUtils.isEmpty(parentConfigList)){
////                        for(InterventionConfig config: parentConfigList){
////                            if(config==null){
////                                continue;
////                            }
////
////
////                        }
////                    }
////               }
////           });
//
//    }

    void handlerField(JsonNode jsonNode, InterventionConfigEntity entity) {

    }


    /**
     * 查询失败标识
     */
    private boolean failed = true;

    @Override
    public String logTag() {
        return "人工干预需求详情-详情查询上下文";
    }
}
