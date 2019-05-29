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
import com.twilio.twiml.voice.Reject;
import com.twilio.twiml.voice.Say;

@Controller
public class VoiceController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public Object voice(HttpServletRequest req) {
		
		//return dial();
		
		//return conference(req);
		
		return conferenceSpecificPhone(req);

	}
	
	private Object conferenceSpecificPhone(HttpServletRequest req) {
		// Get the number of the incoming caller
	    String fromNumber = req.getParameter("From");
	    
	    log.info("fromNumber="+fromNumber);
	    
	    String SpecificPhone = "+15017250604";

	    VoiceResponse twiml = null;
	    if(!SpecificPhone.equalsIgnoreCase(fromNumber)) {
	    	Reject reject = new Reject.Builder().build();
	    	twiml = new VoiceResponse.Builder()
	        	.reject(reject)
	            .build();
	    }
	    else {
	    	Conference.Builder conferenceBuilder = new Conference.Builder("My Conference SpecificPhone");
	    	String MODERATOR = "+15558675310";
	    	if (MODERATOR.equalsIgnoreCase(fromNumber)) {
	    		conferenceBuilder.startConferenceOnEnter(true);
	    		conferenceBuilder.endConferenceOnExit(true);
	    	} else {
	    		conferenceBuilder.endConferenceOnExit(false);
	    	}
	    	
	    	// Create a TwiML builder object
	    	twiml = new VoiceResponse.Builder()
	    			.dial(new Dial.Builder().conference(conferenceBuilder.build()).build())
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
