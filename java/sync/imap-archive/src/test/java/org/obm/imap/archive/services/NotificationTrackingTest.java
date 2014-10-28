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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.mail.MessagingException;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.Mailing;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.RealRunTaskStatusChanged;
import org.obm.sync.base.EmailAddress;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.linagora.scheduling.ScheduledTask.State;

import fr.aliacom.obm.common.domain.ObmDomain;

public class NotificationTrackingTest {

	private IMocksControl control;
	
	private ArchiveDomainTask task;
	private ObmDomain domain;
	private ArchiveTreatmentRunId runId;
	private DomainConfiguration configuration;
	private Mailing mailing;
	
	private Logger logger;
	private Mailer mailer;
	private NotificationTracking testee;

	
	@Before
	public void setup() {
		control = createControl();
		logger = control.createMock(Logger.class);
		mailer = control.createMock(Mailer.class);
		
		mailing = Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain.org"), EmailAddress.loginAtDomain("user2@mydomain.org")));
		configuration = control.createMock(DomainConfiguration.class);
		expect(configuration.getMailing())
			.andReturn(mailing).anyTimes();
		
		domain = ObmDomain.builder().name("mydomain.org").build();
		runId = ArchiveTreatmentRunId.from("bf0d1a36-3039-4880-9ff7-c84d825b41f7");
		task = control.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration archiveConfiguration = control.createMock(ArchiveConfiguration.class);
		expect(archiveConfiguration.getDomain())
			.andReturn(domain).anyTimes();
		expect(archiveConfiguration.getRunId())
			.andReturn(runId).anyTimes();
		expect(archiveConfiguration.getConfiguration())
			.andReturn(configuration).anyTimes();
		expect(task.getArchiveConfiguration())
			.andReturn(archiveConfiguration).anyTimes();
		
		testee = new NotificationTracking(logger, mailer);
	}
	
	@Test
	public void onTreatmentStateChangeShouldDoNothingWhenCanceled() {
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.CANCELED, task));
		control.verify();
	}
	
	@Test
	public void onTreatmentStateChangeShouldDoNothingWhenNew() {
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.NEW, task));
		control.verify();
	}
	
	@Test
	public void onTreatmentStateChangeShouldDoNothingWhenRunning() {
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.RUNNING, task));
		control.verify();
	}
	
	@Test
	public void onTreatmentStateChangeShouldDoNothingWhenWaiting() {
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.WAITING, task));
		control.verify();
	}
	
	@Test
	public void onTreatmentStateChangeShouldSendWhenFailed() throws Exception {
		mailer.send(domain, runId, State.FAILED, mailing);
		expectLastCall();
		
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.FAILED, task));
		control.verify();
	}
	
	@Test
	public void onTreatmentStateChangeShouldNotThrowWhenFailedAndException() throws Exception {
		mailer.send(domain, runId, State.FAILED, mailing);
		expectLastCall().andThrow(new MessagingException());
		logger.error(anyObject(String.class), anyObject(MessagingException.class));
		
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.FAILED, task));
		control.verify();
	}
	
	@Test
	public void onTreatmentStateChangeShouldSendWhenTerminated() throws Exception {
		mailer.send(domain, runId, State.TERMINATED, mailing);
		expectLastCall();
		
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.TERMINATED, task));
		control.verify();
	}
	
	@Test
	public void onTreatmentStateChangeShouldNotThrowWhenTerminatedAndException() throws Exception {
		mailer.send(domain, runId, State.TERMINATED, mailing);
		expectLastCall().andThrow(new MessagingException());
		logger.error(anyObject(String.class), anyObject(MessagingException.class));
		
		control.replay();
		testee.onTreatmentStateChange(new RealRunTaskStatusChanged(State.TERMINATED, task));
		control.verify();
	}
}
