package com.montnets.twilio.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class SmsController {


	@RequestMapping("/send")
	public String send() {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			PhoneNumber from = new com.twilio.type.PhoneNumber("+16504580008");
			//PhoneNumber to = new com.twilio.type.PhoneNumber("+8613682324521");
			PhoneNumber to = new com.twilio.type.PhoneNumber("+19166196842");
			String body = "我发一个中文试试";
			
			Message message = Message
					.creator(to, from, body)
					//.setStatusCallback(URI.create("http://ca00e6c7.ngrok.io/status"))
					.create();

			return message.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	@RequestMapping("/sendWithCopilot")
	public String sendWithCopilot() {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);

			Message message = Message.creator(new com.twilio.type.PhoneNumber("+8613682324521"),
					"MG466854078d7e9623e525476f35c43374", "Phantom Menace was clearly the best of the prequel trilogy.")
					.create();

			return message.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
