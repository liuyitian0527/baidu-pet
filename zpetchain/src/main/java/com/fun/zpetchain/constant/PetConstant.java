package com.fun.zpetchain.constant;

import java.util.LinkedHashMap;

import com.fun.zpetchain.util.FileUtil;
import com.fun.zpetchain.util.PropUtil;

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
	public final static int TIME_OUT = 1000;

	/**
	 * 根据售价升序
	 */
	public final static String SORT_TYPE_AMT = "AMOUNT_ASC";

	/**
	 * 根据创建时间倒序
	 */
	public final static String SORT_TYPE_TIME = "CREATETIME_DESC";

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
	// public final static String GET_PET_BY_ID = "https://pet-chain.baidu.com/data/pet/queryPetByIdWithAuth";
	/************************* url 结束********************************** ****************************************************/

	/********************************************* 售价配置 开始 **************************************************************/
	public final static LinkedHashMap<String, Integer> SALE_AMOUNT = new LinkedHashMap<String, Integer>();

	static {
		// key = 史诗_1代_2天
		SALE_AMOUNT.put("史诗_0_0分钟", 8000);
		SALE_AMOUNT.put("史诗_0_24小时", 7600);
		SALE_AMOUNT.put("史诗_0_2天", 7300);
		SALE_AMOUNT.put("史诗_0_4天", 6800);

		SALE_AMOUNT.put("史诗_1_0分钟", 7400);
		SALE_AMOUNT.put("史诗_1_24小时", 6800);
		SALE_AMOUNT.put("史诗_1_2天", 6400);
		SALE_AMOUNT.put("史诗_1_4天", 5800);

		SALE_AMOUNT.put("史诗_2_0分钟", 7200);
		SALE_AMOUNT.put("史诗_2_24小时", 6500);
		SALE_AMOUNT.put("史诗_2_2天", 5999);
		SALE_AMOUNT.put("史诗_2_4天", 5888);
	}

	/********************************************* 售价配置 结束 **************************************************************/

	/**
	 * 用户登录cookie
	 */
	public static String COOKIE = null;

	static {
		try {
			COOKIE = FileUtil.readLine(PropUtil.getProp("cookie_path"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
