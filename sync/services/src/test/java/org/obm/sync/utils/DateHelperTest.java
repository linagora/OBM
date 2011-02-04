package org.obm.sync.utils;

import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

public class DateHelperTest extends TestCase{

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
