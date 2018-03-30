package com.fun.zpetchain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.util.HttpUtil;

public class PetCenter {

	/**
	 * 我的cw列表
	 */
	public static List<Pet> getMyPetList() {
		List<Pet> pets = new ArrayList<Pet>();
		int pageNo = 1, totalCount = 0;

		while (true) {
			try {
				// 入参封装
				String params = getPetListParams(pageNo);

				// 接口调用
				JSONObject obj = HttpUtil.doJsonPost(PetConstant.MY_PET_LIST, params, PetConstant.TIME_OUT, PetConstant.TIME_OUT);

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
					System.out.println("列表获取失败，返回：" + obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		System.out.println("一共查询到" + totalCount + "只宠物！");

		System.out.println("稀有属性数量查询...开始...");
		List<Pet> rePets = new ArrayList<Pet>();
		for (Pet pet : pets) {
			Pet info = getPetById(pet.getPetId());
			rePets.add(info);
		}
		System.out.println("稀有属性数量查询...结束...");

		return rePets;
	}

	/**
	 * 根据宠物id，获取单个宠物信息
	 * 
	 * @param petId
	 */
	public static Pet getPetById(String petId) {
		Pet pet = null;
		String params = getPetParams(petId);

		// 接口调用
		JSONObject result = HttpUtil.doJsonPost(PetConstant.GET_PET_BY_ID, params, PetConstant.TIME_OUT, PetConstant.TIME_OUT);

		if (result != null && PetConstant.SUCCESS.equals(result.getString("errorNo"))) {
			JSONObject pJson = result.getJSONObject("data");
			pet = JSONObject.parseObject(pJson.toString(), Pet.class);

			// 稀有属性数量赋值
			int rareNum = 0;
			JSONArray attrs = pJson.getJSONArray("attributes");
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
				}

				if ("稀有".equals(atr.getString("rareDegree"))) {
					rareNum++;
				}
			}

			pet.setRareNum(rareNum);
		}

		return pet;
	}

	public static void main(String[] args) {
		List<Pet> pets = getMyPetList();
		System.out.println(JSONArray.toJSONString(pets));
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

}
