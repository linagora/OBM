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
package org.obm.push.tnefconverter.ScheduleMeeting;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;

/**
 * Decode a global object id
 */
public class GlobalObjectId {

	@VisibleForTesting
	public static final int[] GLOBAL_OBJECT_ID_ARRAY_ID =
		{ 0x04, 0x00, 0x00, 0x00, 0x82, 0x00, 0xE0, 0x00, 0x74, 0xC5, 0xB7, 0x10, 0x1A, 0x82, 0xE0, 0x08};
	
	private String uid;
	private Date recurrenceId;

	private int size;

	/**
	 * http://msdn.microsoft.com/en-us/library/ee160198(EXCHG.80).aspx Spec
	 * MS-OXOCAL
	 */
	public GlobalObjectId(InputStream obj, int recurStartTime)
			throws IOException {
		if (!isGlobalObjectId(obj)) {
			obj.reset();
			this.uid = bytesToHex(obj);
			return;
		}

		short year = readYear(obj);
		int month = readMonth(obj);
		int dayOfMonth = readDayOfMonth(obj);

		if (year != 0 && month != 0 && dayOfMonth != 0) {
			recurrenceId = buildDate(recurStartTime, year, month, dayOfMonth);
		}
		// Creation Time (8 bytes): The date and time that this Global Object ID
		// was generated.
		// Creation Time is a FILETIME structure, as specified in [MS-DTYP].
		if (obj.skip(8) != 8) {
			throw new IOException("Didn't skip enough bytes");
		}
		// X (8 bytes): Reserved, MUST be all zeroes.
		if (obj.skip(8) != 8) {
			throw new IOException("Didn't skip enough bytes");
		}

		size = readDataSize(obj);

		String uidLabel = readUidLabel(obj);
		
		if (!"vCal-Uid".equals(uidLabel)) {
			// TODO USE buf
			StringBuilder uidBuilder = new StringBuilder(
					"040000008200E00074C5B7101A82E00800000000");
			obj.reset();
			if (obj.skip(20) != 20) {
				throw new IOException("Skipped less than 20 bytes");
			}
			uidBuilder.append(bytesToHex(obj));
			this.uid = uidBuilder.toString();
		} else {
			// // skip 0x01,0x00,0x00,0x00
			obj.skip(4);
			size -= 4;
			// skip 0x00 at the end
			InputStream truncatedStream = ByteStreams.limit(obj, size - 1);
			this.uid = bytesToHex(truncatedStream);
		}
	}

	private String bytesToHex(InputStream truncatedStream) throws IOException {
		return BaseEncoding.base16().encode(ByteStreams.toByteArray(truncatedStream));
	}

	private String readUidLabel(InputStream obj) throws IOException {
		int dataSize = 8;
		byte[] b = new byte[dataSize];
		for (int i = 0; i < dataSize; i++) {
			b[i] = (byte) obj.read();
		}
		String uidLabel = new String(b, Charsets.UTF_8);
		size -= dataSize;
		return uidLabel;
	}

	private int readDataSize(InputStream obj) throws IOException {
		// Size (4 bytes): A LONG value that defines the size of the Data
		// component.
		int[] tsize = new int[4];
		tsize[3] = obj.read();
		tsize[2] = obj.read();
		tsize[1] = obj.read();
		tsize[0] = obj.read();
		int size = readInt(tsize);
		return size;
	}

	private Date buildDate(int recurStartTime, short year, int month,
			int dayOfMonth) {
		int h = (recurStartTime >> 12);
		int min = (recurStartTime - h * 4096) >> 6;
		Calendar cal = new GregorianCalendar(year, month, dayOfMonth, h, min, 0);
		Date date = cal.getTime();
		return date;
	}

	private int readDayOfMonth(InputStream obj) throws IOException {
		// D (1 byte): The Day of the month from the PidLidExceptionReplaceTime
		// property if the object
		// represents an exception; otherwise, zero.
		int dayOfMonth = obj.read();
		return dayOfMonth;
	}

	private int readMonth(InputStream obj) throws IOException {
		int m = readDayOfMonth(obj);
		int month = getMonth(m);
		return month;
	}

	private short readYear(InputStream obj) throws IOException {
		int[] yearBuf = new int[2];
		// YH (1 byte): The high-ordered byte of the 2-byte Year from the
		// PidLidExceptionReplaceTime
		// property if the object represents an exception; otherwise, zero.
		// YL (1 byte): The low-ordered byte of the 2-byte Year from the
		// PidLidExceptionReplaceTime
		// property if the object represents an exception; otherwise, zero.
		yearBuf[0] = obj.read();
		yearBuf[1] = obj.read();
		short year = readShort(yearBuf);
		return year;
	}

	private boolean isGlobalObjectId(InputStream obj) throws IOException {
		// Byte Array ID (16 bytes): An array of 16 bytes identifying this BLOB
		// as a Global Object ID.
		for (int i = 0; i < GLOBAL_OBJECT_ID_ARRAY_ID.length; i++) {
			if (GLOBAL_OBJECT_ID_ARRAY_ID[i] != obj.read()) {
				return false;
			}
		}
		return true;
	}

	private int getMonth(int m) {
		switch (m) {
		case 0:
		case 1:
			return Calendar.JANUARY;
		case 2:
			return Calendar.FEBRUARY;
		case 3:
			return Calendar.MARCH;
		case 4:
			return Calendar.APRIL;
		case 5:
			return Calendar.MAY;
		case 6:
			return Calendar.JUNE;
		case 7:
			return Calendar.JULY;
		case 8:
			return Calendar.AUGUST;
		case 9:
			return Calendar.SEPTEMBER;
		case 10:
			return Calendar.OCTOBER;
		case 11:
			return Calendar.NOVEMBER;
		case 12:
			return Calendar.DECEMBER;
		default:
			throw new IllegalArgumentException("out of bound value : " + m);
		}
	}

	public String getUid() {
		return uid;
	}

	public Date getRecurrenceId() {
		return recurrenceId;
	}

	private final short readShort(int[] t) throws IOException {
		int ch1 = t[0];
		int ch2 = t[1];
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	private final static int readInt(int[] t) throws IOException {

		if ((t[0] | t[1] | t[2] | t[3]) < 0)
			throw new EOFException();
		return ((t[0] << 24) + (t[1] << 16) + (t[2] << 8) + (t[3] << 0));
	}
}
