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
package fr.aliacom.obm.freebusy;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.sync.GuiceServletContextListener;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.UserAttendee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class FreeBusyServlet extends HttpServlet {

	public final static String DATASOURCE_PARAMETER = "datasource";
	public final static String LOCAL_DATASOURCE = "local";
	public final static String REMOTE_DATASOURCE = "remote";
	
	private enum FreeBusyQueryType { LOCAL, REMOTE }
	
	private static final long serialVersionUID = -3887606350629311688L;
	private Logger logger = LoggerFactory.getLogger(FreeBusyServlet.class);
	private LocalFreeBusyProvider localFreeBusyProvider;
	private Collection<RemoteFreeBusyProvider> remoteFreeBusyProviders;
	
	@Override
	public void init() throws ServletException {
		super.init();
		Injector injector = (Injector) getServletContext().getAttribute(GuiceServletContextListener.ATTRIBUTE_NAME);
		
		localFreeBusyProvider = injector.getInstance(LocalFreeBusyProvider.class);
		
		TypeLiteral<Set<RemoteFreeBusyProvider>> setOfFreeBusyProviders = new TypeLiteral<Set<RemoteFreeBusyProvider>>() {};
		try {		
			remoteFreeBusyProviders = injector.getInstance(Key.get(setOfFreeBusyProviders));
		}
		catch (ConfigurationException e) {
			logger.info("No remote free busy providers configured");
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String reqString = request.getRequestURI();
		logger.info("FreeBusyServlet : reqString: '" + reqString + "'");

		// get user from reqString : format
		// "/obm-sync/freebusy/firstname.lastname@obmdomain"

		String email = reqString.substring(reqString.lastIndexOf("/") + 1);
		email = URLDecoder.decode(email, "UTF-8");
		logger.info("freebusy email : '" + email + "'");
		

		java.util.Calendar dnow = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		// Date dstamp = dnow.getTime();
		dnow.add(java.util.Calendar.MONTH, -1);
		Date dstart = dnow.getTime();
		dnow.add(java.util.Calendar.MONTH, 2);
		Date dend = dnow.getTime();

		FreeBusyRequest fbr = makeFreeBusyRequest(email, dstart, dend);
		
		Set<FreeBusyQueryType> queryTypes = findFreeBusyQueryTypes(request);
		List<FreeBusyProvider> providers = makeProvidersList(queryTypes);
		String ics = null;
		try {
			ics = findFreeBusyIcs(fbr, providers);
			if (ics != null) {
				response.getOutputStream().write(ics.getBytes());
			}
			else {
				logger.warn("FreeBusyServlet : user not found : '" + email +
				 "'");
			}
		} catch (PrivateFreeBusyException e) {
			logger.warn("FreeBusyServlet : freebusy for user : '" + email +
					 "' is not public.");
		}
		if (ics == null)
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
	}

	private String findFreeBusyIcs(FreeBusyRequest request, List<FreeBusyProvider> freeBusyProviders) throws PrivateFreeBusyException {
		String freeBusyIcs = null;
		for (FreeBusyProvider freeBusyProvider: freeBusyProviders) {
			try {
				freeBusyIcs = freeBusyProvider.findFreeBusyIcs(request);
				if (freeBusyIcs != null)
					return freeBusyIcs;
			}
			catch (PrivateFreeBusyException e) {
				throw e;
			}
			catch (Exception e) {
				logger.error("Got an error while looking up the availability (free/busy) of the " +
						"user '" + request.getOwner() + "' on provider " + freeBusyProvider + ", " + 
						"cascading to next provider if any", e);
			}
		}
		return freeBusyIcs;
	}

	private List<FreeBusyProvider> makeProvidersList(Set<FreeBusyQueryType> queryTypes) {
		List<FreeBusyProvider> freeBusyProviders = new ArrayList<FreeBusyProvider>();
		if (queryTypes.contains(FreeBusyQueryType.LOCAL)) {
			freeBusyProviders.add(localFreeBusyProvider);
		}
		if (queryTypes.contains(FreeBusyQueryType.REMOTE)) {
			if (remoteFreeBusyProviders != null) {
				freeBusyProviders.addAll(remoteFreeBusyProviders);
			}
			else {
				logger.warn("Attempted to retrieve a remote freebusy status, but no " +
						"freebusy providers are configured");
			}
		}
		return freeBusyProviders;
	}

	private Set<FreeBusyQueryType> findFreeBusyQueryTypes(HttpServletRequest request) {
		Set<FreeBusyQueryType> freeBusyQueryTypes = new HashSet<FreeBusyQueryType>();
		String[] dataSources = request.getParameterValues(DATASOURCE_PARAMETER);

		if (dataSources == null) {
			freeBusyQueryTypes.add(FreeBusyQueryType.LOCAL);
			freeBusyQueryTypes.add(FreeBusyQueryType.REMOTE);
		}
		else {
			boolean wantLocalDataSource = (Arrays.binarySearch(dataSources, LOCAL_DATASOURCE) > -1);
			boolean wantRemoteDataSource = (Arrays.binarySearch(dataSources, REMOTE_DATASOURCE) > -1);
			if (wantLocalDataSource) {
				freeBusyQueryTypes.add(FreeBusyQueryType.LOCAL);
			}
			if (wantRemoteDataSource) {
				freeBusyQueryTypes.add(FreeBusyQueryType.REMOTE);
			}
		}
		return freeBusyQueryTypes;
	}

	private FreeBusyRequest makeFreeBusyRequest(String email, Date dstart, Date dend) {
		FreeBusyRequest fbr = new FreeBusyRequest();
		
		fbr.setStart(dstart);
		fbr.setEnd(dend);
		fbr.addAttendee(UserAttendee.builder().email(email).build());
		fbr.setOwner(email);
		return fbr;
	}
}
