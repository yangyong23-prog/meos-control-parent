package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.model.dto.DmpQueryStringsDto;
import com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile.DmpFileBodyDto;
import com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile.DtpFileSimpleInfoDto;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;


/**
 * 中台文件接口
 *
 * @author 叶丁辉
 * @date 2022/6/24
 */
@FeignClient(name = "${persagy.serviceName.bdtpFile:dtp-file-server}", contextId = "dmp-file", url = "${inner-url.bdtp.dtp-file.service:}")
public interface DmpFileServiceClient {


    /**
     * 查询文件下载信息
     *
     * @param dmpQueryStrings 租户信息
     * @param body            fileIds参数
     * @return 文件地址列表，fileId->fileUrl，先判断isSuccess为true
     */
    @PostMapping(value = "/file/queryFetchUrlsByFileIds", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<Map<String, String>> queryFetchUrlsByFileIds(@SpringQueryMap DmpQueryStringsDto dmpQueryStrings, @RequestBody DmpFileBodyDto body);

    /**
     * 查询文件信息
     *
     * @param body fileIds参数</body>
     * @return {@link CustomerResponse}<{@link Map}<{@link String}, {@link String}>>
     */
    @PostMapping(value = "/simple/file/querySimpleFiles", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomerResponse<List<DtpFileSimpleInfoDto>> querySimpleFiles(@SpringQueryMap DmpQueryStringsDto dmpQueryStrings, @RequestBody DmpFileBodyDto body);

}
