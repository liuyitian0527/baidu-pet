package com.fun.zpetchain.task;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.model.VerCode;
import com.fun.zpetchain.util.HttpUtil;
import com.fun.zpetchain.util.OcrUtil;

public class VerCodeTask {
	private static Logger logger = Logger.getLogger(VerCodeTask.class);

	public static void main(String[] args) {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				System.out.println("-------设定要指定任务--------");
			}
		}, 1000, 2000);
	}

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

	public static void doTask() {
		// 单位: 毫秒
		final long timeInterval = 2000;
		Runnable runnable = new Runnable() {
			public void run() {
				while (true) {

					clearInvalidVerCode(PetConstant.USERS);
					// 随机获取User
					User user = PetConstant.USERS.get((int) (System.currentTimeMillis() % PetConstant.USERS.size()));
					genVerCodeByAcount(user);

					try {
						Thread.sleep(timeInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	private static void clearInvalidVerCode(List<User> users) {
		for (User user : users) {
			Queue<VerCode> queue = queueMap.get(user);
			while (!queue.isEmpty()) {
				if (System.currentTimeMillis() - queue.peek().getCreateTime() > PetConstant.VALID_TIME) {
					logger.info(user.getName() + " 验证码过期清理成功");
					queue.poll();
				} else {
					break;
				}
			}
		}
	}

	private static void genVerCodeByAcount(User user) {
		Queue<VerCode> queue = queueMap.get(user);
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
		VerCode vCode = null;
		try {
			vCode = getVerCode(user);
			if (vCode != null) {
				queueMap.get(user).offer(vCode);
				logger.info("储备验证码成功，user:{" + user.getName() + "} code:{" + vCode.getvCode() + "}");
			}
		} catch (Throwable e) {
			logger.error("请求验证码失败", e);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
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
			if (jsonResult == null) {
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
			logger.error(e.getMessage());
		} finally {
		}
		return null;
	}

}
