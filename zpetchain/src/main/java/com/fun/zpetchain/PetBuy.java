package com.fun.zpetchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PathConstant;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.enums.PetEnum;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.model.VerCode;
import com.fun.zpetchain.task.PetBySuperRare;
import com.fun.zpetchain.task.VerCodeTask;
import com.fun.zpetchain.util.FileUtil;
import com.fun.zpetchain.util.HttpUtil;
import com.fun.zpetchain.util.PropUtil;
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

	private static final Logger logger = LoggerFactory.getLogger(PetBuy.class);

	public final static Map<String, Integer> LIMIT_MAP = new HashMap<String, Integer>();
	public static LinkedHashSet<String> petCache = new LinkedHashSet<String>(10);

	/**
	 * main方法启动购买
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// 配置文件加载
			PetBuy.initProp();
			// 验证码初始化
			VerCodeTask.init();
			// 10分后自动上下架
			PetSale.saleTask(1000 * 60 * 5, 1000 * 60 * 15);
		} catch (Exception e) {
			logger.error("init fail. " + e.getMessage());
		}

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					for (final User user : PetConstant.USERS) {
						if (user.getName().equalsIgnoreCase("liuyitian")) {
							// PetBuy.queryPetsOnSale(PetConstant.SORT_TYPE_AMT,
							// PetConstant.FILTER_COND_EPIC, user);
							if (System.currentTimeMillis() % 2 == 0) {
								PetBuy.queryPetsOnSale(PetConstant.SORT_TYPE_TIME, PetConstant.FILTER_COND_EPIC, user);
							} else {
								PetBuy.queryPetsOnSale(PetConstant.SORT_TYPE_AMT, PetConstant.FILTER_COND_MYTH, user);
							}
						} else {
							PetBuy.queryPetsOnSale(PetConstant.SORT_TYPE_TIME, PetConstant.FILTER_COND_EPIC, user);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		};
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.scheduleAtFixedRate(task, 1000, 350);
	}

	private static void initProp() throws Exception {
		LIMIT_MAP.put(PetEnum.LEVEL_COMMON.getDesc(), Integer.parseInt(PropUtil.getProp("price_common")));
		LIMIT_MAP.put(PetEnum.LEVEL_RARE.getDesc(), Integer.parseInt(PropUtil.getProp("price_rare")));
		LIMIT_MAP.put(PetEnum.LEVEL_EXCELLENCE.getDesc(), Integer.parseInt(PropUtil.getProp("price_excellence")));
		LIMIT_MAP.put(PetEnum.LEVEL_EPIC.getDesc(), Integer.parseInt(PropUtil.getProp("price_epic")));
		LIMIT_MAP.put(PetEnum.LEVEL_MYTH.getDesc(), Integer.parseInt(PropUtil.getProp("price_myth")));
		LIMIT_MAP.put(PetEnum.LEVEL_LEGEND.getDesc(), Integer.parseInt(PropUtil.getProp("price_legend")));

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

		if (jsonResult != null && "success".equals(jsonResult.get("errorMsg"))) {
			List<Pet> petArr = JSONArray.parseArray(jsonResult.getJSONObject("data").getJSONArray("petsOnSale").toString(), Pet.class);

			// 超级稀有购买
			try {
				PetBySuperRare.tryBuySuperRare(petArr, user);
			} catch (Exception e) {
			}

			// 找出命中售价的宠物
			List<Pet> pets = choosePetFromPetArr(petArr, sortType, user);
			toBuy(pets, user);
		}
	}

	public static void toBuy(List<Pet> pets, User user) throws InterruptedException {
		for (Pet pet : pets) {
			if (pet != null && !petCache.contains(pet.getPetId())) {
				int trycount = 1;
				logger.info(user.getName() + " 尝试购买 售价:{}, 等级:{}, 休息:{}, petId:{}", pet.getAmount(), pet.getRareDegree(), pet.getCoolingInterval(),
						pet.getPetId());
				while (true) {
					if (trycount <= PetConstant.TYR_COUNT) {
						trycount++;
						if (tryBuy(pet, user, true)) {
							// 线程休息3分钟，等待宠物上链
							Thread.sleep(1000 * 60 * 3);
							break;
						} else {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
							continue;
						}
					} else {
						addCache(pet);
						logger.info("忽略宠物：" + pet);
						break;
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
			if (petPrt != null && System.currentTimeMillis() % 5 == 0) {
				System.out.println(String.format(user.getName() + "  %s: 售价:%s, 等级:%s %s, 休息:%s, petId:%s", sortType, petPrt.getAmount(),
						petPrt.getRareDegree(), petPrt.getGeneration() + "代", petPrt.getCoolingInterval(), petPrt.getPetId()));
			}

			if (petPrt != null && petPrt.getAmount() <= (LIMIT_MAP.get(petPrt.getRareDegree()))) {
				if (new BigDecimal(petPrt.getAmount()).compareTo(BigDecimal.ZERO) > 0 && petPrt.getAmount() > 0) {
					pets.add(petPrt);
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
	public static boolean tryBuy(Pet pet, User user, Boolean isFileLog) {

		if (new BigDecimal(pet.getAmount()).compareTo(BigDecimal.ZERO) <= 0 || pet.getAmount() <= 0) {
			return false;
		}

		VerCode verCode = VerCodeTask.getVerCodeInfo(user);
		if (verCode == null || petCache.contains(pet.getPetId())) {
			return false;
		}
		Map<String, Object> paraMap = new HashMap<String, Object>(16);
		paraMap.put("appId", 1);
		paraMap.put("tpl", "");
		paraMap.put("requestId", System.currentTimeMillis());
		paraMap.put("seed", verCode.getSeed());
		paraMap.put("captcha", verCode.getvCode());
		paraMap.put("petId", pet.getPetId());
		paraMap.put("validCode", pet.getValidCode());
		paraMap.put("amount", pet.getAmount());
		String data = JSONObject.toJSONString(paraMap);

		try {
			JSONObject jsonResult = HttpUtil.post(PetConstant.TXN_CREATE, data, user);

			if (jsonResult != null) {
				logger.info(jsonResult.toString());
			}

			if (jsonResult != null) {
				String errorMsg = jsonResult.getString("errorMsg");
				String errorNo = jsonResult.getString("errorNo");
				if (errorNo.equals("00")) {
					String str = String.format(TimeUtil.now(TimeUtil.TARGET_1) + " " + user.getName() + " 购买成功！ petId:%s, 售价：%s, 等级:%s %s, 休息时间：%s ",
							pet.getId(), pet.getAmount(), pet.getRareDegree(), pet.getGeneration() + "代", pet.getCoolingInterval());

					addCache(pet);
					if (isFileLog) {
						FileUtil.appendTxt(str + "\n", PathConstant.BUY_PATH);
					}
					return true;
				} else if ("有人抢先下单啦".equals(errorMsg)) {
					addCache(pet);
				}
			}
		} catch (Exception e) {
			System.out.println(user.getName() + " 购买 error:" + e.getMessage());
		}

		return false;
	}

	private static void addCache(Pet pet) {
		if (petCache.size() >= 10) {
			petCache.clear();
			petCache.add(pet.getPetId());
		}
	}

}
