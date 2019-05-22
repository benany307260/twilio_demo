package com.bentest.spiders.service.department;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.bentest.spiders.config.SystemConfig;
import com.bentest.spiders.constant.AMZConstant;
import com.bentest.spiders.constant.CmdType;
import com.bentest.spiders.entity.AmzCmdtask;
import com.bentest.spiders.entity.AmzDepartment;
import com.bentest.spiders.repository.AmzCmdtaskRespository;
import com.bentest.spiders.repository.AmzDepartmentRespository;
import com.bentest.spiders.util.GetIncrementId;
import com.bentest.spiders.util.URLUtil;

import cn.hutool.core.util.StrUtil;

@Service
public class AmzDepService {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DepRootHtmlProcess depRootHtmlProcess;
	
	@Autowired
	private DepSonHtmlProcess depSonHtmlProcess;
	
	@Autowired
	private AmzDepartmentRespository amzDepartmentRespository;
	
	@Autowired
	AmzCmdtaskRespository cmdtaskRespository;
	
	@Autowired
    private SystemConfig systemConfig;
	
	public int dealRootDep(String cmdText) {
		
		try {
			if(StrUtil.isBlank(cmdText)) {
				log.error("处理根类目，指令内容为空。");
				return -1;
			}
			
			AmzDepartment parentDep = JSON.parseObject(cmdText, AmzDepartment.class);
			if(parentDep == null) {
				log.error("处理根类目，父类目对象为null。cmdText="+cmdText);
				return -2;
			}
			
			List<AmzDepartment> rootDepList = handleRootDep(parentDep);
			if(rootDepList == null || rootDepList.size() < 1) {
				return -1;
			}
			
			List<AmzCmdtask> cmdList = new ArrayList<>();
			// 写指令通知下载程序下载子类目页面
			for(AmzDepartment dep : rootDepList) {
				AmzCmdtask cmd = new AmzCmdtask();
				cmd.setCmdStatus(0);
				cmd.setCmdType(CmdType.CMD102);
				
				String cmdTextJson = JSON.toJSONString(dep);
				
				cmd.setCmdText(cmdTextJson);
				
				cmdList.add(cmd);
			}
			
			cmdtaskRespository.saveAll(cmdList);
			return 1;
		} catch (Exception e) {
			log.error("处理根类目，异常。cmdText="+cmdText, e);
			return -9999;
		}
	}
	
	public int dealSonDep(String cmdText) {
		
		try {
			if(StrUtil.isBlank(cmdText)) {
				log.error("处理子类目，指令内容为空。");
				return -1;
			}
			
			AmzDepartment parentDep = JSON.parseObject(cmdText, AmzDepartment.class);
			if(parentDep == null) {
				log.error("处理子类目，父类目对象为null。cmdText="+cmdText);
				return -2;
			}
			
			List<AmzDepartment> depList = handleSonDep(parentDep);
			if(depList == null || depList.size() < 1) {
				log.error("处理子类目，获取子类目列表为空。cmdText="+cmdText);
				return -1;
			}
			
			List<AmzCmdtask> cmdList = new ArrayList<>();
			// 写指令通知下载程序下载子类目页面
			for(AmzDepartment dep : depList) {
				
				if(StrUtil.isBlank(dep.getUrl())) {
					log.error("处理子类目，获取子类目url为空。cmdText="+cmdText);
					continue;
				}
				
				String url = dep.getUrl();
				if(url.indexOf(AMZConstant.AMZ_US_DOMAIN) < 0) {
					url = systemConfig.getAmzUrl() + url;
					dep.setUrl(url);
				}
				
				String cmdTextJson = JSON.toJSONString(dep);
				AmzCmdtask cmd102 = new AmzCmdtask(CmdType.CMD102, cmdTextJson);
				cmdList.add(cmd102);
			}
			
			cmdtaskRespository.saveAll(cmdList);
			return 1;
		} catch (Exception e) {
			log.error("处理子类目，异常。cmdText="+cmdText, e);
			return -9999;
		}
	}
	
	/**
	 * 
	 * @param htmlFilePath
	 * @return 返回1为成功
	 */
	public List<AmzDepartment> handleRootDep(AmzDepartment parentDep) {
		
		if(parentDep == null) {
			log.error("处理根类目，传入父类目对象为null。");
			return null;
		}
		Map<String,String> depMap = depRootHtmlProcess.getDepsFromHtml(parentDep.getHtmlFilePath());
		if(depMap == null || depMap.size() < 1) {
			log.error("处理根类目，解析获取不到类目数据。htmlFilePath="+parentDep.getHtmlFilePath());
			return null;
		}
		
		try {
			List<AmzDepartment> amzDepList = new ArrayList<>();
			for(String key : depMap.keySet()) {
				
				AmzDepartment amzDep = new AmzDepartment();
				long id = GetIncrementId.getInstance().getCount(systemConfig.getServerNode(), systemConfig.getAreaNode());
				amzDep.setId(id);
				
				String depUrl = depMap.get(key);
				String depId = getAmzRootDepId(depUrl);
				amzDep.setDepId(depId);

				amzDep.setDepNameCn(key);
				amzDep.setDepName(key);

				amzDep.setUrl(depUrl);
				amzDep.setUrlDomain(AMZConstant.AMZ_US_DOMAIN);
				
				// 设置为根目录
				amzDep.setDepLevel(1);
				
				amzDep.setParentId(0L);
				amzDep.setParentDepId("0");
				
				// 类目状态。0-正常；1-被删除
				amzDep.setDepStatus(0);
				
				amzDep.setFromHtmlFilePath(parentDep.getHtmlFilePath());
				
				amzDep.setCreateTime(new Date());
				amzDep.setUpdateTime(new Date());
				
				amzDepList.add(amzDep);
			}
			amzDepartmentRespository.saveAll(amzDepList);
			return amzDepList;
		} catch (Exception e) {
			log.error("处理根类目，异常。htmlFilePath="+parentDep.getHtmlFilePath(), e);
			return null;
		}
	}
	
	public List<AmzDepartment> handleSonDep(AmzDepartment parentDep) {
		
		if(parentDep == null) {
			log.error("处理子类目，传入父类目对象为null。");
			return null;
		}
		
		List<AmzDepartment> depList = depSonHtmlProcess.getDepsFromHtml(parentDep.getHtmlFilePath(), parentDep.getDepId());
		if(depList == null || depList.size() < 1) {
			log.error("处理子类目，解析获取不到类目数据。param="+parentDep.toString());
			return null;
		}
		
		try {
			
			// 去掉比shopAllContent排序要小的key
			Iterator<AmzDepartment> it = depList.iterator();
			while(it.hasNext()) {
				AmzDepartment dep = it.next();
				String depId = getAmzSonDepId(dep.getUrl());
				// depId是属于父id，要排除掉
				if(parentDep.getDepIdAll().indexOf(depId) > -1) {
					it.remove();
					continue;
				}
				dep.setDepId(depId);
				dep.setDepIdAll(parentDep.getDepIdAll() + "/" + depId);
			}
			
			for(AmzDepartment dep : depList) {
				if(dep == null) {
					continue;
				}
				long id = GetIncrementId.getInstance().getCount(systemConfig.getServerNode(), systemConfig.getAreaNode());
				dep.setId(id);
				
				/*String depId = getAmzSonDepId(dep.getUrl());
				dep.setDepId(depId);*/

				//amzDep.setDepName(depName);
				//amzDep.setDepNameCn(depName);

				//amzDep.setUrl(depUrl);
				dep.setUrlDomain(AMZConstant.AMZ_US_DOMAIN);
				
				// 设置目录层级
				dep.setDepLevel(parentDep.getDepLevel()+1);
				
				dep.setParentId(parentDep.getId());
				dep.setParentDepId(parentDep.getDepId());
				
				// 类目状态。0-正常；1-被删除
				dep.setDepStatus(0);
				
				dep.setFromHtmlFilePath(parentDep.getHtmlFilePath());
				
				dep.setCreateTime(new Date());
				dep.setUpdateTime(new Date());
			}
			amzDepartmentRespository.saveAll(depList);
			return depList;
		} catch (Exception e) {
			log.error("处理子类目，异常。param="+parentDep.toString(), e);
			return null;
		}
	}
	
	private String getAmzRootDepId(String depUrl) {
		try {
			if(StrUtil.isBlank(depUrl)) {
				log.error("根类目通过dep url获取depId，depUrl为空。");
				return null;
			}
			String[] paramArray = depUrl.split("&");
			for(String param : paramArray) {
				String[] value = param.split("=");
				if(value.length < 2) {
					continue;
				}
				if("node".equals(value[0])) {
					return value[1];
				}
			}
			return null;
		} catch (Exception e) {
			log.error("根类目通过dep url获取depId，异常。depUrl="+depUrl, e);
			return null;
		}
	}
	
	private String getAmzSonDepId(String depUrl) {
		try {
			if(StrUtil.isBlank(depUrl)) {
				log.error("子类目通过dep url获取depId，depUrl为空。");
				return null;
			}
			//url示例：/s/ref=lp_16225011011_nr_n_0?fst=as:off&rh=i:kitchen-intl-ship,n:!16225011011,n:3206325011&bbn=16225011011&ie=UTF8&qid=1555152834&rnid=16225011011
			Map<String, String> mapRequestParam = URLUtil.URLRequest(depUrl);
			// rh示例：rh=i%3Akitchen-intl-ship%2Cn%3A%2116225011011%2Cn%3A3206325011
			String rh = mapRequestParam.get("rh");
			if(StrUtil.isBlank(rh)) {
				log.error("子类目通过dep url获取depId，rh为空。depUrl="+depUrl);
				return null;
			}
			// rh解码后示例：rh=i:kitchen-intl-ship,n:!16225011011,n:3206325011
			String rhDe = URLDecoder.decode(rh, "utf-8");
			String[] rhValue = rhDe.split(",");
			
			// 取最后一个
			String value = rhValue[rhValue.length-1];
			String depId = value.replace("n:", "");
			
			/*for(String value : rhValue) {
				// 不存在n:跳过
				if(value.indexOf("n:") < 0) {
					continue;
				}
				// 存在n:!也跳过
				if(value.indexOf("n:!") > -1) {
					continue;
				}
				
				String depId = value.replace("n:", "");
				return depId;
			}*/
			
			return depId;
		} catch (Exception e) {
			log.error("子类目通过dep url获取depId，异常。depUrl="+depUrl, e);
			return null;
		}
	}
	
	public static void main(String[] args) {
		AmzDepartment amzDep = new AmzDepartment();
		//long id = GetIncrementId.getInstance().getCount(systemConfig.getServerNode(), systemConfig.getAreaNode());
		amzDep.setId(1389163537180000267L);
		
		amzDep.setDepId("16225011011");

		amzDep.setDepNameCn("Home & Kitchen");
		amzDep.setDepName("Home & Kitchen");

		amzDep.setUrl("/s/browse?_encoding=UTF8&node=16225011011&ref_=nav_shopall-export_nav_mw_sbd_intl_kitchen");
		amzDep.setUrlDomain(AMZConstant.AMZ_US_DOMAIN);
		
		// 设置为根目录
		amzDep.setDepLevel(1);
		
		amzDep.setParentId(0L);
		amzDep.setParentDepId("0");
		
		// 类目状态。0-正常；1-被删除
		amzDep.setDepStatus(0);
		
		//amzDep.setDataSrcUrl(parentDep.getDataTarUrl());
		//amzDep.setDataTarUrl("F:\\study\\amz\\git\\pc_service\\page\\index.html");
		amzDep.setHtmlFilePath("F:\\study\\amz\\git\\pc_service\\page\\amz_home_kitchen.html");
		
		//amzDep.setCreateTime(new Date());
		//amzDep.setUpdateTime(new Date());
		
		String json = JSON.toJSONString(amzDep);
		System.out.println(json);
	}
}
