package fr.aliasource.utils;

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
}
