/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Ristretto Mail API.
 *
 * The Initial Developers of the Original Code are
 * Timo Stich and Frederik Dietz.
 * Portions created by the Initial Developers are Copyright (C) 2004
 * All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.columba.ristretto.parser;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for dates as defined in RFC 2822.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class DateParser {
	private static final long[] monthOffset = { 0l, // Jan
			2678400000l, // Feb
			5097600000l, // Mar
			7776000000l, // Apr
			10368000000l, // May
			13046400000l, // Jun
			15638400000l, // Jul
			18316800000l, // Aug
			20995200000l, // Sep
			23587200000l, // Oct
			26265600000l, // Nov
			28857600000l // Dec
	};

	private static final Pattern stringPattern = Pattern
			.compile("(\\w{3})\\w*");

	private static final Pattern numberPattern = Pattern
			.compile("(\\d{1,4})");

	private static final Pattern timePattern = Pattern
			.compile("(\\d\\d):(\\d\\d)(:(\\d\\d))?");

	private static final Pattern timezonePattern = Pattern
			.compile("((\\+|-)(\\d\\d)(\\d\\d))|(\\\"?(\\w+)\\\"?)");

	private DateParser() {
	}

	private static int getMonth(String month) {
		String loweredMonth = month.toLowerCase();
		char startChar = loweredMonth.charAt(0);

		switch (startChar) {
		case 'j': {
			if (loweredMonth.equals("jan"))
				return 0;
			if (loweredMonth.equals("jun"))
				return 5;
			if (loweredMonth.equals("jul"))
				return 6;
		}
		case 'f': {
			if (loweredMonth.equals("feb"))
				return 1;
		}
		case 'm': {
			if (loweredMonth.equals("mar"))
				return 2;
			if (loweredMonth.equals("may"))
				return 4;
		}
		case 'a': {
			if (loweredMonth.equals("apr"))
				return 3;
			if (loweredMonth.equals("aug"))
				return 7;
		}
		case 's': {
			if (loweredMonth.equals("sep"))
				return 8;
		}
		case 'o': {
			if (loweredMonth.equals("oct"))
				return 9;
		}
		case 'n': {
			if (loweredMonth.equals("nov"))
				return 10;
		}
		case 'd': {
			if (loweredMonth.equals("dec"))
				return 11;
		}
		}

		return -1;
	}

	private static long getLeapYearCorrection(int day, int month, int year) {
		int normalizedYear = year - 1972;
		if (day <= 29 && month < 2)
			normalizedYear -= 1;

		int leapYears = normalizedYear / 4;

		return 86400000l * leapYears;
	}

	/**
	 * Parses a date String as specified in RFC 2822.
	 * 
	 * @param dateString
	 *            CharSequence of the Date to parse
	 * @return parsed Date
	 * @throws ParserException
	 */
	public static Date parse(CharSequence dateString) throws ParserException {
		String temp = dateString.toString();
		
		Matcher matcher;

		// retrieve date
		matcher = stringPattern.matcher(temp);
		int month = -1;
		while (month == -1 && matcher.find() ) {
			month = getMonth(matcher.group(1));
			temp = temp.substring(0,matcher.start()) + temp.substring(matcher.end());

			matcher = stringPattern.matcher(temp);
		}
		if (month == -1)
			throw new ParserException("Invalid Date: " + dateString);


		matcher = timePattern.matcher(temp);
		if (!matcher.find())
			throw new ParserException("Invalid Date: " + dateString);
		temp = temp.substring(0,matcher.start()) + temp.substring(matcher.end());

		// retrieve time
		int hours = Integer.parseInt(matcher.group(1));
		int minutes = Integer.parseInt(matcher.group(2));
		int seconds = 0;
		if (matcher.group(3) != null) {
			seconds = Integer.parseInt(matcher.group(4));
		}

		
		// retrieve day in month and year		
		matcher = numberPattern.matcher(temp);

		if (!matcher.find())
			throw new ParserException("Invalid Date: " + dateString);
		int day = Integer.parseInt(matcher.group());
		temp = temp.substring(0,matcher.start()) + temp.substring(matcher.end());

		matcher = numberPattern.matcher(temp);
		if (!matcher.find())
			throw new ParserException("Invalid Date: " + dateString);
		int year = Integer.parseInt(matcher.group());
		if (year <= 99) {
			if (year <= 49)
				year += 2000;
			else
				year += 1900;
		}
		temp = temp.substring(0,matcher.start()) + temp.substring(matcher.end());

		// calculate Milliseconds from 1.1.1970 00:00:00 GMT
		long date = seconds * 1000l + minutes * 60000l + hours * 3600000l + day
				* 86400000l + monthOffset[month] + (year - 1970) * 31536000000l
				+ getLeapYearCorrection(day, month, year);

		// Make timezone corrections
		matcher = timezonePattern.matcher(temp);
		long zoneoffset = 0;
		if (matcher.find()) {
			if (matcher.group(1) != null) {
				zoneoffset = Integer.parseInt(matcher.group(4)) * 60000l
						+ Integer.parseInt(matcher.group(3)) * 3600000l;
				if (matcher.group(2).equals("+")) {
					zoneoffset = -zoneoffset;
				}
			} else if (matcher.group(6) != null) {
				if (matcher.group(6).equals("EDT")) {
					zoneoffset = 4 * 3600000l;
				} else if (matcher.group(6).equals("EST")) {
					zoneoffset = 5 * 3600000l;
				} else if (matcher.group(6).equals("CDT")) {
					zoneoffset = 5 * 3600000l;
				} else if (matcher.group(6).equals("CST")) {
					zoneoffset = 6 * 3600000l;
				} else if (matcher.group(6).equals("MDT")) {
					zoneoffset = 6 * 3600000l;
				} else if (matcher.group(6).equals("MST")) {
					zoneoffset = 7 * 3600000l;
				} else if (matcher.group(6).equals("PDT")) {
					zoneoffset = 7 * 3600000l;
				} else if (matcher.group(6).equals("PST")) {
					zoneoffset = 8 * 3600000l;
				}
			}
		}
		date += zoneoffset;

		// return parsed date
		return new Date(date);
	}
}
