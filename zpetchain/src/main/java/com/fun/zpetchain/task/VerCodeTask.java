package com.fun.zpetchain.task;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.model.VerCode;
import com.fun.zpetchain.util.HttpUtil;
import com.fun.zpetchain.util.OcrUtil;

public class VerCodeTask {
	private static Logger logger = Logger.getLogger(VerCodeTask.class);

	public static Map<User, Queue<VerCode>> queueMap = new LinkedHashMap<User, Queue<VerCode>>();

	public static void init() {
		queueMap.clear();
		for (User user : PetConstant.USERS) {
			queueMap.put(user, new ArrayBlockingQueue<VerCode>(100));
		}
		doTask();
	}

	/**
	 * 根据用户获取验证码
	 * 
	 * @param user
	 * @return
	 */
	public static VerCode getVerCodeInfo(User user) {
		if (queueMap.get(user).isEmpty()) {
			storeVerCode(user);
		}
		return queueMap.get(user).poll();
	}

	/**
	 * 验证码自动清理、存储
	 */
	public static void doTask() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					for (User user : PetConstant.USERS) {
						genVerCodeByAcount(user);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		timer.scheduleAtFixedRate(task, 3000, 2000);
	}

	/**
	 * 验证码轮询存储，清理
	 * 
	 * @param user
	 */
	private static void genVerCodeByAcount(User user) {
		Queue<VerCode> queue = queueMap.get(user);
		if (System.currentTimeMillis() % 15 == 0) {
			System.out.println(user.getName() + " 验证码长度=  " + queue.size());
		}
		while (!queue.isEmpty()) {
			if (System.currentTimeMillis() - queue.peek().getCreateTime() > PetConstant.VALID_TIME) {
				queue.poll();
			} else {
				break;
			}
		}
		if (queue.size() < PetConstant.SAFE_QUEUE_SIZE) {
			storeVerCode(user);
		}
	}

	/**
	 * 验证码存储
	 * 
	 * @param user
	 */
	private static void storeVerCode(User user) {
		int i = 0;
		while (i <= 10) {
			i++;
			VerCode vCode = null;
			try {
				vCode = getVerCode(user);
				if (vCode != null) {
					queueMap.get(user).offer(vCode);
					if (queueMap.get(user).size() >= PetConstant.SAFE_QUEUE_SIZE) {
						break;
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

			} catch (Throwable e) {
				logger.error("请求验证码失败", e);
			}
		}
	}

	private static VerCode getVerCode(User user) {
		VerCode code = new VerCode();

		Map<String, Object> paraMap = new HashMap<String, Object>(8);
		paraMap.put("appId", 1);
		paraMap.put("requestId", String.valueOf(System.currentTimeMillis()));
		paraMap.put("tpl", "");
		paraMap.put("nounce", null);
		paraMap.put("timeStamp", null);
		paraMap.put("token", null);

		JSONObject jsonResult = HttpUtil.post(PetConstant.CAPTCHA_URL, JSONObject.toJSONString(paraMap).toString(), user);

		try {
			if (jsonResult == null || !jsonResult.getString("errorNo").equals(PetConstant.SUCCESS)) {
				System.out.println("验证码返回：" + jsonResult.getString("errorMsg"));
				return null;
			}
			String imgData = jsonResult.getJSONObject("data").get("img").toString();
			String seed = jsonResult.getJSONObject("data").get("seed").toString();
			InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(imgData));
			BufferedImage image = ImageIO.read(is);

			String vCode = OcrUtil.ocrByTess4j(image);
			if (StringUtils.isNotEmpty(vCode) && vCode.length() > 4) {
				vCode = vCode.substring(vCode.length() - 4);
			}
			if (StringUtils.isNotEmpty(vCode) && vCode.length() == 3) {
				vCode = "G" + vCode;
			}

			if (StringUtils.isNotEmpty(vCode) && vCode.length() == 4) {
				code.setSeed(seed);
				code.setvCode(vCode);
				code.setCreateTime(System.currentTimeMillis());

				return code;
			} else {
				logger.info("ocr captcha error [" + vCode + "]");
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("验证码解析失败：" + e.getMessage());
		} finally {
		}
		return null;
	}

	public static VerCode getVerCode(User user, Pet pet) {
		VerCode code = new VerCode();

		Map<String, Object> paraMap = new HashMap<String, Object>(8);
		paraMap.put("appId", 1);
		paraMap.put("requestId", String.valueOf(System.currentTimeMillis()));
		paraMap.put("tpl", "");
		paraMap.put("nounce", null);
		paraMap.put("timeStamp", null);
		paraMap.put("token", null);

		JSONObject jsonResult = HttpUtil.post(PetConstant.CAPTCHA_URL, JSONObject.toJSONString(paraMap).toString(), user, pet);

		try {
			if (jsonResult == null || !jsonResult.getString("errorNo").equals(PetConstant.SUCCESS)) {
				System.out.println("验证码返回：" + jsonResult.getString("errorMsg"));
				return null;
			}
			String imgData = jsonResult.getJSONObject("data").get("img").toString();
			String seed = jsonResult.getJSONObject("data").get("seed").toString();
			InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(imgData));
			BufferedImage image = ImageIO.read(is);

			String vCode = OcrUtil.ocrByTess4j(image);
			if (StringUtils.isNotEmpty(vCode) && vCode.length() > 4) {
				vCode = vCode.substring(vCode.length() - 4);
			}
			if (StringUtils.isNotEmpty(vCode) && vCode.length() == 3) {
				vCode = "G" + vCode;
			}

			if (StringUtils.isNotEmpty(vCode) && vCode.length() == 4) {
				code.setSeed(seed);
				code.setvCode(vCode);
				code.setCreateTime(System.currentTimeMillis());

				return code;
			} else {
				logger.info("ocr captcha error [" + vCode + "]");
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("验证码解析失败：" + e.getMessage());
		} finally {
		}
		return null;
	}

}
