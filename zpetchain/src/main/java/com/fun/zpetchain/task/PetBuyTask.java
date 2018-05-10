package com.fun.zpetchain.task;

import java.util.Timer;
import java.util.TimerTask;

import com.fun.zpetchain.PetBuy;
import com.fun.zpetchain.constant.PetConstant;
import com.fun.zpetchain.model.User;

/**
 * 
 * Title.普通市场购买 <br>
 * Description.
 * <p>
 * Copyright: Copyright (c) 2018-4-29 上午9:38:23
 * <p>
 * Author: liuyt
 * <p>
 * Version: 1.0
 * <p>
 */
public class PetBuyTask {

	public static void buyTask() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					for (final User user : PetConstant.USERS) {
						if (user.getName().equalsIgnoreCase("zhangyu")) {
							if (System.currentTimeMillis() % 1 == 0) {
								PetBuy.queryPetsOnSale(PetConstant.SORT_TYPE_TIME, PetConstant.FILTER_COND_EPIC, user);
							} else {
								PetBuy.queryPetsOnSale(PetConstant.SORT_TYPE_AMT, PetConstant.FILTER_COND_MYTH, user);
							}
						} else {
							PetBuy.queryPetsOnSale(PetConstant.SORT_TYPE_TIME, PetConstant.FILTER_COND_EPIC, user);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		};
		timer.scheduleAtFixedRate(task, 1000, PetConstant.BUY_INTERVAL);
	}

}
