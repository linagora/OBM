/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync.client.calendar;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.util.Date;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.NotAllowedException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.client.impl.SyncClientException;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.common.collect.Multimap;

import fr.aliacom.obm.ToolBox;

@RunWith(SlowFilterRunner.class)
public class AbstractEventSyncClientTest {

	private static String CALENDAR = "calendar";
	
	private AbstractEventSyncClient client;
	private Logger logger;
	private AccessToken token;
	private Responder responder;
	private IMocksControl control;
	
	@Before
	public void setUp() {
		control = createControl();
		responder = control.createMock(Responder.class);
		token = ToolBox.mockAccessToken(control);
		logger = control.createMock(Logger.class);
		client = new AbstractEventSyncClient("/calendar", new SyncClientException(), null, logger) {
			@Override
			protected Document execute(AccessToken token, String action, Multimap<String, String> parameters) {
				return responder.execute(token, action, parameters);
			}
		};
	}
	
	@After
	public void tearDown() {
		control.verify();
	}
	
	@Test(expected=NotAllowedException.class)
	public void testCreateEventNotAllowed() throws Exception {
		testCreateEvent(NotAllowedException.class);
	}
	
	@Test(expected=EventAlreadyExistException.class)
	public void testCreateEventEventAlreadyExists() throws Exception {
		testCreateEvent(EventAlreadyExistException.class);
	}
	
	private void testCreateEvent(Class<? extends Exception> exceptionClass) throws Exception {
		Event event = createEvent();
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/createEvent"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.createEvent(token, CALENDAR, event, false);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testRemoveEventByExtIdNotAllowed() throws Exception {
		testRemoveEventByExtId(NotAllowedException.class);
	}
	
	private void testRemoveEventByExtId(Class<? extends Exception> exceptionClass) throws Exception {
		EventExtId extId = new EventExtId("ExtId");
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/removeEventByExtId"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.removeEventByExtId(token, CALENDAR, extId, 0, false);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testRemoveEventByIdNotAllowed() throws Exception {
		testRemoveEventById(NotAllowedException.class);
	}
	
	@Test(expected=EventNotFoundException.class)
	public void testRemoveEventByIdEventNotFound() throws Exception {
		testRemoveEventById(EventNotFoundException.class);
	}
	
	private void testRemoveEventById(Class<? extends Exception> exceptionClass) throws Exception {
		EventObmId id = new EventObmId(1);
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/removeEvent"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.removeEventById(token, CALENDAR, id, 0, false);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testModifyEventNotAllowed() throws Exception {
		testModifyEvent(NotAllowedException.class);
	}
	
	private void testModifyEvent(Class<? extends Exception> exceptionClass) throws Exception {
		Event event = createEvent();
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/modifyEvent"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.modifyEvent(token, CALENDAR, event, false, false);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetSyncInRangeNotAllowed() throws Exception {
		testGetSyncInRange(NotAllowedException.class);
	}
	
	private void testGetSyncInRange(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getSyncInRange"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getSyncInRange(token, CALENDAR, null, null);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetSyncWithSortedChangesNotAllowed() throws Exception {
		testGetSyncWithSortedChanges(NotAllowedException.class);
	}
	
	private void testGetSyncWithSortedChanges(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getSyncWithSortedChanges"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getSyncWithSortedChanges(token, CALENDAR, null, null);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetSyncNotAllowed() throws Exception {
		testGetSync(NotAllowedException.class);
	}
	
	private void testGetSync(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getSync"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getSync(token, CALENDAR, null);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetSyncEventDateNotAllowed() throws Exception {
		testGetSyncEventDate(NotAllowedException.class);
	}
	
	private void testGetSyncEventDate(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getSyncEventDate"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getSyncEventDate(token, CALENDAR, null);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetEventFromIdNotAllowed() throws Exception {
		testGetEventFromId(NotAllowedException.class);
	}
	
	@Test(expected=EventNotFoundException.class)
	public void testGetEventFromIdEventNotFound() throws Exception {
		testGetEventFromId(EventNotFoundException.class);
	}
	
	private void testGetEventFromId(Class<? extends Exception> exceptionClass) throws Exception {
		EventObmId id = new EventObmId(1);
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getEventFromId"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getEventFromId(token, CALENDAR, id);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetEventObmIdFromExtIdNotAllowed() throws Exception {
		testGetEventObmIdFromExtId(NotAllowedException.class);
	}
	
	@Test(expected=EventNotFoundException.class)
	public void testGetEventObmIdFromExtIdEventNotFound() throws Exception {
		testGetEventObmIdFromExtId(EventNotFoundException.class);
	}
	
	private void testGetEventObmIdFromExtId(Class<? extends Exception> exceptionClass) throws Exception {
		EventExtId extId = new EventExtId("ExtId");
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getEventObmIdFromExtId"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getEventObmIdFromExtId(token, CALENDAR, extId);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetEventTwinKeysNotAllowed() throws Exception {
		testGetEventTwinKeys(NotAllowedException.class);
	}
	
	private void testGetEventTwinKeys(Class<? extends Exception> exceptionClass) throws Exception {
		Event event = createEvent();
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getEventTwinKeys"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getEventTwinKeys(token, CALENDAR, event);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetRefusedKeysNotAllowed() throws Exception {
		testGetRefusedKeys(NotAllowedException.class);
	}
	
	private void testGetRefusedKeys(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getRefusedKeys"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getRefusedKeys(token, CALENDAR, null);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetEventFromExtIdNotAllowed() throws Exception {
		testGetEventFromExtId(NotAllowedException.class);
	}
	
	@Test(expected=EventNotFoundException.class)
	public void testGetEventFromExtIdEventNotFound() throws Exception {
		testGetEventFromExtId(EventNotFoundException.class);
	}
	
	private void testGetEventFromExtId(Class<? extends Exception> exceptionClass) throws Exception {
		EventExtId extId = new EventExtId("ExtId");
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getEventFromExtId"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getEventFromExtId(token, CALENDAR, extId);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetListEventsFromIntervalDateNotAllowed() throws Exception {
		testGetListEventsFromIntervalDate(NotAllowedException.class);
	}
	
	private void testGetListEventsFromIntervalDate(Class<? extends Exception> exceptionClass) throws Exception {
		Date start = new Date(123456789), end = new Date(123456780);
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getListEventsFromIntervalDate"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getListEventsFromIntervalDate(token, CALENDAR, start, end);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetEventTimeUpdateNotRefusedFromIntervalDateNotAllowed() throws Exception {
		testGetEventTimeUpdateNotRefusedFromIntervalDate(NotAllowedException.class);
	}
	
	private void testGetEventTimeUpdateNotRefusedFromIntervalDate(Class<? extends Exception> exceptionClass) throws Exception {
		Date start = new Date(123456789), end = new Date(123456780);
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getEventTimeUpdateNotRefusedFromIntervalDate"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getEventTimeUpdateNotRefusedFromIntervalDate(token, CALENDAR, start, end);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testGetLastUpdateNotAllowed() throws Exception {
		testGetLastUpdate(NotAllowedException.class);
	}
	
	private void testGetLastUpdate(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/getLastUpdate"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.getLastUpdate(token, CALENDAR);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testIsWritableCalendarNotAllowed() throws Exception {
		testIsWritableCalendar(NotAllowedException.class);
	}
	
	private void testIsWritableCalendar(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/isWritableCalendar"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.isWritableCalendar(token, CALENDAR);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testChangeParticipationStateNotAllowed() throws Exception {
		testChangeParticipationState(NotAllowedException.class);
	}
	
	private void testChangeParticipationState(Class<? extends Exception> exceptionClass) throws Exception {
		EventExtId extId = new EventExtId("ExtId");
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/changeParticipationState"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.changeParticipationState(token, CALENDAR, extId, Participation.accepted(), 0, false);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testChangeParticipationStateRecNotAllowed() throws Exception {
		testChangeParticipationStateRec(NotAllowedException.class);
	}
	
	private void testChangeParticipationStateRec(Class<? extends Exception> exceptionClass) throws Exception {
		EventExtId extId = new EventExtId("ExtId");
		RecurrenceId recId = new RecurrenceId("RecId");
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/changeParticipationState"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.changeParticipationState(token, CALENDAR, extId, recId, Participation.accepted(), 0, false);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testImportICalendarNotAllowed() throws Exception {
		testImportICalendar(NotAllowedException.class);
	}
	
	private void testImportICalendar(Class<? extends Exception> exceptionClass) throws Exception {
		String ics = "ICS";
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/importICalendar"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.importICalendar(token, CALENDAR, ics);
	}
	
	@Test(expected=NotAllowedException.class)
	public void testPurgeNotAllowed() throws Exception {
		testPurge(NotAllowedException.class);
	}
	
	private void testPurge(Class<? extends Exception> exceptionClass) throws Exception {
		Document document = mockErrorDocument(exceptionClass, null);
		
		expect(responder.execute(eq(token), eq("/calendar/purge"), isA(Multimap.class))).andReturn(document).once();
		control.replay();
		
		client.purge(token, CALENDAR);
	}
	
	private Document mockErrorDocument(Class<? extends Exception> exceptionClass, String message) {
		Document doc = control.createMock(Document.class);
		Element root = control.createMock(Element.class);
		
		expect(doc.getDocumentElement()).andReturn(root).anyTimes();
		expect(root.getNodeName()).andReturn("error").anyTimes();
		
		mockTextElement(root, "message", message);
		mockTextElement(root, "type", exceptionClass.getName());
		
		return doc;
	}
	
	private void mockTextElement(Element root, String elementName, String text) {
		NodeList list = control.createMock(NodeList.class);
		Element element = control.createMock(Element.class);
		Text textNode = control.createMock(Text.class);
		
		expect(root.getElementsByTagName(eq(elementName))).andReturn(list).anyTimes();
		expect(list.getLength()).andReturn(1).anyTimes();
		expect(list.item(eq(0))).andReturn(element).anyTimes();
		expect(element.getFirstChild()).andReturn(textNode).anyTimes();
		expect(textNode.getData()).andReturn(text).anyTimes();
	}
	
	private Event createEvent() {
		Event event = new Event();
		
		event.setType(EventType.VEVENT);
		event.setExtId(EventExtId.newExtId());
		
		return event;
	}
	
	private static interface Responder {
		Document execute(AccessToken token, String action, Multimap<String, String> parameters);
	}

}
