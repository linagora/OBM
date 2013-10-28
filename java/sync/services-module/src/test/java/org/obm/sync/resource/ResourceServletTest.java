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
package org.obm.sync.resource;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventExtId.Factory;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.date.DateProvider;
import org.obm.sync.services.AttendeeService;
import org.obm.sync.services.ICalendar;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.calendar.ResourceNotFoundException;

public class ResourceServletTest {

	private Ical4jHelper helper;
	private Ical4jUser iCalUser;
	private ResourceServlet resourceServlet;
	private ICalendar calendarBinding;
	private Collection<Event> collectionEvents;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private DateProvider dateProvider;
	private AttendeeService attendeeService;
	private Date now;
	private IMocksControl control;
	
	@Before
	public void setUp() {
		now = new Date();
		control = createControl();
		dateProvider = control.createMock(DateProvider.class);
		attendeeService = new SimpleAttendeeService();
		Factory eventExtIdFactory = control.createMock(EventExtId.Factory.class);
		helper = new Ical4jHelper(dateProvider, eventExtIdFactory, attendeeService);
		iCalUser = Ical4jUser.Factory.create().createIcal4jUser("toto@toto.com",
				ToolBox.getDefaultObmDomain());

		calendarBinding = control.createMock(ICalendar.class);
		request = control.createMock(HttpServletRequest.class);
		response = control.createMock(HttpServletResponse.class);

		expect(dateProvider.getDate()).andReturn(now).anyTimes();
		resourceServlet = new ResourceServlet(calendarBinding, helper);
	}

	@Test
	public void testGetResourceICS() throws Exception {
		int collectionSize = 5;
		collectionEvents = ToolBox.getFakeEventCollection(collectionSize);
		expect(
				calendarBinding.getResourceEvents(eq("resource@domain"), anyObject(Date.class)))
				.andReturn(collectionEvents);

		control.replay();

		String ics = resourceServlet.getResourceICS("resource@domain");
		assertThat(helper.parseICS(ics, iCalUser, 0)).isNotNull().hasSize(collectionSize);
		control.verify();
	}

	@Test
	public void testDoGetEmailValid() throws Exception {
		int collectionSize = 5;
		collectionEvents = ToolBox.getFakeEventCollection(collectionSize);
		expect(
				calendarBinding.getResourceEvents(eq("resource@domain"), anyObject(Date.class)))
				.andReturn(collectionEvents);

		String uid = "/resource@domain";
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);

		expect(request.getPathInfo()).andReturn(uid);

		response.setContentType("text/calendar;charset=UTF-8");
		response.setStatus(eq(HttpServletResponse.SC_OK));
		expectLastCall();

		expect(response.getWriter()).andReturn(writer);
		control.replay();

		resourceServlet.doGet(request, response);

		control.verify();
		String ics = stringWriter.toString();
		assertThat(helper.parseICS(ics, iCalUser, 0)).isNotNull().hasSize(collectionSize);
	}

	@Test
	public void testDoGetNoEmail() throws Exception {
		String uid = "";
		expect(request.getPathInfo()).andReturn(uid);

		response.setStatus(eq(HttpServletResponse.SC_NOT_FOUND));
		expectLastCall();

		control.replay();

		resourceServlet.doGet(request, response);
		control.verify();
	}

	@Test
	public void testDoGetNoResourceWithEmail() throws Exception {
		String uid = "/resource@domain";
		expect(request.getPathInfo()).andReturn(uid);
		expect(
				calendarBinding.getResourceEvents(eq("resource@domain"), anyObject(Date.class)))
				.andThrow(new ResourceNotFoundException("Resource with id doesn't exist"));

		response.setContentType("text/calendar;charset=UTF-8");
		response.setStatus(eq(HttpServletResponse.SC_NOT_FOUND));
		response.flushBuffer();
		expectLastCall();

		control.replay();

		resourceServlet.doGet(request, response);
		control.verify();
	}

	@Test
	public void testDoGetResourceHasNoEvents() throws Exception{
		expect(calendarBinding.getResourceEvents(eq("resource@domain"), anyObject(Date.class))).andReturn(Collections.<Event>emptyList());

		String uid = "/resource@domain";

		expect(request.getPathInfo()).andReturn(uid);

		response.setContentType("text/calendar;charset=UTF-8");
		expectLastCall();
		response.setStatus(eq(HttpServletResponse.SC_NO_CONTENT));
		expectLastCall();

		control.replay();

		resourceServlet.doGet(request, response);

		control.verify();
	}

}
