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

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.push.utils.DateUtils;


public class ItemSyncStateTest {
	
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionOnSyncKey() {
		ItemSyncState.builder().build();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionOnSyncDate() {
		ItemSyncState.builder()
			.syncKey(new SyncKey("123"))
			.build();
	}
	
	@Test
	public void testSyncStateBuilder() {
		SyncKey syncKey = new SyncKey("123");
		Date currentDate = DateUtils.getCurrentDate();
		int id = 1;
		
		ItemSyncState syncState = ItemSyncState.builder()
				.syncKey(syncKey)
				.syncDate(currentDate)
				.id(id)
				.syncFiltered(true)
				.build();
		
		assertThat(syncState.getSyncKey()).isEqualTo(syncKey);
		assertThat(syncState.getSyncDate()).isEqualTo(currentDate);
		assertThat(syncState.getId()).isEqualTo(id);
		assertThat(syncState.isSyncFiltered()).isTrue();
	}
	
	@Test
	public void testGetFilteredSyncDateSameFilterType() {
		SyncKey syncKey = new SyncKey("123");
		Date currentDate = DateUtils.getCurrentDate();
		int id = 1;
		
		ItemSyncState syncState = ItemSyncState.builder()
				.syncKey(syncKey)
				.syncDate(currentDate)
				.id(id)
				.syncFiltered(false)
				.build();
		
		Date filteredDate = syncState.getFilteredSyncDate(null);
		assertThat(filteredDate).isEqualTo(syncState.getSyncDate());
	}
	
	@Test
	public void testGetFilteredSyncDateDifferentFilterType() {
		SyncKey syncKey = new SyncKey("123");
		Date currentDate = new DateTime(DateUtils.getCurrentDate()).minusDays(2).toDate();
		Date expectedDate = new DateTime(DateUtils.getMidnightCalendar()).minusDays(1).toDate();
		int id = 1;
		
		ItemSyncState syncState = ItemSyncState.builder()
				.syncKey(syncKey)
				.syncDate(currentDate)
				.id(id)
				.syncFiltered(false)
				.build();
		
		Date filteredDate = syncState.getFilteredSyncDate(FilterType.ONE_DAY_BACK);
		assertThat(filteredDate).isEqualTo(expectedDate);
	}
}
