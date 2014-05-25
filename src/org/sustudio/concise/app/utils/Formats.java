package org.sustudio.concise.app.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formats number, date, and time.
 * 
 * @author Kuan-ming Su.
 *
 */
public class Formats {
	
	/**
	 * Returns number format with thousandths (#,###,###,###).
	 * @param number	number.
	 * @return			number with thousands.
	 */
	public static String getNumberFormat(long number) {
		return (new DecimalFormat("#,###,###,###").format(number));
	}

	
	/**
	 * Returns percentage format (0.00).
	 * @param number	number.
	 * @return			percentage format.
	 */
	public static String getPercentFormat(double number) {
		return (new DecimalFormat("0.00").format(number));
	}
	
	
	/**
	 * Returns decimal format (6 digits).
	 * @param number	number.
	 * @return			decimal format (6 digits).
	 */
	public static String getDecimalFormat(double number) {
		return getDecimalFormat(number, 6);
	}
	
	/**
	 * Returns decimal format with specific digits
	 * @param number	number.
	 * @param digits	digits.
	 * @return
	 */
	public static String getDecimalFormat(double number, int digits) {
		String pattern = "0.";
		for (int i=0; i<digits; i++) {
			pattern += "0";
		}
		return (Double.isNaN(number) ? "NaN" : new DecimalFormat(pattern).format(number));
	}
	
	
	/**
	 * Returns date format
	 * @return 		date string.
	 */
	public static String getDate() {
		return (new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}
	
	
	/**
	 * Returns time format.
	 * @param time		time.
	 * @return			time format.
	 */
	public static String getTime(long time) {
		String format = String.format("%%0%dd", 2);
		time = time / 1000;
		String seconds = String.format(format, time % 60);
		String minutes = String.format(format, (time % 3600) / 60);
		String hours = String.format(format, time / 3600);
		return hours+":"+minutes+":"+seconds;
	}
	
}
