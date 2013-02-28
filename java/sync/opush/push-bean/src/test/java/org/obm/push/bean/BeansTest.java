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
package org.obm.push.bean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.autodiscover.AutodiscoverRequest;
import org.obm.push.bean.autodiscover.AutodiscoverResponse;
import org.obm.push.bean.autodiscover.AutodiscoverResponseError;
import org.obm.push.bean.autodiscover.AutodiscoverResponseServer;
import org.obm.push.bean.autodiscover.AutodiscoverResponseUser;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.ms.MSEmailMetadata;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestCategory;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.sync.bean.EqualsVerifierUtils;
import org.obm.sync.bean.EqualsVerifierUtils.EqualsVerifierBuilder;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
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
					.add(Address.class)
					.add(BodyPreference.class)
					.add(ChangedCollections.class)
					.add(Credentials.class)
					.add(Device.class)
					.add(CollectionChange.class)
					.add(CollectionDeletion.class)
					.add(ItemChange.class)
					.add(ItemDeletion.class)
					.add(MeetingResponse.class)
					.add(MoveItem.class)
					.add(MSAddress.class)
					.add(MSAttachement.class)
					.add(MSAttachementData.class)
					.add(MSAttendee.class)
					.add(MSEmail.class)
					.add(MSContact.class)
					.add(MSEmailBody.class)
					.add(MSEvent.class)
					.add(MSEventException.class)
					.add(MSTask.class)
					.add(MSRecurrence.class)
					.add(SearchResult.class)
					.add(ServerId.class)
					.add(Sync.class)
					.add(SyncCollection.class)
					.add(SyncCollectionChange.class)
					.add(SyncCollectionOptions.class)
					.add(ItemSyncState.class)
					.add(MSEventUid.class)
					.add(User.class)
					.add(AutodiscoverRequest.class)
					.add(AutodiscoverResponse.class)
					.add(AutodiscoverResponseError.class)
					.add(AutodiscoverResponseServer.class)
					.add(AutodiscoverResponseUser.class)
					.add(MSMeetingRequest.class)
					.add(MSMeetingRequestRecurrence.class)
					.add(MSMeetingRequestCategory.class)
					.add(MSEmailHeader.class)
					.add(MSEmailMetadata.class)
					.add(MSEventExtId.class)
					.add(DeviceId.class)
					.add(SyncKey.class)
					.add(SyncKeysKey.class)
					.add(FolderSyncState.class)
					.add(MoveItem.class)
					.add(SyncClientCommands.class)
					.add(SyncClientCommands.Add.class)
					.add(DataDelta.class)
					.build();
		equalsVerifierUtilsTest.test(list);
	}
	
	@Test
	public void testClassWithCharsetField() {
		ImmutableList<Class<?>> list = ImmutableList.<Class<?>>builder()
					.add(org.obm.push.bean.ms.MSEmail.class)
					.add(org.obm.push.bean.ms.MSEmailBody.class)
					.build();
		
		EqualsVerifierBuilder.builder()
					.equalsVerifiers(list)
					.hasCharsetField()
					.verify();
	}
	
	@Test
	public void testClassAsSubBeans() {
		ImmutableList<Class<?>> list = ImmutableList.<Class<?>>builder()
					.add(SyncClientCommands.Update.class)
					.add(SyncClientCommands.Deletion.class)
					.add(UidMSEmail.class)
					.build();
		
		EqualsVerifierBuilder.builder()
					.equalsVerifiers(list)
					.hasCharsetField()
					.withSuperClass(true)
					.verify();
	}
	
}
