package com.fun.zpetchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.util.HttpUtil;
import com.fun.zpetchain.util.TimeUtil;

/**
 * 
 * Title.用户宠物中心 <br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2018-3-31 下午7:58:02
 * <p>
 * Author: liuyt
 * <p>
 * Version: 1.0
 * <p>
 */
public class PetCenter {

	public static void main(String[] args) {
		orderTotal();
	}

	/**
	 * 我的宠物列表
	 */
	public static List<Pet> getMyPetList(User user, Boolean isFindAttr) {
		List<Pet> pets = new ArrayList<Pet>();
		int pageNo = 1, totalCount = 0;

		while (true) {
			try {
				// 入参封装
				String params = getPetListParams(pageNo);

				// 接口调用
				JSONObject obj = HttpUtil.post(PetConstant.MY_PET_LIST, params, user);

				if (obj != null && PetConstant.SUCCESS.equals(obj.getString("errorNo"))) {

					List<Pet> res = JSONArray.parseArray(obj.getJSONObject("data").getString("dataList"), Pet.class);

					if (res.size() > 0) {
						pets.addAll(res);
					}

					totalCount += res.size();

					// 获取狗狗总数，判断是否需要翻页
					int dogCount = obj.getJSONObject("data").getIntValue("totalCount");
					if (totalCount == dogCount) {
						break;
					}

					pageNo++;

				} else {
					System.out.println(user.getName() + " 宠物列表获取失败，返回：" + obj);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		System.out.println(user.getName() + " 一共查询到" + totalCount + "只宠物！");

		if (!isFindAttr) {
			return pets;
		}

		System.out.println(user.getName() + " 稀有属性数量查询...开始...");
		List<Pet> rePets = new ArrayList<Pet>();
		for (Pet pet : pets) {
			Pet info = getPetById(pet.getPetId(), user);
			String amount = null;
			try {
				amount = PetSale.getSalePetAmount(info);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if (StringUtils.isNotBlank(amount)) {
				info.setAmount(new BigDecimal(amount).doubleValue());
			}
			rePets.add(info);
		}
		System.out.println(user.getName() + " 稀有属性数量查询...结束...");

		sort(rePets);
		return rePets;
	}

	private static void sort(List<Pet> pets) {
		Collections.sort(pets, new Comparator<Pet>() {
			@Override
			public int compare(Pet o1, Pet o2) {
				if (o1.getAmount() != null && o2.getAmount() != null) {
					if (o1.getAmount() > o2.getAmount()) {
						return 1;
					} else if (o1.getAmount() < o2.getAmount()) {
						return -1;
					}
				}
				return 0;
			}
		});
	}

	/**
	 * 根据宠物id，获取单个宠物信息
	 * 
	 * @param petId
	 */
	public static Pet getPetById(String petId, User user) {
		Pet pet = null;
		String params = getPetParams(petId);

		// 接口调用
		JSONObject result = HttpUtil.post(PetConstant.GET_PET_BY_ID, params, user);

		if (result != null && PetConstant.SUCCESS.equals(result.getString("errorNo"))) {
			JSONObject pJson = result.getJSONObject("data");
			pet = JSONObject.parseObject(pJson.toString(), Pet.class);

			// 稀有属性数量赋值
			int rareNum = 0;
			JSONArray attrs = pJson.getJSONArray("attributes");
			List<String> rareAttrs = new ArrayList<String>();
			for (int i = 0; i < attrs.size(); i++) {
				JSONObject atr = attrs.getJSONObject(i);
				String name = atr.getString("name");
				String value = atr.getString("value");

				if (name.equals("体型")) {
					if (value.equals("天使")) {
						pet.setIsAngell(true);
					} else {
						pet.setIsAngell(false);
					}
				} else if (name.equals("眼睛")) {
					if (value.equals("白眉斗眼") || value.equals("小对眼")) {
						pet.setIsWhiteEyes(true);
					} else {
						pet.setIsWhiteEyes(false);
					}
				}

				if ("稀有".equals(atr.getString("rareDegree"))) {
					rareNum++;
					rareAttrs.add(name);
				}
			}

			pet.setRareAttrs(StringUtils.join(rareAttrs, ","));
			pet.setRareNum(rareNum);
		}

		return pet;
	}

	private static String getPetParams(String petId) {
		JSONObject j = new JSONObject();
		j.put("appId", "1");
		j.put("nounce", "");
		j.put("petId", petId);
		j.put("requestId", System.currentTimeMillis());
		j.put("timeStamp", "");
		j.put("token", "");
		j.put("tpl", "");

		return j.toString();
	}

	private static String getPetListParams(int pageNo) {
		JSONObject j = new JSONObject();
		j.put("appId", "1");
		j.put("nounce", "");
		j.put("pageNo", pageNo);
		j.put("pageSize", PetConstant.PAGE_SIZE);
		j.put("pageTotal", "-1");
		j.put("requestId", System.currentTimeMillis());
		j.put("timeStamp", "");
		j.put("token", "");
		j.put("tpl", "");

		return j.toString();
	}

	public static void orderTotal() {
		String date = TimeUtil.format(new Date(), TimeUtil.TARGET_3);
		for (User user : PetConstant.USERS) {
			int pageNo = 1, outCount = 0, inCount = 0;
			BigDecimal outPrice = BigDecimal.ZERO, inPrice = BigDecimal.ZERO;

			Boolean b = true;
			while (b) {
				try {
					// 入参封装
					String params = getOrderTotalParams(pageNo);

					// 接口调用
					JSONObject obj = HttpUtil.post(PetConstant.ORDER_LIST, params, user);

					if (obj != null && PetConstant.SUCCESS.equals(obj.getString("errorNo"))) {
						JSONArray array = obj.getJSONObject("data").getJSONArray("dataList");
						for (int i = 0; i < array.size(); i++) {
							JSONObject j = array.getJSONObject(i);
							String transDate = j.getString("transDate");
							if (!transDate.equals(date)) {
								b = false;
								break;
							}

							String status = j.getString("status");
							BigDecimal amount = j.getBigDecimal("amount");
							if (status.equals("1")) { // 卖出
								outCount++;
								outPrice = outPrice.add(amount);

							} else if (status.equals("2")) { // 买入
								inCount++;
								inPrice = inPrice.add(amount);
							}
						}
						pageNo++;
					} else {
						System.out.println(user.getName() + " 订单列表获取失败，返回：" + obj);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}

			System.out.println(String.format(user.getName() + " 买入：%s笔 -%s，卖出：%s笔 +%s", inCount, inPrice, outCount, outPrice));
		}

	}

	private static String getOrderTotalParams(int pageNo) {
		JSONObject j = new JSONObject();
		j.put("appId", "1");
		j.put("nounce", "");
		j.put("pageNo", pageNo);
		j.put("pageSize", 10);
		j.put("pageTotal", "-1");
		j.put("requestId", System.currentTimeMillis());
		j.put("timeStamp", "");
		j.put("token", "");
		j.put("tpl", "");

		return j.toString();
	}
}
