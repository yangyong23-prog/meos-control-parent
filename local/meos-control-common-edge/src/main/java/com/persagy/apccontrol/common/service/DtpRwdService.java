package com.persagy.apccontrol.common.service;

import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface DtpRwdService {

    /**
     * 查询末级冷冻泵组关联的空间ID
     *
     * @param acatSystemIdsAndLastFreezePumpGroupId 空调末端系统ID -> 末级冷冻泵组ID
     * @return 末级冷冻泵组ID -> 空调末端系统关联的空间ID
     * @since v5.7.0
     */
    public Map<String, Set<String>> queryLastFreezePumpGroupRelationSpaceIds(@NonNull Map<String, Collection<String>> acatSystemIdsAndLastFreezePumpGroupId);

    /**
     * 查询中央供冷系统ID
     *
     * @return 中央供冷系统ID
     * @since v5.7.0
     */
    public Set<String> queryCentralCoolingSystemIds();

    /**
     * 查询冷热源系统ID
     *
     * @return 中央供冷系统ID
     * @since v5.7.0
     */
    public Set<String> queryHotColdStationSystemIds();

    /**
     * 通过系统ID 查询系统下的本组信息
     *
     * @param systemId
     * @return
     */
    public Set<String> getFreezePump(Set<String> systemId);


    /**
     * 获取当前系统和泵组的关系
     * key 泵组id val 系统id
     *
     * @return
     */
    public Map<String, String> getSystemIdByFreezePumpId();

    /**
     * 获取泵组关联的系统 如果不是泵组就直接返回原id
     *
     * @param objId
     * @return
     */
    public String getSystemIdByFreezePumpId(String objId);

}
