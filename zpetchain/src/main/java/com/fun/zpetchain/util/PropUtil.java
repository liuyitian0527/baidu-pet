package com.fun.zpetchain.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PropUtil {

	private static Map<String, String> propMap = new HashMap<String, String>(8);

	static {
		loadProp();
	}

	public static String getProp(String key) {
		return propMap.get(key);
	}

	private static void loadProp() {
		ResourceBundle rb = ResourceBundle.getBundle("config");
		for (String key : rb.keySet()) {
			propMap.put(key, rb.getString(key));
		}
	}
}
