package org.obm.sync.utils;

import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

public class DateHelperTest extends TestCase{

	@Test
	public void testHelper() {
		Date d = new Date();
		String s = DateHelper.asString(d);
		Date parsed = DateHelper.asDate(s);
		String reUtc = DateHelper.asString(parsed);

		assertEquals(d, parsed);
		assertEquals(s, reUtc);
	}

}
