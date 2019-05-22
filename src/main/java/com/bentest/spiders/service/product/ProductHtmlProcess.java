package com.bentest.spiders.service.product;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

import com.alibaba.fastjson.JSON;
import com.bentest.spiders.constant.AMZConstant;
import com.bentest.spiders.entity.AmzProduct;
import com.bentest.spiders.entity.AmzProductBsr;
import com.bentest.spiders.util.GetIncrementId;
import com.bentest.spiders.util.URLUtil;

import cn.hutool.core.util.StrUtil;

@Service
public class ProductHtmlProcess {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	/**
	 * 解析html获取产品
	 * @param htmlFilePath
	 * @return
	 */
	public AmzProduct getProductFromHtml(String htmlFilePath) {
		try
		{
		    Document doc = Jsoup.parse( new File(htmlFilePath) , "utf-8" );
		    if(doc == null) {
		    	log.error("解析html获取产品，获取不到页面对象。htmlFilePath="+htmlFilePath);
		    	return null;
		    }
		    
		    AmzProduct product = new AmzProduct();
		    long id = GetIncrementId.getInstance().getCount();
		    product.setId(id);
		    // ASIN
		    String asin = getASIN(doc);
		    product.setProdAsin(asin);
		    // 产品标题
		    String title = selectForText(doc, "span#productTitle");
		    product.setProdTitle(title);
		    // 价格
		    double price = getPrice(doc);
		    product.setProdPrice(price);
		    // 店铺
		    String shopName = selectForText(doc, "a#bylineInfo");
		    product.setShopName(shopName);
		    String shopUrl = selectForAttr(doc, "a#bylineInfo", "href");
		    product.setShopUrl(shopUrl);
		    // 评论数
		    int reviewsNum = getReviewNum(doc);
		    product.setReviewsNum(reviewsNum);
		    // 评分星级
		    double reviewAvg = getReviewAvg(doc);
		    product.setReviewAvg(reviewAvg);
		    // 问答数
		    int askNum = getAskNum(doc);
		    product.setAskNum(askNum);
		    // amz精选
		    int amzChoice = getAmzChoice(doc);
		    product.setProdAmzchoice(amzChoice);
		    // amz精选关键字
		    String acKey = selectForText(doc, "span.ac-keyword-link");
		    product.setAmzchoiceKey(acKey);
		    // amz精选关键字url
		    String acKeyUrl = getAmzChoiceKeyUrl(doc);
		    product.setAmzchoiceKeyUrl(acKeyUrl);
		    // 产品所在类目
		    List<Map<String,String>> depList = getFromDepList(doc);
		    String fromDepId = getFromDepId(depList);
		    product.setFromDepId(fromDepId);
		    String fromDepName = getFromDepName(depList);
		    product.setFromDepName(fromDepName);
		    String fromDepJson = getFromDepListJson(depList);
		    product.setFromDepJson(fromDepJson);
		    // BSR
		    List<Map<String,String>> bsrList = getBSRList(doc);
		    int bsrNum = getBSRRootNum(bsrList);
		    product.setBsr(bsrNum);
		    String bsrDepName = getBSRRootDepName(bsrList);
		    product.setBsrDepName(bsrDepName);
		    String bsrJson = getBSRJson(bsrList);
		    product.setBsrJson(bsrJson);
		    List<AmzProductBsr> prodBsrList = getBsrList(bsrList, product);
		    product.setProductBsrList(prodBsrList);
		    // Seller
			Map<String,String> sellInfoMap = getSellInfo(doc);
			String sellerName = getSellerName(sellInfoMap);
			product.setSellerName(sellerName);
			String sellerUrl = getSellerUrl(sellInfoMap);
			product.setSellerUrl(sellerUrl);
			int sellerType = getSellerType(sellInfoMap);
			product.setSellerType(sellerType);
			// 物流
			String fulfillName = getFulfillName(sellInfoMap);
			product.setFulfillName(fulfillName);
			String fulfillUrl = getFulfillUrl(sellInfoMap);
			product.setFulfillUrl(fulfillUrl);
			int fbaType = getFBAType(sellInfoMap);
			product.setFbaType(fbaType);
			
			product.setCreateTime(new Date());
			product.setUpdateTime(new Date());
		    
		    return product;
		} 
		catch (Exception e) 
		{
		    log.error("解析html获取产品，异常。path="+htmlFilePath, e);
		    return null;
		}
	}
	
	
	private String selectForText(Document doc, String query) {
		try {
			Elements elements = doc.select(query);
			if(elements == null) {
				return null;
			}
			
			String text = elements.text();
			if(StrUtil.isBlank(text)) {
				return null;
			}
			
			// 去除前后空格
			text = text.trim();
			return text;
		} catch (Exception e) {
			log.error("查询标签的text，异常。query="+query, e);
			return null;
		}
	}
	
	private String selectForAttr(Document doc, String query, String attr) {
		try {
			Elements elements = doc.select(query);
			if(elements == null) {
				return null;
			}
			String attrValue = elements.attr(attr);
			return attrValue;
		} catch (Exception e) {
			log.error("查询标签的属性值，异常。query="+query+",attr"+attr, e);
			return null;
		}
	}
	
	private int getReviewNum(Document doc) {
		try {
			// 1,445 customer reviews
	    	String text = selectForText(doc, "span#acrCustomerReviewText");
	    	if(StrUtil.isBlank(text)) {
	    		return 0;
	    	}
	    	
	    	text = text.trim();
	    	text = text.substring(0, text.indexOf(" "));
	    	// 把数字里的逗号去掉
	    	text = text.replace(",", "");
	    	
	    	int reviewsNum = Integer.parseInt(text);
	    	return reviewsNum;
		} catch (Exception e) {
			log.error("获取评论数，异常。", e);
			return 0;
		}
	}
	
	private int getAskNum(Document doc) {
		try {
			// 67 answered questions
	    	String text = selectForText(doc, "a#askATFLink");
	    	if(StrUtil.isBlank(text)) {
	    		return 0;
	    	}
	    	
	    	text = text.trim();
	    	text = text.substring(0, text.indexOf(" "));
	    	// 把数字里的逗号去掉
	    	text = text.replace(",", "");
	    	
	    	int num = Integer.parseInt(text);
	    	return num;
		} catch (Exception e) {
			log.error("获取问答数，异常。", e);
			return 0;
		}
	}
	
	private int getAmzChoice(Document doc) {
		try {
	    	String text = selectForText(doc, "div#acBadge_feature_div");
	    	if(StrUtil.isBlank(text)) {
	    		return 0;
	    	}
	    	
	    	text = text.trim();
	    	text = text.toLowerCase();
	    	if(text.indexOf("choice") > -1) {
	    		return 1;
	    	}
	    	else {
	    		return 0;
	    	}
		} catch (Exception e) {
			log.error("获取amz精选，异常。", e);
			return 0;
		}
	}
	
	private double getPrice(Document doc) {
		try {
			double price = getPriceInOurprice(doc);
			if(price > 0) {
				return price;
			}
			
			price = getPriceInBuybox(doc);
			if(price > 0) {
				return price;
			}
			
			price = getPriceInSnsprice_Based(doc);
			if(price > 0) {
				return price;
			}
			
			price = getPriceInUnqualified(doc);
			if(price > 0) {
				return price;
			}
			
			price = getPriceInPrice(doc);
			if(price > 0) {
				return price;
			}
			
			return 0;
			
		} catch (Exception e) {
			log.error("获取价格，异常。", e);
			return 0;
		}
	}
	
	private double getPriceInOurprice(Document doc) {
		try {
	    	String text = selectForText(doc, "span#priceblock_ourprice");
	    	if(StrUtil.isBlank(text)) {
	    		return 0;
	    	}
	    	
	    	text = text.replace("$", "");
	    	
	    	
	    	List<Double> priceList = new ArrayList<>();

	    	String key = "-";
	    	/*int index = text.indexOf(key);
	    	if(index < 0) {
	    		text = text.substring(0, index);
	    		text = text.trim();
	    		Double price = Double.parseDouble(text);
	    		priceList.add(price);
	    		return priceList;
	    	}*/
	    	
	    	String[] priceStrArray = text.split(key);
	    	if(priceStrArray == null || priceStrArray.length < 1) {
	    		return 0;
	    	}
	    	
	    	for(String priceStr : priceStrArray) {
	    		if(StrUtil.isBlank(priceStr)) {
	    			continue;
	    		}
	    		priceStr = priceStr.trim();
	    		Double price = Double.parseDouble(priceStr);
	    		priceList.add(price);
	    	}
	    	
	    	if(priceList.size() > 0) {
	    		return priceList.get(0);
	    	}
	    	
	    	return 0;
		} catch (Exception e) {
			log.error("获取priceblock_ourprice里的价格，异常。", e);
			return 0;
		}
	}
	
	private double getPriceInBuybox(Document doc) {
		try {
	    	String text = selectForText(doc, "span#price_inside_buybox");
	    	if(StrUtil.isBlank(text)) {
	    		return 0;
	    	}
	    	
	    	text = text.trim();
	    	text = text.substring(0, 1);
	    	
	    	Double price = Double.parseDouble(text);
	    	return price;
		} catch (Exception e) {
			log.error("获取price_inside_buybox里的价格，异常。", e);
			return 0;
		}
	}
	
	private double getPriceInSnsprice_Based(Document doc) {
		try {
			Elements basedEls = doc.select("span#priceblock_snsprice_Based");
			if(basedEls == null) {
				return 0;
			}
			Elements priceSpanEls = basedEls.select("span");
			if(priceSpanEls == null) {
				return 0;
			}
			Element priceEl = priceSpanEls.first();
			if(priceEl == null) {
				return 0;
			}
			
			String text = priceEl.text();
	    	text = text.trim();
	    	text = text.substring(0, 1);
	    	
	    	Double price = Double.parseDouble(text);
	    	return price;
		} catch (Exception e) {
			log.error("获取priceblock_snsprice_Based里的价格，异常。", e);
			return 0;
		}
	}
	
	private double getPriceInUnqualified(Document doc) {
		try {
			Elements unqualifiedEls = doc.select("div#unqualified");
			if(unqualifiedEls == null) {
				return 0;
			}
			Elements divEls = unqualifiedEls.select("div");
			if(divEls == null) {
				return 0;
			}
			Element divEl = divEls.first();
			if(divEl == null) {
				return 0;
			}
			Elements spanEls = divEl.select("span");
			if(spanEls == null) {
				return 0;
			}
			
			Element el = spanEls.first();
			if(el == null) {
				return 0;
			}
			
			String text = el.text();
			text = text.replace("$", "");
	    	
	    	text = text.trim();
	    	
	    	Double price = Double.parseDouble(text);
	    	return price;
		} catch (Exception e) {
			log.error("获取unqualified里的价格，异常。", e);
			return 0;
		}
	}
	
	private double getPriceInPrice(Document doc) {
		try {
			Elements priceEls = doc.select("div#price");
			if(priceEls == null) {
				return 0;
			}
			Elements spanEls = priceEls.select("span");
			if(spanEls == null) {
				return 0;
			}
			
			for(Element spanEl : spanEls) {
				if(spanEl == null) {
					continue;
				}
				String text = spanEl.text();
				if(StrUtil.isBlank(text)) {
					continue;
				}
				if(text.indexOf("$") < 0) {
					continue;
				}
				text = text.replace("$", "");
				text = text.trim();
				if(StrUtil.isBlank(text)) {
					continue;
				}
				
				Double price = Double.parseDouble(text);
				return price;
			}
	    	
	    	return 0;
		} catch (Exception e) {
			log.error("获取unqualified里的价格，异常。", e);
			return 0;
		}
	}
	
	private String getAmzChoiceKeyUrl(Document doc) {
		try {
			Elements elements = doc.select("span.ac-keyword-link");
			if(elements == null) {
				return null;
			}
			
			Elements aEls = elements.select("a");
			if(aEls == null) {
				return null;
			}
			
			String url = aEls.attr("href");
			return url;
		} catch (Exception e) {
			log.error("获取amz精选关键字url，异常。", e);
			return null;
		}
	}
	
	private String getFromDepListJson(List<Map<String,String>> depList) {
		if(depList == null || depList.size() < 1) {
			log.error("获取产品所在类目列表json，传入depList为空。");
			return null;
		}
		String json = JSON.toJSONString(depList);
		return json;
	}
	
	private String getFromDepName(List<Map<String,String>> depList) {
		try {
			if(depList == null || depList.size() < 1) {
				log.error("获取产品所在类目名称，传入depList为空。");
				return null;
			}
			
			Map<String,String> depMap = depList.get(depList.size() - 1);
			if(depMap == null || depMap.size() < 1) {
				log.error("获取产品所在类目名称，获取最后一个类目map为空。");
				return null;
			}
			
			String depName = depMap.get(AMZConstant.AMZ_KEY_PRODUCT_DEPNAME);
			return depName;
			
		} catch (Exception e) {
			log.error("获取产品所在类目名称，异常", e);
			return null;
		}
	}
	
	private String getFromDepId(List<Map<String,String>> depList) {
		try {
			if(depList == null || depList.size() < 1) {
				log.error("获取产品所在类目ID，传入depList为空。");
				return null;
			}
			
			Map<String,String> depMap = depList.get(depList.size() - 1);
			if(depMap == null || depMap.size() < 1) {
				log.error("获取产品所在类目ID，获取最后一个类目map为空。");
				return null;
			}
			
			String depUrl = depMap.get(AMZConstant.AMZ_KEY_PRODUCT_DEPURL);
			
			//url示例：/s/ref=dp_bc_aui_C_3?ie=UTF8&node=723452011&rh=n%3A16225010011%2Cn%3A723418011%2C
			Map<String, String> mapRequestParam = URLUtil.URLRequest(depUrl);
			String node = mapRequestParam.get("node");
			if(StrUtil.isBlank(node)) {
				log.error("获取产品所在类目ID，node为空。depUrl="+depUrl);
				return null;
			}
			
			return node;
		} catch (Exception e) {
			log.error("获取产品所在类目ID，异常", e);
			return null;
		}
	}
	
	private List<Map<String,String>> getFromDepList(Document doc) {
		try {
			Elements divEls = doc.select("div#wayfinding-breadcrumbs_feature_div");
			if(divEls == null) {
				return null;
			}
			Elements liEls = divEls.select("li");
			if(liEls == null) {
				return null;
			}
			
			List<Map<String,String>> depList = new ArrayList<>();
			
			for(Element liEl : liEls) {
				Elements aEls = liEl.select("a");
				if(aEls == null || aEls.size() < 1) {
					continue;
				}
				Element el = aEls.first();
				if(el == null) {
					continue;
				}
				
				Map<String,String> depMap = new HashMap<String, String>();
				
				String depName = el.text();
				if(StrUtil.isBlank(depName)) {
					continue;
				}
				
				String depUrl = el.attr("href");
				if(StrUtil.isBlank(depUrl)) {
					continue;
				}
				
				depMap.put(AMZConstant.AMZ_KEY_PRODUCT_DEPNAME, depName.trim());
				depMap.put(AMZConstant.AMZ_KEY_PRODUCT_DEPURL, depUrl.trim());
				depList.add(depMap);
			}
			return depList;
			
		} catch (Exception e) {
			log.error("获取产品所在类目列表，异常。", e);
			return null;
		}
	}
	
	private String getASINInDetail_bullets(Document doc) {
		try {
			Elements divEls = doc.select("div#detail-bullets");
			if(divEls == null) {
				return null;
			}
			Elements liEls = divEls.select("li");
			if(liEls == null) {
				return null;
			}
			
			for(Element liEl : liEls) {
				if(liEl == null) {
					continue;
				}
				String liContent = liEl.text();
				if(StrUtil.isBlank(liContent)) {
					continue;
				}
				if(liContent.toLowerCase().indexOf("asin") < 0) {
					continue;
				}
				String asin = liEl.ownText();
				if(StrUtil.isBlank(asin)) {
					return null;
				}
				return asin.trim();
			}
			return null;
			
		} catch (Exception e) {
			log.error("获取产品ASIN，异常。", e);
			return null;
		}
	}
	
	private String getASIN(Document doc) {
		String asin = getASINInDetailBullets(doc);
		if(StrUtil.isNotBlank(asin)) {
			return asin;
		}
		
		asin = getASINInDetail_bullets(doc);
		if(StrUtil.isNotBlank(asin)) {
			return asin;
		}
		
		asin = getASINInProdDetails(doc);
		if(StrUtil.isNotBlank(asin)) {
			return asin;
		}
		
		return asin;
	}
	
	private String getASINInDetailBullets(Document doc) {
		try {
			Elements divEls = doc.select("div#detailBullets");
			if(divEls == null) {
				return null;
			}
			Elements liEls = divEls.select("li");
			if(liEls == null) {
				return null;
			}
			
			for(Element liEl : liEls) {
				if(liEl == null) {
					continue;
				}
				String liContent = liEl.text();
				if(StrUtil.isBlank(liContent)) {
					continue;
				}
				
				String key = "asin";
				int asinStrIndex = liContent.toLowerCase().indexOf(key);
				
				if(asinStrIndex < 0) {
					continue;
				}
				asinStrIndex = asinStrIndex + key.length() + 1;
				String asin = liContent.substring(asinStrIndex);
				return asin.trim();
			}
			return null;
			
		} catch (Exception e) {
			log.error("获取产品ASIN，异常。", e);
			return null;
		}
	}
	
	private String getASINInProdDetails(Document doc) {
		try {
			Elements divEls = doc.select("div#prodDetails");
			if(divEls == null) {
				return null;
			}
			Elements trEls = divEls.select("tr");
			if(trEls == null) {
				return null;
			}
			
			for(Element trEl : trEls) {
				if(trEl == null) {
					continue;
				}
				String liContent = trEl.text();
				if(StrUtil.isBlank(liContent)) {
					continue;
				}
				
				String key = "asin";
				int asinStrIndex = liContent.toLowerCase().indexOf(key);
				
				if(asinStrIndex < 0) {
					continue;
				}
				asinStrIndex = asinStrIndex + key.length();
				String asin = liContent.substring(asinStrIndex);
				return asin.trim();
			}
			return null;
			
		} catch (Exception e) {
			log.error("获取产品ASIN，异常。", e);
			return null;
		}
	}
	
	private List<AmzProductBsr> getBsrList(List<Map<String,String>> bsrList, AmzProduct prod){
		if(bsrList == null || bsrList.size() < 1) {
			return null;
		}
		
		try {
			List<AmzProductBsr> prodBsrList = new ArrayList<>();
			
			for(int i = 0; i < bsrList.size(); i++) {
				Map<String,String> bsrMap = bsrList.get(i);
				if(bsrMap == null || bsrMap.size() < 1) {
					continue;
				}
				AmzProductBsr prodBsr = new AmzProductBsr();
				long id = GetIncrementId.getInstance().getCount();
				prodBsr.setId(id);
				prodBsr.setProdAsin(prod.getProdAsin());
				prodBsr.setProdId(prod.getId());
				
				String bsrNumStr = bsrMap.get(AMZConstant.AMZ_KEY_BSR_NUM);
				if(StrUtil.isBlank(bsrNumStr)) {
					continue;
				}
				int bsrNum = Integer.parseInt(bsrNumStr);
				prodBsr.setBsr(bsrNum);
				
				prodBsr.setBsrDepName(bsrMap.get(AMZConstant.AMZ_KEY_BSR_DEPNAME));
				prodBsr.setBsrUrl(bsrMap.get(AMZConstant.AMZ_KEY_BSR_DEPURL));
				prodBsr.setSortNum(i);
				prodBsr.setCreateTime(new Date());
				prodBsr.setUpdateTime(new Date());
				prodBsrList.add(prodBsr);
			}
			
			return prodBsrList;
		} catch (Exception e) {
			log.error("获取bsr，异常。", e);
			return null;
		}
	}
	
	private int getBSRRootNum(List<Map<String,String>> bsrList) {
		if(bsrList == null || bsrList.size() < 1) {
			return 0;
		}
		
		try {
			Map<String,String> bsrMap = bsrList.get(0);
			if(bsrMap == null || bsrMap.size() < 1) {
				return 0;
			}
			
			String bsrNumStr = bsrMap.get(AMZConstant.AMZ_KEY_BSR_NUM);
			if(StrUtil.isBlank(bsrNumStr)) {
				return 0;
			}
			
			int bsrNum = Integer.parseInt(bsrNumStr);
			return bsrNum;
		} catch (Exception e) {
			log.error("获取BSR大类排名，异常。", e);
			return 0;
		}
	}
	
	private String getBSRRootDepName(List<Map<String,String>> bsrList) {
		if(bsrList == null || bsrList.size() < 1) {
			return null;
		}
		
		try {
			Map<String,String> bsrMap = bsrList.get(0);
			if(bsrMap == null || bsrMap.size() < 1) {
				return null;
			}
			
			String bsrNumStr = bsrMap.get(AMZConstant.AMZ_KEY_BSR_DEPNAME);
			return bsrNumStr;
		} catch (Exception e) {
			log.error("获取BSR大类排名机构名称，异常。", e);
			return null;
		}
	}
	
	private String getBSRJson(List<Map<String,String>> bsrList) {
		if(bsrList == null || bsrList.size() < 1) {
			return null;
		}
		
		try {
			String json = JSON.toJSONString(bsrList);
			return json;
		} catch (Exception e) {
			log.error("获取BSR JSON，异常。", e);
			return null;
		}
	}
	
	private List<Map<String,String>> getBSRList(Document doc) {
		
		List<Map<String,String>> bsrList = getBSRListInTable(doc);
		if(bsrList != null && bsrList.size() > 0) {
			return bsrList;
		}
		
		bsrList = getBSRListInSalesRank(doc);
		if(bsrList != null && bsrList.size() > 0) {
			return bsrList;
		}
		
		return null;
	}
	
	private List<Map<String,String>> getBSRListInTable(Document doc) {
		try {
			Elements divEls = doc.select("table#productDetails_detailBullets_sections1");
			if(divEls == null) {
				return null;
			}
			Elements trEls = divEls.select("tr");
			if(trEls == null) {
				return null;
			}
			
			Elements bsrSpanEls = null;
					
			for(Element trEl : trEls) {
				if(trEl == null) {
					continue;
				}
				String trText = trEl.text();
				if(StrUtil.isBlank(trText)) {
					continue;
				}
				
				String key = "best sellers rank";
				int keyIndex = trText.toLowerCase().indexOf(key);
				if(keyIndex < 0) {
					continue;
				}
				
				bsrSpanEls = trEl.select("span span");
				if(bsrSpanEls != null) {
					break;
				}
			}
			
			if(bsrSpanEls == null || bsrSpanEls.size() < 1) {
				return null;
			}
			
			List<Map<String,String>> bsrList = new ArrayList<>();
			
			Element bsrRootSpanEl = bsrSpanEls.get(0);
			if(bsrRootSpanEl == null) {
				return null;
			}
			String text = bsrRootSpanEl.ownText();
			if(StrUtil.isBlank(text)) {
				return null;
			}
			text = text.trim();
			
			int beginIndex = text.indexOf("#");
			if(beginIndex < 0) {
				return null;
			}
			beginIndex +=1;
			String inKey = "in";
			int inIndex = text.indexOf(inKey);
			if(inIndex < 0) {
				return null;
			}
			
			String bsrNumStr = text.substring(beginIndex, inIndex);
			if(StrUtil.isBlank(bsrNumStr)) {
				return null;
			}
			bsrNumStr = bsrNumStr.trim().replace(",", "");
			
			String bsrDepName = text.substring(inIndex+inKey.length(), text.indexOf("("));
			bsrDepName = bsrDepName.trim();
			
			String bsrDepUrl = null;
			Elements aEls = bsrRootSpanEl.select("a");
			if(aEls != null) {
				Element aEl = aEls.first();
				if(aEl != null) {
					bsrDepUrl = aEl.attr("href");
				}
			}
			Map<String,String> bsrMap = new HashMap<>();
			bsrMap.put(AMZConstant.AMZ_KEY_BSR_NUM, bsrNumStr);
			bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPNAME, bsrDepName);
			bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPURL, bsrDepUrl);
			bsrList.add(bsrMap);
			
			for(int i = 1; i < bsrSpanEls.size(); i++)
			{
				Element bsrSpanEl = bsrSpanEls.get(i);
				if(bsrSpanEl == null) {
					continue;
				}
				text = bsrSpanEl.ownText();
				if(StrUtil.isBlank(text)) {
					continue;
				}
				text = text.trim();
				
				beginIndex = text.indexOf("#");
				if(beginIndex < 0) {
					continue;
				}
				beginIndex +=1;
				inIndex = text.indexOf(inKey);
				if(inIndex < 0) {
					continue;
				}
				
				bsrNumStr = text.substring(beginIndex, inIndex);
				if(StrUtil.isBlank(bsrNumStr)) {
					continue;
				}
				bsrNumStr = bsrNumStr.trim().replace(",", "");
				
				aEls = bsrSpanEl.select("a");
				if(aEls == null) {
					continue;
				}
				Element aEl = aEls.first();
				if(aEl == null) {
					continue;
				}
				bsrDepUrl = aEl.attr("href");
				bsrDepName = aEl.text();
				
				bsrMap = new HashMap<>();
				bsrMap.put(AMZConstant.AMZ_KEY_BSR_NUM, bsrNumStr);
				bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPNAME, bsrDepName);
				bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPURL, bsrDepUrl);
				bsrList.add(bsrMap);
			}
			
			return bsrList;
			
		} catch (Exception e) {
			log.error("获取产品BSR，异常。", e);
			return null;
		}
	}
	
	private List<Map<String,String>> getBSRListInSalesRank(Document doc) {
		try {
			Elements liEls = doc.select("li#SalesRank");
			if(liEls == null) {
				return null;
			}
			String liText = liEls.text();
			if(StrUtil.isBlank(liText)) {
				return null;
			}
			
			int bsrBeginIndex = liText.indexOf("#");
			if(bsrBeginIndex < 0) {
				return null;
			}
			bsrBeginIndex +=1;
			
			String key = "in";
			int bsrEndIndex = liText.indexOf(key);
			if(bsrEndIndex < 0) {
				return null;
			}
			
			String bsrNumStr = liText.substring(bsrBeginIndex, liText.indexOf(key));
			bsrNumStr = bsrNumStr.trim();
			if(StrUtil.isBlank(bsrNumStr)) {
				return null;
			}
			bsrNumStr = bsrNumStr.trim().replace(",", "");
			
			int bsrDepEndIndex = liText.indexOf("(");
			if(bsrEndIndex < 0) {
				return null;
			}
			String bsrDepName = liText.substring(bsrEndIndex+key.length(), bsrDepEndIndex);
			bsrDepName = bsrDepName.trim();
			if(StrUtil.isBlank(bsrDepName)) {
				return null;
			}
			
			String bsrDepUrl = null;
			Elements aEls = liEls.select("a");
			if(aEls != null) {
				Element aEl = aEls.first();
				if(aEl != null) {
					bsrDepUrl = aEl.attr("href");
				}
			}
			
			List<Map<String,String>> bsrList = new ArrayList<>();
			Map<String,String> bsrMap = new HashMap<>();
			bsrMap.put(AMZConstant.AMZ_KEY_BSR_NUM, bsrNumStr);
			bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPNAME, bsrDepName);
			bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPURL, bsrDepUrl);
			bsrList.add(bsrMap);
			
			Elements bsrLiEls = liEls.select("li li");
			if(bsrLiEls == null || bsrLiEls.size() < 1) {
				return bsrList;
			}
			
			for(Element li : bsrLiEls) {
				if(li == null) {
					continue;
				}
				String text = li.text();
				if(StrUtil.isBlank(text)) {
					continue;
				}
				
				bsrBeginIndex = text.indexOf("#");
				if(bsrBeginIndex < 0) {
					continue;
				}
				bsrBeginIndex +=1;
				
				//String key = "in";
				bsrEndIndex = text.indexOf(key);
				if(bsrEndIndex < 0) {
					continue;
				}
				
				bsrNumStr = text.substring(bsrBeginIndex, bsrEndIndex);
				bsrNumStr = bsrNumStr.trim();
				if(StrUtil.isBlank(bsrNumStr)) {
					continue;
				}
				bsrNumStr = bsrNumStr.trim().replace(",", "");
				
				Elements bsrAEls = li.select("a");
				if(bsrAEls != null) {
					Element aEl = bsrAEls.first();
					if(aEl != null) {
						bsrDepUrl = aEl.attr("href");
						bsrDepName = aEl.text();
					}
				}
				
				bsrMap = new HashMap<>();
				bsrMap.put(AMZConstant.AMZ_KEY_BSR_NUM, bsrNumStr);
				bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPNAME, bsrDepName);
				bsrMap.put(AMZConstant.AMZ_KEY_BSR_DEPURL, bsrDepUrl);
				bsrList.add(bsrMap);
			}
			
			return bsrList;
			
		} catch (Exception e) {
			log.error("获取产品BSR，异常。", e);
			return null;
		}
	}
	
	private double getReviewAvg(Document doc) {
		try {
	    	String titleValue = selectForAttr(doc, "span#acrPopover", "title");
	    	if(StrUtil.isBlank(titleValue)) {
	    		return 0;
	    	}
	    	
	    	titleValue = titleValue.substring(0, titleValue.indexOf(" "));
	    	titleValue = titleValue.trim();
	    	if(StrUtil.isBlank(titleValue)) {
	    		return 0;
	    	}
	    	
	    	double reviewAvg = Double.parseDouble(titleValue);
	    	return reviewAvg;
		} catch (Exception e) {
			log.error("获取产品评分星级，异常。", e);
			return 0;
		}
	}
	
	private Map<String,String> getSellInfo(Document doc) {
		Map<String,String> sellInfoMap = getSellInMerchant_info(doc);
		if(sellInfoMap != null && sellInfoMap.size() > 0) {
			return sellInfoMap;
		}
		
		sellInfoMap = getSellInMerchant_text(doc);
		if(sellInfoMap != null && sellInfoMap.size() > 0) {
			return sellInfoMap;
		}
		
		sellInfoMap = getSellInSns_availability(doc);
		if(sellInfoMap != null && sellInfoMap.size() > 0) {
			return sellInfoMap;
		}
		
		sellInfoMap = getSellInusedbuyBox(doc);
		if(sellInfoMap != null && sellInfoMap.size() > 0) {
			return sellInfoMap;
		}
		
		return null;
	}
	
	private Map<String,String> getSellInMerchant_info(Document doc) {
		try {
			Elements elements = doc.select("div#merchant-info");
			if(elements == null) {
				return null;
			}
			
			Elements aEls = elements.select("a");
			if(aEls == null || aEls.size() < 2) {
				return null;
			}
			
			Map<String,String> sellMap = new HashMap<>();
			
			Element seller_aEl = aEls.get(0);
			if(seller_aEl != null) {
				String sellerName = seller_aEl.text();
				if(StrUtil.isNotBlank(sellerName)) {
					sellerName = sellerName.trim();
				}
				String sellerUrl = seller_aEl.attr("href");
				
				sellMap.put("sellerName", sellerName);
				sellMap.put("sellerUrl", sellerUrl);
			}
			
			Element fulfill_aEl = aEls.get(1);
			if(fulfill_aEl != null) {
				String fulfillName = fulfill_aEl.text();
				if(StrUtil.isNotBlank(fulfillName)) {
					fulfillName = fulfillName.trim();
				}
				String fulfillUrl = fulfill_aEl.attr("href");
				sellMap.put("fulfillName", fulfillName);
				sellMap.put("fulfillUrl", fulfillUrl);
			}
			
			return sellMap;
		} catch (Exception e) {
			log.error("获取卖家信息merchant-info，异常。", e);
			return null;
		}
	}
	
	private Map<String,String> getSellInMerchant_text(Document doc) {
		try {
			Elements elements = doc.select("div#merchant-info");
			if(elements == null) {
				return null;
			}
			
			String text = elements.text();
			if(StrUtil.isBlank(text)) {
				return null;
			}
			
			text = text.trim();
			text = text.toLowerCase();
			if(text.indexOf(AMZConstant.AMZ_SELF_FBA_CONTENT.toLowerCase()) < 0) {
				return null;
			}
			
			Map<String,String> sellMap = new HashMap<>();
			sellMap.put("sellerName", AMZConstant.AMZ_SELLER_SELF_NAME);
			sellMap.put("fulfillName", AMZConstant.AMZ_FBA_NAME);
			
			return sellMap;
		} catch (Exception e) {
			log.error("获取卖家信息merchant-info，异常。", e);
			return null;
		}
	}
	
	private Map<String,String> getSellInSns_availability(Document doc) {
		try {
			Elements elements = doc.select("div#sns-availability");
			if(elements == null) {
				return null;
			}
			
			String text = elements.text();
			if(StrUtil.isBlank(text)) {
				return null;
			}
			
			text = text.trim();
			text = text.toLowerCase();
			if(text.indexOf(AMZConstant.AMZ_SELF_FBA_CONTENT.toLowerCase()) < 0) {
				return null;
			}
			
			Map<String,String> sellMap = new HashMap<>();
			sellMap.put("sellerName", AMZConstant.AMZ_SELLER_SELF_NAME);
			sellMap.put("fulfillName", AMZConstant.AMZ_FBA_NAME);
			
			return sellMap;
		} catch (Exception e) {
			log.error("获取卖家信息sns-availability，异常。", e);
			return null;
		}
	}
	
	private Map<String,String> getSellInusedbuyBox(Document doc) {
		try {
			Elements divEls = doc.select("div#usedbuyBox div");
			if(divEls == null || divEls.size() < 1) {
				return null;
			}
			
			Map<String,String> sellMap = new HashMap<>();
			
			for(Element div : divEls) {
				if(div == null) {
					continue;
				}
				String text = div.ownText();
				if(StrUtil.isBlank(text)) {
					continue;
				}
				
				if(text.toLowerCase().indexOf("sold by") < 0) {
					continue;
				}
				
				Elements aEls = div.select("a");
				if(aEls == null) {
					continue;
				}
				String sellerName = aEls.text();
				if(StrUtil.isBlank(sellerName)) {
					continue;
				}
				sellerName = sellerName.trim();
				
				String sellerUrl = aEls.attr("href");
				
				sellMap.put("sellerName", sellerName); 
				sellMap.put("sellerUrl", sellerUrl);
				break;
			}
			
			Elements aEls = doc.select("a#SSOFpopoverLink_ubb");
			if(aEls == null) {
				return sellMap;
			}
			
			String fulfillName = aEls.text();
			String fulfillUrl = aEls.attr("href");
			
			sellMap.put("fulfillName", fulfillName);
			sellMap.put("fulfillUrl", fulfillUrl);
			
			return sellMap;
		} catch (Exception e) {
			log.error("获取卖家信息usedbuyBox，异常。", e);
			return null;
		}
	}
	
	private String getSellerName(Map<String,String> sellInfoMap) {
		if(sellInfoMap == null || sellInfoMap.size() < 1) {
			return null;
		}
		return sellInfoMap.get("sellerName");
	}
	
	/**
	 * 卖家类型
	 * @param sellInfoMap
	 * @return 卖家类型。0-普通第三方卖家；1-亚马逊自营
	 */
	private int getSellerType(Map<String,String> sellInfoMap) {
		if(sellInfoMap == null || sellInfoMap.size() < 1) {
			return 0;
		}
		
		String sellerName = getSellerName(sellInfoMap);
		if(StrUtil.isBlank(sellerName)) {
			return 0;
		}
		
		if(AMZConstant.AMZ_SELLER_SELF_NAME.toLowerCase().equals(sellerName.toLowerCase())) {
			// 卖家类型。0-普通第三方卖家；1-亚马逊自营
			return 1;
		}
		
		return 0;
	}
	
	private String getSellerUrl(Map<String,String> sellInfoMap) {
		if(sellInfoMap == null || sellInfoMap.size() < 1) {
			return null;
		}
		return sellInfoMap.get("sellerUrl");
	}
	
	private String getFulfillName(Map<String,String> sellInfoMap) {
		if(sellInfoMap == null || sellInfoMap.size() < 1) {
			return null;
		}
		return sellInfoMap.get("fulfillName");
	}
	
	/**
	 * 是否FBA
	 * @param sellInfoMap
	 * @return 是否FBA。0-否；1-是
	 */
	private int getFBAType(Map<String,String> sellInfoMap) {
		if(sellInfoMap == null || sellInfoMap.size() < 1) {
			return 0;
		}
		
		String fulfillName = getFulfillName(sellInfoMap);
		if(StrUtil.isBlank(fulfillName)) {
			return 0;
		}
		
		if(AMZConstant.AMZ_FBA_NAME.toLowerCase().equals(fulfillName.toLowerCase())) {
			// 是否FBA。0-否；1-是
			return 1;
		}
		
		return 0;
	}
	
	private String getFulfillUrl(Map<String,String> sellInfoMap) {
		if(sellInfoMap == null || sellInfoMap.size() < 1) {
			return null;
		}
		return sellInfoMap.get("fulfillUrl");
	}
	
	public static void main(String[] args) {
		try {
			ProductHtmlProcess html = new ProductHtmlProcess();
			//String mkdir = "C:/Users/lenovo/git/pc_service/page/%s";
			String mkdir = "F:/study/amz/git/pc_service/page/%s";
			
			for(int i = 1; i <= 25; i++) {
				//i=11;
				String pageName = "product"+i+".html";
				String path = String.format(mkdir, pageName);
				Document doc = Jsoup.parse( new File(path) , "utf-8" );
				
				// 产品名称
			    /*String title = html.selectForText(doc, "span#productTitle");
			    System.out.println(pageName+"---"+title);*/
			    
				// asin
				/*String asin = html.getASIN(doc);
				System.out.println(pageName+"---"+asin);*/
				
				// 评论数
				/*int reviewsNum = html.getReviewNum(doc);
				System.out.println(pageName+"---"+reviewsNum);*/
				
				// 评分星级
				/*double reviewAvg = html.getReviewAvg(doc);
				System.out.println(pageName+"---"+reviewAvg);*/
				
				// 问答数
			    /*int askNum = html.getAskNum(doc);
			    System.out.println(pageName+"---"+askNum);*/
				
				// 价格
			    /*double price = html.getPrice(doc);
			    System.out.println(pageName+"---"+price);*/
				
				// 产品所在类目ID
			    List<Map<String,String>> depList = html.getFromDepList(doc);
			    String fromDepId = html.getFromDepId(depList);
			    String fromDepJson = html.getFromDepListJson(depList);
			    System.out.println(pageName+"---"+fromDepId+"---"+fromDepJson);
				
				// 店铺
			    /*String shopName = html.selectForText(doc, "a#bylineInfo");
			    String shopUrl = html.selectForAttr(doc, "a#bylineInfo", "href");
			    System.out.println(pageName+"-----"+shopName+"------"+shopUrl);*/
				
				// amz精选
			    /*int amzChoice = html.getAmzChoice(doc);
			    // amz精选关键字
			    String acKey = html.selectForText(doc, "span.ac-keyword-link");
			    // amz精选关键字url
			    String acKeyUrl = html.getAmzChoiceKeyUrl(doc);
			    System.out.println(pageName+"---"+amzChoice+"---"+acKey+"---"+acKeyUrl);*/
			    
				// BSR
				/*List<Map<String,String>> bsrList = html.getBSRList(doc);
				int bsrRoot = html.getBSRRootNum(bsrList);
				String bsrJson = html.getBSRJson(bsrList);
				System.out.println(pageName+"---"+bsrRoot+"---"+bsrJson);*/
				
				// Seller
				/*Map<String,String> sellInfoMap = html.getSellInfo(doc);
				String sellerName = html.getSellerName(sellInfoMap);
				String sellerUrl = html.getSellerUrl(sellInfoMap);
				int sellerType = html.getSellerType(sellInfoMap);
				String fulfillName = html.getFulfillName(sellInfoMap);
				int fbaType = html.getFBAType(sellInfoMap);
				System.out.println(pageName+"---"+sellerName+"---"+fulfillName+"---"+sellerType+"---"+fbaType);*/ 
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
