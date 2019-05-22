package com.bentest.spiders.text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bentest.spiders.entity.AmzUA;
import com.bentest.spiders.repository.AmzCmdtaskRespository;
import com.bentest.spiders.repository.AmzUARespository;

@RestController
public class TestController {
	
	
	@Autowired
	private AmzUARespository amzUARespository;
	
	@Autowired
	private HtmlProcess htmlProcess;
	
	@Autowired
	AmzCmdtaskRespository cmdtaskRespository;
	
	@RequestMapping("/au")
	public Boolean getAu() {
		String htmlFilePath = "C:\\Users\\lenovo\\git\\pc_service\\page\\ua.html";
		List<String> uaList = htmlProcess.getUa(htmlFilePath);
		AtomicInteger idCount = new AtomicInteger(1);
		List<AmzUA> amzUAList = new ArrayList<>();
		for(String uaStr : uaList) {
			AmzUA amzUA = new AmzUA();
			int id = idCount.getAndAdd(1);
			amzUA.setId(id);
			amzUA.setUa(uaStr);
			amzUAList.add(amzUA);
		}
		
		amzUARespository.saveAll(amzUAList);
		return true;
	}
	
}
