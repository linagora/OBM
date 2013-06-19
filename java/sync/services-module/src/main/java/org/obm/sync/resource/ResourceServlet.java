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

package org.obm.sync.resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.icalendar.Ical4jHelper;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;
import org.obm.sync.services.ICalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.calendar.ResourceNotFoundException;

@Singleton
public class ResourceServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(ResourceServlet.class);
	private ICalendar calendarBinding;
	private Ical4jHelper ical4jHelper;

	@Inject
	@VisibleForTesting ResourceServlet(ICalendar calendarBinding, Ical4jHelper ical4jHelper) {
		this.calendarBinding = calendarBinding;
		this.ical4jHelper = ical4jHelper;
		
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String resourceEmail = extractResourceEmail(request);
		if (resourceEmail == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			logger.warn("The email of the resource is null or empty ");
			return;			
		}
		
		try {
			response.setContentType("text/calendar;charset=UTF-8");
			String resourceICS = getResourceICS(resourceEmail);
			
			if (resourceICS == null) {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			} else {
				response.getWriter().write(resourceICS);
				response.setStatus(HttpServletResponse.SC_OK);
			}
		}
		catch(ResourceNotFoundException e) {
			logger.error(e.getMessage(),  e);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.flushBuffer();			
		}
		catch (ServerFault e) {
			logger.error(e.getMessage(),  e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.flushBuffer();
		}
	}

	private String extractResourceEmail(HttpServletRequest request) {
		String pathInfo = Strings.nullToEmpty(request.getPathInfo()).replaceFirst("^/", "");
		String resourceEmail;
		if (Strings.isNullOrEmpty(pathInfo)) {
			resourceEmail = null;
		} else {
			resourceEmail = pathInfo;
		}
		return resourceEmail;
	}

	@VisibleForTesting
	String getResourceICS(String resourceEmail) throws ServerFault {
		Collection<Event> resourceEvents = calendarBinding.getResourceEvents(resourceEmail, new Date());
		
		if (Iterables.isEmpty(resourceEvents)) {
			return null;
		}
		
		return this.ical4jHelper.buildIcs(null, resourceEvents, null);
	}
}
