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
package org.obm.sync.calendar;

import java.util.Collection;
import java.util.EnumSet;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class RecurrenceDaysTest {

	@Test
	public void testNonNullInput() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Tuesday,
				RecurrenceDay.Wednesday);
		Assertions.assertThat(recurrenceDays).containsOnly(RecurrenceDay.Tuesday,
				RecurrenceDay.Wednesday);
	}

	@Test
	public void testNullVarArgs() {
		RecurrenceDay[] daysAr = null;
		RecurrenceDays recurrenceDays = new RecurrenceDays(daysAr);
		Assertions.assertThat(recurrenceDays.isEmpty());
	}

	@Test
	public void testNullCollection() {
		Collection<RecurrenceDay> daysColl = null;
		RecurrenceDays recurrenceDays = new RecurrenceDays(daysColl);
		Assertions.assertThat(recurrenceDays.isEmpty());
	}

	@Test
	public void testDefaultConstructor() {
		RecurrenceDays recurrenceDays = new RecurrenceDays();
		Assertions.assertThat(recurrenceDays.isEmpty());
	}

	@Test
	public void testSizeWithEmpty() {
		RecurrenceDays emptyRecurrenceDays = new RecurrenceDays();
		Assertions.assertThat(emptyRecurrenceDays).hasSize(0);
	}

	@Test
	public void testSizeWithNonEmpty() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Tuesday,
				RecurrenceDay.Wednesday, RecurrenceDay.Friday);
		Assertions.assertThat(recurrenceDays).hasSize(3);
	}

	@Test
	public void testIsEmptyWithEmpty() {
		RecurrenceDays emptyRecurrenceDays = new RecurrenceDays();
		Assertions.assertThat(emptyRecurrenceDays.isEmpty()).isTrue();
	}

	@Test
	public void testIsEmptyWithNotEmpty() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Saturday);
		Assertions.assertThat(recurrenceDays.isEmpty()).isFalse();
	}

	@Test
	public void testContainsShouldSucceed() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Saturday,
				RecurrenceDay.Thursday);
		Assertions.assertThat(recurrenceDays.contains(RecurrenceDay.Saturday)).isTrue();
	}

	@Test
	public void testContainsShouldFail() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Saturday,
				RecurrenceDay.Thursday);
		Assertions.assertThat(recurrenceDays.contains(RecurrenceDay.Monday)).isFalse();
	}

	@Test
	public void testIteratorWithoutDuplicates() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Saturday,
				RecurrenceDay.Thursday);
		Assertions.assertThat(recurrenceDays).isNotNull()
				.containsOnly(RecurrenceDay.Saturday, RecurrenceDay.Thursday);
	}

	@Test
	public void testIteratorWithDuplicates() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Saturday,
				RecurrenceDay.Thursday, RecurrenceDay.Saturday);
		Assertions.assertThat(recurrenceDays).isNotNull()
				.containsOnly(RecurrenceDay.Saturday, RecurrenceDay.Thursday);
		Assertions.assertThat(recurrenceDays).doesNotHaveDuplicates();
	}

	@Test
	public void testToArrayWithoutParameter() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Saturday,
				RecurrenceDay.Thursday, RecurrenceDay.Saturday);
		Object[] daysAr = recurrenceDays.toArray();
		Assertions.assertThat(daysAr).hasSize(2);
		Assertions.assertThat(daysAr).containsOnly(RecurrenceDay.Saturday, RecurrenceDay.Thursday);
	}

	@Test
	public void testToArrayWithParameter() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Saturday,
				RecurrenceDay.Thursday, RecurrenceDay.Saturday);
		RecurrenceDay[] daysAr = new RecurrenceDay[2];
		recurrenceDays.toArray(daysAr);
		Assertions.assertThat(daysAr).containsOnly(RecurrenceDay.Saturday, RecurrenceDay.Thursday);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAdd() {
		RecurrenceDays recurrenceDays = new RecurrenceDays();
		recurrenceDays.add(RecurrenceDay.Monday);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemove() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Monday);
		recurrenceDays.remove(RecurrenceDay.Monday);
	}

	@Test
	public void testContainsAllShouldSucceed() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Monday,
				RecurrenceDay.Friday);
		Assertions.assertThat(
				recurrenceDays.containsAll(EnumSet.of(RecurrenceDay.Monday, RecurrenceDay.Friday)))
				.isTrue();
	}

	@Test
	public void testContainsAllShouldFail() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Monday,
				RecurrenceDay.Friday);
		Assertions.assertThat(
				recurrenceDays.containsAll(EnumSet.of(RecurrenceDay.Monday, RecurrenceDay.Friday,
						RecurrenceDay.Sunday))).isFalse();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddAll() {
		RecurrenceDays recurrenceDays = new RecurrenceDays();
		recurrenceDays.addAll(EnumSet.of(RecurrenceDay.Monday, RecurrenceDay.Friday,
				RecurrenceDay.Sunday));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRetainAll() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Monday,
				RecurrenceDay.Friday, RecurrenceDay.Sunday);
		recurrenceDays.retainAll(EnumSet.of(RecurrenceDay.Monday, RecurrenceDay.Friday));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemoveAll() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Monday,
				RecurrenceDay.Friday, RecurrenceDay.Sunday);
		recurrenceDays.removeAll(EnumSet.of(RecurrenceDay.Monday, RecurrenceDay.Sunday));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testClear() {
		RecurrenceDays recurrenceDays = new RecurrenceDays(RecurrenceDay.Monday,
				RecurrenceDay.Friday, RecurrenceDay.Sunday);
		recurrenceDays.clear();
	}
}
