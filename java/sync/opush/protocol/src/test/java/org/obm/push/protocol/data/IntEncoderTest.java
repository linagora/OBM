/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.protocol.data;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class IntEncoderTest {

	private IntEncoder intEncoder;

	@Before
	public void setUp() {
		intEncoder = new IntEncoder();
	}

	private void testInt(int value, byte[] expected) {
		byte[] byteArray = intEncoder.toByteArray(value);
		Assertions.assertThat(byteArray).isEqualTo(expected);
	}

	@Test
	public void testZero() {
		testInt(0, new byte[] {0, 0, 0, 0});
	}

	@Test
	public void testOne() {
		testInt(1, new byte[] {1, 0, 0, 0});
	}
	
	@Test
	public void test255() {
		testInt(255, new byte[] {-1, 0, 0, 0});
	}
	
	@Test
	public void test256() {
		testInt(256, new byte[] {0, 1, 0, 0});
	}
	
	@Test
	public void testIntMax() {
		testInt(Integer.MAX_VALUE, new byte[] {-1, -1, -1, 127});
	}
	
	@Test
	public void testIntMin() {
		testInt(Integer.MIN_VALUE, new byte[] {0, 0, 0, -128});
	}

}
