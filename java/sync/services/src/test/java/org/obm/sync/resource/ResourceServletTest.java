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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.calendar.Event;
import org.obm.sync.services.ICalendar;

import com.google.inject.Injector;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.calendar.ResourceNotFoundException;

public class ResourceServletTest {

	private Ical4jHelper helper;
	private Ical4jUser iCalUser;
	private ResourceServlet resourceServlet;
	private Injector injector;
	private ServletConfig servletConfig;
	private ServletContext servletContext;
	private ICalendar calendarBinding;
	private Collection<Event> collectionEvents;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void setUp() {
		iCalUser = Ical4jUser.Factory.create().createIcal4jUser("toto@toto.com",
				ToolBox.getDefaultObmDomain());
		helper = new Ical4jHelper();
		resourceServlet = new ResourceServlet();

		servletConfig = EasyMock.createMock(ServletConfig.class);
		servletContext = EasyMock.createMock(ServletContext.class);
		injector = EasyMock.createMock(Injector.class);
		calendarBinding = EasyMock.createMock(ICalendar.class);
		request = EasyMock.createMock(HttpServletRequest.class);
		response = EasyMock.createMock(HttpServletResponse.class);

		EasyMock.expect(servletConfig.getServletContext()).andReturn(servletContext);
		EasyMock.expect(servletContext.getAttribute(EasyMock.isA(String.class)))
				.andReturn(injector);
		EasyMock.expect(injector.getInstance(EasyMock.eq(ICalendar.class))).andReturn(
				calendarBinding);
		EasyMock.expect(injector.getInstance(Ical4jHelper.class)).andReturn(helper);
	}

	@Test
	public void testGetResourceICS() throws Exception {
		int collectionSize = 5;
		collectionEvents = ToolBox.getFakeEventCollection(collectionSize);
		EasyMock.expect(
				calendarBinding.getResourceEvents(EasyMock.eq("resource@domain"), EasyMock.anyObject(Date.class)))
				.andReturn(collectionEvents);

		Object[] mocks = { servletConfig, servletContext, injector, calendarBinding };
		EasyMock.replay(mocks);
		resourceServlet.init(servletConfig);

		String ics = resourceServlet.getResourceICS("resource@domain");
		Assertions.assertThat(helper.parseICS(ics, iCalUser)).isNotNull().hasSize(collectionSize);
		EasyMock.verify(mocks);
	}

	@Test
	public void testDoGetEmailValid() throws Exception {
		int collectionSize = 5;
		collectionEvents = ToolBox.getFakeEventCollection(collectionSize);
		EasyMock.expect(
				calendarBinding.getResourceEvents(EasyMock.eq("resource@domain"), EasyMock.anyObject(Date.class)))
				.andReturn(collectionEvents);

		String uid = "/resource@domain";
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);

		EasyMock.expect(request.getPathInfo()).andReturn(uid);

		response.setStatus(EasyMock.eq(HttpServletResponse.SC_OK));
		EasyMock.expectLastCall();

		EasyMock.expect(response.getWriter()).andReturn(writer);
		Object[] mocks = { servletConfig, servletContext, injector, calendarBinding, request,
				response };

		EasyMock.replay(mocks);

		resourceServlet.init(servletConfig);
		resourceServlet.doGet(request, response);

		EasyMock.verify(mocks);
		String ics = stringWriter.toString();
		Assertions.assertThat(helper.parseICS(ics, iCalUser)).isNotNull().hasSize(collectionSize);
	}

	@Test
	public void testDoGetNoEmail() throws Exception {
		String uid = "";
		EasyMock.expect(request.getPathInfo()).andReturn(uid);

		response.setStatus(EasyMock.eq(HttpServletResponse.SC_NOT_FOUND));
		EasyMock.expectLastCall();

		Object[] mocks = { servletConfig, servletContext, injector, calendarBinding, request,
				response };
		EasyMock.replay(mocks);

		resourceServlet.init(servletConfig);
		resourceServlet.doGet(request, response);
		EasyMock.verify(mocks);
	}

	@Test
	public void testDoGetNoResourceWithEmail() throws Exception {
		String uid = "/resource@domain";
		EasyMock.expect(request.getPathInfo()).andReturn(uid);
		EasyMock.expect(
				calendarBinding.getResourceEvents(EasyMock.eq("resource@domain"), EasyMock.anyObject(Date.class)))
				.andThrow(new ResourceNotFoundException("Resource with id doesn't exist"));

		response.setStatus(EasyMock.eq(HttpServletResponse.SC_NOT_FOUND));
		response.flushBuffer();
		EasyMock.expectLastCall();

		Object[] mocks = { servletConfig, servletContext, injector, calendarBinding, request,
				response };
		EasyMock.replay(mocks);

		resourceServlet.init(servletConfig);
		resourceServlet.doGet(request, response);
		EasyMock.verify(mocks);
	}

}
