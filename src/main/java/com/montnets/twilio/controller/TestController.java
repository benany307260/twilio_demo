package com.montnets.twilio.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

@Controller
public class TestController {
	
	@RequestMapping(value = "/sms", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public Object sms(HttpServletRequest req) {
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
	
	
	
}
