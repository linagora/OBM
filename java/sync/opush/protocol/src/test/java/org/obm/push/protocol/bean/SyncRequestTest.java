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
package org.obm.push.protocol.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.bean.SyncCollectionRequest;
import org.obm.push.bean.SyncDefaultValues;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;


public class SyncRequestTest implements SyncDefaultValues {

	@Test
	public void testBuilderWaitIsNotRequired() {
		SyncRequest syncRequest = SyncRequest.builder().build();
		
		assertThat(syncRequest.getWaitInMinute()).isEqualTo(DEFAULT_WAIT);
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWaitNegative() {
		SyncRequest.builder().waitInMinute(-1).build();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWaitMoreThanValid() {
		SyncRequest.builder().waitInMinute(60).build();
	}

	@Test
	public void testBuilderWaitValid() {
		SyncRequest syncRequest = SyncRequest.builder().waitInMinute(58).build();
		
		assertThat(syncRequest.getWaitInMinute()).isEqualTo(58);
	}

	@Test
	public void testBuilderPartialIsNotRequired() {
		SyncRequest syncRequest = SyncRequest.builder().build();
		
		assertThat(syncRequest.isPartial()).isNull();
	}

	@Test
	public void testBuilderPartialTrue() {
		SyncRequest syncRequest = SyncRequest.builder().partial(true).build();
		
		assertThat(syncRequest.isPartial()).isTrue();
	}

	@Test
	public void testBuilderPartialFalse() {
		SyncRequest syncRequest = SyncRequest.builder().partial(false).build();
		
		assertThat(syncRequest.isPartial()).isFalse();
	}
	@Test
	public void testBuilderWindowSizeIsNotRequired() {
		SyncRequest syncRequest = SyncRequest.builder().build();
		
		assertThat(syncRequest.getWindowSize()).isEqualTo(DEFAULT_WINDOW_SIZE);
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWindowSizeZero() {
		SyncRequest.builder().windowSize(0).build();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWindowSizeMoreThanValid() {
		SyncRequest.builder().windowSize(513).build();
	}

	@Test
	public void testBuilderWindowSizeValid() {
		SyncRequest syncRequest = SyncRequest.builder().windowSize(511).build();
		
		assertThat(syncRequest.getWindowSize()).isEqualTo(511);
	}

	@Test
	public void testBuilderCollectionsByDefault() {
		SyncRequest syncRequest = SyncRequest.builder().build();
		
		assertThat(syncRequest.getCollections()).isEmpty();
	}

	@Test
	public void testBuilderCollectionsNonEmpty() {
		SyncRequest syncRequest = SyncRequest.builder()
				.addCollection(SyncCollectionRequest.builder()
						.collectionId(1)
						.syncKey(new SyncKey("1234"))
						.build())
				.build();
		
		assertThat(syncRequest.getCollections()).hasSize(1);
	}
	
	@Test
	public void testWaitNullIsZero() {
		SyncRequest syncRequest = SyncRequest.builder().waitInMinute(null).build();
		assertThat(syncRequest.getWaitInSecond()).isEqualTo(0);
	}

	@Test
	public void testWaitZeroIsZero() {
		SyncRequest syncRequest = SyncRequest.builder().waitInMinute(0).build();
		assertThat(syncRequest.getWaitInSecond()).isEqualTo(0);
	}

	@Test
	public void testWaitMinutesToSeconds() {
		SyncRequest syncRequest = SyncRequest.builder().waitInMinute(1).build();
		assertThat(syncRequest.getWaitInSecond()).isEqualTo(60);
	}
}
