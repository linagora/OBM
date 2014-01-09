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
package org.obm.push.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEventException;
import org.obm.sync.calendar.Event;


public class ObmEventToMsEventConverterRecurrenceTest {

	private ObmEventToMSEventConverterImpl converter;

	@Before
	public void setUp() {
		converter = new ObmEventToMSEventConverterImpl();
	}

	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndGMT() {
		Date realEventStartTime = date("2004-01-10T00:13:12+00");
		Date dbExceptionDate = date("2004-01-17T00:00:00+00");
		Date realExceptionDate = date("2004-01-17T00:13:12+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("GMT");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndGMT() {
		Date realEventStartTime = date("2004-01-10T23:13:12+00");
		Date dbExceptionDate = date("2004-01-17T00:00:00+00");
		Date realExceptionDate = date("2004-01-17T23:13:12+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("GMT");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndParisWinterTz() {
		Date realEventStartTime = date("2004-12-13T01:13:12+00");
		Date dbExceptionDate = date("2004-12-19T23:00:00+00");
		Date realExceptionDate = date("2004-12-20T01:13:12+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndParisWinterTz() {
		Date realEventStartTime = date("2004-12-13T22:13:12+00");
		Date dbExceptionDate = date("2004-12-19T23:00:00+00");
		Date realExceptionDate = date("2004-12-20T22:13:12+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndParisSummerTz() {
		Date realEventStartTime = date("2005-05-13T00:13:12+00");
		Date dbExceptionDate = date("2005-05-19T22:00:00+00");
		Date realExceptionDate = date("2005-05-20T00:13:12+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndParisSummerTz() {
		Date realEventStartTime = date("2005-05-13T21:13:12+00");
		Date dbExceptionDate = date("2005-05-19T22:00:00+00");
		Date realExceptionDate = date("2005-05-20T21:13:12+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndParisTzCreatedWinterButExcepSummer() {
		Date realEventStartTime = date("2005-01-13T00:13:12+00");
		Date dbExceptionDate = date("2005-05-19T22:00:00+00");
		Date realExceptionDate = date("2005-05-19T23:13:12+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndParisTzCreatedWinterButExcepSummer() {
		Date realEventStartTime = date("2005-01-13T22:15:34+00");
		Date dbExceptionDate = date("2005-05-19T22:00:00+00");
		Date realExceptionDate = date("2005-05-20T21:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndParisTzCreatedSummerButExcepWinter() {
		Date realEventStartTime = date("2005-05-13T00:15:34+00");
		Date dbExceptionDate = date("2005-12-19T23:00:00+00");
		Date realExceptionDate = date("2005-12-20T01:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndParisTzCreatedSummerButExcepWinter() {
		Date realEventStartTime = date("2005-05-13T21:15:34+00");
		Date dbExceptionDate = date("2005-12-19T23:00:00+00");
		Date realExceptionDate = date("2005-12-20T22:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndSaoPauloWinterTz() {
		Date realEventStartTime = date("2004-08-13T03:15:34+00");
		Date dbExceptionDate = date("2004-08-20T03:00:00+00");
		Date realExceptionDate = date("2004-08-20T03:15:34+00");

		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndSaoPauloWinterTz() {
		Date realEventStartTime = date("2004-08-14T02:15:34+00");
		Date dbExceptionDate = date("2004-08-20T03:00:00+00");
		Date realExceptionDate = date("2004-08-21T02:15:34+00");

		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndSaoPauloSummerTz() {
		Date realEventStartTime = date("2005-01-13T02:22:30+00");
		Date dbExceptionDate = date("2005-01-20T02:00:00+00");
		Date realExceptionDate = date("2005-01-20T02:22:30+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndSaoPauloSummerTz() {
		Date realEventStartTime = date("2005-01-14T01:22:30+00");
		Date dbExceptionDate = date("2005-01-20T02:00:00+00");
		Date realExceptionDate = date("2005-01-21T01:22:30+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndSaoPauloTzCreatedWinterButExcepSummer() {
		Date realEventStartTime = date("2005-08-13T03:22:30+00");
		Date dbExceptionDate = date("2005-01-20T02:00:00+00");
		Date realExceptionDate = date("2005-01-20T02:22:30+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndSaoPauloTzCreatedWinterButExcepSummer() {
		Date realEventStartTime = date("2005-08-14T02:22:30+00");
		Date dbExceptionDate = date("2005-01-20T02:00:00+00");
		Date realExceptionDate = date("2005-01-21T01:22:30+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndSaoPauloTzCreatedSummerButExcepWinter() {
		Date realEventStartTime = date("2005-01-13T02:22:30+00");
		Date dbExceptionDate = date("2005-08-20T03:00:00+00");
		Date realExceptionDate = date("2005-08-20T03:22:30+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndSaoPauloTzCreatedSummerButExcepWinter() {
		Date realEventStartTime = date("2005-01-14T01:22:30+00");
		Date dbExceptionDate = date("2005-08-20T03:00:00+00");
		Date realExceptionDate = date("2005-08-21T02:22:30+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndTurkeyTz() {
		Date realEventStartTime = date("2004-01-09T23:15:34+00");
		Date dbExceptionDate = date("2004-01-16T22:00:00+00");
		Date realExceptionDate = date("2004-01-16T23:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndTurkeyTz() {
		Date realEventStartTime = date("2004-01-10T21:15:34+00");
		Date dbExceptionDate = date("2004-01-16T22:00:00+00");
		Date realExceptionDate = date("2004-01-17T21:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndTurkeyTzFirstDayOfYear() {
		Date realEventStartTime = date("2004-12-30T23:15:34+00");
		Date dbExceptionDate = date("2004-12-31T22:00:00+00");
		Date realExceptionDate = date("2004-12-31T23:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndTurkeyTzFirstDayOfYear() {
		Date realEventStartTime = date("2004-12-30T21:15:34+00");
		Date dbExceptionDate = date("2004-12-31T22:00:00+00");
		Date realExceptionDate = date("2005-01-01T21:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenEarlyEventAndTurkeyTzLastDayOfYear() {
		Date realEventStartTime = date("2004-12-30T23:15:34+00");
		Date dbExceptionDate = date("2004-12-30T22:00:00+00");
		Date realExceptionDate = date("2004-12-30T23:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenNightlyEventAndTurkeyTzLastDayOfYear() {
		Date realEventStartTime = date("2004-12-30T21:15:34+00");
		Date dbExceptionDate = date("2004-12-30T22:00:00+00");
		Date realExceptionDate = date("2004-12-31T21:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenMightnightEventAndParisTz() {
		Date realEventStartTime = date("2004-12-13T23:15:34+00");
		Date dbExceptionDate = date("2004-12-20T23:00:00+00");
		Date realExceptionDate = date("2004-12-20T23:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenFirstOccurenceAndParisTz() {
		Date realEventStartTime = date("2004-12-13T11:15:34+00");
		Date dbExceptionDate = date("2004-12-12T23:00:00+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realEventStartTime);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenFirstOccurenceAndTurkeyTz() {
		Date realEventStartTime = date("2004-12-13T11:15:34+00");
		Date dbExceptionDate = date("2004-12-12T22:00:00+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realEventStartTime);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenFirstOccurenceAndSaoPauloTz() {
		Date realEventStartTime = date("2004-12-13T11:15:34+00");
		Date dbExceptionDate = date("2004-12-13T03:00:00+00");

		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realEventStartTime);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenOtherOccurenceAndTurkeyTz() {
		Date realEventStartTime = date("2004-12-13T11:15:34+00");
		Date dbExceptionDate = date("2004-12-19T22:00:00+00");
		Date realExceptionDate = date("2004-12-20T11:15:34+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(false);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenAllDayAndParisTz() {
		Date realEventStartTime = date("2004-12-12T23:22:30+00");
		Date dbExceptionDate = date("2004-12-12T23:00:00+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(true);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realEventStartTime);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}

	@Test
	public void testConvertDeletedExcetionWhenSecondOccurrenceAllDayAndParisTz() {
		Date realEventStartTime = date("2004-12-12T23:22:30+00");
		Date dbExceptionDate = date("2004-12-19T23:00:00+00");
		Date realExceptionDate = date("2004-12-19T23:22:30+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(true);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Europe/Paris");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realExceptionDate);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenAllDayAndTurkeyTz() {
		Date realEventStartTime = date("2004-12-12T22:22:30+00");
		Date dbExceptionDate = date("2004-12-12T22:00:00+00");
		
		Event dbEvent = new Event();
		dbEvent.setAllday(true);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("Turkey");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realEventStartTime);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
	
	@Test
	public void testConvertDeletedExcetionWhenAllDayAndSaoPauloTz() {
		Date realEventStartTime = date("2004-12-13T03:22:30+00");
		Date dbExceptionDate = date("2004-12-13T03:00:00+00");

		Event dbEvent = new Event();
		dbEvent.setAllday(true);
		dbEvent.setStartDate(realEventStartTime);
		dbEvent.setTimezoneName("America/Sao_Paulo");
		
		MSEventException expectedException = new MSEventException();
		expectedException.setDeleted(true);
		expectedException.setExceptionStartTime(realEventStartTime);
		
		MSEventException deletionException = converter.deletionException(dbEvent, dbExceptionDate);
		assertThat(deletionException).isEqualTo(expectedException);
	}
}
