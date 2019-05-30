package com.montnets.twilio.controller;

import java.net.URI;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Conference;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Redirect;
import com.twilio.twiml.voice.Reject;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;

@RestController
public class CallRestController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@RequestMapping("/call")
	public String call() {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			PhoneNumber from = new com.twilio.type.PhoneNumber("+16504580008");
			//PhoneNumber to = new com.twilio.type.PhoneNumber("+8613682324521");
			PhoneNumber to = new com.twilio.type.PhoneNumber("+19166196842");
			
			URI voiceURI = new URI("http://demo.twilio.com/docs/voice.xml");
			
			Call call = Call.creator(to, from, voiceURI).create();

			return call.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	

}
