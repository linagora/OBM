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
package org.obm.push.mail.greenmail;

import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.util.Date;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMailUtil;

public class ExternalGreenMailTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject MailboxService mailboxService;
	private String mailbox;
	private String password;
	private BackendSession bs;
	
	private ClosableProcess greenMailProcess;

	@Before
	public void setUp() throws ExternalProcessException {
	    mailbox = "to@localhost.com";
	    password = "password";
	    greenMailProcess = new GreenMailExternalProcess(mailbox, password).execute();
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password, null), null, null, null);
	}
	
	@After
	public void tearDown() {
		greenMailProcess.closeProcess();
	}
	
	@Test
	public void testExternalGreenMail() throws MailException {
		Date before = new Date();
		Set<Email> emails = sendOneEmailAndFetchAll(before);
		Assertions.assertThat(emails).isNotNull().hasSize(1);
	}

	@Test
	public void testMailsArePurgedBetweenTwoTest() throws MailException, ExternalProcessException {
		Date before = new Date();
		
		Set<Email> emailsOfFirstTest = sendOneEmailAndFetchAll(before);
		reinitTestContext();
		Set<Email> emailsOfSecondTest = sendOneEmailAndFetchAll(before);
		
		Assertions.assertThat(emailsOfFirstTest).isNotNull().hasSize(1);
		Assertions.assertThat(emailsOfSecondTest).isNotNull().hasSize(1);
	}

	private void reinitTestContext() throws ExternalProcessException {
		tearDown();
		setUp();
	}

	private Set<Email> sendOneEmailAndFetchAll(Date before) throws MailException {
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		return mailboxService.fetchEmails(bs, IMAP_INBOX_NAME, before);
	}
}
