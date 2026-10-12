package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.framework.database.service.SuperService;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【rc_info_code_cfg(信息点配置 )】的数据库操作Service
 * @createDate 2023-03-15 20:30:22
 */
public interface RcInfoCodeCfgService extends SuperService<RcInfoCodeCfg> {

    /**
     * 获取当前项目所有的信息点配置
     *
     * @param classCode
     * @return
     */
    List<RcInfoCodeCfg> queryAllInfoCodeConfigs(String classCode);


    /**
     * 查询公共库的信息点配置信息
     * 返回数据 classCode,infoCode,DataSource ,Unit
     * 筛选条件 DataSource ,Unit 任意有值的数据.
     *
     * @return key = classCode + infoCode, val 包含classCode,infoCode,DataSource ,Unit的数据
     */
    Map<String, RcInfoCodeCfg> queryAllDataSourceAndUnit(@NotEmpty Set<String> classCode);

}
