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

import org.apache.commons.codec.binary.Base64;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.push.bean.MSEventUid;

import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class EmailEncoderTest {

	private EmailEncoder emailEncoder;

	@Before
	public void setUp() {
		emailEncoder = new EmailEncoder(new IntEncoder());
	}
	
	@Test
	public void testConvertMSEventUidToGlobalObjId() {
		/*
		 * Bytes 1-16:  <04><00><00><00><82><00><E0><00><74><C5><B7><10><1A><82><E0><08>
		 * Bytes 17-20: <00><00><00><00>
		 * Bytes 21-36: <00><00><00><00><00><00><00><00><00><00><00><00><00><00><00><00>
		 * Bytes 37-40: <33><00><00><00>
		 * Bytes 41-52: vCal-Uid<01><00><00><00>
		 * Bytes 53-91: {81412D3C-2A24-4E9D-B20E-11F7BBE92799}<00>
		 */
		byte[] expectedBytes = emailEncoder.buildByteSequence(
				0x04, 0x00, 0x00, 0x00, 0x82, 0x00, 0xE0, 0x00, 0x74, 0xC5, 0xB7, 0x10, 
				0x1A, 0x82, 0xE0, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x33, 0x00, 0x00, 0x00, 0x76, 0x43, 0x61, 0x6C, 0x2D, 0x55, 0x69, 0x64,
				0x01, 0x00, 0x00, 0x00, 0x7B, 0x38, 0x31, 0x34, 0x31, 0x32, 0x44, 0x33,
				0x43, 0x2D, 0x32, 0x41, 0x32, 0x34, 0x2D, 0x34, 0x45, 0x39, 0x44, 0x2D,
				0x42, 0x32, 0x30, 0x45, 0x2D, 0x31, 0x31, 0x46, 0x37, 0x42, 0x42, 0x45,
				0x39, 0x32, 0x37, 0x39, 0x39, 0x7D, 0x00);
		String expected = Base64.encodeBase64String(expectedBytes);
		String actual = emailEncoder.msEventUidToGlobalObjId(
				new MSEventUid("{81412D3C-2A24-4E9D-B20E-11F7BBE92799}"));
		Assertions.assertThat(actual).isEqualTo(expected);
	}

}
