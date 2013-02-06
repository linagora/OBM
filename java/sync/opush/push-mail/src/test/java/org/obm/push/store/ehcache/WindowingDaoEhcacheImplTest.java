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
package org.obm.push.store.ehcache;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.SyncKey;
import org.obm.push.store.ehcache.WindowingDaoEhcacheImpl.WindowingIndex;

@RunWith(SlowFilterRunner.class)
public class WindowingDaoEhcacheImplTest {

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionIndexNegative() {
		new WindowingIndex(-1, new SyncKey("123"));
	}
	
	@Test
	public void testPreconditionIndexZero() {
		assertThat(new WindowingIndex(0, new SyncKey("123")).getIndex()).isEqualTo(0);
	}


	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionSyncKeyNull() {
		new WindowingIndex(-1, null);
	}

	@SuppressWarnings("unused")
	@Test(expected=IllegalArgumentException.class)
	public void testPreconditionSyncKeyInitial() {
		new WindowingIndex(-1, SyncKey.INITIAL_FOLDER_SYNC_KEY);
	}
	
	@Test
	public void testNextToBeStoredWhenIndexIsZero() {
		assertThat(new WindowingIndex(0, new SyncKey("123")).nextToBeStored())
			.isEqualTo(new WindowingIndex(1, new SyncKey("123")));
	}
	
	@Test
	public void testNextToBeStoredWhenIndexIsOne() {
		assertThat(new WindowingIndex(1, new SyncKey("123")).nextToBeStored())
			.isEqualTo(new WindowingIndex(2, new SyncKey("123")));
	}

	@Test
	public void testNextToBeStoredWhenIndexIsTenThousand() {
		assertThat(new WindowingIndex(10000, new SyncKey("123")).nextToBeStored())
			.isEqualTo(new WindowingIndex(10001, new SyncKey("123")));
	}

	@Test
	public void testNextToBeRetrievedWhenIndexIsZeroThousand() {
		assertThat(new WindowingIndex(0, new SyncKey("123")).nextToBeRetrieved()).isNull();
	}
	
	@Test
	public void testNextToBeRetrievedWhenIndexIsOneThousand() {
		assertThat(new WindowingIndex(1, new SyncKey("123")).nextToBeRetrieved())
			.isEqualTo(new WindowingIndex(0, new SyncKey("123")));
	}
	
	@Test
	public void testNextToBeRetrievedWhenIndexIsTenThousand() {
		assertThat(new WindowingIndex(10000, new SyncKey("123")).nextToBeRetrieved())
			.isEqualTo(new WindowingIndex(9999, new SyncKey("123")));
	}
	
}
