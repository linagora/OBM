package org.obm.sync.utils;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class DateHelperTest {

	@Test
	public void testHelper() {
		Date d = new Date();
		System.out.println("Initial date is: " + d);
		String s = DateHelper.asString(d);
		System.out.println("Date as UTC string is: " + s);

		Date parsed = DateHelper.asDate(s);
		System.out.println("Parsed date from string is: " + parsed);
		String reUtc = DateHelper.asString(parsed);
		System.out.println("Back to UTC string: " + reUtc);

		assertEquals(d, parsed);
		assertEquals(s, reUtc);
	}

}
