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
package org.obm.push.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.util.Date;

import org.junit.Test;


public class FilterTypeTest {

	@Test
	public void testSpecificationValue() {
		assertThat(FilterType.fromSpecificationValue("0")).isEqualTo(FilterType.ALL_ITEMS);
		assertThat(FilterType.fromSpecificationValue("1")).isEqualTo(FilterType.ONE_DAY_BACK);
		assertThat(FilterType.fromSpecificationValue("2")).isEqualTo(FilterType.THREE_DAYS_BACK);
		assertThat(FilterType.fromSpecificationValue("3")).isEqualTo(FilterType.ONE_WEEK_BACK);
		assertThat(FilterType.fromSpecificationValue("4")).isEqualTo(FilterType.TWO_WEEKS_BACK);
		assertThat(FilterType.fromSpecificationValue("5")).isEqualTo(FilterType.ONE_MONTHS_BACK);
		assertThat(FilterType.fromSpecificationValue("6")).isEqualTo(FilterType.THREE_MONTHS_BACK);
		assertThat(FilterType.fromSpecificationValue("7")).isEqualTo(FilterType.SIX_MONTHS_BACK);
		assertThat(FilterType.fromSpecificationValue("8")).isEqualTo(FilterType.FILTER_BY_NO_INCOMPLETE_TASKS);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSpecificationValueForInvalidArgumentExceed() {
		FilterType.fromSpecificationValue("9");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSpecificationValueForInvalidArgumentNull() {
		FilterType.fromSpecificationValue(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSpecificationValueForInvalidArgumentEmpty() {
		FilterType.fromSpecificationValue("");
	}
	
	@Test
	public void testGetFilterdDateForAllItems() {
		Date date = date("2009-06-08T16:22:53+00");
		assertThat(FilterType.ALL_ITEMS.getFilteredDate(date)).isEqualTo(date("1970-01-01T00:00:01+00"));
	}

	@Test
	public void testGetFilterdDateForOneDayBack() {
		Date date = date("2009-06-08T16:22:53+00");
		assertThat(FilterType.ONE_DAY_BACK.getFilteredDate(date)).isEqualTo(date("2009-06-07T16:22:53+00"));
	}
	
	@Test
	public void testGetFilterdDateForThreeDayBack() {
		Date date = date("2009-06-08T16:22:53+00");
		assertThat(FilterType.THREE_DAYS_BACK.getFilteredDate(date)).isEqualTo(date("2009-06-05T16:22:53+00"));
	}
	
	@Test
	public void testGetFilterdDateForOneWeekBack() {
		Date date = date("2009-06-08T16:22:53+00");
		assertThat(FilterType.ONE_WEEK_BACK.getFilteredDate(date)).isEqualTo(date("2009-06-01T16:22:53+00"));
	}
	
	@Test
	public void testGetFilterdDateForTwoWeeksBack() {
		Date date = date("2009-06-21T16:22:53+00");
		assertThat(FilterType.TWO_WEEKS_BACK.getFilteredDate(date)).isEqualTo(date("2009-06-07T16:22:53+00"));
	}
	
	@Test
	public void testGetFilterdDateForOneMonthBack() {
		Date date = date("2009-06-08T16:22:53+00");
		assertThat(FilterType.ONE_MONTHS_BACK.getFilteredDate(date)).isEqualTo(date("2009-05-08T16:22:53+00"));
	}
	
	@Test
	public void testGetFilterdDateForThreeMonthsBack() {
		Date date = date("2009-06-08T16:22:53+00");
		assertThat(FilterType.THREE_MONTHS_BACK.getFilteredDate(date)).isEqualTo(date("2009-03-08T16:22:53+00"));
	}
	
	@Test
	public void testGetFilterdDateForSixMonthsBack() {
		Date date = date("2009-06-08T16:22:53+00");
		assertThat(FilterType.SIX_MONTHS_BACK.getFilteredDate(date)).isEqualTo(date("2008-12-08T16:22:53+00"));
	}
	
}
