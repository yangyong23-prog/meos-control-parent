package com.persagy.apccontrol.basic.model.vo.systemgraph;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author 薛涛
 * @Description 系统图细节调整VO
 * @Date 17:32 2024/6/25
 * @Param
 * @return
 **/
@Data
public class SystemGraphDetailVO {

    @Schema(description = "id")
    private Long id;

    /**
     * 系统图副本id
     */
    @Schema(description = "系统图副本id")
    private Long systemGraphDuplicateId;


    /**
     * 项目id
     */
    @Schema(description = "项目id")
    private String projectId;

    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 1:设备2:信息点3:标签4:分组
     */
    @Schema(description = "1:设备2:信息点3:标签4:分组")
    private Integer type;

    /**
     * 细节调整相关数据，如信息点列表，名称，位置等
     */
    @Schema(description = "细节调整相关数据，如信息点列表，名称，位置等")
    private Object data;


    private Map<String, Set<String>> objectAndInfoCodeMapData;


    /**
     * @return com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphDetailVO
     * @Author 薛涛
     * @Description systemGraphDetailToVO
     * @Date 19:04 2024/7/3
     * @Param [systemGraphDetail, abstractIotDataCacheServer]
     **/
    public static SystemGraphDetailVO systemGraphDetailToVO(SystemGraphDetail systemGraphDetail){

        SystemGraphDetailVO systemGraphDetailVONew = new SystemGraphDetailVO();
        BeanUtil.copyProperties(systemGraphDetail, systemGraphDetailVONew);
        JSONObject jsonObject = JSONObject.parseObject(systemGraphDetail.getData());
        String type = jsonObject.getString("type");

        Map<String, Set<String>> objectAndInfoCodeMap = new HashMap<>();
        List<JSONObject> infoCodeObjects = new ArrayList<>();
        if (type.equals("infoCodeToEquips")) {
            JSONArray groups = jsonObject.getJSONArray("groups");
            if (CollectionUtils.isNotEmpty(groups)) {
                for (int i = 0; i < groups.size(); i++) {
                    JSONObject groupsJsonObject = groups.getJSONObject(i);
                    JSONObject infoCodeObject = new JSONObject();
                    String localId = groupsJsonObject.getString("localId");
                    if(StringUtils.isBlank(localId)){
                        localId =  groupsJsonObject.getString("id");
                    }
                    String infoCode = groupsJsonObject.getString("infoCode");
                    infoCodeObject.put("localId", localId);
                    infoCodeObject.put("infoCode", infoCode);
                    infoCodeObjects.add(infoCodeObject);
                }
            }

        }

        if (type.equals("tab")) {
            JSONObject infoObject = jsonObject.getJSONObject("infoObject");
            JSONArray linkCodes = infoObject.getJSONArray("linkCodes");
            String localId = infoObject.getString("localId");
            if (CollectionUtils.isNotEmpty(linkCodes)) {
                for (int i = 0; i < linkCodes.size(); i++) {
                    JSONObject groupsJsonObject = linkCodes.getJSONObject(i);
                    JSONObject infoCodeObject = new JSONObject();
                    String infoCode = groupsJsonObject.getString("infoCode");
                    infoCodeObject.put("localId", localId);
                    infoCodeObject.put("infoCode", infoCode);
                    infoCodeObjects.add(infoCodeObject);
                }
            }
            JSONArray equipmentIotInfoCodes = jsonObject.getJSONArray("equipmentIotInfoCodes");
            if (CollectionUtils.isNotEmpty(equipmentIotInfoCodes)) {
                for (int i = 0; i < equipmentIotInfoCodes.size(); i++) {
                    JSONObject equipmentIotInfoCode = equipmentIotInfoCodes.getJSONObject(i);
                    JSONArray infoCodes = equipmentIotInfoCode.getJSONArray("infoCodes");
                    for (int j = 0; j < infoCodes.size(); j++) {
                        JSONObject infoCodeJson = infoCodes.getJSONObject(j);
                        JSONObject infoCodeObject = new JSONObject();
                        localId = infoCodeJson.getString("objectId");
                        String infoCode = infoCodeJson.getString("infoCode");
                        infoCodeObject.put("localId", localId);
                        infoCodeObject.put("infoCode", infoCode);
                        infoCodeObjects.add(infoCodeObject);
                    }

                }
            }
            String infoCode = infoObject.getString("infoCode");
            if(StringUtils.isNotBlank(infoCode)){
                JSONObject infoCodeObject = new JSONObject();
                infoCodeObject.put("localId", localId);
                infoCodeObject.put("infoCode", infoCode);
                infoCodeObjects.add(infoCodeObject);
            }
        }

        if (type.equals("equipToInfoCodes")) {
            JSONArray groups = jsonObject.getJSONArray("groups");
            if (CollectionUtils.isNotEmpty(groups)) {
                for (int i = 0; i < groups.size(); i++) {
                    JSONObject groupsJsonObject = groups.getJSONObject(i);
                    JSONObject infoCodeObject = new JSONObject();
                    String localId = groupsJsonObject.getString("localId");
                    String infoCode = groupsJsonObject.getString("infoCode");
                    infoCodeObject.put("localId", localId);
                    infoCodeObject.put("infoCode", infoCode);
                    infoCodeObjects.add(infoCodeObject);
                }
            }
            JSONArray equipmentIotInfoCodes = jsonObject.getJSONArray("equipmentIotInfoCodes");
            if (CollectionUtils.isNotEmpty(equipmentIotInfoCodes)) {
                for (int i = 0; i < equipmentIotInfoCodes.size(); i++) {
                    JSONObject equipmentIotInfoCode = equipmentIotInfoCodes.getJSONObject(i);
                    JSONArray infoCodes = equipmentIotInfoCode.getJSONArray("infoCodes");
                    for (int j = 0; j < infoCodes.size(); j++) {
                        JSONObject infoCodeJson = infoCodes.getJSONObject(j);
                        JSONObject infoCodeObject = new JSONObject();
                        String localId = infoCodeJson.getString("objectId");
                        String infoCode = infoCodeJson.getString("infoCode");
                        infoCodeObject.put("localId", localId);
                        infoCodeObject.put("infoCode", infoCode);
                        infoCodeObjects.add(infoCodeObject);
                    }

                }
            }

        }
        if (type.equals("tabGroup")) {
            JSONArray groups = jsonObject.getJSONArray("groups");
            if (CollectionUtils.isNotEmpty(groups)) {
                for (int i = 0; i < groups.size(); i++) {
                    JSONObject groupsJsonObject = groups.getJSONObject(i);
                    JSONObject infoCodeObject = new JSONObject();
                    String localId = groupsJsonObject.getString("localId");
                    JSONArray linkCodes = groupsJsonObject.getJSONArray("linkCodes");
                    if (CollectionUtils.isNotEmpty(linkCodes)) {
                        for (int j = 0; j < linkCodes.size(); j++) {
                            JSONObject linkCode = linkCodes.getJSONObject(j);
                            String infoCode = linkCode.getString("infoCode");
                            infoCodeObject = new JSONObject();
                            infoCodeObject.put("localId", localId);
                            infoCodeObject.put("infoCode", infoCode);
                            infoCodeObjects.add(infoCodeObject);
                        }
                    } else {
                        String infoCode = groupsJsonObject.getString("infoCode");
                        infoCodeObject.put("localId", localId);
                        infoCodeObject.put("infoCode", infoCode);
                        infoCodeObjects.add(infoCodeObject);
                    }
                }
            }
        }
        if (type.equals("singleInfoCode")) {
            JSONObject infoObject = jsonObject.getJSONObject("infoObject");
            String localId = jsonObject.getString("key");
            JSONArray linkCodes = infoObject.getJSONArray("linkCodes");
            if (CollectionUtils.isNotEmpty(linkCodes)) {
                for (int i = 0; i < linkCodes.size(); i++) {
                    JSONObject linkCode = linkCodes.getJSONObject(i);
                    String infoCode = linkCode.getString("infoCode");
                    JSONObject infoCodeObject = new JSONObject();
                    infoCodeObject.put("localId", localId);
                    infoCodeObject.put("infoCode", infoCode);
                    infoCodeObjects.add(infoCodeObject);
                }
            }else {
                JSONObject infoCodeObject = new JSONObject();
                String infoCode = infoObject.getString("infoCode");
                infoCodeObject.put("localId", localId);
                infoCodeObject.put("infoCode", infoCode);
                infoCodeObjects.add(infoCodeObject);
            }
        }
        if (CollectionUtils.isNotEmpty(infoCodeObjects)) {
            for (JSONObject infoCodeObject : infoCodeObjects) {
                String localId = infoCodeObject.getString("localId");
                Set<String> infoCodes = objectAndInfoCodeMap.get(localId);
                String infoCode = infoCodeObject.getString("infoCode");
                if (CollectionUtils.isEmpty(infoCodes)) {
                    infoCodes = new HashSet<>();
                    infoCodes.add(infoCode);
                    objectAndInfoCodeMap.put(localId, infoCodes);
                } else {
                    infoCodes.add(infoCode);
                    objectAndInfoCodeMap.put(localId, infoCodes);
                }
            }
        }
        if (MapUtils.isEmpty(objectAndInfoCodeMap)) {
            systemGraphDetailVONew.setData(JSONObject.parseObject(systemGraphDetail.getData()));
            return systemGraphDetailVONew;
        }else {
            systemGraphDetailVONew.setData(JSONObject.parseObject(systemGraphDetail.getData()));
            systemGraphDetailVONew.setObjectAndInfoCodeMapData(objectAndInfoCodeMap);
            return systemGraphDetailVONew;
        }

    }


}
