package org.sustudio.concise.app.utils;

public class FullChar {

	public static String toHalfCharString(String src) {
		if (src == null) return null;
		return toHalfCharStringBuilder(new StringBuilder(src)).toString();
	}
	
	
	public static StringBuilder toHalfCharStringBuilder(StringBuilder sb) {
		StringBuilder r = new StringBuilder();
		for (int i=0; i<sb.length(); i++) {
			r.appendCodePoint(toHalfChar(sb.codePointAt(i)));
		}
		sb.setLength(0);
		return r;
	}
	
	public static int toHalfChar(int codePoint) {
		if (Character.isLetterOrDigit(codePoint)) {
			if ( codePoint >= 65281 && codePoint <= 65374 ) {
				codePoint -= 65248;
			}
			else if (codePoint == 12288) {  // 全形空白
				codePoint = 32;
			}
		}
		return codePoint;
	}
}
