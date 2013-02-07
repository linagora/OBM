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
package org.obm.opush.command.moveitem;

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.easymock.IMocksControl;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.ImapConnectionCounter;
import org.obm.opush.IntegrationTestUtils;
import org.obm.opush.MailBackendTestModule;
import org.obm.opush.PendingQueriesLock;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.Configuration;
import org.obm.push.bean.MoveItemsStatus;
import org.obm.push.mail.imap.GuiceModule;
import org.obm.push.mail.imap.SlowGuiceRunner;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.MoveItemsResponse;
import org.obm.sync.push.client.MoveItemsResponse.MoveResult;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.commands.MoveItemsCommand.Move;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

@RunWith(SlowGuiceRunner.class) @Slow
@GuiceModule(MailBackendTestModule.class)
public class MoveItemsHandlerTest {

	@Inject	SingleUserFixture singleUserFixture;
	@Inject	OpushServer opushServer;
	@Inject	ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject GreenMail greenMail;
	@Inject IMocksControl mocksControl;
	@Inject PendingQueriesLock pendingQueries;
	@Inject ImapConnectionCounter imapConnectionCounter;
	@Inject Configuration configuration;
	
	private CollectionDao collectionDao;

	private GreenMailUser greenMailUser;
	private ImapHostManager imapHostManager;
	private OpushUser user;
	private String mailbox;
	private String inboxCollectionPath;
	private int inboxCollectionId;
	private String trashCollectionPath;
	private int trashCollectionId;

	@Before
	public void init() throws Exception {
		user = singleUserFixture.jaures;
		greenMail.start();
		mailbox = user.user.getLoginAtDomain();
		greenMailUser = greenMail.setUser(mailbox, user.password);
		imapHostManager = greenMail.getManagers().getImapHostManager();
		imapHostManager.createMailbox(greenMailUser, "Trash");

		inboxCollectionPath = IntegrationTestUtils.buildEmailInboxCollectionPath(user);
		inboxCollectionId = 1234;
		trashCollectionPath = IntegrationTestUtils.buildEmailTrashCollectionPath(user);
		trashCollectionId = 1645;
		
		collectionDao = classToInstanceMap.get(CollectionDao.class);

		bindCollectionIdToPath();
	}

	private void bindCollectionIdToPath() throws Exception {
		expect(collectionDao.getCollectionPath(inboxCollectionId)).andReturn(inboxCollectionPath).anyTimes();
		expect(collectionDao.getCollectionPath(trashCollectionId)).andReturn(trashCollectionPath).anyTimes();
		expect(collectionDao.getCollectionMapping(user.device, trashCollectionPath)).andReturn(trashCollectionId).anyTimes();
	}

	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		greenMail.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testMoveInboxToTrash() throws Exception {
		String emailId1 = ":1";
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		mocksControl.replay();
		opushServer.start();
		sendEmailsToImapServer("email body data");
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		MoveItemsResponse response = opClient.moveItems(
				new Move(inboxCollectionId + emailId1, inboxCollectionId, trashCollectionId));
		mocksControl.verify();

		MoveResult moveResult = Iterables.getOnlyElement(response.getMoveResults());
		assertThat(moveResult.status).isEqualTo(MoveItemsStatus.SUCCESS);
		assertThat(moveResult.srcMsgId).isEqualTo(inboxCollectionId + emailId1);
		assertThat(moveResult.dstMsgId).startsWith(trashCollectionId + ":");

		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(1);
	}

	@Test
	public void testTwoMoveInboxToTrash() throws Exception {
		String emailId1 = ":1";
		String emailId2 = ":2";
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		mocksControl.replay();
		opushServer.start();
		sendEmailsToImapServer("email one", "email two");
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		MoveItemsResponse response = opClient.moveItems(
				new Move(inboxCollectionId + emailId1, inboxCollectionId, trashCollectionId),
				new Move(inboxCollectionId + emailId2, inboxCollectionId, trashCollectionId));
		mocksControl.verify();

		MoveResult moveResult = Iterables.get(response.getMoveResults(), 0);
		assertThat(moveResult.status).isEqualTo(MoveItemsStatus.SUCCESS);
		assertThat(moveResult.srcMsgId).isEqualTo(inboxCollectionId + emailId1);
		assertThat(moveResult.dstMsgId).isEqualTo(trashCollectionId + emailId1);
		
		MoveResult moveResult2 = Iterables.get(response.getMoveResults(), 1);
		assertThat(moveResult2.status).isEqualTo(MoveItemsStatus.SUCCESS);
		assertThat(moveResult2.srcMsgId).isEqualTo(inboxCollectionId + emailId2);
		assertThat(moveResult2.dstMsgId).isEqualTo(trashCollectionId + emailId2);
		
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(2);
	}

	@Ignore("Greenmail has replied that the command succeed")
	@Test
	public void testMoveUnexistingEmailFromInboxToTrash() throws Exception {
		String unexistingEmailId1 = ":1561";
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		mocksControl.replay();
		opushServer.start();
		sendEmailsToImapServer("email body data");
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		MoveItemsResponse response = opClient.moveItems(
				new Move(inboxCollectionId + unexistingEmailId1, inboxCollectionId, trashCollectionId));
		mocksControl.verify();

		MoveResult moveResult = Iterables.getOnlyElement(response.getMoveResults());
		assertThat(moveResult.status).isEqualTo(MoveItemsStatus.INVALID_SOURCE_COLLECTION_ID);
		assertThat(moveResult.srcMsgId).isEqualTo(inboxCollectionId + unexistingEmailId1);
		assertThat(moveResult.dstMsgId).isNull();
	}
	
	private void sendEmailsToImapServer(String...bodies) throws InterruptedException {
		for (String body : bodies) {
			GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject", body, greenMail.getSmtp().getServerSetup());
		}
		greenMail.waitForIncomingEmail(bodies.length);
	}
}
