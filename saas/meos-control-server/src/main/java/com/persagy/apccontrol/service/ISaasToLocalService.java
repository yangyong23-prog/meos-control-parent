package com.persagy.apccontrol.service;

import com.persagy.apccontrol.basic.model.vo.LocalityProjectCfgVO;

public interface ISaasToLocalService {

    LocalityProjectCfgVO getLocalBaseUrl(String projectId);

    String handleHeart(String heartPackage) throws Exception;

    void synUser(String groupCode, String projectId);
}
