package com.montnets.twilio.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

import cn.hutool.core.util.StrUtil;

@Controller
public class TestController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(value = "/sms", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public Object sms(HttpServletRequest req) {
		return To_do_Bot(req);
	}
	
	@RequestMapping(value = "/status", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public Object status(HttpServletRequest req) {
		log.info("接收到请求：");
		
		log.info("接收到请求，header：");
		Map<String, String> header = getAllRequestHeaders(req);
		for(String key : header.keySet()) {
			log.info(key+"="+header.get(key));
		}
		
		log.info("接收到请求，param：");
		Map<String, String> param = getAllRequestParam(req);
		for(String key : param.keySet()) {
			log.info(key+"="+param.get(key));
		}
		return getTwiml("delivered");
	}
	
	private Object NEW_APP_WHO_DIS(HttpServletRequest req) {
		String FromCountry = req.getParameter("FromCountry");
		//String reqbody = getRequestBody(req);
		String content = "your phone number was born in "+FromCountry;
		Body body = new Body
                .Builder(content)
                .build();
        Message sms = new Message
                .Builder(content)
                //.body(body)
                //.from("+16504580008")
                //.to("+16504580008")
                .build();
        MessagingResponse twiml = new MessagingResponse
                .Builder()
                .message(sms)
                .build();
        return twiml.toXml();
	}
	
	private static List<String> todoList = new ArrayList<String>();
	
	private Object To_do_Bot(HttpServletRequest req) {
		
		log.info("接收到请求：");
		Map<String, String> param = getAllRequestParam(req);
		for(String key : param.keySet()) {
			log.info(key+"="+param.get(key));
		}
		
		String Body = req.getParameter("Body");
		if(StrUtil.isBlank(Body)) {
			log.error("no body");
		}
		
		String addCmdName = "add";
		int addIndex = Body.indexOf(addCmdName);
		if(addIndex > -1) {
			String cmdText = Body.substring(addCmdName.length()+1);
			log.info("add命令, cmdtext="+cmdText);
			todoList.add(cmdText);
			return getTwiml("");
		}
		
		String listCmdName = "list";
		int listIndex = Body.indexOf(listCmdName);
		if(listIndex > -1) {
			//String cmdText = Body.substring(addCmdName.length()+1);
			log.info("list命令");
			String content = todoList.get(0);
			return getTwiml(content);
		}
		
		return null;
	}
	
	private Object getTwiml(String content) {
		Body body = new Body
                .Builder(content)
                .build();
        Message sms = new Message
                .Builder(content)
                //.body(body)
                //.from("+16504580008")
                //.to("+16504580008")
                .build();
        MessagingResponse twiml = new MessagingResponse
                .Builder()
                .message(sms)
                .build();
        return twiml.toXml();
	}
	
	private Map<String, String> getAllRequestParam(final HttpServletRequest request) {
		Map<String, String> res = new HashMap<String, String>();
		Enumeration<String> temp = request.getParameterNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String) temp.nextElement();
				String value = request.getParameter(en);
				res.put(en, value);
				//如果字段的值为空，判断若值为空，则删除这个字段>
				if (null == res.get(en) || "".equals(res.get(en))) {
					res.remove(en);
				}
			}
		}
		return res;
	}
	
	private Map<String, String> getAllRequestHeaders(final HttpServletRequest request) {
		Map<String, String> res = new HashMap<String, String>();
		Enumeration<String> temp = request.getHeaderNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String) temp.nextElement();
				String value = request.getHeader(en);
				res.put(en, value);
				//如果字段的值为空，判断若值为空，则删除这个字段>
				if (null == res.get(en) || "".equals(res.get(en))) {
					res.remove(en);
				}
			}
		}
		return res;
	}
	
	
	
}
