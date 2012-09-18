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
package org.obm.push.protocol;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.protocol.bean.AnalysedSyncRequest;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.ASTimeZoneConverter;
import org.obm.push.protocol.data.Base64ASTimeZoneDecoder;
import org.obm.push.protocol.data.SyncAnalyser;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class)
public class SyncProtocolTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(ProtocolModuleTest.class);
	
	@Inject CollectionPathHelper collectionPathHelper;

	private final int DEFAULT_WINDOW_SIZE = 100;
	
	private User user;
	private Device device;
	private Credentials credentials;
	private UserDataRequest udr;
	private String mailbox;
	private String password;

	@Before
	public void setUp() {
	    mailbox = "to@localhost.com";
	    password = "password";
		user = Factory.create().createUser(mailbox, mailbox, "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		credentials = new Credentials(user, password);
		udr = new UserDataRequest(credentials, "noCommand", device);
	}

	@Test
	public void testEncodeValidResponse() throws TransformerException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId);

		collectionResponse.setNewSyncKey(new SyncKey("123456789"));
		collectionResponse.setCollectionValidity(true);
		
		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newCollectionNoChangeResponse(collectionId));
	}
	
	@Test
	public void testEncodeResponseCollectionIdError() throws TransformerException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId);

		collectionResponse.setCollectionValidity(false);

		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newCollectionNotFoundResponse(collectionId));
	}
	
	@Test
	public void testEncodeResponseSyncKeyError() throws TransformerException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId);

		collectionResponse.setCollectionValidity(true);
		collectionResponse.getSyncCollection().setStatus(SyncStatus.INVALID_SYNC_KEY);
		
		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newSyncKeyErrorResponse(collectionId));
	}
	private String newCollectionNoChangeResponse(int collectionId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>123456789</SyncKey>" +
							"<CollectionId>" + String.valueOf(collectionId) + "</CollectionId>" +
							"<Status>1</Status>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
	}

	private String newCollectionNotFoundResponse(int collectionId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<CollectionId>" + String.valueOf(collectionId) + "</CollectionId>" +
							"<Status>8</Status>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
	}

	private String newSyncKeyErrorResponse(int collectionId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<CollectionId>" + String.valueOf(collectionId) + "</CollectionId>" +
							"<Status>3</Status>" +
							"<SyncKey>0</SyncKey>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
	}

	private String encodeResponse(SyncCollectionResponse collectionResponse) throws TransformerException {
		Document endcodedResponse = new SyncProtocol(null, null).encodeResponse(syncResponse(collectionResponse));
		return DOMUtils.serialize(endcodedResponse);
	}
	
	private SyncResponse syncResponse(SyncCollectionResponse collectionResponse) {
		return new SyncResponse(Sets.newHashSet(collectionResponse), udr, null, Collections.<String, String>emptyMap());
	}

	private SyncCollectionResponse newSyncCollectionResponse(int collectionId) {
		String collectionPath = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath);
		syncCollection.setSyncKey(new SyncKey("123456789"));
		SyncCollectionResponse collectionResponse = new SyncResponse.SyncCollectionResponse(syncCollection);
		collectionResponse.setItemChanges(Collections.<ItemChange>emptyList());
		collectionResponse.setItemChangesDeletion(Collections.<ItemDeletion>emptyList());
		return collectionResponse;
	}
	
	@Test(expected=NoDocumentException.class)
	public void testDecodeRequestWithNullDocument() throws Exception {
		Document request = null;
		
		SyncDecoder syncDecoder = createStrictMock(SyncDecoder.class);
		
		new SyncProtocol(syncDecoder, null).decodeRequest(request);
	}
	
	@Test
	public void testDecodeRequestCallsSyncDecoderWithExpectedDocument() throws Exception {
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>10</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>1234-5678</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncDecoder syncDecoder = createStrictMock(SyncDecoder.class);
		expect(syncDecoder.decodeSync(request)).andReturn(null);
		replay(syncDecoder);
		
		new SyncProtocol(syncDecoder, null).decodeRequest(request);

		verify(syncDecoder);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testGetWaitWhen0() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>0</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(syncingCollectionId, syncingCollectionSyncKey);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(analyzedRequest.getSync().getWaitInSecond()).isEqualTo(0);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testGetWaitWhen1000() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>1000</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(syncingCollectionId, syncingCollectionSyncKey);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(analyzedRequest.getSync().getWaitInSecond()).isEqualTo(60000);
	}

	@Ignore("Sync decoding is in progress")
	@Test(expected=PartialException.class)
	public void testPartialRequest() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Partial/>" +
					"<Wait>1</Wait>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(syncingCollectionId, syncingCollectionSyncKey);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		syncProtocol.analyzeRequest(udr, syncRequest);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testSyncCollectionDefaultValues() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(syncingCollectionId, syncingCollectionSyncKey);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection expectedSyncCollection = new SyncCollection();
		expectedSyncCollection.setCollectionId(syncingCollectionId);
		expectedSyncCollection.setCollectionPath(collectionPath(syncingCollectionId));
		expectedSyncCollection.setDataClass(null);
		expectedSyncCollection.setDataType(PIMDataType.EMAIL);
		expectedSyncCollection.setStatus(SyncStatus.OK);
		expectedSyncCollection.setItemSyncState(null);
		expectedSyncCollection.setFetchIds(ImmutableList.<String>of());
		expectedSyncCollection.setMoreAvailable(false);
		expectedSyncCollection.setSyncKey(new SyncKey(syncingCollectionSyncKey));
		expectedSyncCollection.setWindowSize(100);
		expectedSyncCollection.setOptions(new SyncCollectionOptions());
		assertThat(analyzedRequest.getSync().getCollections()).containsOnly(expectedSyncCollection);
		assertThat(analyzedRequest.getSync().getCollection(syncingCollectionId)).isEqualTo(expectedSyncCollection);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testWindowSizeIsTookInParentSync() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<WindowSize>150</WindowSize>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, 150);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getWindowSize()).isEqualTo(150);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testWindowSizeDifferentInSyncAndCollection() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<WindowSize>150</WindowSize>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<WindowSize>75</WindowSize>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, 75);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getWindowSize()).isEqualTo(75);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testOptionToZero() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Options>" +
								"<FilterType>0</FilterType>" +
								"<Conflict>0</Conflict>" +
								"<MIMETruncation>0</MIMETruncation>" +
								"<MIMESupport>0</MIMESupport>" +
							"</Options>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, 0, 0, 0, 0);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollectionOptions syncCollectionOptions = analyzedRequest.getSync().getCollection(syncingCollectionId).getOptions();
		assertThat(syncCollectionOptions.getBodyPreferences()).isEmpty();
		assertThat(syncCollectionOptions.getConflict()).isEqualTo(0);
		assertThat(syncCollectionOptions.getFilterType()).isEqualTo(FilterType.ALL_ITEMS);
		assertThat(syncCollectionOptions.getMimeSupport()).isEqualTo(0);
		assertThat(syncCollectionOptions.getMimeTruncation()).isEqualTo(0);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testOptionToNonZero() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Options>" +
								"<FilterType>5</FilterType>" +
								"<Conflict>1</Conflict>" +
								"<MIMETruncation>8</MIMETruncation>" +
								"<MIMESupport>2</MIMESupport>" +
							"</Options>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, 5, 1, 8, 2);
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollectionOptions syncCollectionOptions = analyzedRequest.getSync().getCollection(syncingCollectionId).getOptions();
		assertThat(syncCollectionOptions.getBodyPreferences()).isEmpty();
		assertThat(syncCollectionOptions.getConflict()).isEqualTo(1);
		assertThat(syncCollectionOptions.getFilterType()).isEqualTo(FilterType.ONE_MONTHS_BACK);
		assertThat(syncCollectionOptions.getMimeSupport()).isEqualTo(2);
		assertThat(syncCollectionOptions.getMimeTruncation()).isEqualTo(8);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testOptionsBodyPreferencesMinSpecValues() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Options>" +
								"<BodyPreference>" +
									"<Type>1</Type>" +
									"<TruncationSize>0</TruncationSize>" +
									"<AllOrNone>0</AllOrNone>" +
								"</BodyPreference>" +
							"</Options>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, ImmutableList.of(bodyPreference(1, 0, false)));
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollectionOptions syncCollectionOptions = analyzedRequest.getSync().getCollection(syncingCollectionId).getOptions();
		assertThat(syncCollectionOptions.getBodyPreferences()).containsOnly(bodyPreference(1, 0, false));
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testOptionsBodyPreferencesMaxSpecValues() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		int maxSpecTruncationSize = 2147483647;
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Options>" +
								"<BodyPreference>" +
									"<Type>1</Type>" +
									"<TruncationSize>0</TruncationSize>" +
									"<AllOrNone>0</AllOrNone>" +
								"</BodyPreference>" +
								"<BodyPreference>" +
									"<Type>8</Type>" +
									"<TruncationSize>" + maxSpecTruncationSize + "</TruncationSize>" +
									"<AllOrNone>1</AllOrNone>" +
								"</BodyPreference>" +
							"</Options>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, ImmutableList.of(
						bodyPreference(1, 0, false),
						bodyPreference(8, maxSpecTruncationSize, true)));
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollectionOptions syncCollectionOptions = analyzedRequest.getSync().getCollection(syncingCollectionId).getOptions();
		assertThat(syncCollectionOptions.getBodyPreferences()).containsOnly(
				bodyPreference(1, 0, false),
				bodyPreference(8, maxSpecTruncationSize, true));
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testOptionsBodyPreferencesTwoEntries() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		int maxSpecTruncationSize = 2147483647;
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Options>" +
								"<BodyPreference>" +
									"<Type>8</Type>" +
									"<TruncationSize>" + maxSpecTruncationSize + "</TruncationSize>" +
									"<AllOrNone>1</AllOrNone>" +
								"</BodyPreference>" +
							"</Options>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, ImmutableList.of(bodyPreference(8, maxSpecTruncationSize, true)));
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollectionOptions syncCollectionOptions = analyzedRequest.getSync().getCollection(syncingCollectionId).getOptions();
		assertThat(syncCollectionOptions.getBodyPreferences()).containsOnly(bodyPreference(8, maxSpecTruncationSize, true));
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsAdd() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Add>" +
									"<ServerId>123</ServerId>" +
									"<ClientId>13579</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Add>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		MSContact expectedMSContact = new MSContact();
		expectedMSContact.setEmail1Address("opush@obm.org");
		expectedMSContact.setFileAs("Dobney, JoLynn Julie");
		expectedMSContact.setFirstName("JoLynn");
		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", "13579", "Add", expectedMSContact, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS, 
				ImmutableList.of(expectedSyncCollectionChange));
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(expectedSyncCollectionChange);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsTwoAdd() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Add>" +
									"<ServerId>123</ServerId>" +
									"<ClientId>13579</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Add>" +
								"<Add>" +
									"<ServerId>456</ServerId>" +
									"<ClientId>02468</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush2@obm.org\"&lt;opush2@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney2, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn2</FirstName>" +
									"</ApplicationData>" +
								"</Add>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		MSContact expectedMSContact = new MSContact();
		expectedMSContact.setEmail1Address("opush@obm.org");
		expectedMSContact.setFileAs("Dobney, JoLynn Julie");
		expectedMSContact.setFirstName("JoLynn");
		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", "13579", "Add", expectedMSContact, PIMDataType.CONTACTS);
		
		MSContact expectedMSContact2 = new MSContact();
		expectedMSContact2.setEmail1Address("opush2@obm.org");
		expectedMSContact2.setFileAs("Dobney2, JoLynn Julie");
		expectedMSContact2.setFirstName("JoLynn2");
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", "02468", "Add", expectedMSContact2, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS,
				ImmutableList.of(expectedSyncCollectionChange, expectedSyncCollectionChange2));
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(expectedSyncCollectionChange, expectedSyncCollectionChange2);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsChange() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>10</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Change>" +
									"<ServerId>123</ServerId>" +
									"<ClientId>13579</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Change>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		MSContact expectedMSContact = new MSContact();
		expectedMSContact.setEmail1Address("opush@obm.org");
		expectedMSContact.setFileAs("Dobney, JoLynn Julie");
		expectedMSContact.setFirstName("JoLynn");
		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", "13579", "Change", expectedMSContact, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS,
				ImmutableList.of(expectedSyncCollectionChange));
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(expectedSyncCollectionChange);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsTwoChange() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Change>" +
									"<ServerId>123</ServerId>" +
									"<ClientId>13579</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Change>" +
								"<Change>" +
									"<ServerId>456</ServerId>" +
									"<ClientId>02468</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush2@obm.org\"&lt;opush2@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney2, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn2</FirstName>" +
									"</ApplicationData>" +
								"</Change>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		MSContact expectedMSContact = new MSContact();
		expectedMSContact.setEmail1Address("opush@obm.org");
		expectedMSContact.setFileAs("Dobney, JoLynn Julie");
		expectedMSContact.setFirstName("JoLynn");
		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", "13579", "Change", expectedMSContact, PIMDataType.CONTACTS);
		
		MSContact expectedMSContact2 = new MSContact();
		expectedMSContact2.setEmail1Address("opush2@obm.org");
		expectedMSContact2.setFileAs("Dobney2, JoLynn Julie");
		expectedMSContact2.setFirstName("JoLynn2");
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", "02468", "Change", expectedMSContact2, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS,
				ImmutableList.of(expectedSyncCollectionChange, expectedSyncCollectionChange2));
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(expectedSyncCollectionChange, expectedSyncCollectionChange2);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsFetch() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Fetch>" +
									"<ServerId>123</ServerId>" +
								"</Fetch>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", null, "Fetch", null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL,
				ImmutableList.of(expectedSyncCollectionChange), ImmutableList.of("123"));
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(expectedSyncCollectionChange);
		assertThat(syncCollection.getFetchIds()).containsOnly("123");
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsTwoFetch() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Fetch>" +
									"<ServerId>123</ServerId>" +
								"</Fetch>" +
								"<Fetch>" +
									"<ServerId>456</ServerId>" +
								"</Fetch>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", null, "Fetch", null, PIMDataType.EMAIL);
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", null, "Fetch", null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL,
				ImmutableList.of(expectedSyncCollectionChange, expectedSyncCollectionChange2),
				ImmutableList.of("123", "456"));
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(
				expectedSyncCollectionChange, expectedSyncCollectionChange2);
		assertThat(syncCollection.getFetchIds()).containsOnly("123", "456");
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsDelete() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Delete>" +
									"<ServerId>123</ServerId>" +
								"</Delete>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", null, "Delete", null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL,
				ImmutableList.of(expectedSyncCollectionChange));
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(expectedSyncCollectionChange);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsTwoDelete() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Delete>" +
									"<ServerId>123</ServerId>" +
								"</Delete>" +
								"<Delete>" +
									"<ServerId>456</ServerId>" +
								"</Delete>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"123", null, "Delete", null, PIMDataType.EMAIL);
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", null, "Delete", null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL,
				ImmutableList.of(expectedSyncCollectionChange, expectedSyncCollectionChange2));
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);
		

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(
				expectedSyncCollectionChange, expectedSyncCollectionChange2);
	}

	@Ignore("Sync decoding is in progress")
	@Test
	public void testCommandsAddChangeFetchDelete() throws Exception {
		int syncingCollectionId = 3;
		String syncingCollectionSyncKey = "1234-5678";
		Document request = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>" + syncingCollectionSyncKey  + "</SyncKey>" +
							"<CollectionId>" +syncingCollectionId + "</CollectionId>" +
							"<Commands>" +
								"<Add>" +
									"<ServerId>12</ServerId>" +
									"<ClientId>120</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Add>" +
								"<Add>" +
									"<ServerId>13</ServerId>" +
									"<ClientId>130</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Add>" +
								"<Change>" +
									"<ServerId>34</ServerId>" +
									"<ClientId>340</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Change>" +
								"<Change>" +
									"<ServerId>35</ServerId>" +
									"<ClientId>350</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Change>" +
								"<Fetch>" +
									"<ServerId>56</ServerId>" +
								"</Fetch>" +
								"<Fetch>" +
									"<ServerId>57</ServerId>" +
								"</Fetch>" +
								"<Delete>" +
									"<ServerId>78</ServerId>" +
								"</Delete>" +
								"<Delete>" +
									"<ServerId>79</ServerId>" +
								"</Delete>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");
		MSContact expectedMSContact = new MSContact();
		expectedMSContact.setEmail1Address("opush@obm.org");
		expectedMSContact.setFileAs("Dobney, JoLynn Julie");
		expectedMSContact.setFirstName("JoLynn");

		SyncCollectionChange expectedSyncCollectionAdd = new SyncCollectionChange(
				"12", "120", "Add", expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionAdd2 = new SyncCollectionChange(
				"13", "130", "Add", expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"34", "340", "Change", expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"35", "350", "Change", expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionFetch = new SyncCollectionChange(
				"56", null, "Fetch", null, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionFetch2 = new SyncCollectionChange(
				"57", null, "Fetch", null, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionDelete = new SyncCollectionChange(
				"78", null, "Delete", null, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionDelete2 = new SyncCollectionChange(
				"79", null, "Delete", null, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS,
				ImmutableList.of(
						expectedSyncCollectionAdd, expectedSyncCollectionAdd2,
						expectedSyncCollectionChange, expectedSyncCollectionChange2,
						expectedSyncCollectionFetch,  expectedSyncCollectionFetch2,
						expectedSyncCollectionDelete, expectedSyncCollectionDelete2),
				ImmutableList.of("56", "57"));
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getChanges()).containsOnly(
				expectedSyncCollectionAdd, expectedSyncCollectionAdd2,
				expectedSyncCollectionChange, expectedSyncCollectionChange2,
				expectedSyncCollectionFetch,  expectedSyncCollectionFetch2,
				expectedSyncCollectionDelete, expectedSyncCollectionDelete2);
		assertThat(syncCollection.getFetchIds()).containsOnly("56", "57");
	}

	private BodyPreference bodyPreference(Integer bodyType, Integer truncationSize, Boolean allOrNone) {
		return BodyPreference.builder()
			.bodyType(MSEmailBodyType.getValueOf(bodyType))
			.truncationSize(truncationSize)
			.allOrNone(allOrNone)
			.build();
	}
	
	private SyncProtocol newSyncProtocol(SyncedCollectionDao syncedCollectionDao, CollectionDao collectionDao,
			CollectionPathHelper collectionPathHelper) {
		SyncDecoder syncDecoder = new SyncDecoderTest();
		SyncAnalyser syncAnalyser = new SyncAnalyserTest(syncedCollectionDao, collectionDao, collectionPathHelper, null, null);
		return new SyncProtocol(syncDecoder, syncAnalyser);
	}

	private CollectionDao mockFindCollectionPathForId(int syncingCollectionId) throws Exception {
		return mockFindCollectionPathForId(PIMDataType.EMAIL, syncingCollectionId);
	}

	private CollectionDao mockFindCollectionPathForId(PIMDataType pimDataType, int syncingCollectionId) throws Exception {
		String foundPath = collectionPath(pimDataType, syncingCollectionId);
		CollectionDao collectionDao = createMock(CollectionDao.class);
		expect(collectionDao.getCollectionPath(syncingCollectionId)).andReturn(foundPath);
		return collectionDao;
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey) {
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, DEFAULT_WINDOW_SIZE);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey,
			Integer filterType, Integer conflict, Integer mimeTruncation, Integer mimeSupport) {
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.fromSpecificationValue(String.valueOf(filterType)));
		options.setConflict(conflict);
		options.setMimeTruncation(mimeTruncation);
		options.setMimeSupport(mimeSupport);

		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, options);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey,
			List<BodyPreference> bodyPreferences) {
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setBodyPreferences(bodyPreferences);
		options.setFilterType(FilterType.ALL_ITEMS);
		
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, options);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey,
			SyncCollectionOptions options) {
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath(collectionId));
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setSyncKey(new SyncKey(syncKey));
		syncCollection.setOptions(options);
		
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncCollection);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey, Integer windowSize) {
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, windowSize, PIMDataType.EMAIL,
				Collections.<SyncCollectionChange>emptySet(), Collections.<String>emptyList());
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey, PIMDataType pimDataType,
			Iterable<SyncCollectionChange> syncCollectionChanges) {
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, DEFAULT_WINDOW_SIZE, pimDataType,
				syncCollectionChanges, Collections.<String>emptyList());
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey, PIMDataType pimDataType,
			Iterable<SyncCollectionChange> syncCollectionChanges, List<String> fetchIds) {
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, DEFAULT_WINDOW_SIZE, pimDataType,
				syncCollectionChanges, fetchIds);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(
			int collectionId, String syncKey, Integer windowSize, PIMDataType pimDataType,
			Iterable<SyncCollectionChange> syncCollectionChanges, List<String> fetchIds) {
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath(pimDataType, collectionId));
		syncCollection.setDataType(pimDataType);
		syncCollection.setSyncKey(new SyncKey(syncKey));
		syncCollection.setWindowSize(windowSize);
		syncCollection.setFetchIds(fetchIds);
		for (SyncCollectionChange change : syncCollectionChanges) {
			syncCollection.addChange(change);
		}
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncCollection);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(
			int collectionId, SyncCollection syncCollection) {
		SyncedCollectionDao syncedCollectionDao = createMock(SyncedCollectionDao.class);
		expect(syncedCollectionDao.get(credentials, device, collectionId)).andReturn(null);
		syncedCollectionDao.put(credentials, device, Sets.newHashSet(syncCollection));
		expectLastCall();
		return syncedCollectionDao;
	}

	private CollectionPathHelper mockCollectionPathHelperRecognizeDataType() {
		return mockCollectionPathHelperRecognizeDataType(PIMDataType.EMAIL);
	}

	private CollectionPathHelper mockCollectionPathHelperRecognizeDataType(PIMDataType pimDataType) {
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(anyObject(String.class))).andReturn(pimDataType);
		return collectionPathHelper;
	}

	private String collectionPath(int collectionId) {
		return collectionPath(PIMDataType.EMAIL, collectionId);
	}

	private String collectionPath(PIMDataType pimDataType, int collectionId) {
		return "obm:\\\\" + user.getLoginAtDomain() + "\\" + pimDataType.asCollectionPathValue() + "\\" + collectionId;
	}
	
	static class SyncDecoderTest extends SyncDecoder {}
	
	static class SyncAnalyserTest extends SyncAnalyser {

		protected SyncAnalyserTest(
				SyncedCollectionDao syncedCollectionStoreService,
				CollectionDao collectionDao,
				CollectionPathHelper collectionPathHelper,
				Base64ASTimeZoneDecoder base64AsTimeZoneDecoder,
				ASTimeZoneConverter asTimeZoneConverter) {
			super(syncedCollectionStoreService, collectionDao, collectionPathHelper,
					base64AsTimeZoneDecoder, asTimeZoneConverter);
		}
		
	}
	
}
