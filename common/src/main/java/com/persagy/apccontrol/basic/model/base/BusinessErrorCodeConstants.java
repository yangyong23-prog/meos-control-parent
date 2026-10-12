package com.persagy.apccontrol.basic.model.base;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.framework.common.exception.ErrorCode;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;

public interface BusinessErrorCodeConstants extends ErrorCodeConstants {

    ErrorCode BUSINESS_ERROR_K0001 = new ErrorCode("K0001", "loginType不能为空");

    ErrorCode BUSINESS_ERROR_K0002 = new ErrorCode("K0002", LocaleMessageUtil.getMessage("key_18536a769348415b8eaec0e4c49671c3"));

    ErrorCode BUSINESS_ERROR_K0003 = new ErrorCode("K0003", LocaleMessageUtil.getMessage("key_138c2e75c49d43c1838943eb8af45b3a"));

}
