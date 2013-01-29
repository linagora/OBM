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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
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
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.protocol.bean.AnalysedSyncRequest;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.ContactDecoder;
import org.obm.push.protocol.data.ContactEncoder;
import org.obm.push.protocol.data.DecoderFactory;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.data.SyncAnalyser;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.protocol.data.SyncEncoder;
import org.obm.push.protocol.data.ms.MSEmailDecoder;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.utils.DOMUtils;
import org.obm.xml.AcceptDifferentNamespaceXMLUnit;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

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
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), new BigDecimal("12.1"));
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
		Document endcodedResponse = new SyncProtocol(null, null, null, null, udr)
			.encodeResponse(syncResponse(collectionResponse));
		return DOMUtils.serialize(endcodedResponse);
	}
	
	private SyncResponse syncResponse(SyncCollectionResponse collectionResponse) {
		return new SyncResponse(Sets.newHashSet(collectionResponse), SyncClientCommands.empty());
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
		
		new SyncProtocol(syncDecoder, null, null, null, null).decodeRequest(request);
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
		
		new SyncProtocol(syncDecoder, null, null, null, null).decodeRequest(request);

		verify(syncDecoder);
	}

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

	@Test(expected=ASRequestIntegerFieldException.class)
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
		syncProtocol.decodeRequest(request);
	}

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

	@Test(expected=ProtocolException.class)
	public void testCommandsAddWithoutApplicationData() throws Exception {
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
								"</Add>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS);
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		syncProtocol.analyzeRequest(udr, syncRequest);
	}

	@Test(expected=ProtocolException.class)
	public void testCommandsChangeWithoutApplicationData() throws Exception {
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
								"</Change>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>");

		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS);
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		CollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		syncProtocol.analyzeRequest(udr, syncRequest);
	}

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
				"123", "13579", SyncCommand.ADD, expectedMSContact, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS);
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
				"123", "13579", SyncCommand.ADD, expectedMSContact, PIMDataType.CONTACTS);
		
		MSContact expectedMSContact2 = new MSContact();
		expectedMSContact2.setEmail1Address("opush2@obm.org");
		expectedMSContact2.setFileAs("Dobney2, JoLynn Julie");
		expectedMSContact2.setFirstName("JoLynn2");
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", "02468", SyncCommand.ADD, expectedMSContact2, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS);
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
				"123", "13579", SyncCommand.CHANGE, expectedMSContact, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS);
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
				"123", "13579", SyncCommand.CHANGE, expectedMSContact, PIMDataType.CONTACTS);
		
		MSContact expectedMSContact2 = new MSContact();
		expectedMSContact2.setEmail1Address("opush2@obm.org");
		expectedMSContact2.setFileAs("Dobney2, JoLynn Julie");
		expectedMSContact2.setFirstName("JoLynn2");
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", "02468", SyncCommand.CHANGE, expectedMSContact2, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS);
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
				"123", null, SyncCommand.FETCH, null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL);
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
				"123", null, SyncCommand.FETCH, null, PIMDataType.EMAIL);
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", null, SyncCommand.FETCH, null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL);
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
				"123", null, SyncCommand.DELETE, null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL);
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
				"123", null, SyncCommand.DELETE, null, PIMDataType.EMAIL);
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"456", null, SyncCommand.DELETE, null, PIMDataType.EMAIL);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.EMAIL);
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
				"12", "120", SyncCommand.ADD, expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionAdd2 = new SyncCollectionChange(
				"13", "130", SyncCommand.ADD, expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionChange = new SyncCollectionChange(
				"34", "340", SyncCommand.CHANGE, expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionChange2 = new SyncCollectionChange(
				"35", "350", SyncCommand.CHANGE, expectedMSContact, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionFetch = new SyncCollectionChange(
				"56", null, SyncCommand.FETCH, null, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionFetch2 = new SyncCollectionChange(
				"57", null, SyncCommand.FETCH, null, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionDelete = new SyncCollectionChange(
				"78", null, SyncCommand.DELETE, null, PIMDataType.CONTACTS);
		SyncCollectionChange expectedSyncCollectionDelete2 = new SyncCollectionChange(
				"79", null, SyncCommand.DELETE, null, PIMDataType.CONTACTS);
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, syncingCollectionSyncKey, PIMDataType.CONTACTS);
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

	@Test
	public void testEncodeDecodeLoopForPartialNoCollectionSyncRequest() throws Exception {
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Partial>1</Partial>" +
					"<Wait>30</Wait>" +
				"</Sync>";
		
		SyncProtocol syncProtocol = newSyncProtocol();
		SyncRequest decodedSyncRequest = syncProtocol.decodeRequest(DOMUtils.parse(request));
		Document encodedRequest = syncProtocol.encodeRequest(decodedSyncRequest);
		
		assertThat(request).isEqualTo(DOMUtils.serialize(encodedRequest));
	}

	@Test
	public void testEncodeDecodeLoopForSimpleSyncRequest() throws Exception {
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>1234-5678</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
							"<Commands>" +
								"<Delete>" +
									"<ServerId>79</ServerId>" +
								"</Delete>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
		
		SyncProtocol syncProtocol = newSyncProtocol();
		SyncRequest decodedSyncRequest = syncProtocol.decodeRequest(DOMUtils.parse(request));
		Document encodedRequest = syncProtocol.encodeRequest(decodedSyncRequest);
		
		assertThat(request).isEqualTo(DOMUtils.serialize(encodedRequest));
	}

	@Test
	public void testEncodeDecodeLoopForComplexeSyncRequest() throws Exception {
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Partial>1</Partial>" +
					"<Wait>30</Wait>" +
					"<WindowSize>150</WindowSize>" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>1234-5678</SyncKey>" +
							"<CollectionId>2</CollectionId>" +
							"<WindowSize>75</WindowSize>" +
							"<Options>" +
								"<FilterType>0</FilterType>" +
								"<Conflict>0</Conflict>" +
								"<MIMETruncation>0</MIMETruncation>" +
								"<MIMESupport>0</MIMESupport>" +
							"</Options>" +
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
						"<Collection>" +
							"<SyncKey>1235-6789</SyncKey>" +
							"<CollectionId>5</CollectionId>" +
							"<Options>" +
								"<FilterType>1</FilterType>" +
								"<Conflict>1</Conflict>" +
								"<MIMETruncation>1</MIMETruncation>" +
								"<MIMESupport>1</MIMESupport>" +
							"</Options>" +
							"<Commands>" +
								"<Add>" +
									"<ServerId>22</ServerId>" +
									"<ClientId>220</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Add>" +
								"<Add>" +
									"<ServerId>23</ServerId>" +
									"<ClientId>230</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Add>" +
								"<Change>" +
									"<ServerId>44</ServerId>" +
									"<ClientId>440</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Change>" +
								"<Change>" +
									"<ServerId>55</ServerId>" +
									"<ClientId>550</ClientId>" +
									"<ApplicationData>" +
										"<Email1Address>\"opush@obm.org\"&lt;opush@obm.org&gt;</Email1Address>" +
										"<FileAs>Dobney, JoLynn Julie</FileAs>" +
										"<FirstName>JoLynn</FirstName>" +
									"</ApplicationData>" +
								"</Change>" +
								"<Fetch>" +
									"<ServerId>66</ServerId>" +
								"</Fetch>" +
								"<Fetch>" +
									"<ServerId>77</ServerId>" +
								"</Fetch>" +
								"<Delete>" +
									"<ServerId>88</ServerId>" +
								"</Delete>" +
								"<Delete>" +
									"<ServerId>99</ServerId>" +
								"</Delete>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
		
		SyncProtocol syncProtocol = newSyncProtocol();
		SyncRequest decodedSyncRequest = syncProtocol.decodeRequest(DOMUtils.parse(request));
		Document encodedRequest = syncProtocol.encodeRequest(decodedSyncRequest);
		
		assertThat(request).isEqualTo(DOMUtils.serialize(encodedRequest));
	}

	@Test
	public void testDecodePartialErrorResponse() throws Exception {
		String response = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Status>13</Status>" +
				"</Sync>";

		SyncProtocol syncProtocol = newSyncProtocol(null, null, null);
		SyncResponse decodedSyncResponse = syncProtocol.decodeResponse(DOMUtils.parse(response));
		
		assertThat(decodedSyncResponse.getStatus()).isEqualTo(SyncStatus.PARTIAL_REQUEST);
	}

	@Test
	public void testDecodeProvisionErrorResponse() throws Exception {
		String response = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Status>11</Status>" +
				"</Sync>";

		SyncProtocol syncProtocol = newSyncProtocol(null, null, null);
		SyncResponse decodedSyncResponse = syncProtocol.decodeResponse(DOMUtils.parse(response));
		
		assertThat(decodedSyncResponse.getStatus()).isEqualTo(SyncStatus.NOT_YET_PROVISIONNED);
	}

	@Test
	public void testDecodeAddResponse() throws Exception {
		String response = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<Class>Contacts</Class>" +
							"<SyncKey>a8480e22-1072-40eb-b4d9-da486b4c245b</SyncKey>" +
							"<CollectionId>55</CollectionId>" +
							"<Status>1</Status>" +
							"<Responses>" +
								"<Add>" +
									"<ClientId>1337</ClientId>" +
									"<ServerId>55:8</ServerId>" +
									"<Status>1</Status>" +
								"</Add>" +
							"</Responses>" +
						"</Collection>" +	
						"<Collection>" +
							"<Class>Contacts</Class>" +
							"<SyncKey>b9061a11-1072-40eb-b4d9-da486b4c245b</SyncKey>" +
							"<CollectionId>58</CollectionId>" +
							"<Status>1</Status>" +
							"<Responses>" +
								"<Add>" +
									"<ClientId>1339</ClientId>" +
									"<ServerId>58:12</ServerId>" +
									"<Status>1</Status>" +
								"</Add>" +
							"</Responses>" +
						"</Collection>" +	
					"</Collections>" +
				"</Sync>";

		SyncProtocol syncProtocol = newSyncProtocol(null, null, null);
		SyncResponse decodedSyncResponse = syncProtocol.decodeResponse(DOMUtils.parse(response));
		Document encodedResponse = syncProtocol.encodeResponse(decodedSyncResponse);
		
		assertThat(DOMUtils.serialize(encodedResponse)).isEqualTo(response);
	}

	@Test
	public void testDecodeFetchResponse() throws Exception {
		String response = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
				"<Collections>" +
					"<Collection>" +
						"<Class>Contacts</Class>" +
						"<SyncKey>a8480e22-1072-40eb-b4d9-da486b4c245b</SyncKey>" +
						"<CollectionId>55</CollectionId>" +
						"<Status>1</Status>" +
						"<Responses>" +
							"<Fetch>" +
								"<ServerId>55:3</ServerId>" +
								"<Status>1</Status>" +
								"<ApplicationData>" +
									"<FileAs>name0 lastname</FileAs>" +
									"<FirstName>name0</FirstName>" +
									"<LastName>lastname</LastName>" +
									"<Email1Address>name0.lastname@thilaire.lng.org</Email1Address>" +
									"<Body>" +
										"<Type>1</Type>" +
										"<EstimatedDataSize>0</EstimatedDataSize>" +
									"</Body>" +
									"<NativeBodyType>3</NativeBodyType>" +
								"</ApplicationData>" +
							"</Fetch>" +
						"</Responses>" +
					"</Collection>" +	
					"<Collection>" +
						"<Class>Contacts</Class>" +
						"<SyncKey>b9061a11-1072-40eb-b4d9-da486b4c245b</SyncKey>" +
						"<CollectionId>58</CollectionId>" +
						"<Status>1</Status>" +
						"<Responses>" +
							"<Fetch>" +
								"<ServerId>58:10</ServerId>" +
								"<Status>1</Status>" +
								"<ApplicationData>" +
									"<FileAs>name lastname</FileAs>" +
									"<FirstName>name</FirstName>" +
									"<LastName>lastname</LastName>" +
									"<Email1Address>name.lastname@thilaire.lng.org</Email1Address>" +
									"<Body>" +
										"<Type>1</Type>" +
										"<EstimatedDataSize>0</EstimatedDataSize>" +
									"</Body>" +
									"<NativeBodyType>3</NativeBodyType>" +
								"</ApplicationData>" +
							"</Fetch>" +
						"</Responses>" +
					"</Collection>" +	
				"</Collections>" +
			"</Sync>";

		SyncProtocol syncProtocol = newSyncProtocol(null, null, null);
		Document inputResponse = DOMUtils.parse(response);
		SyncResponse decodedSyncResponse = syncProtocol.decodeResponse(inputResponse);
		Document encodedResponse = syncProtocol.encodeResponse(decodedSyncResponse);

		Diff compareXML = XMLUnit.compareXML(inputResponse, encodedResponse);
		compareXML.overrideElementQualifier(AcceptDifferentNamespaceXMLUnit.newElementQualifier());
		compareXML.overrideDifferenceListener(AcceptDifferentNamespaceXMLUnit.newDifferenceListener());
		XMLAssert.assertXMLEqual(compareXML, true);
	}

	@Test
	public void testDecodeComplexeResponse() throws Exception {
		String response = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<Sync>" +
				"<Collections>" +
					"<Collection>" +
						"<Class>Contacts</Class>" +
						"<SyncKey>a8480e22-1072-40eb-b4d9-da486b4c245b</SyncKey>" +
						"<CollectionId>55</CollectionId>" +
						"<Status>1</Status>" +
						"<Responses>" +
							"<Add>" +
								"<ClientId>1337</ClientId>" +
								"<ServerId>55:8</ServerId>" +
								"<Status>1</Status>" +
							"</Add>" +
						"</Responses>" +
					"</Collection>" +
					"<Collection>" +
						"<Class>Contacts</Class>" +
						"<SyncKey>a1f2f8z9-1072-40eb-b4d9-da486b4c245b</SyncKey>" +
						"<CollectionId>56</CollectionId>" +
						"<Status>1</Status>" +
						"<Responses>" +
							"<Fetch>" +
								"<ServerId>56:16</ServerId>" +
								"<Status>1</Status>" +
								"<ApplicationData>" +
									"<FileAs>name2 lastname</FileAs>" +
									"<FirstName>name2</FirstName>" +
									"<LastName>lastname</LastName>" +
									"<Email1Address>name2.lastname@thilaire.lng.org</Email1Address>" +
									"<Body>" +
										"<Type>1</Type>" +
										"<EstimatedDataSize>0</EstimatedDataSize>" +
									"</Body>" +
									"<NativeBodyType>3</NativeBodyType>" +
								"</ApplicationData>" +
							"</Fetch>" +
						"</Responses>" +
					"</Collection>" +	
					"<Collection>" +
						"<Class>Contacts</Class>" +
						"<SyncKey>adfbf3a5-840e-4215-b0de-783da605d760</SyncKey>" +
						"<CollectionId>65</CollectionId>" +
						"<Status>1</Status>" +
						"<Commands>" +
							"<Add>" +
								"<ServerId>65:3</ServerId>" +
								"<ApplicationData>" +
									"<FileAs>name lastname</FileAs>" +
									"<FirstName>name</FirstName>" +
									"<LastName>lastname</LastName>" +
									"<Email1Address>name.lastname@thilaire.lng.org</Email1Address>" +
									"<Body>" +
										"<Type>1</Type>" +
										"<EstimatedDataSize>0</EstimatedDataSize>" +
									"</Body>" +
									"<NativeBodyType>3</NativeBodyType>" +
								"</ApplicationData>" +
							"</Add>" +
							"<Delete>" +
								"<ServerId>65:2</ServerId>" +
							"</Delete>" +
						"</Commands>" +
					"</Collection>" +
				"</Collections>" +
			"</Sync>";

		SyncProtocol syncProtocol = newSyncProtocol(null, null, null);
		Document inputResponse = DOMUtils.parse(response);
		SyncResponse decodedSyncResponse = syncProtocol.decodeResponse(inputResponse);
		Document encodedResponse = syncProtocol.encodeResponse(decodedSyncResponse);

		Diff compareXML = XMLUnit.compareXML(inputResponse, encodedResponse);
		compareXML.overrideElementQualifier(AcceptDifferentNamespaceXMLUnit.newElementQualifier());
		compareXML.overrideDifferenceListener(AcceptDifferentNamespaceXMLUnit.newDifferenceListener());
		XMLAssert.assertXMLEqual(compareXML, true);
	}

	private BodyPreference bodyPreference(Integer bodyType, Integer truncationSize, Boolean allOrNone) {
		return BodyPreference.builder()
			.bodyType(MSEmailBodyType.getValueOf(bodyType))
			.truncationSize(truncationSize)
			.allOrNone(allOrNone)
			.build();
	}
	
	private SyncProtocol newSyncProtocol() {
		return newSyncProtocol(null, null, null);
	}
	
	private SyncProtocol newSyncProtocol(SyncedCollectionDao syncedCollectionDao, CollectionDao collectionDao,
			CollectionPathHelper collectionPathHelper) {
		SyncDecoder syncDecoder = new SyncDecoderTest();
		SyncEncoder syncEncoder = new SyncEncoderTest();
		EncoderFactory encoderFactory = new EncoderFactoryTest();
		SyncAnalyser syncAnalyser = new SyncAnalyserTest(syncedCollectionDao, collectionDao, collectionPathHelper);
		return new SyncProtocol(syncDecoder, syncAnalyser, syncEncoder, encoderFactory, udr);
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
		options.setFilterType(FilterType.THREE_DAYS_BACK);
		
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
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, windowSize, PIMDataType.EMAIL);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey, PIMDataType pimDataType) {
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, DEFAULT_WINDOW_SIZE, pimDataType);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(
			int collectionId, String syncKey, Integer windowSize, PIMDataType pimDataType) {
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath(pimDataType, collectionId));
		syncCollection.setDataType(pimDataType);
		syncCollection.setSyncKey(new SyncKey(syncKey));
		syncCollection.setWindowSize(windowSize);
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncCollection);
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(
			int collectionId, SyncCollection syncCollection) {
		SyncedCollectionDao syncedCollectionDao = createMock(SyncedCollectionDao.class);
		expect(syncedCollectionDao.get(credentials, device, collectionId)).andReturn(null);
		syncedCollectionDao.put(credentials, device, syncCollection);
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
	
	static class SyncDecoderTest extends SyncDecoder {

		protected SyncDecoderTest() {
			super(new DecoderFactoryTest());
		}
	}
	
	static class SyncEncoderTest extends SyncEncoder {}
	
	static class SyncAnalyserTest extends SyncAnalyser {

		protected SyncAnalyserTest(
				SyncedCollectionDao syncedCollectionStoreService,
				CollectionDao collectionDao,
				CollectionPathHelper collectionPathHelper) {
			super(syncedCollectionStoreService, collectionDao, collectionPathHelper, new DecoderFactoryTest());
		}

	}
	
	static class DecoderFactoryTest extends DecoderFactory {

		protected DecoderFactoryTest() {
			super(null,
					new Provider<ContactDecoder>() {
						@Override
						public ContactDecoder get() {
							return new ContactDecoder(null, null);
						}
					},
					null,
					new Provider<MSEmailDecoder>() {
						@Override
						public MSEmailDecoder get() {
							return new MSEmailDecoder(null){};
						}
					});
		}
	}
	
	static class EncoderFactoryTest extends EncoderFactory {

		protected EncoderFactoryTest() {
			super(null,
					new Provider<ContactEncoder>() {
						@Override
						public ContactEncoder get() {
							return new ContactEncoder(){};
						}
					},
					null, null, null);
		}
	}
}
