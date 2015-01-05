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
package org.obm.push.minig.imap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@GuiceModule(org.obm.push.minig.imap.MailEnvModule.class)
@RunWith(GuiceRunner.class)
public class DeleteCommandIntegrationTest {

	@Inject StoreClient.Factory storeClientFactory;
	@Inject GreenMail greenMail;
	
	private String mailbox;
	private String password;
	private StoreClient client;

	@Before
	public void setUp() throws Exception {
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		client = loggedClient();
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	private StoreClient loggedClient() throws Exception  {
		StoreClient storeClient = storeClientFactory.create(greenMail.getImap().getBindTo(), mailbox, password.toCharArray());
		storeClient.login(false);
		return storeClient;
	}
	
	@Test
	public void testDeleteOneMailboxWithAccent() throws ImapTimeoutException {
		client.create("déplacements");
		boolean result = client.delete("déplacements");
		ListResult folders = client.listAll();
		
		assertThat(result).isTrue();
		assertThat(folders).containsOnly(
				mailbox("INBOX"));
	}
	
	@Test
	public void testDeleteTwoMailboxOfMany() throws ImapTimeoutException {
		client.create("déplacements");
		client.create("another");
		client.create("another/déplacements");
		boolean result1 = client.delete("another/déplacements");
		boolean result2 = client.delete("déplacements");
		ListResult folders = client.listAll();
		
		assertThat(result1).isEqualTo(result2).isTrue();
		assertThat(folders).containsOnly(
				mailbox("INBOX"),
				mailbox("another"));
	}
	
	@Test
	public void testDeleteInexistantMailbox() throws ImapTimeoutException {
		client.create("déplacements");
		client.create("another");
		client.create("another/déplacements");
		boolean result1 = client.delete("unknown");
		ListResult folders = client.listAll();
		
		assertThat(result1).isFalse();
		assertThat(folders).containsOnly(
				mailbox("INBOX"),
				mailbox("déplacements"),
				mailbox("another"),
				mailbox("another/déplacements"));
	}
	
	private ListInfo mailbox(String mailbox) {
		return new ListInfo(mailbox, true, true);
	}
}
