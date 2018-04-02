package com.fun.zpetchain.enums;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public enum PetEnum {

	/** 等级 */
	LEVEL_COMMON("0", "普通"), //
	LEVEL_RARE("1", "稀有"), 	//
	LEVEL_EXCELLENCE("2", "卓越"), //
	LEVEL_EPIC("3", "史诗"), //
	LEVEL_MYTH("4", "神话"), //
	LEVEL_LEGEND("5", "传说"), //

	/** 代数 */
	GENERATION_0("0", "0代"), //
	GENERATION_1("1", "1代"), //
	GENERATION_2("2", "2代"), //
	GENERATION_3("3", "3代"), //

	/** 休息时间 */
	COOLINGINTERVAL_0("0", "0分钟"), //
	COOLINGINTERVAL_1("1", "24小时"), //
	COOLINGINTERVAL_2("0", "2天"), //
	COOLINGINTERVAL_4("0", "4天"), //

	END("9999", "9999"); // 结束

	private String code;
	private String desc;

	PetEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getDesc() {
		return this.desc;
	}

	public String getCode() {
		return this.code;
	}

	public static LinkedHashMap<String, String> getMapByPrefix(String prefix) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (PetEnum rb : values()) {
			if (StringUtils.isNotBlank(prefix)) {
				if (rb.name().startsWith(prefix))
					map.put(rb.getCode(), rb.getDesc());
			}
		}
		return map;
	}

	@Override
	public String toString() {
		return String.format("%s - %s, ", this.getCode(), this.getDesc());
	}
}
