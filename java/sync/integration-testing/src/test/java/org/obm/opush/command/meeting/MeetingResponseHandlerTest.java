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
package org.obm.opush.command.meeting;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.replayMocks;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.io.IOException;
import java.util.Date;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpStatus;
import org.easymock.EasyMock;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.PortNumber;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.mail.MailBackend;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.push.wbxml.WBXmlException;
import org.obm.sync.push.client.HttpStatusException;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class MeetingResponseHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MeetingResponseHandlerTestModule.class);

	@Inject @PortNumber int port;
	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;

	private int meetingCollectionId;
	private int meetingItemId;
	private int invitationCollectionId;
	private int invitationItemId;

	@Before
	public void setUp() {
		meetingCollectionId = 2;
		meetingItemId = 8;
		invitationCollectionId = 5;
		invitationItemId = 10;
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testItemNotFoundInInvitationEmailDeletionDoesNotMakeFailTheCommand() throws Exception {
		prepareMockForEmailDeletionError(new ItemNotFoundException());
		opushServer.start();

		Document serverResponse = postMeetingAcceptedResponse();
		
		assertMeetingResponseIsSuccess(serverResponse);
	}

	@Test
	public void testServerExceptionInInvitationEmailDeletionDoesNotMakeFailTheCommand() throws Exception {
		prepareMockForEmailDeletionError(new UnexpectedObmSyncServerException());
		opushServer.start();
		
		Document serverResponse = postMeetingAcceptedResponse();
		
		assertMeetingResponseIsSuccess(serverResponse);
	}

	@Test
	public void testRuntimeExceptionInInvitationEmailDeletionTriggersHttpServerErrorStatus() throws Exception {
		prepareMockForEmailDeletionError(new RuntimeException());
		opushServer.start();
		
		int expectedHttpStatus = -1;
		try {
			postMeetingAcceptedResponse();
		} catch (HttpStatusException e) {
			expectedHttpStatus = e.getStatusCode();
		}
		Assertions.assertThat(expectedHttpStatus).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void testItemNotFoundInMeetingResponseHandlingMakeFailTheCommand() throws Exception {
		prepareMockForMeetingResponseHandlingError(new ItemNotFoundException());
		opushServer.start();

		Document serverResponse = postMeetingAcceptedResponse();
		
		assertMeetingResponseIsFailure(serverResponse);
	}

	@Test
	public void testServerExceptionInMeetingResponseHandlingMakeFailTheCommand() throws Exception {
		prepareMockForMeetingResponseHandlingError(new UnexpectedObmSyncServerException());
		opushServer.start();

		Document serverResponse = postMeetingAcceptedResponse();
		
		assertMeetingResponseIsFailure(serverResponse);
	}

	@Test
	public void testRuntimeExceptionInMeetingResponseHandlingTriggersHttpServerErrorStatus() throws Exception {
		prepareMockForMeetingResponseHandlingError(new RuntimeException());
		opushServer.start();
		
		int expectedHttpStatus = -1;
		try {
			postMeetingAcceptedResponse();
		} catch (HttpStatusException e) {
			expectedHttpStatus = e.getStatusCode();
		}
		Assertions.assertThat(expectedHttpStatus).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	private Document postMeetingAcceptedResponse()
			throws TransformerException, WBXmlException, IOException, HttpStatusException, SAXException  {
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		Document document = buildMeetingAcceptedResponse();

		Document serverResponse = opClient.postXml("MeetingResponse", document, "MeetingResponse");
		return serverResponse;
	}

	private void assertMeetingResponseIsSuccess(Document response) throws TransformerException {
		String responseAsText = DOMUtils.serialise(response);
		String responseExpected = buildMeetingResponseCommandSuccess();
		Assertions.assertThat(responseAsText).isEqualTo(responseExpected);
	}

	private void assertMeetingResponseIsFailure(Document response) throws TransformerException {
		String responseAsText = DOMUtils.serialise(response);
		String responseExpected = buildMeetingResponseCommandFailure();
		Assertions.assertThat(responseAsText).isEqualTo(responseExpected);
	}

	private void prepareMockForEmailDeletionError(Exception triggeredException) throws Exception {
		prepareMockForCommonNeeds();

		expectMailbackendDeleteInvitationTriggersException(classToInstanceMap.get(MailBackend.class), triggeredException);
		expectHandleMeetingResponseProcessCorrectly(classToInstanceMap.get(CalendarBackend.class));
		
		replayMocks(classToInstanceMap);
	}

	private void prepareMockForMeetingResponseHandlingError(Exception triggeredException) throws Exception {
		prepareMockForCommonNeeds();
		
		expectMailbackendDeleteInvitationProcessCorrectly(classToInstanceMap.get(MailBackend.class));
		expectHandleMeetingResponseTriggersException(classToInstanceMap.get(CalendarBackend.class), triggeredException);
		
		replayMocks(classToInstanceMap);
	}
	
	private void prepareMockForCommonNeeds() throws Exception {
		
		mockUsersAccess(classToInstanceMap, Sets.newHashSet(singleUserFixture.jaures));
		expectCollectionDaoUnchange(classToInstanceMap.get(CollectionDao.class));
		expectMailbackendGiveEmailForAnyIds(classToInstanceMap.get(MailBackend.class));
	}
	
	private void expectMailbackendDeleteInvitationProcessCorrectly(MailBackend mailBackend) throws Exception {
		
		mailBackend.delete(anyObject(BackendSession.class), anyInt(), anyObject(String.class), anyBoolean());
		EasyMock.expectLastCall().once();
	}
	
	private void expectMailbackendDeleteInvitationTriggersException(MailBackend mailBackend, Exception triggeredException)
			throws Exception {
		
		mailBackend.delete(anyObject(BackendSession.class), anyInt(), anyObject(String.class), anyBoolean());
		EasyMock.expectLastCall().andThrow(triggeredException);
	}

	private void expectHandleMeetingResponseProcessCorrectly(CalendarBackend calendarBackend)
			throws Exception {
		
		expect(calendarBackend.handleMeetingResponse(
				anyObject(BackendSession.class),
				anyObject(MSEmail.class),
				anyObject(AttendeeStatus.class)))
			.andReturn(serverId(meetingCollectionId, meetingItemId));
	}

	private void expectHandleMeetingResponseTriggersException(CalendarBackend calendarBackend, Exception triggeredException)
			throws Exception {
		
		expect(calendarBackend.handleMeetingResponse(
				anyObject(BackendSession.class),
				anyObject(MSEmail.class),
				anyObject(AttendeeStatus.class)))
			.andThrow(triggeredException);
	}

	private void expectMailbackendGiveEmailForAnyIds(MailBackend mailBackend)
			throws CollectionNotFoundException, ProcessingEmailException {
		
		expect(mailBackend.getEmail(anyObject(BackendSession.class), anyInt(), anyObject(String.class)))
			.andReturn(new MSEmail());
	}

	private void expectCollectionDaoUnchange(CollectionDao collectionDao) throws DaoException {
		Date dateFirstSyncFromASSpecs = new Date(0);
		
		SyncState syncState = new SyncState("sync state");
		expect(collectionDao.lastKnownState(anyObject(Device.class), anyInt())).andReturn(syncState).anyTimes();
		
		ChangedCollections noChangeCollections = new ChangedCollections(dateFirstSyncFromASSpecs, ImmutableSet.<SyncCollection>of());
		expect(collectionDao.getContactChangedCollections(dateFirstSyncFromASSpecs)).andReturn(noChangeCollections).anyTimes();
		expect(collectionDao.getCalendarChangedCollections(dateFirstSyncFromASSpecs)).andReturn(noChangeCollections).anyTimes();
	}

	private String buildMeetingResponseCommandSuccess() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<MeetingResponse>" +
					"<Result>" +
						"<Status>1</Status>" +
						"<CalId>" + serverId(meetingCollectionId, meetingItemId) + "</CalId>" +
						"<ReqId>" + serverId(invitationCollectionId, invitationItemId) + "</ReqId>" +
					"</Result>" +
				"</MeetingResponse>";
	}

	private String buildMeetingResponseCommandFailure() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<MeetingResponse>" +
					"<Result>" +
						"<Status>4</Status>" + // MS-ASCMD 2.2.1.9.2.4 : 4 is SERVER_ERROR
						"<ReqId>" + serverId(invitationCollectionId, invitationItemId) + "</ReqId>" +
					"</Result>" +
				"</MeetingResponse>";
	}

	private Document buildMeetingAcceptedResponse()
			throws SAXException, IOException {
		
		return DOMUtils.parse(
				"<MeetingResponse>" +
					"<Request>" +
						"<UserResponse>1</UserResponse>" + // MS-ASCMD 2.2.1.9.2.4 : 1 is SUCCESS
						"<CollectionId>" + invitationCollectionId + "</CollectionId>" +
						"<ReqId>" + serverId(invitationCollectionId, invitationItemId) + "</ReqId>" +
					"</Request>" +
				"</MeetingResponse>");
	}

	private String serverId(int collectionId, int invitationId) {
		return String.valueOf(collectionId) + ":" + String.valueOf(invitationId);
	}
}
