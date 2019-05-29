package com.montnets.twilio.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Conference;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Redirect;
import com.twilio.twiml.voice.Reject;
import com.twilio.twiml.voice.Say;

@Controller
public class VoiceController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@RequestMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public Object voice(HttpServletRequest req) {

		// return dial();

		// return conference(req);

		//return conferenceSpecificPhone(req);
		
		return gather(req);

	}

	private Object gather(HttpServletRequest req) {
		
		String digits = req.getParameter("Digits");
		
		VoiceResponse.Builder builder = new VoiceResponse.Builder();
		
		// 没输入数字，表示进入语音应答提示
		if (digits == null || digits.trim().length() < 1) {
        	return getGather(builder);
        }
		
		// 有输入数字，需要按数字走对应指令
		String sayText = getSayByInputNum(digits);
		// 输入的数字未定义
		if(sayText == null) {
			Say notDefine = new Say.Builder("Sorry, I don\'t understand that choice.").build();
			builder.say(notDefine);
			return getGather(builder);
		}
		
		Say say = new Say.Builder(sayText).build();
		builder.say(say);

		VoiceResponse response = builder.build();

		try {
			return response.toXml();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Object getGather(VoiceResponse.Builder builder) {
		Say say = new Say.Builder("For sales, press 1. For support, press 2.").build();
		
		Gather gather = new Gather.Builder().timeout(5).numDigits(1)
				.inputs(Arrays.asList(Gather.Input.SPEECH, Gather.Input.DTMF))
				.say(say)
				.build();

		Redirect redirect = new Redirect.Builder("/voice").build();

		VoiceResponse response = builder
				.gather(gather)
				//不断调用重定向回当前url
				.redirect(redirect)
				.build();
		
		return response.toXml();
	}
        
    private String getSayByInputNum(String digits) {
        
        
        String sayText = null;
        
        switch (digits) {
            case "1":
            	sayText = "You selected sales. Good for you!";
                break;
            case "2":
            	sayText = "You need support. We will help!";
                break;
            default:
            	//sayText = "Sorry, I don\'t understand that choice.";
                break;
        }
        
        return sayText;
    }

	private Object conferenceSpecificPhone(HttpServletRequest req) {
		// Get the number of the incoming caller
		String fromNumber = req.getParameter("From");

		log.info("fromNumber=" + fromNumber);

		String SpecificPhone = "+15017250604";

		VoiceResponse twiml = null;
		if (!SpecificPhone.equalsIgnoreCase(fromNumber)) {
			Reject reject = new Reject.Builder().build();
			twiml = new VoiceResponse.Builder().reject(reject).build();
		} else {
			Conference.Builder conferenceBuilder = new Conference.Builder("My Conference SpecificPhone");
			String MODERATOR = "+15558675310";
			if (MODERATOR.equalsIgnoreCase(fromNumber)) {
				conferenceBuilder.startConferenceOnEnter(true);
				conferenceBuilder.endConferenceOnExit(true);
			} else {
				conferenceBuilder.endConferenceOnExit(false);
			}

			// Create a TwiML builder object
			twiml = new VoiceResponse.Builder().dial(new Dial.Builder().conference(conferenceBuilder.build()).build())
					.build();
		}

		try {

			return twiml.toXml();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Object conference(HttpServletRequest req) {
		// Get the number of the incoming caller
		String fromNumber = req.getParameter("From");

		Conference.Builder conferenceBuilder = new Conference.Builder("My Conference");

		String MODERATOR = "+15558675310";
		if (MODERATOR.equalsIgnoreCase(fromNumber)) {
			conferenceBuilder.startConferenceOnEnter(true);
			conferenceBuilder.endConferenceOnExit(true);
		} else {
			conferenceBuilder.endConferenceOnExit(false);
		}

		// Create a TwiML builder object
		VoiceResponse twiml = new VoiceResponse.Builder()
				.dial(new Dial.Builder().conference(conferenceBuilder.build()).build()).build();

		try {

			return twiml.toXml();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Object dial() {
		Dial dial = new Dial.Builder("+13125486404").build();
		Say say = new Say.Builder("Goodbye").build();
		VoiceResponse response = new VoiceResponse.Builder().dial(dial).say(say).build();

		try {

			return response.toXml();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
