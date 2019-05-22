package com.bentest.spiders.service.product;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.bentest.spiders.entity.AmzProduct;
import com.bentest.spiders.entity.AmzProductBsr;
import com.bentest.spiders.repository.AmzCmdtaskRespository;
import com.bentest.spiders.repository.AmzProductBsrRespository;
import com.bentest.spiders.repository.AmzProductRespository;

import cn.hutool.core.util.StrUtil;

@Service
public class AmzProductService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ProductHtmlProcess productHtmlProcess;
	
	@Autowired
	private AmzProductRespository amzProductRespository;
	
	@Autowired
	private AmzProductBsrRespository amzProductBsrRespository;
	
	@Autowired
	AmzCmdtaskRespository cmdtaskRespository;
	
	/*@Autowired
    private SystemConfig systemConfig;*/
	
	public int dealProduct(String cmdText) {
		
		try {
			if(StrUtil.isBlank(cmdText)) {
				log.error("处理产品，指令内容为空。");
				return -1;
			}
			
			AmzProduct productTemp = JSON.parseObject(cmdText, AmzProduct.class);
			if(productTemp == null) {
				log.error("处理产品，产品对象为null。cmdText="+cmdText);
				return -2;
			}
			if(StrUtil.isBlank(productTemp.getHtmlFilePath())) {
				log.error("处理产品，html文件路径为空。cmdText="+cmdText);
				return -3;
			}
			
			AmzProduct product = productHtmlProcess.getProductFromHtml(productTemp.getHtmlFilePath());
			if(product == null) {
				log.error("处理产品，解析html获取产品对象为null。cmdText="+cmdText);
				return -4;
			}
			product.setHtmlFilePath(productTemp.getHtmlFilePath());
			product.setProdUrl(productTemp.getProdUrl());
			
			if(product.getProdPrice() == null || product.getProdPrice() <=0) {
				product.setProdPrice(productTemp.getProdPrice());
			}
			
			amzProductRespository.save(product);
			
			List<AmzProductBsr> bsrList = product.getProductBsrList();
			if(bsrList != null && bsrList.size() > 0) {
				amzProductBsrRespository.saveAll(bsrList);
			}
			
			return 1;
		} catch (Exception e) {
			log.error("处理产品，异常。cmdText="+cmdText, e);
			return -9999;
		}
	}
	
	

}
