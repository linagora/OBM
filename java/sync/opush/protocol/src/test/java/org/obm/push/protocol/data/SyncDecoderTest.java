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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.push.TestUtils.getXml;

import java.util.Properties;

import org.easymock.IMocksControl;
import org.junit.Before;
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
import org.obm.push.bean.Sync;
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
	
	private Device device;
	private UserDataRequest udr;
	private String collectionPath;
	private int collectionId;
	
	private IMocksControl mocks;
	private SyncedCollectionDao syncedCollectionDao;
	private CollectionDao collectionDao;
	private CollectionPathHelper collectionPathHelper;

	@Before
	public void setUp() throws Exception {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), null);
		User user = Factory.create().createUser("adrien@test.tlse.lngr", "email@test.tlse.lngr", "Adrien");
		udr = new UserDataRequest(new Credentials(user, "test"), "Sync", device);
		collectionPath = "INBOX";
		collectionId = 5;

		mocks = createControl();
		syncedCollectionDao = mocks.createMock(SyncedCollectionDao.class);
		collectionDao = mocks.createMock(CollectionDao.class);
		collectionPathHelper = mocks.createMock(CollectionPathHelper.class);
		
		expect(collectionDao.getCollectionPath(collectionId)).andReturn(collectionPath).anyTimes();
		expect(collectionPathHelper.recognizePIMDataType(collectionPath)).andReturn(PIMDataType.EMAIL).anyTimes();
	}
	
	@Test
	public void testRequestOptionsAreStored() throws Exception {
		Document request = buildRequestWithOptions("0", 
				"<Options>" +
					"<FilterType>2</FilterType>" +
					"<Conflict>1</Conflict>" +
					"<MIMESupport>1</MIMESupport>" +
					"<MIMETruncation>100</MIMETruncation>" +
					"<BodyPreference>" +
						"<Type>1</Type>" +
					"</BodyPreference>" +
					"<BodyPreference>" +
						"<Type>2</Type>" +
					"</BodyPreference>" +
					"<BodyPreference>" +
						"<Type>4</Type>" +
						"<TruncationSize>5120</TruncationSize>" +
					"</BodyPreference>" +
				"</Options>");
		
		SyncCollectionOptions requestOptionsToStore = new SyncCollectionOptions();
		requestOptionsToStore.setFilterType(FilterType.THREE_DAYS_BACK);
		requestOptionsToStore.setConflict(1);
		requestOptionsToStore.setMimeSupport(1);
		requestOptionsToStore.setMimeTruncation(100);
		requestOptionsToStore.setBodyPreferences(ImmutableList.<BodyPreference> of(
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.PlainText)
				.build(),
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.HTML)
				.build(),
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.MIME)
				.truncationSize(5120)
				.build()
			));
		SyncCollection requestSyncCollectionToStore = buildRequestCollectionWithOptions(requestOptionsToStore, "0");
		
		expect(syncedCollectionDao.get(udr.getCredentials(), device, collectionId)).andReturn(null).once();
		syncedCollectionDao.put(udr.getCredentials(), device, requestSyncCollectionToStore);
		expectLastCall().once();
		
		mocks.replay();
		SyncDecoder syncDecoder = new SyncDecoder(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		Sync decodedRequest = syncDecoder.decodeSync(request, udr);
		mocks.verify();
		
		assertThat(decodedRequest.getCollections()).containsOnly(requestSyncCollectionToStore);
	}
	
	@Test
	public void testRequestWithOnlyFilterTypeOptionsStoreOthersWithDefaultValue() throws Exception {
		Document request = buildRequestWithOptions("0", 
				"<Options>" +
					"<FilterType>2</FilterType>" +
				"</Options>");
		
		SyncCollectionOptions requestOptionsToStore = new SyncCollectionOptions();
		requestOptionsToStore.setFilterType(FilterType.THREE_DAYS_BACK);

		SyncCollection requestSyncCollectionToStore = buildRequestCollectionWithOptions(requestOptionsToStore, "0");
		
		expect(syncedCollectionDao.get(udr.getCredentials(), device, collectionId)).andReturn(null).once();
		syncedCollectionDao.put(udr.getCredentials(), device, requestSyncCollectionToStore);
		expectLastCall().once();
		
		mocks.replay();
		SyncDecoder syncDecoder = new SyncDecoder(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		Sync decodedRequest = syncDecoder.decodeSync(request, udr);
		mocks.verify();
		
		assertThat(decodedRequest.getCollections()).containsOnly(requestSyncCollectionToStore);
	}
	
	@Test
	public void testNoRequestOptionsTakeTheDefaultOneIfNoPrevious() throws Exception {
		SyncCollectionOptions toStoreOptions = new SyncCollectionOptions();
		toStoreOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());
		toStoreOptions.setConflict(1);
		toStoreOptions.setDeletesAsMoves(true);
		toStoreOptions.setFilterType(FilterType.THREE_DAYS_BACK);
		toStoreOptions.setMimeSupport(null);
		toStoreOptions.setMimeTruncation(null);
		toStoreOptions.setTruncation(SyncCollectionOptions.SYNC_TRUNCATION_ALL);
		SyncCollection toStoreSyncCollection = buildRequestCollectionWithOptions(toStoreOptions, "156");

		Document requestWithoutOptions = buildRequestWithoutOptions("156");
		
		expect(syncedCollectionDao.get(udr.getCredentials(), device, collectionId)).andReturn(null).once();
		syncedCollectionDao.put(udr.getCredentials(), device, toStoreSyncCollection);
		expectLastCall().once();
		
		mocks.replay();
		SyncDecoder syncDecoder = new SyncDecoder(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		Sync decodedRequest = syncDecoder.decodeSync(requestWithoutOptions, udr);
		mocks.verify();
		
		assertThat(decodedRequest.getCollections()).containsOnly(toStoreSyncCollection);
	}
	
	@Test
	public void testNoRequestOptionsTakeThePreviousOne() throws Exception {
		Document firstRequest = buildRequestWithOptions("0", 
				"<Options>" +
					"<FilterType>2</FilterType>" +
					"<Conflict>1</Conflict>" +
					"<MIMESupport>1</MIMESupport>" +
					"<MIMETruncation>100</MIMETruncation>" +
					"<BodyPreference>" +
						"<Type>1</Type>" +
					"</BodyPreference>" +
					"<BodyPreference>" +
						"<Type>2</Type>" +
					"</BodyPreference>" +
					"<BodyPreference>" +
						"<Type>4</Type>" +
						"<TruncationSize>5120</TruncationSize>" +
					"</BodyPreference>" +
				"</Options>");
		Document secondRequest = buildRequestWithoutOptions("156");
		
		SyncCollectionOptions firstRequestOptionsToStore = new SyncCollectionOptions();
		firstRequestOptionsToStore.setFilterType(FilterType.THREE_DAYS_BACK);
		firstRequestOptionsToStore.setConflict(1);
		firstRequestOptionsToStore.setMimeSupport(1);
		firstRequestOptionsToStore.setMimeTruncation(100);
		firstRequestOptionsToStore.setBodyPreferences(ImmutableList.<BodyPreference> of(
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.PlainText)
				.build(),
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.HTML)
				.build(),
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.MIME)
				.truncationSize(5120)
				.build()
			));
		SyncCollection firstSyncCollectionToStore = buildRequestCollectionWithOptions(firstRequestOptionsToStore, "0");
		SyncCollection secondSyncCollectionToStore = buildRequestCollectionWithOptions(firstRequestOptionsToStore, "156");
		
		expect(syncedCollectionDao.get(udr.getCredentials(), device, collectionId)).andReturn(null).once();
		syncedCollectionDao.put(udr.getCredentials(), device, firstSyncCollectionToStore);
		expectLastCall().once();
		
		expect(syncedCollectionDao.get(udr.getCredentials(), device, collectionId)).andReturn(firstSyncCollectionToStore).once();
		syncedCollectionDao.put(udr.getCredentials(), device, secondSyncCollectionToStore);
		expectLastCall().once();
		
		mocks.replay();
		SyncDecoder syncDecoder = new SyncDecoder(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		Sync firstDecodedRequest = syncDecoder.decodeSync(firstRequest, udr);
		Sync secondDecodedRequest = syncDecoder.decodeSync(secondRequest, udr);
		mocks.verify();
		
		assertThat(firstDecodedRequest.getCollections()).containsOnly(firstSyncCollectionToStore);
		assertThat(secondDecodedRequest.getCollections()).containsOnly(secondSyncCollectionToStore);
	}
	
	@Test
	public void testZeroTruncationSizeMustNotBeInterpreted() throws Exception {
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.THREE_DAYS_BACK);
		syncCollectionOptions.setMimeSupport(1);
		syncCollectionOptions.setConflict(1);
		syncCollectionOptions.setMimeTruncation(100);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference> of(
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.PlainText)
				.build()
			));
		SyncCollection syncCollection = new SyncCollection();
		syncCollection.setCollectionId(collectionId);
		syncCollection.setCollectionPath(collectionPath);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setOptions(syncCollectionOptions);
		syncCollection.setSyncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY);
		
		Document firstDoc = buildRequestWithOptions("0",
				"<Options>" +
					"<FilterType>2</FilterType>" +
					"<Conflict>1</Conflict>" +
					"<MIMESupport>1</MIMESupport>" +
					"<MIMETruncation>100</MIMETruncation>" +
					"<BodyPreference>" +
						"<Type>1</Type>" +
						"<TruncationSize>0</TruncationSize>" +
					"</BodyPreference>" +
				"</Options>");

		expect(syncedCollectionDao.get(udr.getCredentials(), device, collectionId)).andReturn(null).once();
		syncedCollectionDao.put(udr.getCredentials(), device, syncCollection);
		expectLastCall().once();
		
		mocks.replay();
		SyncDecoder syncDecoder = new SyncDecoder(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		Sync sync = syncDecoder.decodeSync(firstDoc, udr);
		mocks.verify();
		
		SyncCollectionOptions options = sync.getCollection(collectionId).getOptions();
		BodyPreference bodyPreference = options.getBodyPreferences().get(0);
		assertThat(bodyPreference.getTruncationSize()).isNull();
	}
	
	@Test
	public void testTruncationSizeMustBeInterpreted() throws Exception {
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.THREE_DAYS_BACK);
		syncCollectionOptions.setMimeSupport(1);
		syncCollectionOptions.setConflict(1);
		syncCollectionOptions.setMimeTruncation(100);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference> of(
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.PlainText)
				.truncationSize(1000)
				.build()
			));
		SyncCollection syncCollection = new SyncCollection();
		syncCollection.setCollectionId(collectionId);
		syncCollection.setCollectionPath(collectionPath);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setOptions(syncCollectionOptions);
		syncCollection.setSyncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY);
		
		expect(syncedCollectionDao.get(udr.getCredentials(), device, collectionId)).andReturn(null).once();
		syncedCollectionDao.put(udr.getCredentials(), device, syncCollection);
		expectLastCall().once();
		
		Document firstRequest = buildRequestWithOptions("0",
				"<Options>" +
					"<FilterType>2</FilterType>" +
					"<Conflict>1</Conflict>" +
					"<MIMESupport>1</MIMESupport>" +
					"<MIMETruncation>100</MIMETruncation>" +
					"<BodyPreference>" +
						"<Type>1</Type>" +
						"<TruncationSize>1000</TruncationSize>" +
					"</BodyPreference>" +
				"</Options>");

		mocks.replay();
		SyncDecoder syncDecoder = new SyncDecoder(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		Sync sync = syncDecoder.decodeSync(firstRequest, udr);
		mocks.verify();
		
		SyncCollectionOptions options = sync.getCollection(collectionId).getOptions();
		BodyPreference bodyPreference = options.getBodyPreferences().get(0);
		assertThat(bodyPreference.getTruncationSize()).isEqualTo(1000);
	}
	
	private SyncCollection buildRequestCollectionWithOptions(SyncCollectionOptions options, String syncKey) {
		SyncCollection syncCollection = new SyncCollection();
		syncCollection.setCollectionId(collectionId);
		syncCollection.setCollectionPath(collectionPath);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setOptions(options);
		syncCollection.setSyncKey(new SyncKey(syncKey));
		return syncCollection;
	}

	private Document buildRequestWithoutOptions(String syncKey) throws Exception {
		return buildRequestWithOptions(syncKey, "");
	}

	private Document buildRequestWithOptions(String syncKey, String options) throws Exception {
		return getXml(
			"<Sync>" +
				"<Collections>" +
					"<Collection>" +
						"<SyncKey>" + syncKey +"</SyncKey>" +
						"<CollectionId>" + collectionId + "</CollectionId>" +
						options +
					"</Collection>" +
				"</Collections>" +
			"</Sync>");
	}
}
