package com.fun.zpetchain;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.util.HttpUtil;

public class PetSale {

	public static void main(String[] args) {
		List<Pet> pets = PetCenter.getMyPetList();
		for (Pet pet : pets) {
			 salePet(pet); // 上架
//			cancleSalePet(pet); // 下架
		}
		System.out.println("............................上下架结束！");
	}

	public static void salePet(Pet pet) {
		String amount = getSalePetAmount(pet);
		if (StringUtils.isNotBlank(amount)) {
			String params = getSalePetParams(pet, amount);
			// 接口调用
			JSONObject result = HttpUtil.doJsonPost(PetConstant.SALE_PET, params, PetConstant.TIME_OUT, PetConstant.TIME_OUT);
			if (PetConstant.SUCCESS.equals(result.getString("errorNo"))) {
				System.out.println("上架成功");
			} else {
				System.out.println("上架失败：" + result);
			}
		}
	}

	public static void cancleSalePet(Pet pet) {
		if ("1".equals(pet.getShelfStatus())) {
			String params = getCancleSalePetParams(pet);
			// 接口调用
			JSONObject result = HttpUtil.doJsonPost(PetConstant.CANCEL_SALE_PET, params, PetConstant.TIME_OUT, PetConstant.TIME_OUT);
			if (result != null && PetConstant.SUCCESS.equals(result.getString("errorNo"))) {
				System.out.println("下架成功：" + pet);
				try {
					// 线程休息500毫秒
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String getCancleSalePetParams(Pet pet) {
		JSONObject j = new JSONObject();

		j.put("appId", "1");
		j.put("nounce", "");
		j.put("petId", pet.getPetId());
		j.put("requestId", System.currentTimeMillis());
		j.put("timeStamp", "");
		j.put("token", "");
		j.put("tpl", "");

		return j.toString();
	}

	private static String getSalePetParams(Pet pet, String amount) {
		JSONObject j = new JSONObject();

		j.put("amount", amount);
		j.put("appId", "1");
		j.put("nounce", "");
		j.put("petId", pet.getPetId());
		j.put("requestId", System.currentTimeMillis());
		j.put("timeStamp", "");
		j.put("token", "");
		j.put("tpl", "");

		return j.toString();
	}

	private static String getSalePetAmount(Pet pet) {
		if (StringUtils.isNotBlank(pet.getShelfStatus()) && "1".equals(pet.getShelfStatus())) {
			System.out.println("销售中...跳过出售！");
			return "";
		}

		if (pet.getRareNum() == 0) {
			System.out.println("0稀有...跳过出售！");
			return "";
		} else if (pet.getRareNum() > 4) {
			System.out.println("超级稀有." + pet.getRareNum() + "..跳过出售！");
			return "";
		}

		if (pet.getIsAngell()) {
			System.out.println("天使宠物...跳过出售！" + pet);
			return "";
		}

		String rareDegree = pet.getRareDegree();
		int rareNum = pet.getRareNum();
		if (rareDegree.equals("史诗") && rareNum <= 4) {
			String k = pet.getRareDegree() + "_" + pet.getGeneration() + "_" + pet.getCoolingInterval();
			Integer amount = PetConstant.SALE_AMOUNT.get(k);
			if (amount != null) {
				System.out.println(k + "  售价：" + amount);
				return amount.toString();
			}
		}

		return "";
	}
}
