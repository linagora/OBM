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
package org.obm.push.handler;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.ContentsExporter;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.protocol.bean.Estimate;
import org.obm.push.state.StateMachine;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.DateUtils;


public class GetItemEstimateHandlerTest {
	
	private IMocksControl control;
	
	private UserDataRequest udr;
	private Device device;

	private StateMachine stMachine;
	private IContentsExporter contentsExporter;
	private UnsynchronizedItemDao unSynchronizedItemCache;

	private GetItemEstimateHandler testee;



	@Before
	public void init() {
		control = createControl();
		
		device = new Device.Factory().create(null, "MultipleCalendarsDevice", "iOs 5", new DeviceId("my phone"), null);
		udr = new UserDataRequest(null,  null, device);
		
		contentsExporter = control.createMock(ContentsExporter.class);
		stMachine = control.createMock(StateMachine.class);
		unSynchronizedItemCache = control.createMock(UnsynchronizedItemDao.class);
		
		testee = new GetItemEstimateHandler(null, null, null, contentsExporter, stMachine, unSynchronizedItemCache, null, null, null, null, null);
		
	}
	
	@Test
	public void testComputeEstimateWithFilterTypeChangedException() throws Exception {
		SyncKey syncKey = new SyncKey("1234");
		int collectionId = 2;
		ItemSyncState syncState = ItemSyncState.builder()
				.syncKey(syncKey)
				.syncDate(DateUtils.getCurrentDate())
				.build();
		
		AnalysedSyncCollection syncCollection = AnalysedSyncCollection.builder()
				.collectionId(collectionId)
				.dataType(PIMDataType.EMAIL)
				.syncKey(syncKey)
				.build();

		expect(stMachine.getItemSyncState(syncKey))
			.andReturn(syncState);
		expect(contentsExporter.getItemEstimateSize(udr, PIMDataType.EMAIL, syncCollection, syncState))
			.andThrow(new FilterTypeChangedException(collectionId, FilterType.THREE_DAYS_BACK, FilterType.ONE_MONTHS_BACK));
		expect(unSynchronizedItemCache.listItemsToAdd(syncKey))
			.andReturn(Collections.EMPTY_SET);
		
		control.replay();
		SyncCollectionResponse.Builder builder = SyncCollectionResponse.builder()
				.collectionId(collectionId);
		Estimate estimate = testee.computeEstimate(udr, syncCollection, PIMDataType.EMAIL, builder);
		control.verify();
		
		assertThat(estimate.getCollection().getStatus()).isEqualTo(SyncStatus.INVALID_SYNC_KEY);
	}
}
