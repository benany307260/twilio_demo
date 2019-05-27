package com.montnets.twilio.controller;

import java.net.URI;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

@RestController
public class TestController2 {

	public static final String ACCOUNT_SID = "AC5ca4e322f2f240db9462a21c56d3bbe4";
	public static final String AUTH_TOKEN = "56eab5b6da28c54707aa2863804acfc0";

	@RequestMapping("/send")
	public String send() {
		try {
			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
			Message message = Message
					.creator(new com.twilio.type.PhoneNumber("+8613682324521"),
							new com.twilio.type.PhoneNumber("+16504580008"), "list")
					.setStatusCallback(URI.create("http://ca00e6c7.ngrok.io/status")).create();

			return message.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping("/sendWithCopilot")
	public String sendWithCopilot() {
		try {
			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

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
