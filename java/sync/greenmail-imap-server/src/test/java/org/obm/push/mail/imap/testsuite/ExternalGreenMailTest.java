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
package org.obm.push.mail.imap.testsuite;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.URLName;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.mail.greenmail.ClosableProcess;
import org.obm.push.mail.greenmail.ExternalGreenMailModule;
import org.obm.push.mail.greenmail.ExternalProcessException;
import org.obm.push.mail.greenmail.GreenMailExternalProcess;
import org.obm.push.mail.greenmail.GreenMailPortProvider;
import org.obm.push.mail.greenmail.GreenMailServerUtil;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(GuiceRunner.class)
@GuiceModule(ExternalGreenMailModule.class)
public class ExternalGreenMailTest {

	@Inject GreenMailExternalProcess greenMailExternalProcess;
	@Inject GreenMailPortProvider greenMailPorts;

	private ClosableProcess greenMailProcess;
	private ServerSetup smtpServerSetup;
	private String mailbox;
	private String password;

	@Before
	public void setUp() throws ExternalProcessException, InterruptedException {
		mailbox = "to@localhost.com";
		password = "password";
		greenMailProcess = greenMailExternalProcess.startGreenMail(mailbox, password);
		smtpServerSetup = greenMailExternalProcess.buildSmtpServerSetup();
		
		GreenMailServerUtil.waitForGreenmailAvailability("localhost", greenMailExternalProcess.getImapPort());
		GreenMailServerUtil.waitForGreenmailAvailability("localhost", greenMailExternalProcess.getSmtpPort());
	}
	
	@After
	public void tearDown() throws InterruptedException {
		greenMailProcess.closeProcess();
	}
	
	@Test
	public void testExternalGreenMail() throws Exception {
		int emailCount = sendOneEmailAndGetCount();
		Assertions.assertThat(emailCount).isEqualTo(1);
	}

	@Test
	public void testMailsArePurgedBetweenTwoTest() throws Exception {
		int emailCountOfFirstTest = sendOneEmailAndGetCount();
		reinitTestContext();
		int emailCountOfSecondTest = sendOneEmailAndGetCount();
		
		Assertions.assertThat(emailCountOfFirstTest).isEqualTo(1);
		Assertions.assertThat(emailCountOfSecondTest).isEqualTo(1);
	}

	private void reinitTestContext() throws ExternalProcessException, InterruptedException {
        try {
        	tearDown();

        	GuiceModule moduleAnnotation = getClass().getAnnotation(GuiceModule.class);
        	Class<? extends Module> module = moduleAnnotation.value();
			Guice.createInjector(module.newInstance()).injectMembers(this);
			
			setUp();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private int sendOneEmailAndGetCount() throws Exception {
		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		Session session = GreenMailUtil.getSession(greenMailExternalProcess.buildImapServerSetup());
		Folder inboxFolder = session.getFolder(new URLName("imap", "localhost", greenMailExternalProcess.getImapPort(), "INBOX", mailbox, password));
		return inboxFolder.getMessageCount();
	}
}
