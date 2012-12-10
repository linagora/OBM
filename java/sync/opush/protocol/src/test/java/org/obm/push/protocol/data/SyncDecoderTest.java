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
package org.obm.push.protocol.data;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.obm.push.TestUtils.getXml;

import java.math.BigDecimal;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;

@RunWith(SlowFilterRunner.class)
public class SyncDecoderTest {
	
	@Test
	public void testFirstSyncCollectionOptionsMustBeStored() throws Exception {
		String inboxCollectionPath = "INBOX";
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.THREE_DAYS_BACK);
		syncCollectionOptions.setMimeSupport(1);
		syncCollectionOptions.setConflict(1);
		syncCollectionOptions.setMimeSupport(1);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference> of(BodyPreference.builder()
				.bodyType(MSEmailBodyType.PlainText)
				.truncationSize(0)
				.build(),
				BodyPreference.builder()
				.bodyType(MSEmailBodyType.HTML)
				.truncationSize(0)
				.build(),
				BodyPreference.builder()
				.bodyType(MSEmailBodyType.MIME)
				.truncationSize(5120)
				.build()
				));
		SyncCollection syncCollection = new SyncCollection();
		syncCollection.setCollectionId(5);
		syncCollection.setCollectionPath(inboxCollectionPath);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setOptions(syncCollectionOptions);
		syncCollection.setSyncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY);
		
		SyncedCollectionDao syncedCollectionDao = createStrictMock(SyncedCollectionDao.class);
		expect(syncedCollectionDao.get(getFakeUserDataRequest().getCredentials(), getFakeDevice(), 5))
			.andReturn(null).once();
		syncedCollectionDao.put(getFakeUserDataRequest().getCredentials(), 
				getFakeDevice(), syncCollection);
		expectLastCall().once();
		expect(syncedCollectionDao.get(getFakeUserDataRequest().getCredentials(), getFakeDevice(), 5))
			.andReturn(syncCollection).once();
		
		CollectionDao collectionDao = createMock(CollectionDao.class);
		expect(collectionDao.getCollectionPath(5)).andReturn(inboxCollectionPath).times(2);
		
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(inboxCollectionPath))
			.andReturn(PIMDataType.EMAIL).times(2);
		
		StringBuilder firstBuilder = new StringBuilder();
		firstBuilder.append("<Sync>");
		firstBuilder.append("<Collections>");
		firstBuilder.append("<Collection>");
		firstBuilder.append("<SyncKey>0</SyncKey>");
		firstBuilder.append("<CollectionId>5</CollectionId>");
		firstBuilder.append("<DeletesAsMoves>0</DeletesAsMoves>");
		firstBuilder.append("<Options>");
		firstBuilder.append("<FilterType>2</FilterType>");
		firstBuilder.append("<BodyPreference>");
		firstBuilder.append("<Type>1</Type>");
		firstBuilder.append("<TruncationSize>0</TruncationSize>");
		firstBuilder.append("</BodyPreference>");
		firstBuilder.append("<BodyPreference>");
		firstBuilder.append("<Type>2</Type>");
		firstBuilder.append("<TruncationSize>0</TruncationSize>");
		firstBuilder.append("</BodyPreference>");
		firstBuilder.append("<Conflict>1</Conflict>");
		firstBuilder.append("<MIMESupport>1</MIMESupport>");
		firstBuilder.append("<BodyPreference>");
		firstBuilder.append("<Type>4</Type>");
		firstBuilder.append("<TruncationSize>5120</TruncationSize>");
		firstBuilder.append("</BodyPreference>");
		firstBuilder.append("</Options>");
		firstBuilder.append("</Collection>");
		firstBuilder.append("</Collections>");
		firstBuilder.append("</Sync>");
		Document firstDoc = getXml(firstBuilder.toString());
		
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncDecoder syncDecoder = new SyncDecoder(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		syncDecoder.decodeSync(firstDoc, getFakeUserDataRequest());
		
		StringBuilder secondBuilder = new StringBuilder();
		secondBuilder.append("<Sync>");
		secondBuilder.append("<Collections>");
		secondBuilder.append("<Collection>");
		secondBuilder.append("<SyncKey>0</SyncKey>");
		secondBuilder.append("<CollectionId>5</CollectionId>");
		secondBuilder.append("<DeletesAsMoves>0</DeletesAsMoves>");
		secondBuilder.append("</Collection>");
		secondBuilder.append("</Collections>");
		secondBuilder.append("</Sync>");
		Document secondDoc = getXml(secondBuilder.toString());
		
		syncDecoder.decodeSync(secondDoc, getFakeUserDataRequest());
		
		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
	}

	private UserDataRequest getFakeUserDataRequest() {
		User user = Factory.create().createUser("adrien@test.tlse.lngr", "email@test.tlse.lngr", "Adrien");
		UserDataRequest udr = new UserDataRequest(
				new Credentials(user, "test"), "Sync", getFakeDevice(), new BigDecimal("12.5"));
		return udr;
	}

	private Device getFakeDevice() {
		return new Device(1, "devType", new DeviceId("devId"), new Properties());
	}
}
