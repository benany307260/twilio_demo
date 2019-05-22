package com.bentest.spiders.listpage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bentest.spiders.constant.AMZConstant;
import com.bentest.spiders.entity.AmzProduct;

import cn.hutool.core.util.StrUtil;

@Service
public class ProductListHtmlProcess {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public List<AmzProduct> getListPage(int type, String htmlFilePath){
		try {
			Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
			if(doc == null) {
				log.error("解析html获取产品列表，获取不到页面对象。htmlFilePath="+htmlFilePath);
				return null;
			}
			
			List<AmzProduct> prodList = getListPageProduct(type, doc);
			if(prodList == null || prodList.size() < 1) {
				return prodList;
			}
			
			setProductPrice(type, doc, prodList);
			
			return prodList;
		} catch (Exception e) {
			log.error("解析html获取产品列表，异常。", e);
			return null;
		}
	}
	
	private void setProductPrice(int type, Document doc, List<AmzProduct> prodList) {
		Map<String,Double> priceMap = getListPagePriceByType(type, doc);
		if(priceMap == null || priceMap.size() < 1) {
			return;
		}
		
		try {
			// 先填充asin
			for(String key : priceMap.keySet()) {
				for(AmzProduct prod : prodList) {
					if(StrUtil.isNotBlank(prod.getProdAsin())) {
						continue;
					}
					String url = prod.getProdUrl();
					if(url.indexOf(key) < 0) {
						continue;
					}
					prod.setProdAsin(key);
				}
			}
			
			for(AmzProduct prod : prodList) {
				if(StrUtil.isBlank(prod.getProdAsin())) {
					continue;
				}
				if(prod.getProdPrice() != null && prod.getProdPrice() > 0) {
					continue;
				}
				
				Double price = priceMap.get(prod.getProdAsin());
				if(price == null) {
					continue;
				}
				prod.setProdPrice(price);
			}
		} catch (Exception e) {
			log.error("设置产品价格，异常。type="+type, e);
		}
	}

	public List<AmzProduct> getListPageProduct(int type, Document doc){
		
		try {
			// 先直接用指定类型获取
			List<AmzProduct> prodList = getListPageByType(type, doc);
			if(prodList != null && prodList.size() > 0) {
				return prodList;
			}
			
			// 指定类型获取不到，用其他类型重试
			Map<Integer,Integer> typeMap = new HashMap<>();
			typeMap.put(1, 1);
			typeMap.put(2, 2);
			// 移除掉试过的类型
			typeMap.remove(type);
			
			for(Integer key : typeMap.keySet()) {
				prodList = getListPageByType(key, doc);
				if(prodList != null && prodList.size() > 0) {
					return prodList;
				}
			}
			
			return null;
		} catch (Exception e) {
			log.error("解析html获取产品列表的产品，异常。", e);
			return null;
		}
	}
	
	private List<AmzProduct> getListPageByType(int type, Document doc){
		
		if(type == AMZConstant.VALUE_PAGE_TYPE_FIRST) {
			return getListFirstPage(doc);
		}
		else if(type == AMZConstant.VALUE_PAGE_TYPE_AFTER) {
			return getListSecondAfter(doc);
		}
		else {
			log.error("解析html获取产品列表，未定义的类型。");
			return null;
		}
	}
	
	private List<AmzProduct> getListFirstPage(Document doc) {
		try
		{
		    Elements liEls = doc.select("div#mainResults li");
		    if(liEls == null || liEls.size() < 1) {
		    	return null;
		    }
		    
		    List<AmzProduct> prodList = new ArrayList<>();
		    
		    for(Element liEl : liEls) {
		    	if(liEl == null) {
		    		continue;
		    	}
		    	String asin = liEl.attr("data-asin");
		    	if(StrUtil.isBlank(asin)) {
		    		continue;
		    	}
		    	
		    	Elements aEls = liEl.select("a");
		    	if(aEls == null || aEls.size() < 1) {
		    		continue;
		    	}
		    	for(Element aEl : aEls) {
		    		String href = aEl.attr("href");
		    		if(StrUtil.isBlank(href)) {
		    			continue;
		    		}
		    		if(href.indexOf(asin) < 0) {
		    			continue;
		    		}
		    		AmzProduct product = new AmzProduct();
		    		product.setProdAsin(asin);
		    		product.setProdUrl(href);
		    		prodList.add(product);
		    		break;
		    	}
		    }
		    return prodList;
		} 
		catch (Exception e) 
		{
		    log.error("解析html获取产品列表，异常。", e);
		    return null;
		}
	}
	
	private List<AmzProduct> getListSecondAfter(Document doc) {
		try
		{
		    Elements aEls = doc.select("span[data-component-type=s-product-image] a");
		    if(aEls == null || aEls.size() < 1) {
		    	return null;
		    }
		    
		    List<AmzProduct> prodList = new ArrayList<>();
		    
		    for(Element aEl : aEls) {
		    	if(aEl == null) {
		    		continue;
		    	}
	    		String href = aEl.attr("href");
	    		if(StrUtil.isBlank(href)) {
	    			continue;
	    		}
	    		if("#".equals(href)) {
	    			continue;
	    		}
	    		AmzProduct product = new AmzProduct();
	    		product.setProdUrl(href);
	    		prodList.add(product);
		    }
		    return prodList;
		} 
		catch (Exception e) 
		{
		    log.error("解析html获取产品列表2，异常。", e);
		    return null;
		}
	}
	
	private Map<String,Double> getListPagePriceByType(int type, Document doc){
		
		if(type == AMZConstant.VALUE_PAGE_TYPE_FIRST) {
			return null;
		}
		else if(type == AMZConstant.VALUE_PAGE_TYPE_AFTER) {
			return getListSecondAfterPrice(doc);
		}
		else {
			log.error("解析html获取产品列表产品价格，未定义的类型。");
			return null;
		}
	}
	
	private Map<String,Double> getListSecondAfterPrice(Document doc) {
		try
		{
		    Elements divEls = doc.select("div[data-asin]");
		    if(divEls == null || divEls.size() < 1) {
		    	return null;
		    }
		    
		    Map<String,Double> priceMap = new HashMap<>();
		    
		    for(Element divEl : divEls) {
		    	if(divEl == null) {
		    		continue;
		    	}
		    	
		    	String asin = divEl.attr("data-asin");
		    	if(StrUtil.isBlank(asin)) {
		    		continue;
		    	}
		    	
		    	Elements spanEls = divEl.select("span:contains($)");
		    	if(spanEls == null || spanEls.size() < 1) {
		    		continue;
		    	}
		    	
		    	Elements classEls = spanEls.select(".a-color-base");
		    	if(classEls == null || classEls.size() < 1) {
		    		continue;
		    	}
		    	
		    	String text = classEls.text();
		    	if(StrUtil.isBlank(text)) {
		    		continue;
		    	}
		    	text = text.trim();
		    	text = text.replace("$", "");
		    	
		    	Double price = getPrice(text);
		    	if(price == null) {
		    		continue;
		    	}
		    	
		    	priceMap.put(asin, price);
		    }
		    return priceMap;
		} 
		catch (Exception e) 
		{
		    log.error("解析html获取产品价格，异常。", e);
		    return null;
		}
	}
	
	private Double getPrice(String priceStr) {
		try {
			return Double.valueOf(priceStr);
		} catch (NumberFormatException e) {
			log.error("价格字符串转数字，异常。priceStr="+priceStr, e);
			return null;
		}
	}
	
	public static void main(String[] args) {
		try {
			ProductListHtmlProcess html = new ProductListHtmlProcess();
			//String mkdir = "C:/Users/lenovo/git/pc_service/page/%s";
			//String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\list-page\\Automotive\\Automotive-2-123456789.html";
			//String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\list-page\\Automotive\\Tools & Equipment-1-123456789.html";
			String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\list-page\\Automotive\\Tools & Equipment-2-123456789.html";
			
			
			Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
			if(doc == null) {
				System.out.println("解析html获取产品列表，获取不到页面对象。htmlFilePath="+htmlFilePath);
			}
			
			List<AmzProduct> prodList = html.getListPage(1, htmlFilePath);
			
			//html.getListSecondAfterPrice(doc);
			
			/*for(AmzProduct prod : prodList) {
				System.out.println(prod.getProdUrl());
			}*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
