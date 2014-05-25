package org.sustudio.concise.app.utils;

public class Whitespace {

	/**
	 * Returns true if token is one of the specific {@link Character} category.
	 * @param token	token
	 * @param type {@link Character} category
	 * @return true if token is one of the specific {@link Character} category.
	 */
	public static final boolean isTokenPunctuation(final String token, int type) {
		if (!isTokenPunctuation(token))
			return false;
		
		return Character.getType(token.codePointAt(0)) == type;
	}
	
	
	public static final boolean isTokenPunctuation(final String token) {
		return token.matches("^\\p{P}$");
	}
	
	
	public static final boolean isTokenAscii(String token) {
		for (char c : token.toCharArray()) {
			if ( (int) c > 127) return false;
		}
		return true;
	}
	
	
	public static final boolean tokenContainsAscii(String token) {
		for (char c : token.toCharArray()) {
			if ( (int) c < 128) return true;
		}
		return false;
	}
	
	public static final String removeTag(String str) {
		return str.replaceAll("<.*?>", "");
	}
	
	
	/**
	 * Checks the space char is required between two tokens.
	 * @param preToken
	 * @param token
	 * @return
	 */
	public static final boolean isSpaceRequired(String preToken, String token) {
		preToken = removeTag(preToken);
		token = removeTag(token);
		
		if (tokenContainsAscii(preToken) && tokenContainsAscii(token)) {
			// 兩個連續的標點符號
			if (isTokenPunctuation(token) && isTokenPunctuation(preToken)
				&& !isTokenPunctuation(preToken, Character.END_PUNCTUATION)
				&& !isTokenPunctuation(preToken, Character.FINAL_QUOTE_PUNCTUATION)
				&& !isTokenPunctuation(token, Character.END_PUNCTUATION)
				&& !isTokenPunctuation(token, Character.FINAL_QUOTE_PUNCTUATION))
				return true;
			
			// "("前要留空白PunctuationUtil
			if (isTokenPunctuation(token, Character.START_PUNCTUATION)
				|| isTokenPunctuation(token, Character.INITIAL_QUOTE_PUNCTUATION))
				return true;
			
			// "("後不留空白
			if (isTokenPunctuation(preToken, Character.START_PUNCTUATION)
				|| isTokenPunctuation(preToken, Character.INITIAL_QUOTE_PUNCTUATION))
				return false;
			
			// dash "-" 前後不留空白
			if (isTokenPunctuation(preToken, Character.DASH_PUNCTUATION)
				|| isTokenPunctuation(token, Character.DASH_PUNCTUATION))
				return false;
			
			// 標點符號後留空白
			if (!isTokenPunctuation(token))
				return true;
		}
		return false;
		
	}
}
