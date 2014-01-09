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
package org.obm.push.bean.msmeetingrequest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class MSMeetingRequestRecurrenceDayOfWeekTest {
	
	@Test
	public void testListOneDaySunday() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(1)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.SUNDAY);
	}

	@Test
	public void testListOneDayMonday() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(2)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.MONDAY);
	}

	@Test
	public void testListOneDayThuesday() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(4)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.TUESDAY);
	}

	@Test
	public void testListOneDayWednesday() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(8)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.WEDNESDAY);
	}

	@Test
	public void testListOneDayThursday() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(16)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.THURSDAY);
	}

	@Test
	public void testListOneDayFriday() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(32)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.FRIDAY);
	}

	@Test
	public void testListOneDaySaturday() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(64)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.SATURDAY);
	}

	@Test
	public void testListTwoMax() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(96)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.FRIDAY,
				MSMeetingRequestRecurrenceDayOfWeek.SATURDAY);
	}

	@Test
	public void testListTwoMin() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(3)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.SUNDAY,
				MSMeetingRequestRecurrenceDayOfWeek.MONDAY);
	}

	@Test
	public void testListMinMax() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(65)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.SUNDAY,
				MSMeetingRequestRecurrenceDayOfWeek.SATURDAY);
	}

	@Test
	public void testListAll() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(127)).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.SUNDAY,
				MSMeetingRequestRecurrenceDayOfWeek.MONDAY,
				MSMeetingRequestRecurrenceDayOfWeek.TUESDAY,
				MSMeetingRequestRecurrenceDayOfWeek.WEDNESDAY,
				MSMeetingRequestRecurrenceDayOfWeek.THURSDAY,
				MSMeetingRequestRecurrenceDayOfWeek.FRIDAY,
				MSMeetingRequestRecurrenceDayOfWeek.SATURDAY);
	}
	
	@Test
	public void testListNull() {
		assertThat(MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(null)).isEmpty();
	}
}
