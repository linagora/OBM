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

package org.obm.push.protocol.data;

import static org.easymock.EasyMock.createMock;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GregorianChronology;
import org.junit.Before;
import org.junit.Test;


public class DecoderTest {
	
	private Decoder decoder;
	private Base64ASTimeZoneDecoder base64AsTimeZoneDecoder;
	private ASTimeZoneConverter asTimeZoneConverter;
	
	@Before
	public void setUp() {
		base64AsTimeZoneDecoder = createMock(Base64ASTimeZoneDecoder.class);
		asTimeZoneConverter = createMock(ASTimeZoneConverter.class);
		decoder = new CalendarDecoder(base64AsTimeZoneDecoder, asTimeZoneConverter);
	}

	@Test
	public void testParseUnmatchDate() {
		Date date = decoder.parseDate("unmatch");
		assertThat(date).isNull();
	}

	@Test
	public void testParseDateWithMillis() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.UTC);
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2012, 6, 8, 10, 33, 52, 256));
		
		Date date = decoder.parseDate("2012-06-08T10:33:52.256Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}

	@Test
	public void testParseDateWitouthMillis() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.UTC);
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2012, 6, 8, 10, 33, 52, 0));
		
		Date date = decoder.parseDate("20120608T103352Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}

	@Test
	public void testParseDateWitTimeZone() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.forID("Pacific/Auckland"));
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2012, 6, 8, 22, 33, 52, 0));
		
		Date date = decoder.parseDate("20120608T103352Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}

	@Test
	public void testUnknownMonth() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.UTC);
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2013, 10, 10, 10, 33, 52, 0));
		
		Date date = decoder.parseDate("20122210T103352Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}

	@Test
	public void testUnknownDay() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.UTC);
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2012, 3, 2, 10, 33, 52, 0));
		
		Date date = decoder.parseDate("20120231T103352Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}

	@Test
	public void testUnknownHour() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.UTC);
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2012, 2, 11, 10, 33, 52, 0));
		
		Date date = decoder.parseDate("20120210T343352Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}

	@Test
	public void testUnknownMinute() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.UTC);
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2012, 2, 10, 11, 33, 52, 0));
		
		Date date = decoder.parseDate("20120210T109352Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}

	@Test
	public void testUnknownSecond() {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(DateTimeZone.UTC);
		Date expectedDate = new Date(gregorianChronology.getDateTimeMillis(2012, 2, 10, 10, 34, 32, 0));
		
		Date date = decoder.parseDate("20120210T103392Z");
		assertThat(date).isNotNull().isEqualTo(expectedDate);
	}
}
