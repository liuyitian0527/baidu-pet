package com.fun.zpetchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.CodeConstant;
import com.fun.zpetchain.constant.PathConstant;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.model.VerCode;
import com.fun.zpetchain.task.PetBuyTask;
import com.fun.zpetchain.task.ShareBuyTask;
import com.fun.zpetchain.task.SuperRareBuyTask;
import com.fun.zpetchain.task.VerCodeTask;
import com.fun.zpetchain.util.FileUtil;
import com.fun.zpetchain.util.HttpUtil;
import com.fun.zpetchain.util.TimeUtil;

/**
 * 
 * Title.宠物购买类 <br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2018-3-31 上午12:35:35
 * <p>
 * Author: liuyt
 * <p>
 * Version: 1.0
 * <p>
 */
public class PetBuy {
	private static final String FAIL = "-1";

	private static final Logger logger = LoggerFactory.getLogger(PetBuy.class);

	public static LinkedHashSet<String> petCache = new LinkedHashSet<String>(10);
	public static LinkedHashSet<Pet> petShareCacheHashSet = new LinkedHashSet<>(1000);

	/**
	 * main方法启动购买
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 配置文件加载
			PetConstant.initProp();
			// 验证码初始化
			// VerCodeTask.init();

			// 专属分享缓存
			ShareBuyTask.initBuySharePet();

			// 10分后自动上下架
			// PetSale.saleTask(1000 * 60 * 5, 1000 * 60 * 15);

			// 间隔刷新市场列表&购买命中
			PetBuyTask.buyTask();
		} catch (Exception e) {
			logger.error("init fail. " + e.getMessage());
		}

	}

	/**
	 * 根据排序规则查找市场
	 * 
	 * @param sortType
	 * @param filterCondition
	 * @param user
	 * @throws InterruptedException
	 */
	public static void queryPetsOnSale(String sortType, String filterCondition, User user) throws InterruptedException {
		Map<String, Object> paraMap = new HashMap<String, Object>(16);
		paraMap.put("appId", 1);
		paraMap.put("lastAmount", "");
		paraMap.put("lastRareDegree", "");
		paraMap.put("pageNo", 1);
		paraMap.put("pageSize", 10);
		paraMap.put("querySortType", sortType);
		paraMap.put("requestId", System.currentTimeMillis());
		paraMap.put("tpl", "");
		paraMap.put("petIds", new int[] {});
		paraMap.put("filterCondition", filterCondition);
		paraMap.put("nounce", null);
		paraMap.put("type", null);                  //
		paraMap.put("token", null);
		paraMap.put("timeStamp", null);

		String data = JSONObject.toJSONString(paraMap);

		JSONObject jsonResult = HttpUtil.post(PetConstant.QUERY_PETS_ON_SALE, data, user);

		if (jsonResult != null && CodeConstant.SUCCESS.equals(jsonResult.get("errorNo"))) {
			List<Pet> pets = JSONArray.parseArray(jsonResult.getJSONObject("data").getJSONArray("petsOnSale").toString(), Pet.class);

			// 超级稀有购买
			try {
				SuperRareBuyTask.tryBuySuperRare(pets, user);
			} catch (Exception e) {
			}

			// 找出命中售价的宠物
			List<Pet> hitPets = choosePetFromPetArr(pets, sortType, user);
			hitPetsToBuy(hitPets, user);
		}
	}

	/**
	 * 购买市场命中的宠物列表
	 * 
	 * @param pets
	 * @param user
	 * @throws InterruptedException
	 */
	public static void hitPetsToBuy(List<Pet> pets, User user) throws InterruptedException {
		for (Pet pet : pets) {
			if (!petCache.contains(pet.getPetId())) {
				if (isShare(pet)) {
					logger.info("专属分享3分钟，跳过购买:" + pet);
					continue;
				}

				logger.info(user.getName() + " 尝试购买 售价:{}, 等级:{}, 休息:{}, petId:{}", pet.getAmount(), pet.getRareDegree(), pet.getCoolingInterval(),
						pet.getPetId());

				int trycount = 0;
				Boolean b = true;
				while (b) {
					trycount++;
					if (trycount <= PetConstant.TYR_COUNT) {
						String errorNo = tryBuy(pet, user, true);
						if (buySuccess(errorNo)) {
							b = false;
							Thread.sleep(1000 * 60 * 2); // 线程休息2分钟，等待宠物上链
						} else if (CodeConstant.ERROR_30010.equals(errorNo)) {
							b = false;
						}

					} else {
						b = false;
						addCache(pet);
						logger.info("超出购买次数，忽略宠物：" + pet);
					}
				}
			}
		}

	}

	/**
	 * 找出符合购买条件的宠物
	 * 
	 * @author 2bears
	 * @since
	 * @param petArr
	 * @param sortType
	 * @return
	 */
	public static List<Pet> choosePetFromPetArr(List<Pet> petArr, String sortType, User user) {
		List<Pet> pets = new ArrayList<Pet>();

		// 每代里面价格最低的宠物
		Map<String, Pet> lowestPetMap = new HashMap<String, Pet>(16);
		for (Pet pet : petArr) {
			// 休息时间大于2天的，直接跳过
			String coolingInterval = pet.getCoolingInterval();
			if (pet.getRareDegree().equals("史诗") && !coolingInterval.equals("0分钟")) {
				continue;
			}

			// 只买0代
			if ((pet.getGeneration() == 0 && pet.getRareDegree().equals("史诗")) || pet.getRareDegree().equals("神话")) {
				Pet itemPet = lowestPetMap.get(pet.getRareDegree());
				if (itemPet == null) {
					lowestPetMap.put(pet.getRareDegree(), pet);
				} else {
					if (itemPet != null && pet.getAmount() < itemPet.getAmount()) {
						lowestPetMap.put(pet.getRareDegree(), pet);
					}
				}
			}
		}

		for (String degree : Pet.levelValueMap.keySet()) {
			Pet petPrt = lowestPetMap.get(degree);
			if (petPrt == null) {
				continue;
			}
			if (System.currentTimeMillis() % 5 == 0) {
				System.out.println(String.format(user.getName() + "  %s: 售价:%s, 等级:%s %s, 休息:%s, petId:%s", sortType, petPrt.getAmount(),
						petPrt.getRareDegree(), petPrt.getGeneration() + "代", petPrt.getCoolingInterval(), petPrt.getPetId()));
			}

			if (new BigDecimal(petPrt.getAmount()).compareTo(BigDecimal.ZERO) <= 0 || petPrt.getAmount() <= 0) {
				addCache(petPrt);
			} else {
				if (petPrt.getAmount() <= (PetConstant.LIMIT_MAP.get(petPrt.getRareDegree()))) {
					if (!petCache.contains(petPrt.getPetId())) {
						pets.add(petPrt);
					}
				}
			}
		}

		return pets;
	}

	/**
	 * 尝试购买
	 * 
	 * @param pet
	 * @param user
	 * @return
	 */
	public static String tryBuy(Pet pet, User user, Boolean isFileLog) {

		if (new BigDecimal(pet.getAmount()).compareTo(BigDecimal.ZERO) <= 0 || pet.getAmount() <= 0) {
			return FAIL;
		}

		// VerCode verCode = VerCodeTask.getVerCodeInfo(user);
		VerCode verCode = VerCodeTask.getVerCode(user, pet);
		if (verCode == null || petCache.contains(pet.getPetId())) {
			logger.error("验证码获取失败！");
			return FAIL;
		}

		Map<String, Object> paraMap = new HashMap<String, Object>(16);
		paraMap.put("appId", 1);
		paraMap.put("tpl", "");
		paraMap.put("nounce", "");
		paraMap.put("timeStamp", "");
		paraMap.put("token", "");
		paraMap.put("requestId", System.currentTimeMillis());
		paraMap.put("seed", verCode.getSeed());
		paraMap.put("captcha", verCode.getvCode());
		paraMap.put("petId", pet.getPetId());
		paraMap.put("validCode", pet.getValidCode());
		// paraMap.put("validCode", "");
		paraMap.put("amount", pet.getAmount());
		String data = JSONObject.toJSONString(paraMap);

		try {
			logger.info("开始购买：{}", pet);
			JSONObject jsonResult = HttpUtil.post(PetConstant.TXN_CREATE, data, user, pet);

			if (jsonResult != null) {
				logger.info(jsonResult.toString());
			}

			if (jsonResult != null) {
				String errorNo = jsonResult.getString("errorNo");
				if (errorNo.equals(CodeConstant.SUCCESS)) {
					addCache(pet);
					petShareCacheHashSet.remove(pet);
					if (isFileLog) {
						logSuccessBuy(pet, user);
					}
				} else if (errorNo.equals(CodeConstant.ERROR_10002)) { // 抢先下单
					addCache(pet);
					petShareCacheHashSet.remove(pet);
				} else if (errorNo.equals(CodeConstant.ERROR_30010)) { // 专属分享
					if (!petShareCacheHashSet.contains(pet)) {
						petShareCacheHashSet.add(pet);
					}
				}

				return errorNo;
			}
		} catch (Exception e) {
			System.out.println(user.getName() + " 购买 error:" + e.getMessage());
		} finally {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return FAIL;
	}

	public static void isAppendOpen(User user) {
		Map<String, Object> paraMap = new HashMap<String, Object>(16);
		paraMap.put("appId", 1);
		paraMap.put("tpl", "");
		paraMap.put("nounce", "");
		paraMap.put("timeStamp", "");
		paraMap.put("token", "");
		paraMap.put("requestId", System.currentTimeMillis());
		String data = JSONObject.toJSONString(paraMap);

		JSONObject jsonResult = HttpUtil.post(PetConstant.IS_APPEND_OPEN, data, user);

		if (jsonResult != null) {
//			logger.info("isAppendOpen返回=" + jsonResult.toString());
		}

	}

	/**
	 * 忽略的市场宠物
	 * 
	 * @param pet
	 */
	private static void addCache(Pet pet) {
		if (petCache.size() >= 100) {
			petCache.clear();
			petCache.add(pet.getPetId());
		}
	}

	/**
	 * 判断是否专属分享缓存
	 * 
	 * @param pet
	 * @return
	 */
	public static Boolean isShare(Pet pet) {
		if (petShareCacheHashSet.contains(pet)) {
			return true;
		}
		return false;
	}

	/**
	 * 根据返回code判断是否购买成功
	 * 
	 * @param errorNo
	 * @return
	 */
	public static Boolean buySuccess(String errorNo) {
		if (CodeConstant.SUCCESS.equals(errorNo)) {
			return true;
		}
		return false;
	}

	public static void logSuccessBuy(Pet pet, User user) {
		String str = String.format(TimeUtil.now(TimeUtil.TARGET_1) + " " + user.getName() + " 购买成功！ petId:%s, 售价：%s, 等级:%s %s, 休息时间：%s ",
				pet.getId(), pet.getAmount(), pet.getRareDegree(), pet.getGeneration() + "代", pet.getCoolingInterval());
		FileUtil.appendTxt(str + "\n", PathConstant.BUY_PATH);
	}

	public static void log(String s) {
		FileUtil.appendTxt(s + "\n", PathConstant.BUY_PATH);
	}

}
