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
package org.obm.push.tnefconverter.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.easymock.IMocksControl;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.tnefconverter.ScheduleMeeting.GlobalObjectId;

import com.google.common.io.BaseEncoding;

public class GlobalObjectIdTests {

	private IMocksControl control;

	@Before
	public void setup() {
		control = createControl();
	}

	@Test
	public void testDecompress1() throws Exception {
		byte[] goi = { 0x04, 0x00, 0x00, 0x00, (byte) 0x82, 0x00, (byte) 0xE0,
				0x00, 0x74, (byte) 0xC5, (byte) 0xB7, 0x10, 0x1A, (byte) 0x82,
				(byte) 0xE0, 0x08, 0x00, 0x00, 0x00, 0x00, 0x6D, (byte) 0xBB,
				0x15, (byte) 0xE4, 0x63, 0x33, 0x54, (byte) 0x9A, 0x5D, 0x48,
				(byte) 0xE3, 0x7D, 0x6E, 0x66, (byte) 0xED, 0x3B, 0x10, 0x00,
				0x00, 0x00, (byte) 0x88, (byte) 0xE2, (byte) 0x98, (byte) 0xBE,
				0x26, (byte) 0xF8, (byte) 0x81, (byte) 0x87, (byte) 0xD3, 0x54,
				0x41, (byte) 0xF6, 0x39, (byte) 0x99, 0x10, 0x34 };
		// 040000008200E00074C5B7101A82E008 07D40910
		// E040C9C12685C4010000000000000000 10000000
		// 0BF0ED4B6E4EE94E9E39E3867B378EF3
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(goi), 0);
		assertThat(gloID.getUid())
			.isEqualTo("040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B1000000088E298BE26F88187D35441F639991034");
	}

	@Test
	public void testDecompress2() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						hexToBytes("040000008200E00074C5B7101A82E00807DA0A1700000000000000000000000000000000150000007643616C2D55696401000000376265373464656400")),
				0);

		assertThat(gloID.getUid()).isEqualTo("3762653734646564");
	}

	@Test
	public void testDecompress3() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						hexToBytes("040000008200E00074C5B7101A82E00807DA0A0D6DBB15E46333549A5D48E37D6E66ED3B10000000EB7523FA41CE256CF911970CF605B651")),
				49152);
		assertThat(gloID.getUid()).isEqualTo(
				"040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B10000000EB7523FA41CE256CF911970CF605B651");
		assertThat(gloID.getRecurrenceId().toString()).isEqualTo("Wed Oct 13 12:00:00 CEST 2010");
	}

	@Test
	public void testDecompress4() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						hexToBytes("040000008200E00074C5B7101A82E00807DA0A0B6DBB15E46333549A5D48E37D6E66ED3B100000000CBE9F652487D768F26FC1CDC5CB0867")),
				61440);
		assertThat(gloID.getUid()).isEqualTo(
				"040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B100000000CBE9F652487D768F26FC1CDC5CB0867");
		assertThat(gloID.getRecurrenceId().toString()).isEqualTo("Mon Oct 11 15:00:00 CEST 2010");

	}

	@Test
	public void testDecompress5() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						hexToBytes("040000008200E00074C5B7101A82E00807DA0A1A6DBB15E46333549A5D48E37D6E66ED3B10000000FC68EC31627BCAF602BB3BF24E59FE00")),
						49152);
		assertThat(gloID.getUid()).isEqualTo(
				"040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B10000000FC68EC31627BCAF602BB3BF24E59FE00");
		assertThat(gloID.getRecurrenceId().toString()).isEqualTo("Tue Oct 26 12:00:00 CEST 2010");
	}
	

	@Test
	public void testDecompressRawBytes() throws IOException {
		GlobalObjectId globalObjectId = new GlobalObjectId(new ByteArrayInputStream(hexToBytes("CC556677")), 49152);
		assertThat(globalObjectId.getUid()).isEqualTo("CC556677");
	}

	@SuppressWarnings("unused")
	@Test(expected = IOException.class)
	public void testFirstSkip() throws IOException {

		InputStream mockInputStream = control.createMock(InputStream.class);
		for (int idByte : GlobalObjectId.GLOBAL_OBJECT_ID_ARRAY_ID) {
			expect(mockInputStream.read()).andReturn(idByte).once();
		}
		expect(mockInputStream.read()).andReturn(0).atLeastOnce();
		expect(mockInputStream.skip(8)).andReturn(0l).once();
		control.replay();
		try {
			new GlobalObjectId(mockInputStream, 49152);
		} finally {
			control.verify();
		}
	}

	@SuppressWarnings("unused")
	@Test(expected = IOException.class)
	public void testSecondSkip() throws IOException {

		InputStream mockInputStream = control.createMock(InputStream.class);
		for (int idByte : GlobalObjectId.GLOBAL_OBJECT_ID_ARRAY_ID) {
			expect(mockInputStream.read()).andReturn(idByte).once();
		}
		expect(mockInputStream.read()).andReturn(0).atLeastOnce();
		expect(mockInputStream.skip(8)).andReturn(8l).once();
		expect(mockInputStream.skip(anyInt())).andReturn(0l).once();
		control.replay();
		try {
			new GlobalObjectId(mockInputStream, 49152);
		} finally {
			control.verify();
		}
	}

	@SuppressWarnings("unused")
	@Test(expected = IOException.class)
	public void testThirdSkip() throws IOException {

		InputStream mockInputStream = control.createMock(InputStream.class);
		for (int idByte : GlobalObjectId.GLOBAL_OBJECT_ID_ARRAY_ID) {
			expect(mockInputStream.read()).andReturn(idByte).once();
		}
		expect(mockInputStream.read()).andReturn(0).atLeastOnce();
		expect(mockInputStream.skip(8)).andReturn(8l).once();
		expect(mockInputStream.skip(8)).andReturn(8l).once();
		mockInputStream.reset();
		expectLastCall();
		expect(mockInputStream.skip(anyInt())).andReturn(0l).once();
		control.replay();
		try {
			new GlobalObjectId(mockInputStream, 49152);
		} finally {
			control.verify();
		}
	}


	private static byte[] hexToBytes(String hexa) {
		return BaseEncoding.base16().decode(hexa);
	}

}
