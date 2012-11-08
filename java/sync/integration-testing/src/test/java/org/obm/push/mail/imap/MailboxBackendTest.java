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
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;
import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MailboxFolders;
import org.obm.push.utils.DateUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

@RunWith(SlowFilterRunner.class) @Slow
public class MailboxBackendTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(org.minig.imap.MailEnvModule.class);

	@Inject MailboxService mailboxService;
	@Inject CollectionPathHelper collectionPathHelper;

	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private MailboxTestUtils testUtils;
	private Date beforeTest;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		beforeTest = new Date();
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		testUtils = new MailboxTestUtils(mailboxService, udr, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test
	public void testFetchMimeSinglePartBase64Email() throws Exception {
		InputStream mailStream = loadEmail("SinglePartBase64.eml");
		mailboxService.storeInInbox(udr, mailStream, false);
		
		String inboxCollectionName = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		List<MSEmail> emails = mailboxService.fetch(udr, 1, inboxCollectionName, 
				Arrays.asList(1l), 
				Arrays.asList(BodyPreference.builder().bodyType(MSEmailBodyType.MIME).build()));
		MSEmail actual = Iterables.getOnlyElement(emails);
		assertThat(actual.getBody().getMimeData()).hasContentEqualTo(loadEmail("SinglePartBase64.eml"));
	}

	@Ignore("greenmail seems to unexpectedly decode base64 part on-the-fly")
	@Test
	public void testFetchTextPlainSinglePartBase64Email() throws Exception {
		InputStream mailStream = loadEmail("SinglePartBase64.eml");
		mailboxService.storeInInbox(udr, mailStream, false);
		
		String inboxCollectionName = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		List<MSEmail> emails = mailboxService.fetch(udr, 1, inboxCollectionName, 
				Arrays.asList(1l), 
				Arrays.asList(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText).build()));
		MSEmail actual = Iterables.getOnlyElement(emails);
		String bodyText = new String(ByteStreams.toByteArray(actual.getBody().getMimeData()), Charsets.UTF_8);
		assertThat(bodyText).contains("Envoyé de mon iPhone");
	}
	
	@Test
	public void testFetchWithoutCorrespondingBodyPreference() throws Exception {
		InputStream mailStream = loadEmail("OBMFULL-4123.eml");
		mailboxService.storeInInbox(udr, mailStream, false);
		
		String inboxCollectionName = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		List<MSEmail> emails = mailboxService.fetch(udr, 1, inboxCollectionName, 
				Arrays.asList(1l), 
				Arrays.asList(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText).build()));
		MSEmail actual = Iterables.getOnlyElement(emails);
		assertThat(actual.getBody().getMimeData()).hasContentEqualTo(loadEmail("OBMFULL-4123.eml"));
	}

}
