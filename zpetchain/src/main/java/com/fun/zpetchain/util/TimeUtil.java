package com.fun.zpetchain.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TimeUtil {

    private static final Log logger = LogFactory.getLog(TimeUtil.class);

    private static final String EMPTY = "";

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final String TARGET_1 = "yyyy-MM-dd HH:mm:ss";

    /**
     * yyyy-MM-dd HH:mm
     */
    public static final String TARGET_2 = "yyyy-MM-dd HH:mm";

    /**
     * yyyy-MM-dd
     */
    public static final String TARGET_3 = "yyyy-MM-dd";

    /**
     * yyyy年MM月dd日
     */
    public static final String TARGET_4 = "yyyy年MM月dd日";

    /**
     * yyyyMMddHHmmss
     */
    public static final String SOURCE_1 = "yyyyMMddHHmmss";

    /**
     * yyyyMMdd
     */
    public static final String SOURCE_2 = "yyyyMMdd";

    /**
     * yyyyMM
     */
    public static final String SOURCE_3 = "yyyyMM";

    /**
     * MMdd
     */
    public static final String SOURCE_4 = "MMdd";

    /**
     * 格式化时间,从一种格式转变为另一种格式
     * <p>
     * 如果源时间对象为String类型，那么认为源格式为 yyyyMMddHHmmss
     * <p>
     * 目标格式为 yyyy-MM-dd HH:mm:ss
     *
     * @param source 源时间对象，可以为String或是Date类型
     * @return 格式化后的时间字符串，有任何错误返回空字符串
     */
    public static String format(Object source) {
        return format(source, null, null, null);
    }

    /**
     * 格式化时间,从一种格式转变为另一种格式
     * <p>
     * 如果源时间对象为String类型，那么认为源格式为 yyyyMMddHHmmss
     *
     * @param source 源时间对象，可以为String或是Date类型
     * @param tfmt   返回的目标格式
     * @return 格式化后的时间字符串，有任何错误返回空字符串
     */
    public static String format(Object source, String tfmt) {
        return format(source, null, tfmt, null);
    }

    /**
     * 格式化时间,从一种格式转变为另一种格式，源时间对象为空的话返回空字符串
     *
     * @param source 源时间对象，可以为String或是Date类型
     * @param sfmt   源格式，如果source为Date类型，则此参数无用
     * @param tfmt   返回的目标格式
     * @return 格式化后的时间字符串，有任何错误返回空字符串
     */
    public static String format(Object source, String sfmt, String tfmt) {
        return format(source, sfmt, tfmt, null);
    }

    /**
     * 格式化时间,从一种格式转变为另一种格式
     *
     * @param source  源时间对象，可以为String或是Date类型
     * @param sfmt    源格式，如果source为Date类型，则此参数无用
     * @param tfmt    返回的目标格式
     * @param nullval 如果源为null的话返回的默认值
     * @return 格式化后的时间字符串，有任何错误返回空字符串
     */
    public static String format(Object source, String sfmt, String tfmt, String nullval) {
        tfmt = (tfmt == null) ? TARGET_1 : tfmt;
        sfmt = (sfmt == null) ? SOURCE_1 : sfmt;

        SimpleDateFormat sdf = new SimpleDateFormat(tfmt);
        try {
            if (source == null || "".equals(source)) {
                return (nullval == null) ? EMPTY : nullval;
            } else if (source instanceof String) {
                SimpleDateFormat parseformat = new SimpleDateFormat(sfmt);
                return sdf.format(parseformat.parse((String) source));
            } else {
                return sdf.format(source);
            }
        } catch (ParseException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Source Parse Exception: source=" + source + ", sfmt=" + sfmt + ", tfmt=" + tfmt, e);
            }
        } catch (IllegalArgumentException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Illegal Source Exception: source=" + source + ", sfmt=" + sfmt + ", tfmt=" + tfmt, e);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception: source=" + source + ", sfmt=" + sfmt + ", tfmt=" + tfmt, e);
            }
        }
        return EMPTY;
    }

    /**
     * 返回当前时间
     *
     * @return yyyyMMddHHmmss格式的当前时间字符串
     */
    public static String now() {
        return now(SOURCE_1);
    }

    /**
     * 返回当前日期
     *
     * @return yyyyMMdd格式的当前时间字符串
     */
    public static String nowDate() {
        return now(SOURCE_2);
    }

    public static String yestdayDate() {
        return yestday(SOURCE_2);
    }

    public static String yestDateOnMoreDate() {
        return yestdayOnMore(SOURCE_2);
    }

    public static String preOneWeekDate() {
        return preOneWeekDate(SOURCE_2);
    }

    /**
     * 返回当前月份
     *
     * @return yyyyMM格式的当前月份字符串
     */
    public static String nowMonth() {
        return now(SOURCE_3);
    }

    /**
     * 返回当前时间
     *
     * @param fmt 格式化字符
     * @return fmt指定样式的当前时间字符串
     */
    public static String now(String fmt) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(cal.getTime());
    }

    public static String yestday(String fmt) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(cal.getTime());
    }

    /**
     * 明天
     *
     * @param fmt
     * @return
     */
    public static String tomorrowDate(String fmt) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(cal.getTime());
    }

    public static String tomorrowDate() {
        return tomorrowDate(SOURCE_2);
    }

    /**
     * @param
     * @param fmt
     * @return 昨天的前一天
     */
    public static String yestdayOnMore(String fmt) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(cal.getTime());
    }

    /**
     * @param
     * @param fmt
     * @return 往前一周
     */
    public static String preOneWeekDate(String fmt) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(cal.getTime());
    }

    /**
     * 获得指定时间之后或者之前N小时的14位时间字符串
     *
     * @param strTime
     * @param hours
     * @return
     */
    public static String addHours(String strTime, int hours) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d = sdf.parse(strTime);
            return TimeUtil.format(DateUtils.addHours(d, hours), SOURCE_1);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String addMinutes(String strTime, int amount) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d = sdf.parse(strTime);
            return TimeUtil.format(DateUtils.addMinutes(d, amount), SOURCE_1);
        } catch (ParseException e) {
            return "";
        }
    }


    public static int compareTime(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            return d1.compareTo(d2);
        } catch (ParseException e) {
            return -2;
        }
    }

    /**
     * 获得指定时间之后或者之前N天的14位时间字符串
     *
     * @param strTime 处理的比较时间，14位时间字符串
     * @param days    N天，可以是负数
     * @return 14位时间字符串
     */
    public static String addDayTimes(String strTime, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d = sdf.parse(strTime);
            return TimeUtil.format(DateUtils.addDays(d, days), SOURCE_1);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String addDays(String strTime, int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date d = sdf.parse(strTime);
            return TimeUtil.format(DateUtils.addDays(d, days), SOURCE_2);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String addDays(int days) {
        Calendar cal = Calendar.getInstance();
        return TimeUtil.addDays(cal.getTime(), days);
    }

    public static String addDays(Date d, int days) {
        return TimeUtil.format(DateUtils.addDays(d, days), SOURCE_1);
    }

    /**
     * 获得指定时间之后的N月的14位时间字符串
     *
     * @param strTime
     * @param m
     * @return 14位时间字符串，异常返回空字符串
     */
    public static String addMonths(String strTime, int m) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d = sdf.parse(strTime);
            return TimeUtil.format(DateUtils.addMonths(d, m), SOURCE_1);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String addMonths(int m) {
        Calendar cal = Calendar.getInstance();
        return TimeUtil.addMonths(cal.getTime(), m);
    }

    public static String addMonths(Date d, int m) {
        return TimeUtil.format(DateUtils.addMonths(d, m), SOURCE_1);
    }

    /**
     * 获得指定年之后N年的14位时间字符串
     *
     * @param strTime 14时间字符串
     * @param years   N年，可以是负数
     * @return 14位时间字符串
     */
    public static String addYears(String strTime, int years) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d = sdf.parse(strTime);
            return TimeUtil.format(DateUtils.addYears(d, years), SOURCE_1);
        } catch (ParseException e) {
            return strTime;
        }
    }

    public static String addYears(int years) {
        Calendar cal = Calendar.getInstance();
        return TimeUtil.addYears(cal.getTime(), years);
    }

    public static String addYears(Date d, int years) {
        return TimeUtil.format(DateUtils.addDays(d, years), SOURCE_1);
    }

    /**
     * 获得当前是星期几的数字 WEEK_ZH_MAP.put("1", "星期日"); WEEK_ZH_MAP.put("2", "星期一");
     * WEEK_ZH_MAP.put("3", "星期二"); WEEK_ZH_MAP.put("4", "星期三");
     * WEEK_ZH_MAP.put("5", "星期四"); WEEK_ZH_MAP.put("6", "星期五");
     * WEEK_ZH_MAP.put("7", "星期六");
     *
     * @return 表示星期的数字
     */
    public static int getWeek() {
        Calendar cal = Calendar.getInstance(); // 创建一个日历对象。
        cal.setTime(new Date());
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 格式化日期（返回Date类型）
     *
     * @param date
     * @param format
     * @return
     */

    public static Date formatReturnDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return null;
        }

    }

    public static Date addDaysRsDate(Date d, int days) {

        String str = TimeUtil.addDays(d, days);
        return TimeUtil.formatReturnDate(str, "yyyyMMddHHmmss");


    }

    /**
     * @param sDate yyyy-MM-dd
     * @param eDate yyyy-MM-dd
     * @param days  拆分开始时间和结束时间间隔天数    如1号到2号为2,1号到5号为5
     *              days >= 1 否则，返回null
     * @return 每个map包含s_date, e_date
     */
    public static List<Map<String, String>> splitDateByDays(String sDate, String eDate, int days) {
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        days = days - 1;
        if (days < 0) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date s = sdf.parse(sDate);
            Date e = sdf.parse(eDate);
            //日期相同，直接返回时间段
            if (s.equals(e)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("s_date", sdf.format(s));
                map.put("e_date", sdf.format(e));
                resultList.add(map);
                return resultList;
            }
            //循环执行时间添加
            while (s.before(e)) {
                Date d = DateUtils.addDays(s, days);
                Map<String, String> map = new HashMap<String, String>();
                //如果开始时间加了天数后，还在结束时间之前
                if (d.before(e)) {
                    //先将当前时间段加入list
                    String date = TimeUtil.format(DateUtils.addDays(s, days), TARGET_3);
                    map.put("s_date", sdf.format(s));
                    map.put("e_date", date);
                    resultList.add(map);
                    //开始时间向后推一天
                    s = DateUtils.addDays(d, 1);
                    //开始时间退一天后，可能和结束时间相同，相同则把该段时间加入list，伺候循环不再执行
                    if (s.equals(e)) {
                        Map<String, String> newMap = new HashMap<String, String>();
                        newMap.put("s_date", sdf.format(s));
                        newMap.put("e_date", sdf.format(e));
                        resultList.add(newMap);
                        /*return resultList;*/
                    }
                } else {//开始时间加了天数后，在结束时间之后，将区间直接加入list
                    map.put("s_date", sdf.format(s));
                    map.put("e_date", sdf.format(e));
                    resultList.add(map);
                    s = e;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 计算目标时间距当前时间时长
     *
     * @param time yyyyMMddHHmmss
     * @return 如1天2小时，2小时
     */
    public static String calFromNow(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(SOURCE_1);
        try {
            long target = sdf.parse(time).getTime();
            long now = sdf.parse(TimeUtil.now(SOURCE_1)).getTime();
            int hours = (int) ((now - target) / (1000 * 60 * 60));
            if (hours > 0) {
                //还未到目标时间
                //>24小时，显示多少天多少小时
                int days = hours / 24;
                if (days > 0) {
                    hours = hours % 24;
                    return days + "天" + hours + "小时";
                } else {
                    return hours + "小时";
                }
            } else {
                return "";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        // System.out.println(TimeUtil.addMinutes("20121212090000", 30));
        // System.out.println(compareTime(TimeUtil.addMinutes("20121212090000",
        // 30), "20121212090000"));

//		String str = TimeUtil.addDays(new Date(), 2);
//
//		Date d = TimeUtil.addDaysRsDate(new Date(), -1);
//
//		String dd= TimeUtil.addDays("20161115", -1);
//		System.out.println("dd:"+dd);
//
//		System.out.println(TimeUtil.format("2016-11-15",TimeUtil.TARGET_3,TimeUtil.SOURCE_2));
//
//		System.out.println(TimeUtil.format(new Date(),TimeUtil.SOURCE_1));

//		List<Map<String, String>> splitDateByDays = splitDateByDays("2017-12-21", "2017-12-31", 5);
//		for (Map<String, String> map : splitDateByDays) {
//			String s_date = map.get("s_date");
//			String e_date = map.get("e_date");
//			System.out.println(s_date+"____"+e_date);
//		}

//       System.out.println(calculateTimeFromNow("20180123153212"));
    }

}
