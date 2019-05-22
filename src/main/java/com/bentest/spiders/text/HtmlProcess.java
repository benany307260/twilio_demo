package com.bentest.spiders.text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;

@Service
public class HtmlProcess {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	

	
	public List<String> getUa(String htmlFilePath) {
		try
		{
			Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
		    if(doc == null) {
		    	log.error("解析html获取列表首页下一页，获取不到页面对象。htmlFilePath="+htmlFilePath);
		    	return null;
		    }
		    
		    Elements divEls = doc.select("div#liste li a");
		    if(divEls == null || divEls.size() < 1) {
		    	return null;
		    }
		    
		    List<String> uaList= new ArrayList<String>();
		    
		    for(Element a : divEls) {
		    	String text = a.text();
		    	if(StrUtil.isBlank(text)) {
		    		continue;
		    	}
		    	if(text.indexOf("Mozilla") < 0) {
		    		continue;
		    	}
		    	if(text.indexOf("Mozilla/4.0") > -1) {
		    		continue;
		    	}
		    	uaList.add(text);
		    	System.out.println(text);
		    }
		    
		    return uaList;
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
		    return null;
		}
	}
	
	
	
	public static void main(String[] args) {
		try {
			HtmlProcess html = new HtmlProcess();
			String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\ua.html";
			html.getUa(htmlFilePath);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
