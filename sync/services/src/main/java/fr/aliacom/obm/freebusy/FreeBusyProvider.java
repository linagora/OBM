/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.freebusy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.GuiceServletContextListener;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;

import com.google.common.base.Strings;
import com.google.inject.Injector;

import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.domain.DomainDao;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.utils.Ical4jHelper;

public class FreeBusyProvider extends HttpServlet {

	private static final long serialVersionUID = -3887606350629311688L;
	private Log logger = LogFactory.getLog(FreeBusyProvider.class);
	private CalendarDao calendarDao;
	private UserDao userDao;
	private DomainDao domainDao;
	
	@Override
	public void init() throws ServletException {
		super.init();
		Injector injector = (Injector) getServletContext().getAttribute(GuiceServletContextListener.ATTRIBUTE_NAME);
		calendarDao = injector.getInstance(CalendarDao.class);
		userDao = injector.getInstance(UserDao.class);
		domainDao = injector.getInstance(DomainDao.class);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String reqString = request.getRequestURI();
		logger.info("FreeBusyProvider : reqString: '" + reqString + "'");

		// get user from reqString : format
		// "/obm-sync/freebusy/firstname.lastname@obmdomain"

		String email = reqString.substring(reqString.lastIndexOf("/") + 1);
		logger.info("freebusy email : '" + email + "'");
		
		ObmDomain domain = null;
		ObmUser user = null;
		
		String domainName = getDomainName(email);
		if(!Strings.isNullOrEmpty(domainName)){
			domain = domainDao.findDomainByName(domainName);
		}
		if(domain != null){
			user = userDao.findUser(email, domain);
		}
		if (domain == null || user == null || !user.isPublicFreeBusy()) {
			logger.warn("FreeBusyProvider : user not found : '" + email + "' or freebusy is not public.");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		java.util.Calendar dnow = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		// Date dstamp = dnow.getTime();
		dnow.add(java.util.Calendar.MONTH, -1);
		Date dstart = dnow.getTime();
		dnow.add(java.util.Calendar.MONTH, 2);
		Date dend = dnow.getTime();

		FreeBusyRequest fbr = new FreeBusyRequest();
		fbr.setStart(dstart);
		fbr.setEnd(dend);
		List<Attendee> atts = new ArrayList<Attendee>(1);
		Attendee att = new Attendee();
		att.setEmail(email);
		atts.add(att);
		fbr.setAttendees(atts);
		fbr.setOwner(email);

		List<FreeBusy> fb = calendarDao.getFreeBusy(domain, fbr);
		String ics = "";
		if (fb.size() > 0) {
			ics = Ical4jHelper.parseFreeBusy(fb.iterator().next());
		}
		response.getOutputStream().write(ics.getBytes());
	}

	private String getDomainName(String email) {
		String[] parts = email.split("@");
		String domain = null;
		if (parts.length > 1) {
			domain = parts[1];
		}
		return domain;
	}

}