/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.server.handler;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.server.Request;
import org.obm.sync.server.XmlResponder;

import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.common.session.SessionManagement;
import fr.aliacom.obm.utils.LogUtils;

/**
 * Responds to the following urls :
 *
 * <code>/user/getUserEmail?sid=xxx</code>
 */
public class UserHandler extends SecureSyncHandler {

	private final CalendarBindingImpl binding;

	@Inject
	public UserHandler(SessionManagement sessionManagement, CalendarBindingImpl calendarBindingImpl) {
		super(sessionManagement);
		
		this.binding = calendarBindingImpl;
	}

	@Override
	public void handle(Request request,
			XmlResponder responder) throws Exception {
		
		AccessToken at = getCheckedToken(request);
		String res = searchAndInvokeMethod(request, responder, at);
		logger.debug(LogUtils.prefix(at) + res);
	}

	private String searchAndInvokeMethod(Request request, XmlResponder responder, AccessToken at)
		throws ServerFault, FactoryConfigurationError, Exception {
		String method = request.getMethod();
		if (method.equals("getUserEmail")) {
			return getUserEmail(at, responder);
		} else {
			logger.error(LogUtils.prefix(at) + "cannot handle method '" + method + "'");
			return "";
		}
	} 


	private String getUserEmail(AccessToken at, XmlResponder responder) throws ServerFault {
		String ue = binding.getUserEmail(at);
		return responder.sendString(ue);
	}
	
}
