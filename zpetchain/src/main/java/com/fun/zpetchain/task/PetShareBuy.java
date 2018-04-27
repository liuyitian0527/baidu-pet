package com.fun.zpetchain.task;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.fun.zpetchain.PetBuy;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;

public class PetShareBuy {
	private static Logger logger = Logger.getLogger(PetShareBuy.class);
	private static final Lock lock = new ReentrantLock();

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
			while (trycount <= 30) {
				trycount++;
				if (PetBuy.tryBuy(pet, user, false)) {
					logger.info("专属分享购买成功：" + pet);
					break;
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
}
