package com.bentest.spiders.listpage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bentest.spiders.constant.AMZConstant;

import cn.hutool.core.util.StrUtil;

@Service
public class PageHtmlProcess {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	

	public Map<String,String> getNextPage(int type, String htmlFilePath){
		
		Map<String,String> nextPageMap = getNextPageByType(type, htmlFilePath);
		if(nextPageMap != null && nextPageMap.size() > 0) {
			return nextPageMap;
		}
		
		// 指定类型获取不到，用其他类型重试
		Map<Integer,Integer> typeMap = new HashMap<>();
		typeMap.put(1, 1);
		typeMap.put(2, 2);
		// 移除掉试过的类型
		typeMap.remove(type);
		
		for(Integer key : typeMap.keySet()) {
			nextPageMap = getNextPageByType(key, htmlFilePath);
			if(nextPageMap != null && nextPageMap.size() > 0) {
				return nextPageMap;
			}
		}
		
		return null;
	}
	
	public Map<String,String> getNextPageByType(int type, String htmlFilePath){
		
		Map<String,String> nextPageMap = null;
		if(type == AMZConstant.VALUE_PAGE_TYPE_FIRST) {
			nextPageMap = getFirstNextPage(htmlFilePath);
		}
		else if(type == AMZConstant.VALUE_PAGE_TYPE_AFTER) {
			nextPageMap = getNextSecondAfter(htmlFilePath);
		}
		else {
			log.error("解析html获取产品列表，未定义的类型。");
			return null;
		}
		
		if(nextPageMap == null || nextPageMap.size() < 1) {
			return nextPageMap;
		}
		
		nextPageMap.put(AMZConstant.CMD_KEY_PAGE_TYPE, "2");
		
		return nextPageMap;
	}
	
	private Map<String,String> getFirstNextPage(String htmlFilePath) {
		try
		{
			Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
		    if(doc == null) {
		    	log.error("解析html获取列表首页下一页，获取不到页面对象。htmlFilePath="+htmlFilePath);
		    	return null;
		    }
		    
		    Elements aEls = doc.select("a#pagnNextLink");
		    if(aEls == null || aEls.size() < 1) {
		    	return null;
		    }
		    
		    String href = aEls.attr("href");
		    if(StrUtil.isBlank(href)) {
		    	return null;
		    }
		    
		    Map<String,String> dataMap = new HashMap<>();
		    dataMap.put(AMZConstant.CMD_KEY_NEXT_PAGE_URL, href);
		    
		    return dataMap;
		} 
		catch (Exception e) 
		{
		    log.error("解析html获取列表首页下一页，异常。path="+htmlFilePath, e);
		    return null;
		}
	}
	
	private Map<String,String> getNextSecondAfter(String htmlFilePath) {
		try
		{
			Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
		    if(doc == null) {
		    	log.error("解析html获取列表第二页后的下一页，获取不到页面对象。htmlFilePath="+htmlFilePath);
		    	return null;
		    }
		    
		    Elements aEls = doc.select("span[data-component-type=s-pagination] a");
		    if(aEls == null || aEls.size() < 1) {
		    	return null;
		    }
		    
		    for(Element aEl : aEls) {
		    	if(aEl == null) {
		    		continue;
		    	}
		    	String text = aEl.text();
		    	if(StrUtil.isBlank(text)) {
		    		continue;
		    	}
		    	text = text.toLowerCase();
		    	if(text.indexOf("next") < 0) {
		    		continue;
		    	}
		    	
	    		String href = aEl.attr("href");
	    		if(StrUtil.isBlank(href)) {
	    			continue;
	    		}
	    		Map<String,String> dataMap = new HashMap<>();
			    dataMap.put(AMZConstant.CMD_KEY_NEXT_PAGE_URL, href);
			    return dataMap;
		    }
		    
		    return null;
		} 
		catch (Exception e) 
		{
		    log.error("解析html获取列表第二页后的下一页，异常。path="+htmlFilePath, e);
		    return null;
		}
	}
	
	
	public static void main(String[] args) {
		try {
			PageHtmlProcess html = new PageHtmlProcess();
			//String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\list-page\\Arts & Crafts-123456789.html";
			//String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\list-page\\Arts & Crafts-page2-123456789.html";
			String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\list-page\\Automotive\\Tools & Equipment-1-123456789.html";
			Map<String,String> dataMap = html.getNextPage(2, htmlFilePath);
			System.out.println(dataMap.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
