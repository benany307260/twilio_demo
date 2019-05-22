package com.bentest.spiders.listpage;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bentest.spiders.config.SystemConfig;
import com.bentest.spiders.constant.AMZConstant;
import com.bentest.spiders.constant.CmdType;
import com.bentest.spiders.entity.AmzCmdtask;
import com.bentest.spiders.repository.AmzCmdtaskRespository;

import cn.hutool.core.util.StrUtil;

@Service
public class PageService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PageHtmlProcess pageHtmlProcess;
	
	@Autowired
	AmzCmdtaskRespository cmdtaskRespository;
	
	@Autowired
    private SystemConfig systemConfig;
	
	public int dealPage(String cmdText) {
		
		try {
			if(StrUtil.isBlank(cmdText)) {
				log.error("处理产品列表下一页，指令内容为空。");
				return -1;
			}
			
			Map<String,String> dataMap = JSON.parseObject(cmdText, new TypeReference<Map<String, String>>(){});
			
			if(dataMap == null || dataMap.size() < 1) {
				log.error("处理产品列表下一页，数据为null。cmdText="+cmdText);
				return -2;
			}
			
			String pageTypeStr = dataMap.get(AMZConstant.CMD_KEY_PAGE_TYPE);
			if(StrUtil.isBlank(pageTypeStr)) {
				log.error("处理产品列表下一页，页面类型为空。cmdText="+cmdText);
				return -3;
			}
			String htmlFilePath = dataMap.get(AMZConstant.CMD_KEY_HTML_FILE_PATH);
			if(StrUtil.isBlank(htmlFilePath)) {
				log.error("处理产品列表下一页，页面文件路径为空。cmdText="+cmdText);
				return -4;
			}
			Map<String,String> nextPageMap = pageHtmlProcess.getNextPage(Integer.parseInt(pageTypeStr), htmlFilePath);
			if(nextPageMap == null || nextPageMap.size() < 1) {
				log.error("处理产品列表下一页，获取下一页为空。cmdText="+cmdText);
				return -5;
			}
			
			String nextPageUrl = nextPageMap.get(AMZConstant.CMD_KEY_NEXT_PAGE_URL);
			if(StrUtil.isBlank(nextPageUrl)) {
				log.error("处理产品列表下一页，获取下一页url为空。cmdText="+cmdText);
				return -6;
			}
			
			if(nextPageUrl.indexOf(AMZConstant.AMZ_US_DOMAIN) < 0) {
				nextPageUrl = systemConfig.getAmzUrl() + nextPageUrl;
				nextPageMap.put(AMZConstant.CMD_KEY_NEXT_PAGE_URL, nextPageUrl);
			}
			
			String cmdTextJson = JSON.toJSONString(nextPageMap);
			AmzCmdtask cmd106 = new AmzCmdtask(CmdType.CMD106, cmdTextJson);
			
			cmdtaskRespository.save(cmd106);
			return 1;
		} catch (Exception e) {
			log.error("处理产品列表下一页，异常。cmdText="+cmdText, e);
			return -9999;
		}
	}
	
}
