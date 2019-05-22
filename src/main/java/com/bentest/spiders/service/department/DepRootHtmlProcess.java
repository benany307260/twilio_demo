package com.bentest.spiders.service.department;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.util.StrUtil;

@Service
public class DepRootHtmlProcess {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final String key_wishlistContent = "\"wishlistContent\"";
	private static final String key_yourAccountContent = "\"yourAccountContent\"";
	private static final String key_shopAllContent = "\"shopAllContent\"";
	private static final String key_cartContent = "\"cartContent\"";
	private static final String key_signinContent = "\"signinContent\"";
	private static final String key_accountListContent = "\"accountListContent\"";
	private static final String key_templates = "\"templates\"";
	
	/**
	 * 解析html获取类目
	 * @param htmlFilePath
	 * @return
	 */
	public Map<String,String> getDepsFromHtml(String htmlFilePath) {
		try
		{
		    Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
		    if(doc == null) {
		    	log.error("解析html获取类目，获取不到页面对象。htmlFilePath="+htmlFilePath);
		    	return null;
		    }
		    
		    // 拿到所有的script标签
		    Elements scriptElements = doc.getElementsByTag("script");
		    
		    String depJson = null;
		    
		    for (Element element : scriptElements) {

				// 檢查是否有detailInfoObject字串
				String script = element.toString();
				// 不存在关键字
				if (script.indexOf("shopAllContent") < 0) {
					continue;
				}
				
				String scriptStr = element.childNode(0).toString();
				System.out.println(scriptStr);
				
				String beginKey = key_shopAllContent;
				String endKey = getKeyNextJsonKey(beginKey, script);
				
				int beginIndex = scriptStr.indexOf(beginKey);
				int endIndex = scriptStr.indexOf(endKey)-1;
				
				String json = scriptStr.substring(beginIndex, endIndex);
				
				depJson = "{" + json + "}";
				//System.out.println(depJson);
				break;
		    }
		    
		    Map<String,String> depMap = getDepsFromJson(depJson);
		    return depMap;
		} 
		catch (Exception e) 
		{
		    log.error("解析html获取类目，异常。path="+htmlFilePath, e);
		    return null;
		}
	}
	
	private Map<String,String> getDepsFromJson(String json) {
		try {
			if(StrUtil.isBlank(json)) {
				log.error("解析json，获取类目，出入的json为空。");
				return null;
			}
			JSONObject jsonObj = JSON.parseObject(json);
			JSONObject shopAllContentObj = jsonObj.getJSONObject("shopAllContent");
			JSONObject templateObj = shopAllContentObj.getJSONObject("template");
			JSONObject dataObj = templateObj.getJSONObject("data");
			JSONArray itemsArray = dataObj.getJSONArray("items");
			
			// key为text，value为url
			Map<String,String> depMap = new HashMap<>();
			
			int size = itemsArray.size();
			for (int i = 0; i < size; i++){
				JSONObject jsonObject = itemsArray.getJSONObject(i);
				String text = jsonObject.getString("text");
				String url = jsonObject.getString("url");
				depMap.put(text, url);
			}
			return depMap;
		} catch (Exception e) {
			log.error("解析json，获取类目，异常。json="+json, e);
			return null;
		}
	}
	
	private String getKeyNextJsonKey(String targetKey, String jsonStr) {
		Map<String,Integer> jsonKeyMap = getJsonKeyMap(jsonStr);
		
		// 获取targetKey的序号
		int index_targetKey = jsonKeyMap.get(targetKey);
		jsonKeyMap.remove(targetKey);
		
		// 去掉比shopAllContent排序要小的key
		Iterator<Entry<String, Integer>> itJsonKeyMap= jsonKeyMap.entrySet().iterator();
		while(itJsonKeyMap.hasNext()) {
			Entry<String, Integer> entry = itJsonKeyMap.next();
			int index = jsonKeyMap.get(entry.getKey());
			if(index < index_targetKey) {
				itJsonKeyMap.remove();
			}
		}
		
		// 如果空了，就表示shopAllContent是最后一个
		if(jsonKeyMap.isEmpty()) {
			log.info("解析htmljson，获取"+targetKey+"的下一个key，没下一个key了，它自己就是最后一个");
			return targetKey;
		}
		
		int minIndex = Integer.MAX_VALUE;
		String minKey = null;
		// 在剩下的key中找一个排序最小的key
		for(String key : jsonKeyMap.keySet()) {
			int index = jsonKeyMap.get(key);
			if(index < minIndex) {
				minIndex = index;
				minKey = key;
			}
		}
		
		return minKey;
	}
	
	private Map<String,Integer> getJsonKeyMap(String jsonStr) {
		
		Map<String,Integer> jsonKeyMap = new HashMap<>();
		
		int index = jsonStr.indexOf(key_wishlistContent);
		jsonKeyMap.put(key_wishlistContent, index);
		
		index = jsonStr.indexOf(key_yourAccountContent);
		jsonKeyMap.put(key_yourAccountContent, index);
		
		index = jsonStr.indexOf(key_shopAllContent);
		jsonKeyMap.put(key_shopAllContent, index);
		
		index = jsonStr.indexOf(key_cartContent);
		jsonKeyMap.put(key_cartContent, index);
		
		index = jsonStr.indexOf(key_signinContent);
		jsonKeyMap.put(key_signinContent, index);
		
		index = jsonStr.indexOf(key_accountListContent);
		jsonKeyMap.put(key_accountListContent, index);
		
		index = jsonStr.indexOf(key_templates);
		jsonKeyMap.put(key_templates, index);
		
		return jsonKeyMap;
	}
	
	public static void main(String[] args) {
		DepRootHtmlProcess html = new DepRootHtmlProcess();
		String path = "D:\\study\\spiders\\pc_service\\page\\index.html";
		html.getDepsFromHtml(path);
	}
}
