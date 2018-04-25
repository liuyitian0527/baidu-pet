package com.fun.zpetchain.task;

import java.util.LinkedHashSet;
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

	private static final Lock lock = new ReentrantLock();
	private static final Lock lock2 = new ReentrantLock();
	private static LinkedHashSet<String> superPet = new LinkedHashSet<>(2000);

	/**
	 * 超级稀有购买，自动加价<br>
	 * 目前只识别史诗以上级别
	 * 
	 * @param pets
	 * @param user
	 */

	public static void tryBuySuperRare(final List<Pet> pets, final User user) {

		String uname = user.getName();
		if (uname.equals("liuyitian")) {
			try {
				if (lock.tryLock()) {
					buySuperRare(pets, user);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();   // 释放锁
			}
		} else {
			try {
				if (lock2.tryLock()) {
					buySuperRare(pets, user);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock2.unlock();   // 释放锁
			}
		}

	}

	public static void buySuperRare(final List<Pet> pets, final User user) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (Pet pet : pets) {
					if (superPet.size() >= 10000) {
						superPet.clear();
						System.out.println("超级稀有缓存清理...");
					}
					if (superPet.contains(pet.getId())) {
						continue;
					}
					superPet.add(pet.getId());

					Pet pInfo = PetCenter.getPetById(pet.getPetId(), user);
					if (pInfo == null) {
						continue;
					}

					// 大于4天，不考虑
					String coolingInterval = pInfo.getCoolingInterval();
					if (coolingInterval.indexOf("天") > -1 && Integer.parseInt(coolingInterval.charAt(0) + "") >= 4) {
						continue;
					}

					Integer superAmount = 0;

					// 超级稀有
					if (pInfo.getRareNum() > 4 && pInfo.getRareNum() % 2 == 1) {
						// 0代
						if (pInfo.getGeneration() == 0) {
							if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 100000;
							} else if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 20000;
							} else if (pInfo.getIsWhiteEyes()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 10000;
							} else if (pInfo.getIsAngell()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 7000;
							} else {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + PetConstant.SUPER_RARE_RAISE;
							}
						}
						// 1代
						else if (pInfo.getGeneration() == 1) {
							if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 50000;
							} else if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 20000;
							} else if (pInfo.getIsWhiteEyes()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 8000;
							} else if (pInfo.getIsAngell()) {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 5000;
							} else {
								superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + PetConstant.SUPER_RARE_RAISE;
							}
						} else {
							continue;
						}

						if (pet.getAmount() > superAmount) {
							continue;
						}

						FileUtil.appendTxt(TimeUtil.now(TimeUtil.TARGET_1) + " " + user.getName() + " 【超级稀有】尝试购买: " + pInfo + "\n",
								PathConstant.BUY_PATH);
						int trycount = 1;
						while (trycount <= 100) {
							trycount++;
							if (PetBuy.tryBuy(pet, user, false)) {
								FileUtil.appendTxt(TimeUtil.now(TimeUtil.TARGET_1) + " " + user.getName() + " 【超级稀有】购买成功: " + pInfo + "\n",
										PathConstant.BUY_PATH);
								// 线程休息3分钟，等待宠物上链
								break;
							} else {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
								}
							}
						}
					}

					// 天使、白眉
					else {
						if (pInfo.getRareNum() >= 4 && (pInfo.getIsAngell() || pInfo.getIsWhiteEyes() || pInfo.getIsYingTao())) {

							if (!coolingInterval.equals("0分钟")) {
								continue;
							}
							// 0代
							if (pInfo.getGeneration() == 0) {
								if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 50000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 8000;
								} else if (pInfo.getIsWhiteEyes()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 4000;
								} else if (pInfo.getIsAngell()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 1000;
								}
							}
							// 1代
							else if (pInfo.getGeneration() == 1) {
								if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 10000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 4000;
								} else if (pInfo.getIsWhiteEyes()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 2000;
								} else if (pInfo.getIsAngell()) {
									superAmount = PetBuy.LIMIT_MAP.get(pet.getRareDegree()) + 500;
								}
							} else {
								continue;
							}

							if (pInfo.getAmount() <= superAmount) {
								int trycount = 1;
								while (trycount <= 100) {
									trycount++;
									if (PetBuy.tryBuy(pet, user, false)) {
										FileUtil.appendTxt(TimeUtil.now(TimeUtil.TARGET_1) + " " + user.getName() + " 【天使|白眉】购买成功: " + pInfo + "\n",
												PathConstant.BUY_PATH);
										// 线程休息3分钟，等待宠物上链
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

				}

			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

}
