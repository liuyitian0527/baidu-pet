package com.fun.zpetchain.task;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fun.zpetchain.PetBuy;
import com.fun.zpetchain.constant.CodeConstant;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.util.TimeUtil;

/**
 * 
 * Title. 专属分享3分钟，购买任务<br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2018-4-29 上午10:51:03
 * <p>
 * Author: liuyt
 * <p>
 * Version: 1.0
 * <p>
 */
public class ShareBuyTask {
	private static final Lock lock = new ReentrantLock();

	/**
	 * 专属分享购买任务
	 */
	public static void initBuySharePet() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					if (lock.tryLock() && PetBuy.petShareCacheHashSet.size() > 0) {
						tryBuy();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();   // 释放锁
				}
			}
		};
		timer.scheduleAtFixedRate(task, 3000, 2000);
	}

	private static void tryBuy() {
		for (Pet pet : PetBuy.petShareCacheHashSet) {
			User user = PetConstant.USERS.get(1);
			if (System.currentTimeMillis() % 2 == 0) {
				user = PetConstant.USERS.get(0);
			}

			int trycount = 0;
			Boolean b = true;
			while (b && trycount <= PetConstant.TYR_COUNT) {
				trycount++;
				String errorNo = PetBuy.tryBuy(pet, user, true);
				if (PetBuy.buySuccess(errorNo)) {
					b = false;
				} else if (CodeConstant.ERROR_30010.equals(errorNo)) {
					b = false;
				}
			}
		}
	}
}
