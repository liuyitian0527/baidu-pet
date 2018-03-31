package com.fun.zpetchain.task;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fun.zpetchain.PetBuy;
import com.fun.zpetchain.PetCenter;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.util.FileUtil;
import com.fun.zpetchain.util.PropUtil;
import com.fun.zpetchain.util.TimeUtil;

/**
 * Title. 超级稀有抓取<br>
 * Description.例如：史诗5稀，神话7稀
 * <p>
 * Copyright: Copyright (c) 2018-3-31 下午7:53:01
 * <p>
 * Author: liuyt
 * <p>
 * Version: 1.0
 * <p>
 */
public class PetBySuperRare {

	private static Lock lock = new ReentrantLock();

	/**
	 * 超级稀有购买，自动加价<br>
	 * 目前只识别史诗以上级别
	 * 
	 * @param pets
	 * @param user
	 */
	public static void tryBuySuperRare(final List<Pet> pets, final User user) {

		try {
			if (lock.tryLock()) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						for (Pet pet : pets) {
							Pet pInfo = PetCenter.getPetById(pet.getPetId(), user);
							if (pInfo == null) {
								continue;
							}

							if (pInfo.getRareNum() > 4 && pInfo.getRareNum() % 2 == 1) {
								Integer superAmount = PetBuy.LIMIT_MAP.get(pInfo.getRareDegree()) + PetConstant.SUPER_RARE_RAISE;

								if (pet != null && pet.getAmount() <= superAmount) {
									System.out.println("尝试购买超级稀有: " + pInfo);
									Boolean b = PetBuy.tryBuy(pInfo, user);

									if (b) {
										String str = String.format(user.getName() + " 超级稀有购买成功！交易时间：%s, petId:%s, 售价：%s, 等级:%s %s %s, 休息时间：%s ",
												TimeUtil.now(TimeUtil.TARGET_1), pet.getId(), pet.getAmount(), pet.getRareDegree(),
												pet.getGeneration() + "代", pInfo.getRareNum() + "稀", pet.getCoolingInterval());

										try {
											FileUtil.appendTxt(str + "\n", PropUtil.getProp("success_buy_path"));
										} catch (Exception e) {
											e.printStackTrace();
										}

									}
								}
							}
						}
					}
				};

				Thread thread = new Thread(runnable);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();   // 释放锁
		}

	}
}
