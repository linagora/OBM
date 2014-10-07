/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.sync.IntegrationTestICSUtils.assertIcsReplyFormat;
import static org.obm.sync.IntegrationTestUtils.newEvent;

import java.net.URL;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatGuiceArquillianRunner;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.IntegrationTestICSUtils.StoreMessageReceivedListener;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.ServicesClientModule.ArquillianLocatorService;
import org.obm.sync.ServicesTestModule;
import org.obm.sync.ServicesWithSocketJMSTestModule;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.ServicesClientWithJMSModule.MessageConsumerResourcesManager;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.login.LoginClient;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.UserPassword;

@RunWith(ManagedTomcatGuiceArquillianRunner.class)
@GuiceModule(ServicesClientWithJMSModule.class)
public class ChangeParticipationIntegrationTest extends ObmSyncIntegrationTest {

	private static final int TIMEOUT = 5000;
	private static final int ONE_SECOND = 1000;

	@Inject ArquillianLocatorService locatorService;
	@Inject CalendarClient calendarClient;
	@Inject LoginClient loginClient;
	@Inject MessageConsumerResourcesManager messageConsumerResourcesManager;
	
	StoreMessageReceivedListener storeMessageReceivedListener;
	String owner;
	UserPassword ownerPassword;
	String attendee;
	UserPassword attendeePassword;
	String ownerEmail;
	String attendeeEmail;

	@Before
	public void setup() throws Exception {
		storeMessageReceivedListener = new StoreMessageReceivedListener();
		messageConsumerResourcesManager.start();
		messageConsumerResourcesManager.getConsumer().setMessageListener(storeMessageReceivedListener);
		owner = "user1";
		ownerPassword = UserPassword.valueOf(owner);
		ownerEmail = "user1@domain.org";
		attendee = "user2";
		attendeePassword = UserPassword.valueOf(attendee);
		attendeeEmail = "user2@domain.org";
	}
	
	@After
	public void tearDown() throws Exception {
		super.teardown();
		messageConsumerResourcesManager.close();
	}
	
	@Test
	@RunAsClient
	public void changeParticipationShouldNotifyWhenRegularEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		Event event = newEvent(ownerEmail, owner, "changeParticipationShouldNotifyWhenRegularEvent");
		event.addAttendee(UserAttendee.builder()
				.asAttendee()
				.email(attendeeEmail)
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.REQ)
				.build());
		AccessToken ownerToken = loginClient.login(ownerEmail, ownerPassword);
		calendarClient.storeEvent(ownerToken, ownerEmail, event, false, null);
		
		AccessToken attendeeToken = loginClient.login(attendeeEmail, attendeePassword);
		boolean changed = calendarClient.changeParticipationState(attendeeToken, attendeeEmail, event.getExtId(), 
				Participation.accepted(), 0, true);
		Event eventFromServer = calendarClient.getEventFromExtId(ownerToken, ownerEmail, event.getExtId());

		assertThat(changed).isTrue();
		assertThat(eventFromServer.findAttendeeFromEmail(ownerEmail).getParticipation()).isEqualTo(Participation.accepted());
		assertThat(eventFromServer.findAttendeeFromEmail(attendeeEmail).getParticipation()).isEqualTo(Participation.accepted());
		storeMessageReceivedListener.waitForMessageCount(2, TIMEOUT);
		assertIcsReplyFormat(storeMessageReceivedListener.messages().get(1))
			.contains(
				"UID:changeParticipationShouldNotifyWhenRegularEvent\r\n")
			.contains(
				"SEQUENCE:0\r\n" +
				"ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;RSVP=TRUE;CN=Firstname Lastna\r\n" +
				" me;ROLE=REQ-PARTICIPANT:mailto:" + attendeeEmail + "\r\n" +
				"COMMENT:\r\n" +
				"DTSTART:20130601T120000Z\r\n" +
				"DURATION:PT0S\r\n" +
				"TRANSP:OPAQUE\r\n" +
				"ORGANIZER;CN=Firstname Lastname:mailto:" + ownerEmail + "\r\n" +
				"PRIORITY:5\r\n" +
				"CLASS:PUBLIC\r\n" +
				"SUMMARY:Title_changeParticipationShouldNotifyWhenRegularEvent\r\n");
	}
	
	@Test(expected=TimeoutException.class)
	@RunAsClient
	public void changeParticipationShouldNotifyOnlyOnceWhenAcceptingTwiceRegularEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		Event event = newEvent(ownerEmail, owner, "changeParticipationShouldNotifyOnlyOnceWhenAcceptingTwiceRegularEvent");
		event.addAttendee(UserAttendee.builder()
				.asAttendee()
				.email(attendeeEmail)
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.REQ)
				.build());
		AccessToken ownerToken = loginClient.login(ownerEmail, ownerPassword);
		calendarClient.storeEvent(ownerToken, ownerEmail, event, false, null);
		
		AccessToken attendeeToken = loginClient.login(attendeeEmail, attendeePassword);
		calendarClient.changeParticipationState(attendeeToken, attendeeEmail, event.getExtId(), Participation.accepted(), 0, true);
		boolean changed = calendarClient.changeParticipationState(attendeeToken, attendeeEmail, event.getExtId(), 
				Participation.accepted(), 0, true);
		Event eventFromServer = calendarClient.getEventFromExtId(ownerToken, ownerEmail, event.getExtId());

		assertThat(changed).isFalse();
		assertThat(eventFromServer.findAttendeeFromEmail(ownerEmail).getParticipation()).isEqualTo(Participation.accepted());
		assertThat(eventFromServer.findAttendeeFromEmail(attendeeEmail).getParticipation()).isEqualTo(Participation.accepted());
		storeMessageReceivedListener.waitForMessageCount(3, ONE_SECOND);
	}
	
	@Test
	@RunAsClient
	public void changeParticipationShouldNotifyWhenRecurrentEvent(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		Event toStoreEvent = newEvent(ownerEmail, owner, "changeParticipationShouldNotifyWhenRecurrentEvent");
		EventRecurrence recurrence = new EventRecurrence(RecurrenceKind.daily);
		toStoreEvent.setRecurrence(recurrence);
		toStoreEvent.addAttendee(UserAttendee.builder()
				.asAttendee()
				.email(attendeeEmail)
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.REQ)
				.build());
		AccessToken ownerToken = loginClient.login(ownerEmail, ownerPassword);
		calendarClient.storeEvent(ownerToken, ownerEmail, toStoreEvent, false, null);
		
		AccessToken attendeeToken = loginClient.login(attendeeEmail, attendeePassword);
		RecurrenceId recurrenceId = new RecurrenceId("20130602T120000Z");
		boolean changed = calendarClient.changeParticipationState(attendeeToken, attendeeEmail, toStoreEvent.getExtId(), 
				recurrenceId, Participation.accepted(), 0, true);
		
		Event event = calendarClient.getEventFromExtId(ownerToken, ownerEmail, toStoreEvent.getExtId());
		Event occurrence = event.getOccurrence(new DateTime("2013-06-02T12:00:00Z").toDate());
		assertThat(changed).isTrue();
		assertThat(event.findAttendeeFromEmail(ownerEmail).getParticipation()).isEqualTo(Participation.accepted());
		assertThat(event.findAttendeeFromEmail(attendeeEmail).getParticipation()).isEqualTo(Participation.needsAction());
		assertThat(occurrence.findAttendeeFromEmail(ownerEmail).getParticipation()).isEqualTo(Participation.accepted());
		assertThat(occurrence.findAttendeeFromEmail(attendeeEmail).getParticipation()).isEqualTo(Participation.accepted());
		storeMessageReceivedListener.waitForMessageCount(2, TIMEOUT);
		assertIcsReplyFormat(storeMessageReceivedListener.messages().get(1))
			.contains(
				"UID:changeParticipationShouldNotifyWhenRecurrentEvent\r\n")
			.contains(
				"SEQUENCE:0\r\n" +
				"ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;RSVP=TRUE;CN=Firstname Lastna\r\n" +
				" me;ROLE=REQ-PARTICIPANT:mailto:" + attendeeEmail + "\r\n" +
				"COMMENT:\r\n" +
				"DTSTART:20130602T120000Z\r\n" +
				"DURATION:PT0S\r\n" +
				"TRANSP:OPAQUE\r\n" +
				"ORGANIZER;CN=Firstname Lastname:mailto:" + ownerEmail + "\r\n" +
				"PRIORITY:5\r\n" +
				"CLASS:PUBLIC\r\n" +
				"SUMMARY:Title_changeParticipationShouldNotifyWhenRecurrentEvent\r\n" +
				"RECURRENCE-ID:20130602T120000Z\r\n");
	}
	
	@DeployForEachTests
	@Deployment(managed=false, name=ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils
				.createDeployment(ServicesWithSocketJMSTestModule.class)
				.addClass(ServicesTestModule.class);
	}
}
