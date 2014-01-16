/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.utils.type;

import static org.fest.assertions.api.Assertions.assertThat;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class UnsignedShortTest {

	private static final int UNSIGNED_SHORT_BYTES_COUNT = 2;
	
	@Test(expected=IllegalArgumentException.class)
	public void testCastNegative() {
		UnsignedShort.checkedCast(-1);
	}
	
	@Test
	public void testCastZero() {
		int toTestValue = UnsignedShort.checkedCast(0).getValue();
		
		assertThat(toTestValue).isEqualTo(0);
	}
	
	@Test
	public void testCastOne() {
		int toTestValue = UnsignedShort.checkedCast(1).getValue();
		
		assertThat(toTestValue).isEqualTo(1);
	}
	
	@Test
	public void testCastMinValue() {
		int toTestValue = UnsignedShort.checkedCast(UnsignedShort.MIN_VALUE).getValue();
		
		assertThat(toTestValue).isEqualTo(0);
	}

	@Test
	public void testCastMaxValue() {
		int toTestValue = UnsignedShort.checkedCast(UnsignedShort.MAX_VALUE).getValue();
		
		assertThat(toTestValue).isEqualTo(65535);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCastLessThanMinValue() {
		UnsignedShort.checkedCast(UnsignedShort.MIN_VALUE - 1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCastMoreThanMaxValue() {
		UnsignedShort.checkedCast(UnsignedShort.MAX_VALUE + 1);
	}

	@Test
	public void testZero() {
		byte[] byteArray = UnsignedShort.checkedCast(0).toByteArray();
		
		byte[] expectedBytes = new byte[] {0, 0};
		Assertions.assertThat(byteArray).hasSize(UNSIGNED_SHORT_BYTES_COUNT).isEqualTo(expectedBytes);
	}

	@Test
	public void testOne() {
		byte[] byteArray = UnsignedShort.checkedCast(1).toByteArray();
		
		byte[] expectedBytes = new byte[] {1, 0};
		Assertions.assertThat(byteArray).hasSize(UNSIGNED_SHORT_BYTES_COUNT).isEqualTo(expectedBytes);
	}
	
	@Test
	public void testMaxValue() {
		byte[] byteArray = UnsignedShort.checkedCast(UnsignedShort.MAX_VALUE).toByteArray();
		
		byte[] expectedBytes = new byte[] {-1, -1};
		Assertions.assertThat(byteArray).hasSize(UNSIGNED_SHORT_BYTES_COUNT).isEqualTo(expectedBytes);
	}
}
