package com.montnets.twilio.controller;

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
import com.twilio.twiml.voice.Say;

@Controller
public class VoiceController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public Object voice(HttpServletRequest req) {
		
		//return dial();
		
		return conference(req);

	}
	
	// Update with your own phone number in E.164 format
	public static final String MODERATOR = "+15558675310";
	
	private Object conference(HttpServletRequest req) {
		// Get the number of the incoming caller
	    String fromNumber = req.getParameter("From");

	    Conference.Builder conferenceBuilder = new Conference.Builder("My Conference");

	    if (MODERATOR.equalsIgnoreCase(fromNumber)) {
	      conferenceBuilder.startConferenceOnEnter(true);
	      conferenceBuilder.endConferenceOnExit(true);
	    } else {
	      conferenceBuilder.endConferenceOnExit(false);
	    }

	    // Create a TwiML builder object
	    VoiceResponse twiml = new VoiceResponse.Builder()
	        .dial(new Dial.Builder()
	              .conference(conferenceBuilder.build())
	              .build()
	        ).build();

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
