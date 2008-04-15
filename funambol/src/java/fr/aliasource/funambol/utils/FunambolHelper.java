package fr.aliasource.funambol.utils;

import java.util.ArrayList;

/**
 * Helps funambol
 * 
 * @author tom
 * 
 */
public class FunambolHelper {

	public static String removeQuotedPrintableFromVCalString(String vcal) {
		String cleaned = vcal.replace("\r\n", "\n");
		String[] lines = safeSplit(cleaned, '\n');
		StringBuffer noQuoted = new StringBuffer(lines.length * 76);
		boolean quotedMode = false;
		boolean concatNext = false;
		// concat quoted lines ending with =
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i];
			if (l.contains("QUOTED-PRINTABLE")) {
				quotedMode = true;
			}
			if (quotedMode && l.endsWith("=")) {
				concatNext = true;
				l = l.substring(0, l.length() -1);
			} else {
				concatNext = false;
				quotedMode = false;
			}
			noQuoted.append(l);
			if (!concatNext) {
				noQuoted.append('\n');
			}
		}
				
		return noQuoted.toString();
	}

	private static String[] safeSplit(String s, char sep) {
		ArrayList<String> al = new ArrayList<String>(25);
		StringBuffer cur = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == sep) {
				al.add(cur.toString().trim());
				cur = new StringBuffer();
			} else {
				cur.append(s.charAt(i));
			}
		}
		al.add(cur.toString().trim());

		String[] ret = al.toArray(new String[al.size()]);
		return ret;
	}

}
