package com.fun.zpetchain.constant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.util.FileUtil;
import com.fun.zpetchain.util.PropUtil;

/**
 * 
 * Title. pet基础常量类<br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2018-3-31 下午9:41:49
 * <p>
 * Author: liuyt
 * <p>
 * Version: 1.0
 * <p>
 */
public class PetConstant {
	/**
	 * 接口成功标识
	 */
	public final static String SUCCESS = "00";

	/**
	 * 分页大小
	 */
	public final static int PAGE_SIZE = 20;

	/**
	 * 请求超时时间（毫秒）
	 */
	public final static int TIME_OUT = 1000 * 3;

	/**
	 * 验证码有效时间
	 */
	public static final int VALID_TIME = 60000;

	/**
	 * 验证码缓存数
	 */
	public static final int SAFE_QUEUE_SIZE = 20;

	/**
	 * 最大尝试购买次数
	 */

	public final static int TYR_COUNT = 10;

	/**
	 * 根据售价升序
	 */
	public final static String SORT_TYPE_AMT = "AMOUNT_ASC";

	/**
	 * 根据创建时间倒序
	 */
	public final static String SORT_TYPE_TIME = "CREATETIME_DESC";

	/**
	 * 筛选条件: 史诗 休息不限 状态 正常
	 */
	public final static String FILTER_COND_EPIC = "{\"1\":\"3\",\"6\":\"1\"}";

	/**
	 * 筛选条件: 神话 休息不限 状态 正常
	 */
	public final static String FILTER_COND_MYTH = "{\"1\":\"4\",\"6\":\"1\"}";

	/********************** url 开始 ********************************** *******************************************************/

	/** 市场列表 */
	public final static String QUERY_PETS_ON_SALE = "https://pet-chain.baidu.com/data/market/queryPetsOnSale";

	/** 验证码 */
	public final static String CAPTCHA_URL = "https://pet-chain.baidu.com/data/captcha/gen";

	/** 购买 */
	public final static String TXN_CREATE = "https://pet-chain.baidu.com/data/txn/create";

	/** 我的宠物列表 */
	public final static String MY_PET_LIST = "https://pet-chain.baidu.com/data/user/pet/list";

	/** 我的订单列表 */
	public final static String ORDER_LIST = "https://pet-chain.baidu.com/data/user/order/list";

	/** 上架 */
	public final static String SALE_PET = "https://pet-chain.baidu.com/data/market/salePet";

	/** 下架 */
	public final static String CANCEL_SALE_PET = "https://pet-chain.baidu.com/data/market/unsalePet";

	/** 获取宠物信息 */
	public final static String GET_PET_BY_ID = "https://pet-chain.baidu.com/data/pet/queryPetById";
	// public final static String GET_PET_BY_ID =
	// "https://pet-chain.baidu.com/data/pet/queryPetByIdWithAuth";

	/************************* url 结束********************************** ****************************************************/

	/********************************************* 售价配置 开始 **************************************************************/
	/**
	 * 天使卖出加价金额
	 */
	public final static Integer ANGEL_RAISE = 11000;

	/**
	 * 超级稀有买入加价
	 */
	public final static Integer SUPER_RARE_RAISE = 4000;

	public final static LinkedHashMap<String, Integer> SALE_AMOUNT = new LinkedHashMap<String, Integer>();

	static {
		// key = 史诗_1代_2天_4稀
		SALE_AMOUNT.put("史诗_0_0分钟_4稀", 6800);
		SALE_AMOUNT.put("史诗_0_24小时_4稀", 6500);
		SALE_AMOUNT.put("史诗_0_2天_4稀", 6200);
		SALE_AMOUNT.put("史诗_0_4天_4稀", 5500);

		SALE_AMOUNT.put("史诗_1_0分钟_4稀", 5500);
		SALE_AMOUNT.put("史诗_1_24小时_4稀", 5400);
		SALE_AMOUNT.put("史诗_1_2天_4稀", 5100);
		SALE_AMOUNT.put("史诗_1_4天_4稀", 4888);

		SALE_AMOUNT.put("史诗_2_0分钟_4稀", 6400);
		SALE_AMOUNT.put("史诗_2_24小时_4稀", 5888);
		SALE_AMOUNT.put("史诗_2_2天_4稀", 5666);
		SALE_AMOUNT.put("史诗_2_4天_4稀", 5444);

		SALE_AMOUNT.put("史诗_1_0分钟_5稀", 22000);
		SALE_AMOUNT.put("史诗_1_24小时_5稀", 20000);
		SALE_AMOUNT.put("史诗_1_2天_5稀", 19000);
		SALE_AMOUNT.put("史诗_1_4天_5稀", 18000);

		SALE_AMOUNT.put("史诗_2_0分钟_5稀", 22000);
		SALE_AMOUNT.put("史诗_2_24小时_5稀", 20000);
		SALE_AMOUNT.put("史诗_2_2天_5稀", 19000);
		SALE_AMOUNT.put("史诗_2_4天_5稀", 18000);

		SALE_AMOUNT.put("史诗_3_0分钟_4稀", 6400);
		SALE_AMOUNT.put("史诗_3_0分钟_5稀", 22000);
	}

	/********************************************* 售价配置 结束 **************************************************************/

	public static final String SUCCESS_BUY_PATH_STRING = null;

	/**
	 * 用户信息
	 */
	public static List<User> USERS = new ArrayList<User>();

	static {
		try {
			String jsonString = FileUtil.readTxt(PropUtil.getProp("cookie_path"));
			JSONArray array = JSONArray.parseArray(jsonString);
			for (int i = 0; i < array.size(); i++) {
				JSONObject userJson = array.getJSONObject(i);
				for (String key : userJson.keySet()) {
					User user = new User();
					user.setName(key);
					user.setCookie(userJson.getString(key));
					USERS.add(user);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
