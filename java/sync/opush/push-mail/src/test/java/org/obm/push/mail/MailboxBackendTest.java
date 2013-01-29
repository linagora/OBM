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
package org.obm.push.mail;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;
import org.obm.push.mail.transformer.Transformer;
import org.obm.push.mail.transformer.Transformer.TransformersFactory;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.UserEmailParserUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

@RunWith(SlowFilterRunner.class)
public class MailboxBackendTest {

	private String mailbox;
	private String password;
	private UserDataRequest udr;
	private Device device;
	
	private IMocksControl mocks;
	private MailBackendImpl mailBackendImpl;
	private MailboxService mailboxService;
	private MappingService mappingService;
	private MSEmailFetcher msEmailFetcher;
	private TransformersFactory transformersFactory;
	private MailViewToMSEmailConverter msEmailConverter;
	private Transformer transformer;
	private EventService eventService;
	private SnapshotService snapshotService;
	private WindowingService windowingService;

	@Before
	public void setUp() {
		mailbox = "to@localhost.com";
		password = "password";
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), null);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, device);
		mocks = createControl();
		mailboxService = mocks.createMock(MailboxService.class);
		mappingService = mocks.createMock(MappingService.class);
		UserEmailParserUtils emailParserUtils = new UserEmailParserUtils();
		MSEmailHeaderConverter msEmailHeaderConverter = new MSEmailHeaderConverter(emailParserUtils );
		eventService = mocks.createMock(EventService.class);
		msEmailConverter = new MailViewToMSEmailConverterImpl(msEmailHeaderConverter , eventService );
		transformersFactory = mocks.createMock(TransformersFactory.class);
		transformer = mocks.createMock(Transformer.class);
		expect(transformersFactory.create(anyObject(FetchInstruction.class))).andReturn(transformer).anyTimes();
		msEmailFetcher = new MSEmailFetcher(mailboxService, transformersFactory, msEmailConverter);
		snapshotService = mocks.createMock(SnapshotService.class);
		windowingService = mocks.createMock(WindowingService.class);
		
		mailBackendImpl = new MailBackendImpl(mailboxService, null, null, null, null,
				snapshotService, null, mappingService, null, msEmailFetcher, null, null, windowingService);
	}
	
	@Test(expected=ItemNotFoundException.class)
	public void testFetchWithFailure() throws Exception {
		long itemId = 2;
		int collectionId = 1;
		String serverId = collectionId + ":" + itemId;
		ImmutableList<BodyPreference> bodyPreferences = ImmutableList.of(BodyPreference.builder().bodyType(MSEmailBodyType.MIME).build());
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(bodyPreferences);
		String collectionPath = "INBOX";

		expect(mailboxService.fetchEmailMetadata(udr, collectionPath, itemId)).andThrow(new ItemNotFoundException("failure"));
		expect(mappingService.getCollectionIdFromServerId(serverId)).andReturn(collectionId);
		expect(mappingService.getItemIdFromServerId(serverId)).andReturn(Ints.checkedCast(itemId));
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath);
		
		mocks.replay();
		try {
			mailBackendImpl.fetch(udr, collectionId, ImmutableList.of(serverId), syncCollectionOptions);
		} catch (ItemNotFoundException e) {
			mocks.verify();
			throw e;
		}
	}

	@Test
	public void testFetchMimeSinglePartBase64Email() throws Exception {
		long itemId = 2;
		int collectionId = 1;
		String serverId = collectionId + ":" + itemId;
		ImmutableList<BodyPreference> bodyPreferences = ImmutableList.of(BodyPreference.builder().bodyType(MSEmailBodyType.MIME).build());
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(bodyPreferences);
		String collectionPath = "INBOX";

		InputStream mailStream = loadEmail("SinglePartBase64.eml");
		expect(transformer.targetType()).andReturn(MSEmailBodyType.MIME);
		expect(transformer.transform(mailStream, Charsets.UTF_8)).andReturn(mailStream);
		
		mockMailboxServiceFetchFullMail(mailStream, itemId, collectionPath);
		expect(mappingService.getCollectionIdFromServerId(serverId)).andReturn(collectionId);
		expect(mappingService.getItemIdFromServerId(serverId)).andReturn(Ints.checkedCast(itemId));
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath);
		expect(mappingService.getServerIdFor(collectionId, String.valueOf(itemId))).andReturn(serverId);
		
		SyncKey previousSyncKey = new SyncKey("123");
		ItemSyncState previousItemSyncState = ItemSyncState.builder()
				.id(1)
				.syncKey(previousSyncKey)
				.syncDate(date("2012-01-01T11:22:33"))
				.build();

		SyncKey newSyncKey = new SyncKey("456");
		snapshotService.actualizeSnapshot(device.getDevId(), previousSyncKey, collectionId, newSyncKey);
		expectLastCall();
		
		mocks.replay();		
		List<ItemChange> emails = mailBackendImpl.fetch(udr, collectionId, ImmutableList.of(serverId), syncCollectionOptions, previousItemSyncState, newSyncKey);
		mocks.verify();
		
		MSEmail actual = (MSEmail) Iterables.getOnlyElement(emails).getData();
		assertThat(actual.getBody().getMimeData()).hasContentEqualTo(loadEmail("SinglePartBase64.eml"));
	}

	@Ignore("greenmail seems to unexpectedly decode base64 part on-the-fly")
	@Test
	public void testFetchTextPlainSinglePartBase64Email() throws Exception {
		int itemId = 2;
		int collectionId = 1;
		String serverId = collectionId + ":" + itemId;
		String collectionPath = "INBOX";

		InputStream mailStream = loadEmail("SinglePartBase64.eml");
		expect(transformer.targetType()).andReturn(MSEmailBodyType.PlainText);
		expect(transformer.transform(mailStream, Charsets.UTF_8)).andReturn(mailStream);
		
		mockMailboxServiceFetchFullMail(mailStream, itemId, collectionPath);
		expect(mappingService.getCollectionIdFromServerId(serverId)).andReturn(collectionId);
		expect(mappingService.getItemIdFromServerId(serverId)).andReturn(Ints.checkedCast(itemId));
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath);
		expect(mappingService.getServerIdFor(collectionId, String.valueOf(itemId))).andReturn(serverId);
		
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(
				ImmutableList.of(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText).build()));
		
		SyncKey previousSyncKey = new SyncKey("123");
		ItemSyncState previousItemSyncState = ItemSyncState.builder()
				.id(1)
				.syncKey(previousSyncKey)
				.syncDate(date("2012-01-01T11:22:33"))
				.build();

		SyncKey newSyncKey = new SyncKey("456");
		snapshotService.actualizeSnapshot(device.getDevId(), previousSyncKey, collectionId, newSyncKey);
		expectLastCall();
		
		mocks.replay();
		List<ItemChange> emails = mailBackendImpl.fetch(udr, collectionId, ImmutableList.of(serverId), syncCollectionOptions, previousItemSyncState, newSyncKey);
		mocks.verify();
		
		MSEmail actual = (MSEmail) Iterables.getOnlyElement(emails).getData();
		String bodyText = new String(ByteStreams.toByteArray(actual.getBody().getMimeData()), Charsets.UTF_8);
		assertThat(bodyText).contains("Envoyé de mon iPhone");
	}
	
	private void mockMailboxServiceFetchFullMail(InputStream mailStream, long itemId, String collectionPath) {
		mockMailboxServiceFetchEmailView(itemId, collectionPath);
		expectFetchMailStream(collectionPath, itemId, mailStream);
	}

	private void mockMailboxServiceFetchEmailView(long itemId, String collectionPath) {
		MimeMessage mimeMessage = buildMimeMessage(itemId);
		expect(mailboxService.fetchEmailMetadata(udr, collectionPath, itemId)).andReturn(
				EmailMetadata.builder()
					.uid(mimeMessage.getUid())
					.size(mimeMessage.getSize())
					.flags(new FlagsList(ImmutableList.of(Flag.SEEN)))
					.envelope(buildEnvelope())
					.mimeMessage(mimeMessage)
					.build());
	}
	
	@Test
	public void testFetchWithoutCorrespondingBodyPreference() throws Exception {
		int itemId = 2;
		int collectionId = 1;
		String serverId = collectionId + ":" + itemId;
		String collectionPath = "INBOX";

		final Capture<InputStream> capturedStream = new Capture<InputStream>();
		expect(transformer.targetType()).andReturn(MSEmailBodyType.MIME);
		expect(transformer.transform(capture(capturedStream), eq(Charsets.UTF_8)))
			.andAnswer(new IAnswer<InputStream>() {

				@Override
				public InputStream answer() throws Throwable {
					return capturedStream.getValue();
				}
			});

		mockMailboxServiceFetchFullMailWithMimePartAddress(loadEmail("OBMFULL-4123.eml"), itemId, collectionPath);
		expect(mappingService.getCollectionIdFromServerId(serverId)).andReturn(collectionId);
		expect(mappingService.getItemIdFromServerId(serverId)).andReturn(Ints.checkedCast(itemId));
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath);
		expect(mappingService.getServerIdFor(collectionId, String.valueOf(itemId))).andReturn(serverId);
		
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(
				ImmutableList.of(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText).build()));
		
		SyncKey previousSyncKey = new SyncKey("123");
		ItemSyncState previousItemSyncState = ItemSyncState.builder()
				.id(1)
				.syncKey(previousSyncKey)
				.syncDate(date("2012-01-01T11:22:33"))
				.build();

		SyncKey newSyncKey = new SyncKey("456");
		snapshotService.actualizeSnapshot(device.getDevId(), previousSyncKey, collectionId, newSyncKey);
		expectLastCall();
		
		mocks.replay();
		List<ItemChange> emails = mailBackendImpl.fetch(udr, collectionId, ImmutableList.of(serverId), syncCollectionOptions, previousItemSyncState, newSyncKey);
		mocks.verify();
		
		MSEmail actual = (MSEmail) Iterables.getOnlyElement(emails).getData();
		assertThat(actual.getBody().getMimeData()).hasContentEqualTo(loadEmail("OBMFULL-4123.eml"));
		actual.getBody().getMimeData().reset();
		assertThat(capturedStream.hasCaptured()).isTrue();
		assertThat(capturedStream.getValue()).hasContentEqualTo(loadEmail("OBMFULL-4123.eml"));
	}

	private void mockMailboxServiceFetchFullMailWithMimePartAddress(InputStream mailStream, int itemId, String collectionPath) {
		mockMailboxServiceFetchEmailView(itemId, collectionPath);
		expectFetchMimePartStream(collectionPath, itemId, mailStream, new MimeAddress("1"));
	}
	
	private MimeMessage buildMimeMessage(long uid) {
		return MimeMessage.builder()
				.uid(uid)
				.addChild(buildMimePart())
				.size(1)
				.build();
	}
	
	private MimePart buildMimePart() {
		return MimePart.builder()
				.contentType("text/plain; charset= utf-8")
				.size(1)
				.build();
	}
	
	private Envelope buildEnvelope() {
		Address address = new Address(mailbox);
		return Envelope.builder()
				.to(ImmutableList.of(address))
				.cc(ImmutableList.of(address))
				.from(ImmutableList.of(address))
				.bcc(ImmutableList.of(address))
				.replyTo(ImmutableList.of(address))
				.build();
	}

	private InputStream loadEmail(String name) throws IOException {
		return new ByteArrayInputStream(ByteStreams.toByteArray(ClassLoader.getSystemResourceAsStream("eml/" + name)));
	}

	public void expectFetchFlags(String collectionName, long uid, FlagsList value) {
		expect(mailboxService.fetchFlags(udr, collectionName, MessageSet.singleton(uid))).andReturn(ImmutableMap.of(uid, value));
	}

	public void expectFetchEnvelope(String collectionName, long uid, UIDEnvelope envelope) {
		expect(mailboxService.fetchEnvelope(udr, collectionName, MessageSet.singleton(uid)))
			.andReturn(ImmutableList.of(envelope));
	}

	public void expectFetchBodyStructure(String collectionName, long uid, MimeMessage mimeMessage) {
		expect(mailboxService.fetchBodyStructure(udr, collectionName, MessageSet.singleton(uid)))
			.andReturn(ImmutableList.of(mimeMessage));
	}

	public void expectFetchMailStream(String collectionName, long uid, InputStream mailStream) {
		expect(mailboxService.fetchMailStream(udr, collectionName, uid))
				.andReturn(mailStream);
	}

	public void expectFetchMimePartStream(String collectionName, long uid, InputStream mailStream, MimeAddress partAddress) {
		expect(mailboxService.fetchMimePartStream(udr, collectionName, uid, partAddress))
			.andReturn(mailStream);
	}
}
