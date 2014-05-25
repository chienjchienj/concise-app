package org.sustudio.concise.app.utils;

/**
 * 把可能是Link的文字掛上link的tag
 * 
 * @author Kuan-ming Su
 *
 */
public class LinkTagEncoder {

	public static String encode(String url) {
		String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		if (url.matches(regex)) {
			url = "<a href=\"" + url + "\">" + url + "</a>";
		}
		return url;
	};
	
}
