package com.fun.zpetchain.util;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;


/**
 * tool class of http, send post request
 * <br><b>Copyright 2018 the original author or authors.</b>
 * @author 2bears
 * @since
 * @version 1.0
 */
public class HttpUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	/**
	 * do post request
	 * @author 2bears
	 * @since
	 * @param url
	 * @param data   json parameters
	 * @param conTimeOut connection timeout
	 * @param rspTimeOut response timeout
	 * @return
	 */
	public static JSONObject doJsonPost(String url, String data, int conTimeOut, int rspTimeOut) {
		
		HttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(conTimeOut).setSocketTimeout(rspTimeOut).build();
	    
	    JSONObject jsonResult = null;
	    HttpPost httpPost = new HttpPost(url);
	    httpPost.setConfig(requestConfig);
	    setHeader(httpPost);
	    
	    try {
            StringEntity entity = new StringEntity(data, "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

	        HttpResponse result = httpClient.execute(httpPost);
	        if (result.getStatusLine().getStatusCode() == 200) {
	            String str = "";
	            try {
	                str = EntityUtils.toString(result.getEntity());
	                jsonResult = JSONObject.parseObject(str);
	            } catch (Exception e) {
	                logger.error("post request error, rsponse info [{}]",str);
	            }
	        }else{
	        	logger.error("post rsp code ："+result.getStatusLine().getStatusCode());
	        }
	    } catch (IOException e) {
	        logger.error("post error:[{}]", e.getMessage());
	    }	    
	    
	    return jsonResult;
	}
	
	/**
	 * set header, in additions to cookies, other fixed
	 * @author 2bears
	 * @since
	 * @param httpPost
	 */
	private static void setHeader(HttpPost httpPost) {
		httpPost.setHeader("Cookie", PetConstant.COOKIE);
		httpPost.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_2_5 like Mac OS X) AppleWebKit/604.5.6 (KHTML, like Gecko) Mobile/15D60 MicroMessenger/6.6.3 NetType/4G Language/zh_CN");
		httpPost.setHeader("Accept","application/json");
		httpPost.setHeader("Connection","keep-alive");
		httpPost.setHeader("Content-Type","application/json");
		httpPost.setHeader("Accept-Encoding","gzip, deflate, br");
		httpPost.setHeader("Accept-Language","zh-CN,zh;q=0.9");
		httpPost.setHeader("Host","pet-chain.baidu.com");
		httpPost.setHeader("Origin","https://pet-chain.baidu.com");
		httpPost.setHeader("Referer","https://pet-chain.baidu.com/chain/detail?channel=market&petId=1855388969513255367&validCode=6848e4fc4175e6893c2e5d01e7d03a35");   		
	}
}
