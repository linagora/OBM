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
package org.obm.push;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.protocol.bean.AnalysedSyncRequest;
import org.obm.push.protocol.bean.Estimate;
import org.obm.push.protocol.bean.FolderSyncRequest;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.protocol.bean.GetItemEstimateRequest;
import org.obm.push.protocol.bean.GetItemEstimateResponse;
import org.obm.push.protocol.bean.ItemChangeMeetingResponse;
import org.obm.push.protocol.bean.MeetingHandlerRequest;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.bean.MoveItemsItem;
import org.obm.push.protocol.bean.MoveItemsRequest;
import org.obm.push.protocol.bean.MoveItemsResponse;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.protocol.bean.ProvisionRequest;
import org.obm.push.protocol.bean.ProvisionResponse;
import org.obm.push.protocol.bean.SearchRequest;
import org.obm.push.protocol.bean.SearchResponse;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.request.SendEmailSyncRequest;
import org.obm.sync.bean.EqualsVerifierUtils;
import org.obm.sync.bean.EqualsVerifierUtils.EqualsVerifierBuilder;

import com.google.common.collect.ImmutableList;


public class BeansTest {

	private EqualsVerifierUtils equalsVerifierUtilsTest;
	
	@Before
	public void init() {
		equalsVerifierUtilsTest = new EqualsVerifierUtils();
	}
	
	@Test
	public void test() {
		ImmutableList<Class<?>> list = 
				ImmutableList.<Class<?>>builder()
					.add(ASSystemTime.class)
					.add(ASTimeZone.class)
					.add(PingRequest.class)
					.add(PingResponse.class)
					.add(FolderSyncRequest.class)
					.add(FolderSyncResponse.class)
					.add(ProvisionRequest.class)
					.add(ProvisionResponse.class)
					.add(GetItemEstimateRequest.class)
					.add(GetItemEstimateResponse.class)
					.add(MeetingHandlerRequest.class)
					.add(MeetingHandlerResponse.class)
					.add(ItemChangeMeetingResponse.class)
					.add(MoveItemsRequest.class)
					.add(MoveItemsResponse.class)
					.add(MoveItemsItem.class)
					.add(SearchRequest.class)
					.add(SearchResponse.class)
					.add(SyncRequest.class)
					.add(AnalysedSyncRequest.class)
					.add(SendEmailSyncRequest.class)
					.build();
		equalsVerifierUtilsTest.test(list);
		
		EqualsVerifierBuilder.builder()
		.equalsVerifiers(ImmutableList.<Class<?>>of(Estimate.class))
		.prefabValue(SyncCollectionResponse.class,
				SyncCollectionResponse.builder()
					.collectionId(1)
					.dataType(PIMDataType.EMAIL)
					.status(SyncStatus.OK)
					.syncKey(new SyncKey("123"))
					.build(),
				SyncCollectionResponse.builder()
					.collectionId(2)
					.dataType(PIMDataType.EMAIL)
					.status(SyncStatus.OK)
					.syncKey(new SyncKey("456"))
					.build())
		.verify();
	}
	
}
