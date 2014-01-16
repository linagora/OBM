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
package org.obm.push.protocol;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.transform.TransformerException;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionCommand;
import org.obm.push.bean.SyncCollectionCommands;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncCollectionRequest;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.protocol.bean.AnalysedSyncRequest;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
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
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;

@GuiceModule(ProtocolModuleTest.class)
@RunWith(SlowGuiceRunner.class)
public class SyncProtocolTest {

	@Inject EncoderFactory encoderFactory;

	private final int DEFAULT_WINDOW_SIZE = 100;
	
	private User user;
	private Device device;
	private Credentials credentials;
	private UserDataRequest udr;
	private String mailbox;
	private String password;

	private IMocksControl mocks;
	private SyncedCollectionDao syncedCollectionDao;
	private CollectionDao collectionDao;
	private ICollectionPathHelper collectionPathHelper;
	
	@Before
	public void setUp() {
	    mailbox = "to@localhost.com";
	    password = "password";
		user = Factory.create().createUser(mailbox, mailbox, "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), ProtocolVersion.V121);
		credentials = new Credentials(user, password);
		udr = new UserDataRequest(credentials, "noCommand", device);
		
		mocks = createControl();
		syncedCollectionDao = mocks.createMock(SyncedCollectionDao.class);
		collectionDao = mocks.createMock(CollectionDao.class);
		collectionPathHelper = mocks.createMock(ICollectionPathHelper.class);
	}

	@Test
	public void testEncodeValidResponseRespectWindowsMobileOrderingExpectation() throws TransformerException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId, SyncStatus.OK);

		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newCollectionNoChangeResponse(collectionId));
	}
	
	@Test
	public void testEncodeResponseCollectionIdError() throws TransformerException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId, SyncStatus.OBJECT_NOT_FOUND);

		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newCollectionNotFoundResponse(collectionId));
	}
	
	@Test
	public void testEncodeResponseSyncKeyError() throws TransformerException {
		int collectionId = 515;
		SyncCollectionResponse collectionResponse = newSyncCollectionResponse(collectionId, SyncStatus.INVALID_SYNC_KEY);

		String endcodedResponse = encodeResponse(collectionResponse);
		
		assertThat(endcodedResponse).isEqualTo(newSyncKeyErrorResponse(collectionId));
	}
	
	private String newCollectionNoChangeResponse(int collectionId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<Class>Email</Class>" +
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
							"<Class>Email</Class>" +
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
							"<Class>Email</Class>" +
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
		return SyncResponse.builder()
				.addResponse(collectionResponse)
				.status(SyncStatus.OK)
				.build();
	}

	private SyncCollectionResponse newSyncCollectionResponse(int collectionId, SyncStatus status) {
		return SyncCollectionResponse.builder()
				.collectionId(collectionId)
				.syncKey(new SyncKey("123456789"))
				.dataType(PIMDataType.EMAIL)
				.status(status)
				.build();
	}
	
	@Test(expected=NoDocumentException.class)
	public void testDecodeRequestWithNullDocument() {
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getWaitInSecond()).isEqualTo(0);
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
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
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
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		SyncCollectionRequest expectedSyncCollection = SyncCollectionRequest.builder()
			.collectionId(syncingCollectionId)
			.dataType(null)
			.syncKey(new SyncKey(syncingCollectionSyncKey))
			.windowSize(100)
			.commands(SyncCollectionCommands.Request.builder().build())
			.options(null)
			.build();
		assertThat(syncRequest.getCollections()).containsOnly(expectedSyncCollection);
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getCollections()).hasSize(1);
		assertThat(syncRequest.getWindowSize()).isEqualTo(150);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		assertThat(syncCollection.getWindowSize()).isEqualTo(100);
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		SyncCollectionOptions syncCollectionOptions = syncCollection.getOptions();
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		SyncCollectionOptions syncCollectionOptions = syncCollection.getOptions();
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		SyncCollectionOptions syncCollectionOptions = syncCollection.getOptions();
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		SyncCollectionOptions syncCollectionOptions = syncCollection.getOptions();
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

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);

		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		SyncCollectionOptions syncCollectionOptions = syncCollection.getOptions();
		assertThat(syncCollectionOptions.getBodyPreferences()).containsOnly(bodyPreference(8, maxSpecTruncationSize, true));
	}

	@Test
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

		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		assertThat(syncCollection.getCommands().getCommands().iterator().next().getApplicationData()).isNull();
	}

	@Test
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

		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		SyncCollectionRequest syncCollection = syncRequest.getCollections().iterator().next();
		assertThat(syncCollection.getCommands().getCommands().iterator().next().getApplicationData()).isNull();
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
		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId("13579")
				.commandType(SyncCommand.ADD)
				.serverId("123")
				.applicationData(expectedMSContact)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.CONTACTS, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange);
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
		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId("13579")
				.commandType(SyncCommand.ADD)
				.serverId("123")
				.applicationData(expectedMSContact)
				.build();
		
		MSContact expectedMSContact2 = new MSContact();
		expectedMSContact2.setEmail1Address("opush2@obm.org");
		expectedMSContact2.setFileAs("Dobney2, JoLynn Julie");
		expectedMSContact2.setFirstName("JoLynn2");
		SyncCollectionCommand.Response expectedSyncCollectionChange2 = SyncCollectionCommand.Response.builder()
				.clientId("02468")
				.commandType(SyncCommand.ADD)
				.serverId("456")
				.applicationData(expectedMSContact2)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.CONTACTS, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.addCommand(expectedSyncCollectionChange2)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange, expectedSyncCollectionChange2);
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
		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId("13579")
				.commandType(SyncCommand.CHANGE)
				.serverId("123")
				.applicationData(expectedMSContact)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.CONTACTS, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange);
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
		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId("13579")
				.commandType(SyncCommand.CHANGE)
				.serverId("123")
				.applicationData(expectedMSContact)
				.build();
		
		MSContact expectedMSContact2 = new MSContact();
		expectedMSContact2.setEmail1Address("opush2@obm.org");
		expectedMSContact2.setFileAs("Dobney2, JoLynn Julie");
		expectedMSContact2.setFirstName("JoLynn2");
		SyncCollectionCommand.Response expectedSyncCollectionChange2 = SyncCollectionCommand.Response.builder()
				.clientId("02468")
				.commandType(SyncCommand.CHANGE)
				.serverId("456")
				.applicationData(expectedMSContact2)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.CONTACTS, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.addCommand(expectedSyncCollectionChange2)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange, expectedSyncCollectionChange2);
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

		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.FETCH)
				.serverId("123")
				.applicationData(null)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.EMAIL, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange);
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

		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.FETCH)
				.serverId("123")
				.applicationData(null)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionChange2 = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.FETCH)
				.serverId("456")
				.applicationData(null)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.EMAIL, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.addCommand(expectedSyncCollectionChange2)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange, expectedSyncCollectionChange2);
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

		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.DELETE)
				.serverId("123")
				.applicationData(null)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.EMAIL, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange);
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

		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.DELETE)
				.serverId("123")
				.applicationData(null)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionChange2 = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.DELETE)
				.serverId("456")
				.applicationData(null)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.EMAIL, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionChange)
					.addCommand(expectedSyncCollectionChange2)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType();
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);
		

		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(expectedSyncCollectionChange, expectedSyncCollectionChange2);
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

		SyncCollectionCommand.Response expectedSyncCollectionAdd = SyncCollectionCommand.Response.builder()
				.clientId("120")
				.commandType(SyncCommand.ADD)
				.serverId("12")
				.applicationData(expectedMSContact)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionAdd2 = SyncCollectionCommand.Response.builder()
				.clientId("130")
				.commandType(SyncCommand.ADD)
				.serverId("13")
				.applicationData(expectedMSContact)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionChange = SyncCollectionCommand.Response.builder()
				.clientId("340")
				.commandType(SyncCommand.CHANGE)
				.serverId("34")
				.applicationData(expectedMSContact)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionChange2 = SyncCollectionCommand.Response.builder()
				.clientId("350")
				.commandType(SyncCommand.CHANGE)
				.serverId("35")
				.applicationData(expectedMSContact)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionFetch = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.FETCH)
				.serverId("56")
				.applicationData(null)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionFetch2 = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.FETCH)
				.serverId("57")
				.applicationData(null)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionDelete = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.DELETE)
				.serverId("78")
				.applicationData(null)
				.build();
		SyncCollectionCommand.Response expectedSyncCollectionDelete2 = SyncCollectionCommand.Response.builder()
				.clientId(null)
				.commandType(SyncCommand.DELETE)
				.serverId("79")
				.applicationData(null)
				.build();
		
		SyncedCollectionDao syncedCollectionDao = mockReadThenWriteSyncedCollectionCache(
				syncingCollectionId, 
				syncingCollectionSyncKey, 
				DEFAULT_WINDOW_SIZE, 
				PIMDataType.CONTACTS, 
				SyncCollectionCommands.Response.builder()
					.addCommand(expectedSyncCollectionAdd)
					.addCommand(expectedSyncCollectionAdd2)
					.addCommand(expectedSyncCollectionChange)
					.addCommand(expectedSyncCollectionChange2)
					.addCommand(expectedSyncCollectionFetch)
					.addCommand(expectedSyncCollectionFetch2)
					.addCommand(expectedSyncCollectionDelete)
					.addCommand(expectedSyncCollectionDelete2)
					.build());
		CollectionDao collectionDao = mockFindCollectionPathForId(PIMDataType.CONTACTS, syncingCollectionId);
		ICollectionPathHelper collectionPathHelper = mockCollectionPathHelperRecognizeDataType(PIMDataType.CONTACTS);
		replay(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
		SyncRequest syncRequest = syncProtocol.decodeRequest(request);
		AnalysedSyncRequest analyzedRequest = syncProtocol.analyzeRequest(udr, syncRequest);

		verify(syncedCollectionDao, collectionDao, collectionPathHelper);
		
		assertThat(syncRequest.getCollections()).hasSize(1);
		AnalysedSyncCollection syncCollection = analyzedRequest.getSync().getCollection(syncingCollectionId);
		assertThat(syncCollection.getCommands().getCommands()).containsOnly(
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
		int syncingCollectionId = 2;
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Sync>" +
					"<Wait>0</Wait>" +
					"<Collections>" +
						"<Collection>" +
							"<Class>Contacts</Class>" +
							"<SyncKey>1234-5678</SyncKey>" +
							"<CollectionId>" + syncingCollectionId + "</CollectionId>" +
							"<Options><FilterType>2</FilterType><Conflict>1</Conflict></Options>" +
							"<Commands>" +
								"<Delete>" +
									"<ServerId>79</ServerId>" +
								"</Delete>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
		
		
		SyncProtocol syncProtocol = newSyncProtocol(syncedCollectionDao, collectionDao, collectionPathHelper);
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
			ICollectionPathHelper collectionPathHelper) {
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
		return mockReadThenWriteSyncedCollectionCache(collectionId, syncKey, DEFAULT_WINDOW_SIZE, PIMDataType.EMAIL,
				SyncCollectionCommands.Response.builder().build());
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(int collectionId, String syncKey, Integer windowSize, PIMDataType dataType, SyncCollectionCommands.Response syncCollectionCommands) {
		return mockReadThenWriteSyncedCollectionCache(collectionId,
				AnalysedSyncCollection.builder()
					.collectionId(collectionId)
					.collectionPath(collectionPath(dataType, collectionId))
					.dataType(dataType)
					.syncKey(new SyncKey(syncKey))
					.windowSize(windowSize)
					.commands(syncCollectionCommands)
					.status(SyncStatus.OK)
					.build());
	}

	private SyncedCollectionDao mockReadThenWriteSyncedCollectionCache(
			int collectionId, AnalysedSyncCollection syncCollection) {
		SyncedCollectionDao syncedCollectionDao = createMock(SyncedCollectionDao.class);
		expect(syncedCollectionDao.get(credentials, device, collectionId)).andReturn(null);
		syncedCollectionDao.put(credentials, device, syncCollection);
		expectLastCall();
		return syncedCollectionDao;
	}
	
	private ICollectionPathHelper mockCollectionPathHelperRecognizeDataType() {
		return mockCollectionPathHelperRecognizeDataType(PIMDataType.EMAIL);
	}

	private ICollectionPathHelper mockCollectionPathHelperRecognizeDataType(PIMDataType pimDataType) {
		ICollectionPathHelper collectionPathHelper = createMock(ICollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(anyObject(String.class))).andReturn(pimDataType);
		return collectionPathHelper;
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
				ICollectionPathHelper collectionPathHelper) {
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
