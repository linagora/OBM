package org.obm.push.tnefconverter.test;

import java.io.ByteArrayInputStream;

import org.obm.push.tnefconverter.ScheduleMeeting.GlobalObjectId;

import junit.framework.TestCase;

public class GlobalObjectIdTests extends TestCase {


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
		assertEquals(
				"040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B1000000088E298BE26F88187D35441F639991034",
				gloID.getUid());
	}

	public void testDecompress2() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						getHexa("040000008200E00074C5B7101A82E00807DA0A1700000000000000000000000000000000150000007643616C2D55696401000000376265373464656400")),
				0);

		assertEquals(
				"3762653734646564",
				gloID.getUid());
	}

	public void testDecompress3() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						getHexa("040000008200E00074C5B7101A82E00807DA0A0D6DBB15E46333549A5D48E37D6E66ED3B10000000EB7523FA41CE256CF911970CF605B651")),
				49152);
		assertEquals(
				"040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B10000000EB7523FA41CE256CF911970CF605B651",
				gloID.getUid());

		assertEquals("Wed Oct 13 12:00:00 CEST 2010", gloID.getRecurrenceId().toString());
	}

	public void testDecompress4() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						getHexa("040000008200E00074C5B7101A82E00807DA0A0B6DBB15E46333549A5D48E37D6E66ED3B100000000CBE9F652487D768F26FC1CDC5CB0867")),
				61440);
		assertEquals(
				"040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B100000000CBE9F652487D768F26FC1CDC5CB0867",
				gloID.getUid());
		assertEquals("Mon Oct 11 15:00:00 CEST 2010", gloID.getRecurrenceId().toString());

	}

	public void testDecompress5() throws Exception {
		GlobalObjectId gloID = new GlobalObjectId(
				new ByteArrayInputStream(
						getHexa("040000008200E00074C5B7101A82E00807DA0A1A6DBB15E46333549A5D48E37D6E66ED3B10000000FC68EC31627BCAF602BB3BF24E59FE00")),
						49152);
		assertEquals(
				"040000008200E00074C5B7101A82E008000000006DBB15E46333549A5D48E37D6E66ED3B10000000FC68EC31627BCAF602BB3BF24E59FE00",
				gloID.getUid());
		assertEquals("Tue Oct 26 12:00:00 CEST 2010", gloID.getRecurrenceId().toString());

	}
	
	
	private static byte[] getHexa(String hexa) {
		if (hexa.length() % 2 != 0) {
			throw new RuntimeException("Taille incorrecte");
		}
		byte[] buf = new byte[hexa.length() / 2];
		int i = 0;
		for (int pos = 0; pos < hexa.length(); pos += 2) {
			String substring = hexa.substring(pos, pos + 2);
			buf[i++] = (byte) Integer.decode("0x" + substring).intValue();
		}
		return buf;
	}

}
