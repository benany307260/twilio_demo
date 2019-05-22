package com.bentest.spiders.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;

import com.bentest.spiders.constant.PropEncConstant;
import com.bentest.spiders.util.AESUtil;
import com.bentest.spiders.util.SafeProperties;

import cn.hutool.core.util.HexUtil;




public class WriteBackConfig {
	 private static final Logger LOG = LoggerFactory.getLogger(WriteBackConfig.class);
//	 private static Logger LOG = LoggerFactory.getLogger("othersInfo");
	private static volatile WriteBackConfig instance;
	
	private WriteBackConfig(){
		
	}
	
	public WriteBackConfig getInstance(){
		if(WriteBackConfig.instance == null){
			synchronized (WriteBackConfig.class) {
				if(WriteBackConfig.instance == null){
					WriteBackConfig.instance = new WriteBackConfig();
				}
			}
		}
		return WriteBackConfig.instance;
	}
	
  /**
	 * 加密连接
	 * 
	 * @return
	 */
	public static boolean getConnectionTest(Map<String, String> conf) {
		boolean connectFlag = false;
		String driverClassName = conf.get("driverClassName");
		String url = conf.get("url");
		String username = conf.get("username");
		String password = conf.get("password");
		Connection connection = null;
		String passwordTemp=null;
		String usernameTemp=null;
		try {
			try{
				//解密账号
				byte[] unameMiByte = HexUtil.decodeHex(username);
				byte[] unameByte = AESUtil.decrypt(unameMiByte, PropEncConstant.DB_PWD_KEY.getBytes());
				usernameTemp = new String(unameByte, "utf-8");
				//解密密码
				byte[] pwdMiByte = HexUtil.decodeHex(password);
				byte[] pwdByte = AESUtil.decrypt(pwdMiByte, PropEncConstant.DB_PWD_KEY.getBytes());
				passwordTemp = new String(pwdByte, "utf-8");
			}catch(Exception e){
//				LOG.debug("解密失败!原因是:"+e.getMessage());
			}
			if (passwordTemp != null && usernameTemp != null) {
				connection = getConnection(driverClassName, url, usernameTemp, passwordTemp);
				if (connection != null) {// 密文连接
					conf.put("pwdmin", passwordTemp);
					conf.put("unamemin", usernameTemp);
					LOG.info("application.properties文件里的数据库连接账号密码为密文!");
					connectFlag = true;
				}
			} else {// 明文加密
				connection = getConnection(driverClassName, url, username, password);
				if (connection != null) {
					conf.put("pwdmin", password);
					conf.put("unamemin", username);
					LOG.info("application.properties文件里的数据库连接账号密码为明文");
					connectFlag = writeEncodePwd("application.properties", "spring.datasource.password","spring.datasource.username", password, username);
				}
			}
		} catch (Exception e) {
			connectFlag = false;
			LOG.error("连接数据库出错，原因是：" + e.getMessage());
		}
		return connectFlag;
	}
	
	public static Connection getConnection(String driveName, String url, String userName, String password)
	    throws Exception {
		Connection dbConn = null;
		try {
			Class.forName(driveName);
			dbConn = DriverManager.getConnection(url, userName, password);
			dbConn.setAutoCommit(false);
		} catch (Exception e) {
			dbConn = null;
		}
		return dbConn;
	}
	
	/**
	 * 加密回写密文
	 * 
	 * @param conf
	 *          配置文件
	 * @param confParam
	 *          回写的配置参数名
	 * @param password
	 *          明文密码
	 * @return
	 */
	private static boolean writeEncodePwd(String conf, String pwdconf,String unameconf, String password, String uname) throws Exception {
		boolean writeFlag = false;
		InputStream in = null;
		Properties prop = null;
		String path = null;
		ClassPathResource resource = null;
		try {
			ApplicationHome home = new ApplicationHome(WriteBackConfig.class);
			File jarFile = home.getSource();
			String configPath = jarFile.getParentFile().getAbsolutePath()+"/application.properties";
			File file = new File(configPath);
			if(file.exists()) {
				in = new FileInputStream(file);
				path = file.getPath();
				LOG.info("获取外部配置文件成功，path="+path);
			}
			else {
				resource = new ClassPathResource("/application.properties");
				in = resource.getInputStream();
				path = resource.getURI().getPath();
				LOG.info("获取内置配置文件成功，path="+path);
			}
			if(path == null || "".equals(path.trim())){
				LOG.error("获取配置文件路径为空");
				return false;
			}
			//in = ClassLoader.class.getResourceAsStream('/'+"application.properties");
			prop = new SafeProperties();
			prop.load(in);
			LOG.warn("回写密文到配置文件,conf=" + conf + ",pwdconf=" + pwdconf+ ",unameconf=" + unameconf);
			writeFlag = writeEncodePwd(path, pwdconf,unameconf, password,uname, prop);
		} catch (Exception e) {
			writeFlag = false;
			LOG.error("加载application.properties中的参数失败",e);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return writeFlag;
	}
	
	/**
	 * 密文回写
	 * 
	 * @param conf
	 *          配置文件路径
	 * @param confParam
	 *          回写参数名
	 * @param password
	 *          回写之前的明文
	 * @param prop
	 *          完整内存配置对象
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static boolean writeEncodePwd(String path, String pwdconf,String unameconf, String password, String uname,Properties prop)
	    throws IOException, URISyntaxException {
		boolean wirteFlag = true;
		FileOutputStream fo = null;
		String pwdEnc = null;
		String unameEnc = null;
		Properties prop1 = (Properties) prop.clone();
		try {
			try {
				pwdEnc = HexUtil.encodeHexStr(AESUtil.encrypt(password, PropEncConstant.DB_PWD_KEY.getBytes())).toString();
				unameEnc = HexUtil.encodeHexStr(AESUtil.encrypt(uname, PropEncConstant.DB_PWD_KEY.getBytes())).toString();
				prop1.setProperty(pwdconf, pwdEnc);
				prop1.setProperty(unameconf, unameEnc);
			} catch (Exception e) {
				wirteFlag = false;
				LOG.error("对密码加密失败...exception:" + e.getMessage());
			}
			fo = new FileOutputStream(path);
			prop1.store(fo, "encode password");
		} catch (IOException e) {
			wirteFlag = false;
			LOG.error(e.getMessage());
		} finally {
			if (null != fo) {
				try {
					fo.close();
					LOG.warn("回写密文完成");
				} catch (IOException e1) {
					LOG.error(e1.getMessage());
				}
			}
		}
		return wirteFlag;
	}

}
