package com.fun.zpetchain.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
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
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;

/**
 * 
 * Title. http请求工具类<br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2018-3-31 上午12:06:55
 * <p>
 * Author: liuyt
 * <p>
 * Version: 1.0
 * <p>
 */
public class HttpUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

	public static JSONObject post(String url, String data, User user) {

		HttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(PetConstant.TIME_OUT).setSocketTimeout(PetConstant.TIME_OUT).build();

		JSONObject jsonResult = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		setHeader(httpPost, user, null);

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
					logger.error("post request error, rsponse info [{}]", str);
				}
			} else {
				logger.error("post rsp code ：{} - url:{}", result.getStatusLine().getStatusCode(), url);
			}
		} catch (IOException e) {
			logger.error("post error:[{}] - url:{}", e.getMessage(), url);
		}

		return jsonResult;
	}

	public static JSONObject post(String url, String data, User user, Pet pet) {

		HttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(PetConstant.TIME_OUT).setSocketTimeout(PetConstant.TIME_OUT).build();

		JSONObject jsonResult = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		setHeader(httpPost, user, pet);

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
					logger.error("post request error, rsponse info [{}]", str);
				}
			} else {
				logger.error("post rsp code ：{} - url:{}", result.getStatusLine().getStatusCode(), url);
			}
		} catch (IOException e) {
			logger.error("post error:[{}] - url:{}", e.getMessage(), url);
		}

		return jsonResult;
	}

	private static void setHeader(HttpPost httpPost, User user, Pet pet) {

		httpPost.setHeader("Cookie", user.getCookie());
		httpPost.setHeader(
				"User-Agent",
				"Mozilla/5.0 (iPhone; CPU iPhone OS 11_2_5 like Mac OS X) AppleWebKit/604.5.6 (KHTML, like Gecko) Mobile/15D60 MicroMessenger/6.6.3 NetType/4G Language/zh_CN");
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
		httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
		httpPost.setHeader("Host", "pet-chain.baidu.com");
		httpPost.setHeader("Origin", "https://pet-chain.baidu.com");
		if (pet != null) {
			String pid = pet.getPetId();
			String validCode = StringUtils.defaultIfBlank(pet.getValidCode(), "");
			httpPost.setHeader("Referer", "https://pet-chain.baidu.com/chain/detail?channel=market&petId=" + pid + "&validCode=" + validCode
					+ "&appId=1&tpl=");
		} else {
			httpPost.setHeader("Referer",
					"https://pet-chain.baidu.com/chain/detail?channel=market&petId=1855388969513255367&validCode=6848e4fc4175e6893c2e5d01e7d03a35");
		}
	}
}
