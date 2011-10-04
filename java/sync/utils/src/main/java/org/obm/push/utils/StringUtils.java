package org.obm.push.utils;

import java.math.BigInteger;

public final class StringUtils {

	public static final String stripChar(String s, char strip) {
		StringBuilder sb = new StringBuilder(s.length());

		char[] chars = s.toCharArray();
		for (char c : chars) {
			if (c != strip) {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static final String stripAddressForbiddenChars(String s) {
		StringBuilder sb = new StringBuilder(s.length());

		char[] chars = s.toCharArray();
		for (char c : chars) {
			if (c != '"' && c != '<' && c != '>') {
				sb.append(c);
			}
		}

		return sb.toString();
	}
	
	public static String getHexadecimalStringRepresentation(byte[] binaryInput){
		BigInteger numericValue = new BigInteger(binaryInput);
		return numericValue.toString(16);
	}
}
