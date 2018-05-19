package com.fun.zpetchain.task;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fun.zpetchain.PetBuy;
import com.fun.zpetchain.PetCenter;
import com.fun.zpetchain.constant.CodeConstant;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.Pet;
import com.fun.zpetchain.model.User;
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
public class SuperRareBuyTask {

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
					if (superPet.contains(pet.getPetId()) || PetBuy.petCache.contains(pet.getPetId())) {
						continue;
					}
					superPet.add(pet.getPetId());

					// PetBuy.isAppendOpen(user);
					Pet pInfo = PetCenter.getPetById(pet.getPetId(), user);
					if (pInfo == null) {
						continue;
					}
					pInfo.setValidCode(pet.getValidCode());
					if (new BigDecimal(pInfo.getAmount()).compareTo(BigDecimal.ZERO) <= 0 || pInfo.getAmount() <= 0) {
						continue;
					}

					// 大于4天，不考虑
					String coolingInterval = pInfo.getCoolingInterval();
					if (coolingInterval.indexOf("天") > -1 && Integer.parseInt(coolingInterval.replaceAll("天", "")) > 4) {
						continue;
					}

					Integer superAmount = 0;
					String succStr = "【超级稀有】购买成功:";
					Boolean isSupper = false;

					// 超级稀有
					if (pInfo.getRareNum() > 4 && pInfo.getRareNum() % 2 == 1) {
						isSupper = true;
						// 0代
						if (pInfo.getGeneration() == 0) {
							if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 1000000;
							} else if (pInfo.getIsWhiteEyes() && (pInfo.getIsAngell() || pInfo.getIsYingTao())) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 300000;
							} else if (pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 200000;
							} else if (pInfo.getIsWhiteEyes()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 60000;
							} else if (pInfo.getIsAngell()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 30000;
							} else {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 11000;
							}
						}
						// 1代
						else if (pInfo.getGeneration() == 1) {
							if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 500000;
							} else if (pInfo.getIsWhiteEyes() && (pInfo.getIsAngell() || pInfo.getIsYingTao())) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 150000;
							} else if (pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 50000;
							} else if (pInfo.getIsWhiteEyes()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 40000;
							} else if (pInfo.getIsAngell()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 10000;
							} else {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + PetConstant.SUPER_RARE_RAISE;
							}
						} else {
							if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 400000;
							} else if (pInfo.getIsWhiteEyes() && (pInfo.getIsAngell() || pInfo.getIsYingTao())) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 100000;
							} else if (pInfo.getIsAngell() && pInfo.getIsYingTao()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 40000;
							} else if (pInfo.getIsWhiteEyes()) {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 30000;
							} else {
								superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + PetConstant.SUPER_RARE_RAISE;
							}
						}

						int cooling = Integer.parseInt(coolingInterval.replaceAll("天", ""));
						if (cooling > 0 && superAmount - cooling * 10000 > 0) {
							superAmount = superAmount - cooling * 10000;
						}

					}

					// 天使、白眉
					else {
						if (pInfo.getRareNum() >= 4 && (pInfo.getIsAngell() || pInfo.getIsWhiteEyes() || pInfo.getIsYingTao())) {
							succStr = "【天使白眉】购买成功:";
							if (!coolingInterval.equals("0分钟")) {
								continue;
							}
							// 0代
							if (pInfo.getGeneration() == 0) {
								if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 200000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 60000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 50000;
								} else if (pInfo.getIsYingTao() && pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 30000;
								} else if (pInfo.getIsWhiteEyes()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 15000;
								} else if (pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 6000;
								} else {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 2000;
								}
							}
							// 1代
							else if (pInfo.getGeneration() == 1) {
								if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 150000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 25000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 24000;
								} else if (pInfo.getIsYingTao() && pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 20000;
								} else if (pInfo.getIsWhiteEyes()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 4300;
								} else if (pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 1000;
								} else if (pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree());
								} else {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 700;
								}
							} else {
								if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell() && pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 100000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 16000;
								} else if (pInfo.getIsWhiteEyes() && pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 13000;
								} else if (pInfo.getIsAngell() && pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 10000;
								} else if (pInfo.getIsWhiteEyes()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 2000;
								} else if (pInfo.getIsAngell()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree());
								} else if (pInfo.getIsYingTao()) {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) - 1000;
								} else {
									superAmount = PetConstant.LIMIT_MAP.get(pInfo.getRareDegree()) + 700;
								}
							}
						}
					}

					if (superAmount == 0 || superAmount < pInfo.getAmount()) {
						continue;
					}

					if (isSupper) {
						PetBuy.log(TimeUtil.now(TimeUtil.TARGET_1) + " " + user.getName() + " 【超级稀有】尝试购买: " + pInfo);
					}

					int trycount = 1;
					Boolean b = true;
					while (b && trycount <= PetConstant.TYR_COUNT) {
						trycount++;
						String errorNo = PetBuy.tryBuy(pInfo, user, false);
						if (PetBuy.buySuccess(errorNo)) {
							PetBuy.log(TimeUtil.now(TimeUtil.TARGET_1) + " " + user.getName() + succStr + pInfo);
							b = false;
						} else if (CodeConstant.ERROR_30010.equals(errorNo) || PetBuy.petCache.contains(pet.getPetId())) {
							b = false;
						}
					}
				}

			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
}
