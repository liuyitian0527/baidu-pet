package com.fun.zpetchain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.enums.PetEnum;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.model.VerCode;
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

	private final static Map<String, Integer> LIMIT_MAP = new HashMap<String, Integer>();

	/**
	 * main方法启动购买
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final PetBuy petChain = new PetBuy();
		try {
			petChain.initProp();
			VerCodeTask.init();
			PetSale.saleTask();
		} catch (Exception e) {
			logger.error("load properties fail, stop...");
		}

		for (final User user : PetConstant.USERS) {
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							petChain.queryPetsOnSale(PetConstant.SORT_TYPE_AMT, PetConstant.FILTER_COND_EPIC, user);
							Thread.sleep(1000);
						} catch (Exception e) {
							logger.warn("exception:" + e.getMessage());
						}
					}
				}
			}).start();
		}
	}

	private void initProp() throws Exception {
		LIMIT_MAP.put(PetEnum.LEVEL_COMMON.getDesc(), Integer.parseInt(PropUtil.getProp("price_common")));
		LIMIT_MAP.put(PetEnum.LEVEL_RARE.getDesc(), Integer.parseInt(PropUtil.getProp("price_rare")));
		LIMIT_MAP.put(PetEnum.LEVEL_EXCELLENCE.getDesc(), Integer.parseInt(PropUtil.getProp("price_excellence")));
		LIMIT_MAP.put(PetEnum.LEVEL_EPIC.getDesc(), Integer.parseInt(PropUtil.getProp("price_epic")));
		LIMIT_MAP.put(PetEnum.LEVEL_MYTH.getDesc(), Integer.parseInt(PropUtil.getProp("price_myth")));
		LIMIT_MAP.put(PetEnum.LEVEL_LEGEND.getDesc(), Integer.parseInt(PropUtil.getProp("price_legend")));

	}

	/**
	 * query the market, if buy conditions meet, try to purchase
	 * 
	 * @author 2bears
	 * @since
	 * @param sortType
	 */
	private void queryPetsOnSale(String sortType, String filterCondition, User user) {
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

			// 找出命中售价的宠物
			Pet pet = choosePetFromPetArr(petArr, sortType, user);
			if (pet != null) {
				int trycount = 1;
				logger.info(user.getName() + " 尝试购买 售价:{}, 等级:{}, 休息:{}, petId:{}", pet.getAmount(), pet.getRareDegree(), pet.getCoolingInterval(),
						pet.getPetId());
				while (trycount <= PetConstant.TYR_COUNT) {
					trycount++;
					if (tryBuy(pet, user)) {
						break;
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
						continue;
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
	private Pet choosePetFromPetArr(List<Pet> petArr, String sortType, User user) {

		// 每代里面价格最低的宠物
		Map<String, Pet> lowestPetMap = new HashMap<String, Pet>(16);
		for (Pet pet : petArr) {
			// 休息时间大于4天的，直接跳过
			String coolingInterval = pet.getCoolingInterval();
			if (coolingInterval.indexOf("天") > -1 && Integer.parseInt(coolingInterval.charAt(0) + "") >= 4) {
				continue;
			}

			// 不考虑代数
			if (pet.getGeneration() >= 0) {
				Pet itemPet = lowestPetMap.get(pet.getRareDegree());
				if (itemPet == null) {
					lowestPetMap.put(pet.getRareDegree(), pet);
				} else {
					if (pet.getAmount() < itemPet.getAmount()) {
						lowestPetMap.put(pet.getRareDegree(), itemPet);
					}
				}
			}
		}

		for (String degree : Pet.levelValueMap.keySet()) {
			Pet petPrt = lowestPetMap.get(degree);
			if (petPrt != null) {
				System.out.println(String.format(user.getName() + "  %s: 售价:%s, 等级:%s %s, 休息:%s, petId:%s", sortType, petPrt.getAmount(),
						petPrt.getRareDegree(), petPrt.getGeneration() + "代", petPrt.getCoolingInterval(), petPrt.getPetId()));
			}
		}

		Pet pet = null;
		for (String degree : Pet.levelValueMap.keySet()) {
			pet = lowestPetMap.get(degree);
			if (pet != null && pet.getAmount() <= LIMIT_MAP.get(pet.getRareDegree())) {
				return pet;
			}
		}

		return null;
	}

	/**
	 * 尝试购买
	 * 
	 * @param pet
	 * @param user
	 * @return
	 */
	private boolean tryBuy(Pet pet, User user) {
		VerCode verCode = VerCodeTask.getVerCodeInfo(user);
		if (verCode == null) {
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
				FileUtil.appendTxt(jsonResult.toString() + "\n", "E:\\购买记录.txt");
				String errorMsg = jsonResult.getString("errorMsg");
				String errorNo = jsonResult.getString("errorNo");
				if (errorNo.equals("00")) {
					String str = String.format(user.getName() + " 购买成功！交易时间：%s, petId:%s, 售价：%s, 等级:%s %s, 休息时间：%s ",
							TimeUtil.now(TimeUtil.TARGET_1), pet.getId(), pet.getAmount(), pet.getRareDegree(), pet.getGeneration() + "代",
							pet.getCoolingInterval());
					FileUtil.appendTxt(str + "\n", "E:\\购买记录.txt");
					System.out.println(str);
					Thread.sleep(1000 * 60 * 3);
					return true;
				} else if ("有人抢先下单啦".equals(errorMsg)) {
					return true;
				} else {
					return false;
				}

			} else {
				return false;
			}
		} catch (Exception e) {
			System.out.println(user.getName() + " purchase error:" + e.getMessage());
			return false;
		}
	}

	// private Map<String, String> getCaptcha() {
	//
	// Map<String, Object> paraMap = new HashMap<String, Object>(8);
	// paraMap.put("appId", 1);
	// paraMap.put("requestId", String.valueOf(System.currentTimeMillis()));
	// paraMap.put("tpl", "");
	// paraMap.put("nounce", null);
	// paraMap.put("timeStamp", null);
	// paraMap.put("token", null);
	//
	// JSONObject jsonResult = HttpUtil.doJsonPost(PetConstant.CAPTCHA_URL, JSONObject.toJSONString(paraMap).toString(), 1000, 1000);
	//
	// try {
	// if (jsonResult == null) {
	// return null;
	// }
	// String imgData = jsonResult.getJSONObject("data").get("img").toString();
	// String seed = jsonResult.getJSONObject("data").get("seed").toString();
	// InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(imgData));
	// BufferedImage image = ImageIO.read(is);
	//
	// // String vCode = OcrUtil.ocrByTesseract(image);
	// String vCode = OcrUtil.ocrByTess4j(image);
	// System.out.println("验证码：" + vCode);
	// if (StringUtils.isNotEmpty(vCode) && vCode.length() > 4) {
	// vCode = vCode.substring(vCode.length() - 4);
	// }
	// if (StringUtils.isNotEmpty(vCode) && vCode.length() == 3) {
	// vCode = "G" + vCode;
	// }
	//
	// if (StringUtils.isNotEmpty(vCode) && vCode.length() == 4) {
	// Map<String, String> vCodeMap = new HashMap<String, String>(4);
	// vCodeMap.put("seed", seed);
	// vCodeMap.put("vCode", vCode);
	// return vCodeMap;
	// } else {
	// logger.info("ocr captcha error [{}]", vCode);
	// }
	// is.close();
	// } catch (Exception e) {
	// logger.error(e.getMessage());
	// } finally {
	// }
	// return null;
	// }

}
