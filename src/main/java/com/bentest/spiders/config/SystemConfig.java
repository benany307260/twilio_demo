package com.bentest.spiders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="system")
public class SystemConfig {
	
	/**
	 * 扫描指令Cron表达式，默认每5s一次
	 */
	private String scanCmdCron = "0/5 * * * * ?";
	
	/**
	 * 服务端节点编号，最大两位
	 */
	private int serverNode;
	
	/**
	 * 服务端区域编号，最大两位
	 */
	private int areaNode;
	
	private String amzUrl;

	public String getAmzUrl() {
		return amzUrl;
	}

	public void setAmzUrl(String amzUrl) {
		this.amzUrl = amzUrl;
	}

	public String getScanCmdCron() {
		return scanCmdCron;
	}

	public void setScanCmdCron(String scanCmdCron) {
		this.scanCmdCron = scanCmdCron;
	}

	public int getServerNode() {
		return serverNode;
	}

	public void setServerNode(int serverNode) {
		this.serverNode = serverNode;
	}

	public int getAreaNode() {
		return areaNode;
	}

	public void setAreaNode(int areaNode) {
		this.areaNode = areaNode;
	}
	
}
