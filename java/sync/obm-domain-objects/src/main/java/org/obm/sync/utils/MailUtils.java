package org.obm.sync.utils;

public class MailUtils {
	public static String extractFirstEmail(String emails, String domainName) {
		String firstEmail = null;
		if (emails != null) {
			String[] alias = emails.split("\r\n");
			if (alias[0].contains("@")) {
				firstEmail = alias[0];
			} else {
				firstEmail = alias[0] + "@" + domainName;
			}
		}
		return firstEmail;
	}
}
