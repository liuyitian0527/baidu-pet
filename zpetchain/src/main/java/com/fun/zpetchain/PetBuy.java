package com.fun.zpetchain;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.enums.PetEnum;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.util.FileUtil;
import com.fun.zpetchain.util.HttpUtil;
import com.fun.zpetchain.util.OcrUtil;
import com.fun.zpetchain.util.PropUtil;
import com.fun.zpetchain.util.TimeUtil;

public class PetBuy {

	private static final Logger logger = LoggerFactory.getLogger(PetBuy.class);

	/**
	 * key=rare degree, value=price limit when price <= price limit, try purchase
	 */
	private final static Map<String, Integer> LIMIT_MAP = new HashMap<String, Integer>();

	/**
	 * 筛选条件: 卓越 休息0分钟 状态 正常
	 */
	private final static String FILTER_COND_EXC = "{\"1\":\"2\",\"3\":\"0-1\",\"6\":\"1\"}";
	/**
	 * 筛选条件: 史诗 休息不限 状态 正常
	 */
	private final static String FILTER_COND_EPIC = "{\"1\":\"3\",\"6\":\"1\"}";
	/**
	 * 筛选条件: 神话 休息不限 状态 正常
	 */
	private final static String FILTER_COND_MYTH = "{\"1\":\"4\",\"6\":\"1\"}";

	/**
	 * when captcha is wrong, try more times
	 */
	private final static int RETRY_TIMES = 10;

	public static void main(String[] args) {

		PetBuy petChain = new PetBuy();
		try {
			petChain.initProp();
		} catch (Exception e) {
			logger.error("load properties fail, stop...");
		}

		while (true) {
			try {
				// petChain.queryMarket(SORT_TYPE_AMT,
				// FILTER_COND_MYTH);
				// Thread.sleep(200);

				petChain.queryMarket(PetConstant.SORT_TYPE_AMT, "{\"1\":\"3\",\"6\":\"1\"}");

				// petChain.queryMarket(SORT_TYPE_AMT,
				// FILTER_COND_EPIC);
				// petChain.queryMarket(SORT_TYPE_AMT,
				// FILTER_COND_MYTH);
				Thread.sleep(1000);
				// // Thread.sleep(200);
				// petChain.queryMarket(SORT_TYPE_TIME);
				//
			} catch (Exception e) {
				logger.warn("exception:" + e.getMessage());
			}
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
	private void queryMarket(String sortType, String filterCondition) {
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

		JSONObject jsonResult = HttpUtil.doJsonPost(PetConstant.QUERY_PETS_ON_SALE, data, 1000, 1000);

		if (jsonResult != null && "success".equals(jsonResult.get("errorMsg"))) {
			List<Pet> petArr = JSONArray.parseArray(jsonResult.getJSONObject("data").getJSONArray("petsOnSale").toString(), Pet.class);
			int retry = 0;
			// 找出命中售价的宠物
			Pet pet = choosePetFromPetArr(petArr, sortType);
			if (pet != null) {
				logger.info("尝试购买 售价:{}, 等级:{}, 休息:{}, petId:{}", pet.getAmount(), pet.getRareDegree(), pet.getCoolingInterval(), pet.getPetId());
				logger.info("try to putchase...");
				while (retry <= RETRY_TIMES) {
					retry++;
					if (purchase(pet)) {
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
	private Pet choosePetFromPetArr(List<Pet> petArr, String sortType) {

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
				System.out.println(String.format("%s: 售价:%s, 等级:%s, 休息:%s, petId:%s", sortType, petPrt.getAmount(), petPrt.getRareDegree(),
						petPrt.getCoolingInterval(), petPrt.getPetId()));
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
	 * try to purchase, if success or others have purchased, don't purchase again
	 * 
	 * @author 2bears
	 * @since
	 * @param pet
	 * @return true-can try again false-don't try again
	 */
	private boolean purchase(Pet pet) {
		Map<String, String> vCodeMap = getCaptcha();
		if (vCodeMap == null) {
			return false;
		}
		Map<String, Object> paraMap = new HashMap<String, Object>(16);
		paraMap.put("appId", 1);
		paraMap.put("tpl", "");
		paraMap.put("requestId", System.currentTimeMillis());
		paraMap.put("seed", vCodeMap.get("seed"));
		paraMap.put("captcha", vCodeMap.get("vCode"));
		paraMap.put("petId", pet.getPetId());
		paraMap.put("validCode", pet.getValidCode());
		paraMap.put("amount", pet.getAmount());
		String data = JSONObject.toJSONString(paraMap);

		try {
			JSONObject jsonResult = HttpUtil.doJsonPost(PetConstant.TXN_CREATE, data, 1000 * 3, 1000 * 3);

			if (jsonResult != null) {
				logger.info(jsonResult.toString());
			}

			if (jsonResult != null) {
				FileUtil.appendTxt(jsonResult.toString() + "\n", "E:\\购买记录.txt");
				String errorMsg = jsonResult.getString("errorMsg");
				String errorNo = jsonResult.getString("errorNo");
				if (errorNo.equals("00")) {
					String str = String.format("购买成功！交易时间：%s, petId:%s, 售价：%s, 等级:%s, 休息时间：%s ", TimeUtil.now(TimeUtil.TARGET_1), pet.getId(),
							pet.getAmount(), pet.getRareDegree(), pet.getCoolingInterval());
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
			System.out.println("purchase error:" + e.getMessage());
			return false;
		}
	}

	/**
	 * get the captcha from interface, and then identify the captcha by OCR
	 * 
	 * @author 2bears
	 * @since
	 * @return Map: captcha seed and captcha code
	 */
	private Map<String, String> getCaptcha() {

		Map<String, Object> paraMap = new HashMap<String, Object>(8);
		paraMap.put("appId", 1);
		paraMap.put("requestId", String.valueOf(System.currentTimeMillis()));
		paraMap.put("tpl", "");
		paraMap.put("nounce", null);
		paraMap.put("timeStamp", null);
		paraMap.put("token", null);

		JSONObject jsonResult = HttpUtil.doJsonPost(PetConstant.CAPTCHA_URL, JSONObject.toJSONString(paraMap).toString(), 1000, 1000);

		try {
			if (jsonResult == null) {
				return null;
			}
			String imgData = jsonResult.getJSONObject("data").get("img").toString();
			String seed = jsonResult.getJSONObject("data").get("seed").toString();
			InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(imgData));
			BufferedImage image = ImageIO.read(is);

			// String vCode = OcrUtil.ocrByTesseract(image);
			String vCode = OcrUtil.ocrByTess4j(image);
			System.out.println("验证码：" + vCode);
			if (StringUtils.isNotEmpty(vCode) && vCode.length() > 4) {
				vCode = vCode.substring(vCode.length() - 4);
			}
			if (StringUtils.isNotEmpty(vCode) && vCode.length() == 3) {
				vCode = "G" + vCode;
			}

			if (StringUtils.isNotEmpty(vCode) && vCode.length() == 4) {
				Map<String, String> vCodeMap = new HashMap<String, String>(4);
				vCodeMap.put("seed", seed);
				vCodeMap.put("vCode", vCode);
				return vCodeMap;
			} else {
				logger.info("ocr captcha error [{}]", vCode);
			}
			is.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
		}
		return null;
	}

}
