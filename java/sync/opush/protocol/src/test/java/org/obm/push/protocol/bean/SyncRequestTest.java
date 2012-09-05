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
package org.obm.push.protocol.bean;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;

@RunWith(SlowFilterRunner.class)
public class SyncRequestTest {

	@Test
	public void testBuilderWaitIsNotRequired() {
		SyncRequest syncRequest = new SyncRequest.Builder().build();
		
		assertThat(syncRequest.getWaitInMinute()).isNull();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWaitNegative() {
		new SyncRequest.Builder().waitInMinute(-1).build();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWaitMoreThanValid() {
		new SyncRequest.Builder().waitInMinute(60).build();
	}

	@Test
	public void testBuilderWaitValid() {
		SyncRequest syncRequest = new SyncRequest.Builder().waitInMinute(58).build();
		
		assertThat(syncRequest.getWaitInMinute()).isEqualTo(58);
	}

	@Test
	public void testBuilderPartialIsNotRequired() {
		SyncRequest syncRequest = new SyncRequest.Builder().build();
		
		assertThat(syncRequest.isPartial()).isNull();
	}

	@Test
	public void testBuilderPartialTrue() {
		SyncRequest syncRequest = new SyncRequest.Builder().partial(true).build();
		
		assertThat(syncRequest.isPartial()).isTrue();
	}

	@Test
	public void testBuilderPartialFalse() {
		SyncRequest syncRequest = new SyncRequest.Builder().partial(false).build();
		
		assertThat(syncRequest.isPartial()).isFalse();
	}
	@Test
	public void testBuilderWindowSizeIsNotRequired() {
		SyncRequest syncRequest = new SyncRequest.Builder().build();
		
		assertThat(syncRequest.getWindowSize()).isNull();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWindowSizeZero() {
		new SyncRequest.Builder().windowSize(0).build();
	}

	@Test(expected=ASRequestIntegerFieldException.class)
	public void testBuilderWindowSizeMoreThanValid() {
		new SyncRequest.Builder().windowSize(513).build();
	}

	@Test
	public void testBuilderWindowSizeValid() {
		SyncRequest syncRequest = new SyncRequest.Builder().windowSize(511).build();
		
		assertThat(syncRequest.getWindowSize()).isEqualTo(511);
	}
}
