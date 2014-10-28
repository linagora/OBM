/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expectLastCall;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Mailing;
import org.obm.sync.ObmSmtpService;
import org.obm.sync.base.EmailAddress;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.linagora.scheduling.ScheduledTask.State;

import fr.aliacom.obm.common.domain.ObmDomain;


public class MailerImplTest {

	private IMocksControl control;
	
	private ObmSmtpService obmSmtpService;
	private Logger logger;
	private ObmDomain domain;
	private MailerImpl testee;
	
	@Before
	public void setup() {
		control = createControl();
		obmSmtpService = control.createMock(ObmSmtpService.class);
		logger = control.createMock(Logger.class);
		
		domain = ObmDomain.builder().name("mydomain.org").build();
		testee = new MailerImpl(obmSmtpService, logger);
	}
	
	@Test
	public void text() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938");
		
		control.replay();
		String text = testee.text(domain, runId, State.TERMINATED);
		control.verify();
		
		assertThat(text).isEqualTo("IMAP Archive treatment has ended with state "+ State.TERMINATED + "\r\n" + 
				"Logs are available at https://mydomain.org/imap_archive/imap_archive_index.php?action=log_page&run_id=" + runId.serialize() + "\r\n");
	}
	
	@Test
	public void link() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938");
		
		control.replay();
		String link = testee.link(domain, runId);
		control.verify();
		
		assertThat(link).isEqualTo("https://mydomain.org/imap_archive/imap_archive_index.php?action=log_page&run_id=" + runId.serialize());
	}
	
	@Test
	public void internetAddressesShouldBeEmptyWhenEmptyMailing() {
		Mailing mailing = Mailing.empty();
		
		control.replay();
		Address[] internetAddresses = testee.internetAddresses(mailing);
		control.verify();
		
		assertThat(internetAddresses).isEmpty();
	}
	
	@Test
	public void internetAddressesShouldBeEqualsToMailing() throws Exception {
		Mailing mailing = Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain.org"), EmailAddress.loginAtDomain("user2@mydomain.org")));
		
		control.replay();
		Address[] internetAddresses = testee.internetAddresses(mailing);
		control.verify();
		
		assertThat(internetAddresses).containsOnly(new InternetAddress("user@mydomain.org"), new InternetAddress("user2@mydomain.org"));
	}
	
	@Test
	public void internetAddressesShouldIgnoreInvalidAddress() throws Exception {
		Mailing mailing = Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain  .org"), EmailAddress.loginAtDomain("user2@mydomain.org")));
		
		logger.error(anyObject(String.class), anyObject(Exception.class));
		expectLastCall();
		
		control.replay();
		Address[] internetAddresses = testee.internetAddresses(mailing);
		control.verify();
		
		assertThat(internetAddresses).containsOnly(new InternetAddress("user2@mydomain.org"));
	}
	
	@Test
	public void sendShouldDoNothingWhenNoEmailAddress() throws Exception {
		control.replay();
		testee.send(domain, ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938"), State.TERMINATED, Mailing.empty());
		control.verify();
	}
	
	@Test
	public void sendShouldSend() throws Exception {
		obmSmtpService.sendEmail(anyObject(MimeMessage.class), anyObject(Session.class));
		expectLastCall();
		
		control.replay();
		testee.send(domain, ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938"), State.TERMINATED, Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain.org"), EmailAddress.loginAtDomain("user2@mydomain.org"))));
		control.verify();
	}
	
	@Test(expected=MessagingException.class)
	public void sendShouldSendThrowWhenExceptionAppend() throws Exception {
		obmSmtpService.sendEmail(anyObject(MimeMessage.class), anyObject(Session.class));
		expectLastCall().andThrow(new MessagingException());
		
		logger.error(anyObject(String.class), anyObject(Exception.class));
		expectLastCall();
		
		control.replay();
		testee.send(domain, ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938"), State.TERMINATED, Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain.org"), EmailAddress.loginAtDomain("user2@mydomain.org"))));
		control.verify();
	}
}
