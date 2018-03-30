package com.fun.zpetchain.data;

import java.util.Random;

/**
 * 蒙特卡罗模拟繁育后代稀有属性的概览
 * 不考虑突变的情况
 * <br><b>Copyright 2018 the original author or authors.</b>
 * @author 2bears
 * @since
 * @version 1.0
 */
public class PetChainAna {
	
	private final static int ATTR_NUM = 8;
	
	private final static int LOOP_NUM = 1000000000;
	
	
	
	private final static Random random = new Random(System.currentTimeMillis());
	
	public static void main(String []args) {
		anaBreedChange();
	}
	
	/**
	 * 蒙特卡罗方法分析N个硬币投掷M个正面朝上的几率
	 * @author 2bears
	 * @since
	 */
	private static void anaBreedChange() {
		for(int n = 1; n <= ATTR_NUM; n++) {
			int [] dataArr = new int[n+1];
			for(int k = 0; k < LOOP_NUM; k++) {
				int randRare = getRandom(n);
				dataArr[randRare] = dataArr[randRare] + 1;
			}
			System.out.println(String.format("%s个不同稀有属性, 生成不同个数稀有属性的几率：", n));
			for(int i = 0; i <= n; i++) {
				System.out.println(String.format("%s稀有几率:%.2f%s", i, (dataArr[i] * 100.0)/LOOP_NUM, "%"));
			}
		}
	}
	
	private static int getRandom(int attrNum) {
		int count = 0;
		for(int i = 0; i < attrNum; i++) {
			if(random.nextBoolean()) {
				count++;
			}
		}
		
		return count;
	}
	public static void anaBreed(String []args) {
		
		int []dataArr = new int[ATTR_NUM + 1];
		for(int i = 0; i < ATTR_NUM + 1; i++) {
			dataArr[i] = 0;
		}
		
		for(int i = 0; i < LOOP_NUM; i++) {
			int breedRareNum = getRareAttr(arrtBreed(genAttrs(4), genAttrs(4)));
			dataArr[breedRareNum] = dataArr[breedRareNum] + 1;
		}
		
		for(int i = 0; i < ATTR_NUM; i++) {
			System.out.println(String.format("%s rare attr, count=%s", i, (dataArr[i] + 0.0)/LOOP_NUM));
		}
		
	}	
	/**
	 * 随机生成一定数量的稀有属性
	 * @author 2bears
	 * @since
	 * @param rareNum
	 * @return 8个属性的列表, true表示为稀有属性
	 */
	private static Boolean [] genAttrs(int rareNum) {
		Boolean []attrArr = new Boolean[ATTR_NUM];
		for(int i = 0; i < ATTR_NUM; i++) {
			attrArr[i] = false;
		}
		int count = 0;
		while(count != rareNum) {
			count = 0;
			for(int i = 0; i < ATTR_NUM; i++) {
				if(random.nextInt(ATTR_NUM) + 1 <= rareNum) {
					attrArr[i] = true;
					count++;
					if(count == rareNum) {
						break;
					}
				}
			}
		}
		return attrArr;
	}
	
	/**
	 * 返回后代属性
	 * @author 2bears
	 * @since
	 * @param fAttr 
	 * @param mAttr
	 * @return
	 */
	private static Boolean [] arrtBreed(Boolean []fAttr, Boolean []mAttr) {
		Boolean []breedArr = new Boolean[ATTR_NUM];
		for(int i = 0; i < ATTR_NUM; i++) {
			if(random.nextBoolean()) {
				breedArr[i] = fAttr[i];
			} else {
				breedArr[i] = mAttr[i];
			}
		}
		return breedArr;
	}
	
	private static int getRareAttr(Boolean []breedAttr) {
		int count = 0;
		for(Boolean attr : breedAttr) {
			if(attr) {
				count++;
			}
		}
		return count;
	}
	
	
	
}
