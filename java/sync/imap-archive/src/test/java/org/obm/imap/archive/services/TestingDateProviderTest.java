/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.Date;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.utils.ObmHelper;

public class TestingDateProviderTest {

	private IMocksControl control;
	
	private ObmHelper obmHelper;
	private TestingDateProvider testee;
	
	@Before
	public void setup() {
		control = createControl();
		
		obmHelper = control.createMock(ObmHelper.class);
		testee = new TestingDateProvider(obmHelper);
	}
	
	@Test
	public void getDateShouldReturnDateFromObmHelperWhenDefaultDuration() {
		Date expectedDate = DateTime.parse("2014-08-27T12:18:00.000Z").toDate();
		expect(obmHelper.getDate())
			.andReturn(expectedDate);
		
		control.replay();
		Date date = testee.getDate();
		control.verify();
		
		assertThat(date).isEqualTo(expectedDate);
	}
	
	@Test
	public void getDateShouldReturnUpdatedDateWhenDurationHasBeenSet() {
		DateTime dateTime = DateTime.parse("2014-08-17T12:18:00.000Z");
		Date currentDate = dateTime.toDate();
		expect(obmHelper.getDate())
			.andReturn(currentDate).times(2);
		
		Date expectedDate = dateTime.plusDays(10).toDate();
		
		control.replay();
		testee.setReferenceDate(DateTime.parse("2014-08-27T12:18:00.000Z"));
		Date date = testee.getDate();
		control.verify();
		
		assertThat(date).isEqualTo(expectedDate);
	}
	
	@Test
	public void getDateShouldReturnUpdatedDateWhenDurationHasBeenSetAndIsNegative() {
		DateTime dateTime = DateTime.parse("2014-08-17T12:18:00.000Z");
		Date currentDate = dateTime.toDate();
		expect(obmHelper.getDate())
			.andReturn(currentDate).times(2);
		
		Date expectedDate = dateTime.minusDays(10).toDate();
		
		control.replay();
		testee.setReferenceDate(DateTime.parse("2014-08-07T12:18:00.000Z"));
		Date date = testee.getDate();
		control.verify();
		
		assertThat(date).isEqualTo(expectedDate);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void setReferenceDateShouldThrowWhenNullArgument() {
		testee.setReferenceDate(null);
	}
}
