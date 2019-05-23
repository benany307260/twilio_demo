package com.montnets.twilio.controller;

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
	public Object sms() {
		String content = "Hi! It looks like your phone number was born in {{ FromCountry }}";
		Body body = new Body
                .Builder(content)
                .build();
        Message sms = new Message
                .Builder()
                .body(body)
                .build();
        MessagingResponse twiml = new MessagingResponse
                .Builder()
                .message(sms)
                .build();
        return twiml.toXml();
	}
	
	
}
