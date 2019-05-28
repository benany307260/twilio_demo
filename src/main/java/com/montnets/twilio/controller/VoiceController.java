package com.montnets.twilio.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Say;

@Controller
public class VoiceController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public Object voice(HttpServletRequest req) {
		Dial dial = new Dial.Builder("+13125486404").build();
        Say say = new Say.Builder("Goodbye").build();
        VoiceResponse response = new VoiceResponse.Builder().dial(dial)
            .say(say).build();

        try {
        	
            return response.toXml();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	
	
	
}
