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
package org.obm.opush.command.email;

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;
import static org.obm.opush.IntegrationTestUtils.appendToINBOX;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.loadEmail;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Date;

import org.easymock.IMocksControl;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.IntegrationTestUtils;
import org.obm.opush.MailBackendTestModule;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.Configuration;
import org.obm.opush.env.ConfigurationModule.PolicyConfigurationProvider;
import org.obm.push.bean.ServerId;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.client.user.UserClient;
import org.obm.sync.date.DateProvider;
import org.obm.sync.push.client.OPClient;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.SimpleStoredMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(SlowGuiceRunner.class) @Slow
@GuiceModule(MailBackendTestModule.class)
public class SmartReplyHandlerTest {

	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	@Inject GreenMail greenMail;
	@Inject PolicyConfigurationProvider policyConfigurationProvider;
	@Inject DateProvider dateProvider;
	
	private OpushUser user;
	private GreenMailUser greenMailUser;
	private String inboxCollectionPath;
	private int inboxCollectionId;
	private MailFolder inboxFolder;
	private MailFolder sentFolder;
	private ServerId serverId;

	@Before
	public void setUp() throws Exception {
		user = singleUserFixture.jaures;
		greenMail.start();
		greenMailUser = greenMail.setUser(user.user.getLoginAtDomain(), user.password);
		sentFolder = greenMail.getManagers().getImapHostManager().createMailbox(greenMailUser, EmailConfiguration.IMAP_SENT_NAME);
		inboxFolder = greenMail.getManagers().getImapHostManager().getInbox(greenMailUser);
		
		inboxCollectionPath = IntegrationTestUtils.buildEmailInboxCollectionPath(user);
		inboxCollectionId = 1;
		serverId = new ServerId(ServerId.buildServerIdString(inboxCollectionId, 1l));
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		UserClient userClient = classToInstanceMap.get(UserClient.class);
		mockUsersAccess(classToInstanceMap, Lists.newArrayList(user));
		expect(collectionDao.getCollectionPath(inboxCollectionId)).andReturn(inboxCollectionPath).anyTimes();
		expect(userClient.getUserEmail(user.accessToken)).andReturn(user.user.getLoginAtDomain()).anyTimes();
		expect(policyConfigurationProvider.get()).andReturn("fakeConfiguration").anyTimes();
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testReplyWithAlternativeToAnAlternative() throws Exception {
		appendToINBOX(greenMailUser, "eml/multipartAlternative.eml");
		assertThat(sentFolder.getMessageCount()).isEqualTo(0);
		
		mocksControl.replay();
		opushServer.start();
		boolean success = opClient().emailReply(loadEmail("eml/multipartAlternative.eml"), inboxCollectionId, serverId);
		mocksControl.verify();
		
		assertThat(success).isTrue();
		assertThat(sentFolder.getMessageCount()).isEqualTo(1);
	}

	@Test
	public void testReplyWithTextToAnAlternative() throws Exception {
		appendToINBOX(greenMailUser, "eml/multipartAlternative.eml");
		assertThat(sentFolder.getMessageCount()).isEqualTo(0);
		
		mocksControl.replay();
		opushServer.start();
		boolean success = opClient().emailReply(loadEmail("eml/textPlain.eml"), inboxCollectionId, serverId);
		mocksControl.verify();
		
		assertThat(success).isTrue();
		assertThat(sentFolder.getMessageCount()).isEqualTo(1);
	}

	@Test
	public void testReplyWithHtmlToAnAlternative() throws Exception {
		appendToINBOX(greenMailUser, "eml/multipartAlternative.eml");
		assertThat(sentFolder.getMessageCount()).isEqualTo(0);
		
		mocksControl.replay();
		opushServer.start();
		boolean success = opClient().emailReply(loadEmail("eml/textHtml.eml"), inboxCollectionId, serverId);
		mocksControl.verify();
		
		assertThat(success).isTrue();
		assertThat(sentFolder.getMessageCount()).isEqualTo(1);
	}	
	
	@Test
	public void testOBMFULL4924() throws Exception {
		appendToINBOX(greenMailUser, "eml/OBMFULL-4924-inboxEmail.eml");
		assertThat(sentFolder.getMessageCount()).isEqualTo(0);
		
		mocksControl.replay();
		opushServer.start();
		boolean success = opClient().emailReply(loadEmail("eml/OBMFULL-4924-replyingEmail.eml"), inboxCollectionId, serverId);
		mocksControl.verify();
		
		assertThat(success).isTrue();
		assertThat(sentFolder.getMessageCount()).isEqualTo(1);
	}

	@Test
	public void testReplySendsReportEmailWhenError() throws Exception {
		appendToINBOX(greenMailUser, "eml/multipartAlternative.eml");
		
		Date expectedDate = date("2012-05-04T11:30:12");
		expect(dateProvider.getDate()).andReturn(expectedDate);
		
		mocksControl.replay();
		opushServer.start();
		boolean success = opClient().emailReply(loadEmail("eml/badToSyntax.eml"), inboxCollectionId, serverId);
		mocksControl.verify();
		
		assertThat(success).isTrue();
		assertThat(sentFolder.getMessageCount()).isEqualTo(0);
		assertThat(inboxFolder.getMessages().size()).isEqualTo(2);
		SimpleStoredMessage inboxMessage = inboxFolder.getMessages().get(1);
		assertThat(inboxMessage.getMimeMessage().getSentDate()).isEqualTo(expectedDate);
	}
	
	@Test
	public void testQuotaExceededErrorMail() throws Exception {
		appendToINBOX(greenMailUser, "eml/quotaExceeded.eml");
		MailFolder inboxFolder = greenMail.getManagers().getImapHostManager().getFolder(greenMailUser, EmailConfiguration.IMAP_INBOX_NAME);
		assertThat(inboxFolder.getMessageCount()).isEqualTo(1);
		
		expect(dateProvider.getDate()).andReturn(date("2012-05-04T11:30:12"));
		
		mocksControl.replay();
		opushServer.start();
		boolean success = opClient().emailReply(loadEmail("eml/quotaExceeded.eml"), inboxCollectionId, serverId);
		mocksControl.verify();
		
		assertThat(success).isTrue();
		assertThat(inboxFolder.getMessageCount()).isEqualTo(2);
	}

	private OPClient opClient() {
		return buildWBXMLOpushClient(user, opushServer.getPort());
	}
}
