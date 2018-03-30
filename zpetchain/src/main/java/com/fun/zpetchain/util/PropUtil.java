package com.fun.zpetchain.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * class that read configuration files
 * <br><b>Copyright 2018 the original author or authors.</b>
 * @author 2bears
 * @since
 * @version 1.0
 */
public class PropUtil {
	
	private static Map<String, String> propMap = new HashMap<String, String>(8);
	
	static {
		loadProp();
	}
	
	public static String getProp(String key) {
		return  propMap.get(key);
	}
	
	private static void loadProp() {
		ResourceBundle rb = ResourceBundle.getBundle("config");
		for(String key : rb.keySet()) {
			propMap.put(key, rb.getString(key));
		}
	}
}
