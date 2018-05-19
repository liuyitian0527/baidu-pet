package com.fun.zpetchain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.util.HttpUtil;

public class PetSale {

	private static Logger logger = Logger.getLogger(PetSale.class);

	public static void main(String[] args) {
		// saleTask(0, 1000000000);
		User user = PetConstant.USERS.get(1);
		Pet p = new Pet();
		p.setPetId("1896036299490020746");
		p.setShelfStatus("0");
		p.setAmount(200000d);
		salePet(p, user);
	}

	public static void saleTask(long delay, long period) {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Long s = System.currentTimeMillis();

				for (User user : PetConstant.USERS) {
					List<Pet> pets = PetCenter.getMyPetList(user, true);
					for (Pet pet : pets) {
						if (pet.getRareDegree().equals("神话")) {
							// continue;
						}
						cancleSalePet(pet, user); // 下架

						// try {
						// // 线程休息500毫秒
						// Thread.sleep(500);
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }

						salePet(pet, user); // 上架
					}
					System.out.println(user.getName() + "............................上下架结束！");
				}

				Long e = System.currentTimeMillis();

				int ms = ((int) (e - s)) / 1000;

				logger.info("上下架结束！耗时：" + ms / 60 + "分 " + ms % 60 + "秒");

				// for (User user : PetConstant.USERS) {
				// List<Pet> pets = PetCenter.getMyPetList(user,
				// true);
				// for (Pet pet : pets) {
				// salePet(pet, user); // 上架
				// }
				// System.out.println(user.getName() +
				// "............................上架结束！");
				// }

			}
		};

		timer.scheduleAtFixedRate(task, delay, period);
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
			String amount = pet.getAmount() == null ? null : pet.getAmount().toString();
			if (StringUtils.isNotBlank(amount) && pet.getAmount() > 0 && new BigDecimal(amount).compareTo(BigDecimal.ZERO) > 0
					&& "0".equals(pet.getShelfStatus())) {
				String params = getSalePetParams(pet, amount);
				// 接口调用
				JSONObject result = HttpUtil.post(PetConstant.SALE_PET, params, user);
				if (PetConstant.SUCCESS.equals(result.getString("errorNo"))) {
					System.out.println("预上架成功: " + pet.getAmount());
				} else {
					System.out.println("预上架失败：" + result);
				}
				// 密码确认
				// String params2 = getSalePetConfirmParams(pet, amount);
				// JSONObject result2 = HttpUtil.post(PetConstant.SALE_PET_CONFIRM, params2, user);
				// if (PetConstant.SUCCESS.equals(result2.getString("errorNo"))) {
				// System.out.println("上架成功: " + pet.getAmount());
				// } else {
				// System.out.println("上架失败：" + result);
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String getSalePetConfirmParams(Pet pet, String amount) {
		JSONObject j = new JSONObject();

		j.put("appId", "1");
		j.put("confirmType", "1");
		j.put("nounce", "");
		j.put("s",
				"b21wMIveo0zHHmDkj20LLYb0NvulYugVs8HPOsKCC+DIpLd2LfkXrb3TF+2H0MIII1iq4kcY1faHtFZDSCTGvG09qTh70pcr01p6EG8nqmzFuXIedwR+kpQCk7+UM3Rrt/e+0f5ePu8jIDwik7bkEDLeCYIXPC+ulDYIB5ZCZCwr2z8KraHwT2BI7JkTY0GzKuvLWY5O0JkUQKd79+pKVIV4tdYCS8qE31wSIEHE67ft7vr3Jdq+BT0oXAbE3ZOkajASfzHrtM4NTELpW6tfFrgvTiLJe0Tx8wo6Y/VTtejWsh8zHLC1gnNouIx0JhCA/Ga7+GfetgSs56twO5TMuw==");
		j.put("requestId", System.currentTimeMillis());
		j.put("timeStamp", "");
		j.put("token", "");
		j.put("tpl", "");

		return j.toString();
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

	public static String getSalePetAmount(Pet pet) {
		if ("1973672678091063174".equals(pet.getPetId())) {
			return "1900000";
		}
		if ("2000517013966255773".equals(pet.getPetId())) {
			return "1500000";
		}
		if ("2115201024550210286".equals(pet.getPetId())) {
			return "880000";
		}

		String k = pet.getRareDegree() + "_" + pet.getGeneration() + "_" + pet.getCoolingInterval() + "_" + pet.getRareNum() + "稀";
		Integer amount = PetConstant.SALE_AMOUNT.get(k);

		if (amount != null) {

			// 天使 + 白眉 + 樱桃
			if (pet.getIsAngell() && pet.getIsWhiteEyes() && pet.getIsYingTao()) {
				if (pet.getRareNum() > 4 && pet.getRareNum() % 2 == 1) {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 100;
				} else {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 50;
				}
			}
			// 天使 + 白眉
			else if (pet.getIsAngell() && pet.getIsWhiteEyes()) {
				if (pet.getRareNum() > 4 && pet.getRareNum() % 2 == 1) {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 10;
				} else {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 4;
				}
			}
			// 天使+ 樱桃
			else if (pet.getIsAngell() && pet.getIsYingTao()) {
				if (pet.getRareNum() > 4 && pet.getRareNum() % 2 == 1) {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 10;
				} else {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 3;
				}
			}

			// 白眉 + 樱桃
			else if (pet.getIsWhiteEyes() && pet.getIsYingTao()) {
				if (pet.getRareNum() > 4 && pet.getRareNum() % 2 == 1) {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 10;
				} else {
					amount = amount + (PetConstant.ANGEL_RAISE + PetConstant.WHITE_EYES) * 3;
				}
			}

			if (pet.getIsAngell()) {
				if (pet.getRareNum() > 4 && pet.getRareNum() % 2 == 1) {
					amount = amount + PetConstant.ANGEL_RAISE / (pet.getGeneration() + 1) * (pet.getGeneration() + 4);
				} else {
					amount = amount + PetConstant.ANGEL_RAISE - pet.getGeneration() * 500;
				}
				System.out.println(k + "_" + "........天使宠物...售价" + amount);
			}

			if (pet.getIsWhiteEyes()) {
				if (pet.getRareNum() > 4 && pet.getRareNum() % 2 == 1) {
					amount = amount + PetConstant.WHITE_EYES / (pet.getGeneration() + 1) * (pet.getGeneration() + 4);
				} else {
					amount = amount + PetConstant.WHITE_EYES - pet.getGeneration() * 500;
				}
				System.out.println(k + "_" + "........白眉斗眼宠物...售价" + amount);
			}

			if (pet.getIsYingTao()) {
				if (pet.getRareNum() > 4 && pet.getRareNum() % 2 == 1) {
					amount = amount + PetConstant.WHITE_EYES / (pet.getGeneration() + 1) * (pet.getGeneration() + 4);
				} else {
					amount = amount + PetConstant.WHITE_EYES / 3 - pet.getGeneration() * 500;
				}
				System.out.println(k + "_" + "........樱桃宠物...售价" + amount);
			}

			System.out.println(k + "  售价：" + amount);
			if (amount > 0) {
				return amount.toString();
			}
		}

		return "";
	}
}
