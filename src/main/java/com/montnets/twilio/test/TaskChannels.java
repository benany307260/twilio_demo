package com.montnets.twilio.test;

import com.montnets.twilio.controller.TwilioConstant;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.taskrouter.v1.workspace.TaskChannel;

public class TaskChannels {
	public static void main(String[] args) {
		Twilio.init(TwilioConstant.ACCOUNT_SID, TwilioConstant.AUTH_TOKEN);

		String WORKSPACE_SID = "WSd6cc1bbc261d756db7c9e23b44129b8c";
		ResourceSet<TaskChannel> channels = TaskChannel.reader(WORKSPACE_SID).read();

		for (TaskChannel channel : channels) {
			System.out.println(channel.getUniqueName());
		}
	}
}
