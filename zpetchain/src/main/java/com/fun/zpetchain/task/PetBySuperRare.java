package com.fun.zpetchain.task;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fun.zpetchain.PetBuy;
import com.fun.zpetchain.PetCenter;
import com.fun.zpetchain.constant.PathConstant;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
import com.fun.zpetchain.util.FileUtil;

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

	private static final Lock lock = new ReentrantLock();

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
							Integer superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + PetConstant.SUPER_RARE_RAISE;
							if (pet.getAmount() > superAmount) {
								continue;
							}

							Pet pInfo = PetCenter.getPetById(pet.getPetId(), user);
							if (pInfo == null) {
								continue;
							}

							// 大于4天，不考虑
							String coolingInterval = pInfo.getCoolingInterval();
							if (coolingInterval.indexOf("天") > -1 && Integer.parseInt(coolingInterval.charAt(0) + "") >= 4) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree());
								if (pInfo.getAmount() > superAmount) {
									continue;
								}
							}

							// 超级稀有
							if (pInfo.getRareNum() > 4) {
								int trycount = 1;
								while (trycount <= 20) {
									trycount++;
									if (PetBuy.tryBuy(pet, user)) {
										FileUtil.appendTxt(user.getName() + " 【超级稀有】购买成功: " + pInfo, PathConstant.BUY_PATH);
										// 线程休息3分钟，等待宠物上链
										try {
											Thread.sleep(1000 * 60 * 3);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
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

							// 天使、白眉
							else {
								if (pInfo.getIsAngell() || pInfo.getIsWhiteEyes()) {

									if (coolingInterval.indexOf("天") > -1) {
										continue;
									}

									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 2000;
									if (pInfo.getAmount() <= superAmount) {
										int trycount = 1;
										while (trycount <= 20) {
											trycount++;
											if (PetBuy.tryBuy(pet, user)) {
												FileUtil.appendTxt(user.getName() + " 【天使|白眉】购买成功: " + pInfo, PathConstant.BUY_PATH);
												// 线程休息3分钟，等待宠物上链
												try {
													Thread.sleep(1000 * 60 * 3);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
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
