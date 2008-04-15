package fr.aliasource.funambol.utils;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helps funambol
 * 
 * @author tom
 * 
 */
public class FunisHelper {
	
	private static final Log logger = LogFactory.getLog(FunisHelper.class);

	public static String removeQuotedPrintableFromVCalString(String vcal) {
		String cleaned = vcal.replace("\r\n", "\n");
		String[] lines = safeSplit(cleaned, '\n');
		StringBuffer noQuoted = new StringBuffer(lines.length * 76);
		boolean quotedMode = false;
		boolean concatNext = false;
		// concat quoted lines ending with =
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i];
			logger.warn("funishelper: '"+l+"'");
			
			if (l.contains("QUOTED-PRINTABLE")) {
				quotedMode = true;
			}
			if (quotedMode && l.endsWith("=")) {
				concatNext = true;
				l = l.substring(0, l.length() - 1);
			} else {
				concatNext = false;
				quotedMode = false;
			}
			noQuoted.append(l);
			if (!concatNext && i < lines.length - 1 && !nextLineIsIndented(lines[i+1])) {
				noQuoted.append('\n');
			}
		}

		return noQuoted.toString();
	}

	private static boolean nextLineIsIndented(String l) {
		return l.endsWith(" ");
	}

	private static String[] safeSplit(String s, char sep) {
		ArrayList<String> al = new ArrayList<String>(25);
		StringBuffer cur = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == sep) {
				al.add(cur.toString());
				cur = new StringBuffer();
			} else {
				cur.append(s.charAt(i));
			}
		}
		al.add(cur.toString());

		String[] ret = al.toArray(new String[al.size()]);
		return ret;
	}

}
