package com.fun.zpetchain;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.util.HttpUtil;

public class PetSale {

	public static void main(String[] args) {
		saleTask(0, 10000);
	}

	public static void saleTask(long delay, long period) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {

				for (User user : PetConstant.USERS) {
					List<Pet> pets = PetCenter.getMyPetList(user);
					for (Pet pet : pets) {
						cancleSalePet(pet, user); // 下架
					}
					System.out.println(user.getName() + "............................下架结束！");
				}

				for (User user : PetConstant.USERS) {
					List<Pet> pets = PetCenter.getMyPetList(user);
					for (Pet pet : pets) {
						salePet(pet, user); // 上架
					}
					System.out.println(user.getName() + "............................上架结束！");
				}

			}
		};

		timer.scheduleAtFixedRate(task, 1000 * 60 * 3, 1000 * 60 * 10);
	}

	public static User getUser(String name) {
		for (User user : PetConstant.USERS) {
			if (user.getName().equalsIgnoreCase(name)) {
				return user;
			}
		}
		return null;
	}

	public static void salePet(Pet pet, User user) {
		try {
			String amount = getSalePetAmount(pet);
			if (StringUtils.isNotBlank(amount)) {
				String params = getSalePetParams(pet, amount);
				// 接口调用
				JSONObject result = HttpUtil.post(PetConstant.SALE_PET, params, user);
				if (PetConstant.SUCCESS.equals(result.getString("errorNo"))) {
					System.out.println("上架成功");
				} else {
					System.out.println("上架失败：" + result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void cancleSalePet(Pet pet, User user) {
		try {
			if ("1".equals(pet.getShelfStatus())) {
				String params = getCancleSalePetParams(pet);
				// 接口调用
				JSONObject result = HttpUtil.post(PetConstant.CANCEL_SALE_PET, params, user);
				if (result != null && PetConstant.SUCCESS.equals(result.getString("errorNo"))) {
					System.out.println("下架成功：" + pet);
					pet.setShelfStatus("0");
					try {
						// 线程休息500毫秒
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		}

		String k = pet.getRareDegree() + "_" + pet.getGeneration() + "_" + pet.getCoolingInterval() + "_" + pet.getRareNum() + "稀";
		Integer amount = PetConstant.SALE_AMOUNT.get(k);

		if (amount != null) {
			if (pet.getIsAngell()) {
				amount = amount + PetConstant.ANGEL_RAISE;
				System.out.println(k + "_" + "........天使宠物...售价" + amount);
				return amount.toString();
			}

			System.out.println(k + "  售价：" + amount);
			return amount.toString();
		}

		return "";
	}
}
