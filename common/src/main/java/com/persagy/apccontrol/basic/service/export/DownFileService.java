package com.persagy.apccontrol.basic.service.export;

import com.persagy.apccontrol.basic.service.FileService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class DownFileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DownFileService.class);

	@Autowired
	private FileService fileService;

	public String handlerDownLoadByWorkbook(Workbook wb, String excelName) {
		String fileSuffix = "xls";
		String fileKey = fileService.initFileId(excelName, fileSuffix);
		InputStream is = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			byte[] bookByteAry = out.toByteArray();
			is = new ByteArrayInputStream(bookByteAry);
			fileService.uploadFileToImageService(is, fileKey);
		} catch (Exception e) {
			LOGGER.error("handlerDownLoadByWorkbook error:{}", ExceptionUtils.getStackTrace(e));
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					LOGGER.error("handlerDownLoadByWorkbook is.close error:{}", ExceptionUtils.getStackTrace(e));
				}
			}
		}
		return fileKey;
	}
}
