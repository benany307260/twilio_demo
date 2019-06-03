package com.montnets.twilio.test;

import java.net.URI;

import com.montnets.twilio.controller.TwilioConstant;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.notify.v1.service.Binding;
import com.twilio.rest.notify.v1.service.Notification;
import com.twilio.type.PhoneNumber;

public class SmsMessage {
	
	public static void main(String[] args) {
		//SmsMessage.sendSms();
		//SmsMessage.sendMms();
		//SmsMessage.sendFromMessageService(TwilioConstant.TWILIO_PHONE_RAO_16504580008);
		//SmsMessage.sendFromMessageService(TwilioConstant.PHONE_SIMULATOR_14122534494);
		
		//SmsMessage.notify_Binding("0000001", TwilioConstant.PHONE_SIMULATOR_14122534494);
		
		//SmsMessage.notify_Send(TwilioConstant.PHONE_SIMULATOR_14122534494);
		SmsMessage.notifySendByPhoneNum(TwilioConstant.PHONE_SIMULATOR_14122534494);
		
	}
	
	public static String sendSms() {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			PhoneNumber from = new com.twilio.type.PhoneNumber(TwilioConstant.TWILIO_PHONE_HZB_15106166955);
			PhoneNumber to = new com.twilio.type.PhoneNumber("+14122534494");
			//PhoneNumber to = new com.twilio.type.PhoneNumber("+19166196842");
			String body = "try send a sms message again2";
			
			Message message = Message
					.creator(to, from, body)
					//.setStatusCallback(URI.create("http://ca00e6c7.ngrok.io/status"))
					.create();
			System.out.println(message.getSid());
			return message.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static String sendMms() {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			PhoneNumber from = new com.twilio.type.PhoneNumber(TwilioConstant.TWILIO_PHONE_HZB_15106166955);
			PhoneNumber to = new com.twilio.type.PhoneNumber("+14122534494");
			//PhoneNumber to = new com.twilio.type.PhoneNumber("+19166196842");
			String body = "try send a MMS message again";
			String pngUrl = "https://demo.twilio.com/owl.png";
			
			Message message = Message
					.creator(to, from, body)
					.setMediaUrl(new URI(pngUrl))
					//.setStatusCallback(URI.create("http://ca00e6c7.ngrok.io/status"))
					.create();
			System.out.println(message.getSid());
			return message.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	
	public static String sendFromMessageService(String phone) {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			String messagingServiceSid = "MG38fa429f6b5a8f43bac46f859e298b05";
			//PhoneNumber from = new com.twilio.type.PhoneNumber(TwilioConstant.TWILIO_PHONE_NUMBER);
			//PhoneNumber to = new com.twilio.type.PhoneNumber("+14122534494");
			//PhoneNumber to = new com.twilio.type.PhoneNumber("+19166196842");
			PhoneNumber to = new com.twilio.type.PhoneNumber(phone);
			String body = "hzb-try send a sms message from msg service";
			
			Message message = Message
					.creator(to, messagingServiceSid, body)
					//.setStatusCallback(URI.create("http://ca00e6c7.ngrok.io/status"))
					.create();
			System.out.println("sid="+message.getSid()+",from="+message.getFrom());
			return message.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static String notify_Binding(String identity, String bindingPhone) {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			Binding binding = Binding.creator
				    (
				    	TwilioConstant.NOTIFY_SID_HZB,
				    	identity, // We recommend using a GUID or other anonymized identifier for Identity.
				    	Binding.BindingType.SMS,
				    	bindingPhone
				    ).create();

				    System.out.println(binding.getSid());
			return binding.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static String notify_Send(String identity) {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			Notification notification = Notification
			        .creator(TwilioConstant.NOTIFY_SID_HZB)
			        .setBody("try send sms from notify "+identity)
			        .setIdentity(identity)
			        .create();

			    System.out.println(notification.getSid());

			return notification.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static String notifySendByPhoneNum(String phone) {
		try {
			Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);
			
			String bindingStr  = "{\"binding_type\":\"sms\",\"address\":\""+phone+"\"}";
			
			Notification notification = Notification
			        .creator(TwilioConstant.NOTIFY_SID_HZB)
			        .setBody("try send sms from notify "+phone)
			        .setToBinding(bindingStr)
			        .create();

			    System.out.println(notification.getSid());

			return notification.getSid();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
}
