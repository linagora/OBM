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
package org.obm.push.mail.imap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.IntegrationTestUtils;
import org.obm.opush.PortNumber;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.env.DefaultOpushModule;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.mail.MailBackendImpl;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;
import org.obm.push.mail.smtp.SmtpSender;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(SlowFilterRunner.class) @Slow
public class MailboxBackendTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(DefaultOpushModule.class);

	@Inject MailBackendImpl mailBackendImpl;
	@Inject CollectionPathHelper collectionPathHelper;
	@Inject EmailConfiguration emailConfiguration;
	@Inject SmtpSender smtpSender;
	@Inject LinagoraImapClientProvider linagoraImapClientProvider;
	@Inject LinagoraMailboxService linagoraMailboxService;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject @PortNumber int port;
	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;

	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	}
	
	@After
	public void tearDown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testFetchMimeSinglePartBase64Email() throws Exception {
		InputStream mailStream = loadEmail("SinglePartBase64.eml");
		
		int itemId = 2;
		int collectionId = 1;
		String serverId = collectionId + ":" + itemId;
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		IntegrationTestUtils.expectGetCollectionPath(collectionDao, collectionId, serverId);
		
		mockMailboxServiceFetchFullMail(mailStream, itemId, serverId);
		
		IntegrationTestUtils.replayMocks(classToInstanceMap);
		opushServer.start();
		
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(
				ImmutableList.of(BodyPreference.builder().bodyType(MSEmailBodyType.MIME).build()));
		List<ItemChange> emails = mailBackendImpl.fetch(udr, ImmutableList.of(serverId), syncCollectionOptions);
		MSEmail actual = (MSEmail) Iterables.getOnlyElement(emails).getData();
		
		IntegrationTestUtils.verifyMocks(classToInstanceMap);
		assertThat(actual.getBody().getMimeData()).hasContentEqualTo(loadEmail("SinglePartBase64.eml"));
	}

	@Ignore("greenmail seems to unexpectedly decode base64 part on-the-fly")
	@Test
	public void testFetchTextPlainSinglePartBase64Email() throws Exception {
		InputStream mailStream = loadEmail("SinglePartBase64.eml");
		
		int itemId = 2;
		int collectionId = 1;
		String serverId = collectionId + ":" + itemId;
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		IntegrationTestUtils.expectGetCollectionPath(collectionDao, collectionId, serverId);
		
		mockMailboxServiceFetchFullMail(mailStream, collectionId, serverId);
		
		IntegrationTestUtils.replayMocks(classToInstanceMap);
		opushServer.start();
		
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(
				ImmutableList.of(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText).build()));
		List<ItemChange> emails = mailBackendImpl.fetch(udr, ImmutableList.of("1:1"), syncCollectionOptions);
		MSEmail actual = (MSEmail) Iterables.getOnlyElement(emails).getData();
		String bodyText = new String(ByteStreams.toByteArray(actual.getBody().getMimeData()), Charsets.UTF_8);
		assertThat(bodyText).contains("Envoyé de mon iPhone");
	}
	
	private void mockMailboxServiceFetchFullMail(InputStream mailStream, int collectionId, String collectionPath) {
		LinagoraMailboxService mailboxService = classToInstanceMap.get(LinagoraMailboxService.class);
		IntegrationTestUtils.expectFetchFlags(mailboxService, udr, collectionPath, collectionId, ImmutableList.of(Flag.SEEN));
		IntegrationTestUtils.expectFetchEnvelope(mailboxService, udr, collectionPath, collectionId, buildUIDEnvelope(collectionId));
		IntegrationTestUtils.expectFetchBodyStructure(mailboxService, udr, collectionPath, collectionId, buildMimeMessage(collectionId));
		IntegrationTestUtils.expectFetchMailStream(mailboxService, udr, collectionPath, collectionId, mailStream);
	}
	
	@Test
	public void testFetchWithoutCorrespondingBodyPreference() throws Exception {
		InputStream mailStream = loadEmail("OBMFULL-4123.eml");
		
		int itemId = 2;
		int collectionId = 1;
		String serverId = collectionId + ":" + itemId;
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		IntegrationTestUtils.expectGetCollectionPath(collectionDao, collectionId, serverId);
		
		mockMailboxServiceFetchFullMailWithMimePartAddress(mailStream, collectionId, serverId);
		
		IntegrationTestUtils.replayMocks(classToInstanceMap);
		opushServer.start();
		
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(
				ImmutableList.of(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText).build()));
		List<ItemChange> emails = mailBackendImpl.fetch(udr, ImmutableList.of("1:1"), syncCollectionOptions);
		MSEmail actual = (MSEmail) Iterables.getOnlyElement(emails).getData();
		assertThat(actual.getBody().getMimeData()).hasContentEqualTo(loadEmail("OBMFULL-4123.eml"));
	}

	private void mockMailboxServiceFetchFullMailWithMimePartAddress(InputStream mailStream, int collectionId, String serverId) {
		LinagoraMailboxService mailboxService = classToInstanceMap.get(LinagoraMailboxService.class);
		IntegrationTestUtils.expectFetchFlags(mailboxService, udr, serverId, collectionId, ImmutableList.of(Flag.SEEN));
		IntegrationTestUtils.expectFetchEnvelope(mailboxService, udr, serverId, collectionId, buildUIDEnvelope(collectionId));
		IntegrationTestUtils.expectFetchBodyStructure(mailboxService, udr, serverId, collectionId, buildMimeMessage(collectionId));
		IntegrationTestUtils.expectFetchMimePartStream(mailboxService, udr, serverId, collectionId, mailStream, new MimeAddress("1"));
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
	
	private UIDEnvelope buildUIDEnvelope(int uid) {
		return new UIDEnvelope(uid, buildEnvelope());
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
}
