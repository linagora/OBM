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

import java.util.Date;
import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.server.Request;
import org.obm.sync.server.XmlResponder;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.session.SessionManagement;
import fr.aliacom.obm.common.setting.SettingBindingImpl;

/**
 * Handles the following urls :
 * 
 * <code>/setting/getSettings?sid=xxx</code>
 */
@Singleton
public class SettingHandler extends SecureSyncHandler {

	private final SettingBindingImpl binding;

	@Inject
	private SettingHandler(SessionManagement sessionManagement, SettingBindingImpl settingBindingImpl) {
		super(sessionManagement);
		this.binding = settingBindingImpl;
	}

	@Override
	public void handle(Request request, XmlResponder responder)
		throws Exception {
		AccessToken at = getCheckedToken(request);

		String method = request.getMethod();
		if ("getSettings".equals(method)) {
			getSettings(at, responder);
		} else if ("setVacationSettings".equals(method)) {
			setVacationSettings(at, request, responder);
		} else if ("getVacationSettings".equals(method)) {
			getVacationSettings(at, responder);
		} else if ("setEmailForwarding".equals(method)) {
			setEmailForwarding(at, request, responder);
		} else if ("getEmailForwarding".equals(method)) {
			getEmailForwarding(at, responder);
		} else {
			responder.sendError("Cannot handle method '" + method + "'");
		}

	}

	private void getEmailForwarding(AccessToken at, XmlResponder responder) throws ServerFault {
		ForwardingSettings fs = binding.getEmailForwarding(at);
		responder.sendEmailForwarding(fs);

	}

	private void setEmailForwarding(AccessToken at, Request request,
			XmlResponder responder) throws ServerFault {
		ForwardingSettings fs = new ForwardingSettings();
		fs.setEnabled(Boolean.valueOf(request.getParameter("enabled")));
		fs.setLocalCopy(Boolean.valueOf(request.getParameter("localCopy")));
		fs.setEmail(request.getParameter("email"));
		binding.setEmailForwarding(at, fs);
		responder.sendString("Forwarding settings saved");
	}

	private void getVacationSettings(AccessToken at, XmlResponder responder) 
		throws ServerFault {
		VacationSettings vs = binding.getVacationSettings(at);
		responder.sendVacation(vs);
	}

	private void setVacationSettings(AccessToken at, Request request,
			XmlResponder responder) throws ServerFault {
		VacationSettings vs = new VacationSettings();
		vs.setEnabled(Boolean.valueOf(request.getParameter("enabled")));
		if (vs.isEnabled()) {
			long l;
			Date d;

			String s = request.getParameter("start");
			if (s != null) {
				l = Long.parseLong(s);
				d = new Date(l);
				vs.setStart(d);
			}

			s = request.getParameter("end");
			if (s != null) {
				l = Long.parseLong(s);
				d = new Date(l);
				vs.setEnd(d);
			}

			vs.setText(request.getParameter("text"));
		}
		binding.setVacationSettings(at, vs);
		responder.sendString("Vacation settings stored");
	}

	private void getSettings(AccessToken at, XmlResponder responder) throws ServerFault {
		Map<String, String> ret = binding.getSettings(at);
		responder.sendSettings(ret);
	}

}
