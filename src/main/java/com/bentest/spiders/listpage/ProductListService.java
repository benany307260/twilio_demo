package com.bentest.spiders.listpage;

import java.util.ArrayList;
import java.util.List;
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
import com.bentest.spiders.entity.AmzProduct;
import com.bentest.spiders.repository.AmzCmdtaskRespository;

import cn.hutool.core.util.StrUtil;

@Service
public class ProductListService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ProductListHtmlProcess productListHtmlProcess;
	
	@Autowired
	AmzCmdtaskRespository cmdtaskRespository;
	
	@Autowired
    private SystemConfig systemConfig;
	
	public int dealProductList(String cmdText) {
		
		try {
			if(StrUtil.isBlank(cmdText)) {
				log.error("处理产品列表，指令内容为空。");
				return -1;
			}
			
			Map<String,String> dataMap = JSON.parseObject(cmdText, new TypeReference<Map<String, String>>(){});
			
			if(dataMap == null || dataMap.size() < 1) {
				log.error("处理产品列表，数据为null。cmdText="+cmdText);
				return -2;
			}
			
			String pageTypeStr = dataMap.get(AMZConstant.CMD_KEY_PAGE_TYPE);
			if(StrUtil.isBlank(pageTypeStr)) {
				log.error("处理产品列表，页面类型为空。cmdText="+cmdText);
				return -3;
			}
			String htmlFilePath = dataMap.get(AMZConstant.CMD_KEY_HTML_FILE_PATH);
			if(StrUtil.isBlank(htmlFilePath)) {
				log.error("处理产品列表，页面文件路径为空。cmdText="+cmdText);
				return -4;
			}
			List<AmzProduct> prodList = productListHtmlProcess.getListPage(Integer.parseInt(pageTypeStr), htmlFilePath);
			if(prodList == null || prodList.size() < 1) {
				log.error("处理产品列表，获取产品列表为空。cmdText="+cmdText);
				return -5;
			}
			
			List<AmzCmdtask> cmdList = new ArrayList<>();

			for(AmzProduct product : prodList) {
				if(product == null) {
					log.error("处理产品列表，产品列表中产品对象为null。cmdText="+cmdText);
					continue;
				}
				
				String prodUrl = product.getProdUrl();
				if(StrUtil.isBlank(prodUrl)) {
					log.error("处理产品列表，产品的prodUrl为空。cmdText="+cmdText);
					continue;
				}
				if(prodUrl.indexOf(AMZConstant.AMZ_US_DOMAIN) < 0) {
					prodUrl = systemConfig.getAmzUrl() + prodUrl;
					product.setProdUrl(prodUrl);
				}
				
				String cmdTextJson = JSON.toJSONString(product);
				AmzCmdtask cmd = new AmzCmdtask(CmdType.CMD104, cmdTextJson);
				
				cmdList.add(cmd);
			}
			
			cmdtaskRespository.saveAll(cmdList);
			return 1;
		} catch (Exception e) {
			log.error("处理产品列表，异常。cmdText="+cmdText, e);
			return -9999;
		}
	}
	
}
