package com.bentest.spiders.service.department;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bentest.spiders.entity.AmzDepartment;

import cn.hutool.core.util.StrUtil;

@Service
public class DepSonHtmlProcess {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 解析html获取类目
	 * @param htmlFilePath
	 * @return
	 */
	public List<AmzDepartment> getDepsFromHtml(String htmlFilePath, String parentDepId) {
		try {
			Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
			if(doc == null) {
				log.error("解析html获取子类目，获取不到页面对象。htmlFilePath="+htmlFilePath+",parentDepId="+parentDepId);
				return null;
			}
			
			List<AmzDepartment> depList = getDepsInLeftNav(doc, parentDepId);
			if(depList != null && depList.size() > 0) {
				return depList;
			}
			
			depList = getDepsInDepartments(doc);
			if(depList != null && depList.size() > 0) {
				return depList;
			}
			
			return null;
		} catch (Exception e) {
			log.error("解析html获取子类目，异常。htmlFilePath="+htmlFilePath+",parentDepId="+parentDepId, e);
			return null;
		}
	    
	    
	}
	
	/**
	 * 解析html获取类目
	 * @param htmlFilePath
	 * @return
	 */
	private List<AmzDepartment> getDepsInLeftNav(Document doc, String parentDepId) {
		try
		{
		    Elements ulEls = doc.select("div#leftNav ul");
		    if(ulEls == null || ulEls.size() < 1) {
				log.error("解析html用LeftNav获取子类目，获取不到ul数据。parentDepId="+parentDepId);
				return null;
			}
		    
		    Element firstUl = ulEls.first();
		    if(firstUl == null) {
		    	log.error("解析html用LeftNav获取子类目，获取不到第一个ul数据。parentDepId="+parentDepId);
				return null;
		    }
		    
			Elements aEls = firstUl.select("li a");
			if(aEls == null || aEls.size() < 1) {
				log.error("解析html用LeftNav获取子类目，获取不到数据。parentDepId="+parentDepId);
				return null;
			}
			
			List<AmzDepartment> depList = new ArrayList<>();
			
			int softNum = 1;
			
			for (Element aEl : aEls) {
				if(aEl == null) {
					continue;
				}
				String url = aEl.attr("href");
				if(StrUtil.isBlank(url)) {
					continue;
				}
				String key_bbn = "bbn="+parentDepId;
				// 不存在关键字
				if (url.indexOf(key_bbn) < 0) {
					continue;
				}
				
				String depName = aEl.text();
				if(StrUtil.isBlank(depName)) {
					continue;
				}
				
				AmzDepartment dep = new AmzDepartment(depName, url, softNum);
				depList.add(dep);
				softNum++;
			}
		    
		    return depList;
		} 
		catch (Exception e) 
		{
		    log.error("解析html用LeftNav获取子类目，异常。parentDepId="+parentDepId, e);
		    return null;
		}
	}
	
	/**
	 * 解析html获取类目
	 * @param htmlFilePath
	 * @return
	 */
	private List<AmzDepartment> getDepsInDepartments(Document doc) {
		try
		{
		    Elements ulEls = doc.select("div#departments ul");
		    if(ulEls == null || ulEls.size() < 1) {
				log.error("解析html用departments获取子类目，获取不到ul数据。");
				return null;
			}
		    
		    Element firstUl = ulEls.first();
		    if(firstUl == null) {
		    	log.error("解析html用departments获取子类目，获取不到第一个ul数据。");
				return null;
		    }
		    
			Elements aEls = firstUl.select("li a");
			if(aEls == null || aEls.size() < 1) {
				log.error("解析html用LeftNav获取子类目，获取不到数据。");
				return null;
			}
			
			List<AmzDepartment> depList = new ArrayList<>();
			
			int softNum = 1;
			
			for (Element aEl : aEls) {
				if(aEl == null) {
					continue;
				}
				String url = aEl.attr("href");
				if(StrUtil.isBlank(url)) {
					continue;
				}
				
				String depName = aEl.text();
				if(StrUtil.isBlank(depName)) {
					continue;
				}
				
				AmzDepartment dep = new AmzDepartment(depName, url, softNum);
				depList.add(dep);
				softNum++;
			}
		    
		    return depList;
		} 
		catch (Exception e) 
		{
		    log.error("解析html用departments获取子类目，异常。", e);
		    return null;
		}
	}
	
	
	public static void main(String[] args) {
		DepSonHtmlProcess html = new DepSonHtmlProcess();
		String path = "C:\\Users\\lenovo\\git\\pc_service\\page\\list-page\\Painting, Drawing & Art Supplies-123456789.html";
		html.getDepsFromHtml(path, "2747968011");
	}
}
