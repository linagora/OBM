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
package org.obm.push.minig.imap.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.OpushLocatorException;
import org.obm.push.mail.IMAPException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.imap.LinagoraImapClientProvider;
import org.obm.push.minig.imap.StoreClient;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@Slow
@GuiceModule(org.obm.push.minig.imap.MailEnvModule.class)
@RunWith(SlowGuiceRunner.class)
public class CreateCommandIntegrationTest {

	@Inject LinagoraImapClientProvider clientProvider;

	@Inject ICollectionPathHelper collectionPathHelper;
	@Inject MailboxService mailboxService;
	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testCreateMailboxWithAccent() throws Exception {
		StoreClient client = loggedClient();

		boolean result = client.create("déplacements");
		ListResult folders = client.listAll();
		
		assertThat(result).isTrue();
		assertThat(folders).containsOnly(
				mailbox("INBOX"),
				mailbox("déplacements"));
	}
	
	@Test
	public void testCreateMailboxMany() throws Exception {
		StoreClient client = loggedClient();

		boolean result1 = client.create("déplacements");
		boolean result2 = client.create("another");
		boolean result3 = client.create("another/déplacements");
		ListResult folders = client.listAll();
		
		assertThat(result1).isEqualTo(result2).isEqualTo(result3).isTrue();
		assertThat(folders).containsOnly(
				mailbox("INBOX"),
				mailbox("déplacements"),
				mailbox("another"),
				mailbox("another/déplacements"));
	}
	
	@Test
	public void testCreateMailboxChinese() throws Exception {
		StoreClient client = loggedClient();

		boolean result = client.create("&Ti1W,YuwX1U-");
		ListResult folders = client.listAll();
		
		assertThat(result).isTrue();
		assertThat(folders).containsOnly(
				mailbox("INBOX"),
				mailbox("&Ti1W,YuwX1U-"));
	}
	
	@Ignore("Greenmail does not support partition")
	@Test
	public void testCreateMailboxWithPartition() throws Exception {
		StoreClient client = loggedClient();

		boolean result = client.create("user/" + mailbox + "/SentBox", "my_partition");
		ListResult folders = client.listAll();
		
		assertThat(result).isTrue();
		assertThat(folders).containsOnly(
				mailbox("INBOX"),
				mailbox("SentBox"));
	}
	
	private StoreClient loggedClient() throws OpushLocatorException, IMAPException  {
		return clientProvider.getImapClient(udr);
	}

	private ListInfo mailbox(String mailbox) {
		return new ListInfo(mailbox, true, true);
	}
}
