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
package com.linagora.obm.ui.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.linagora.obm.ui.ioc.Module;
import com.linagora.obm.ui.page.CalendarPage;
import com.linagora.obm.ui.page.ContactPage;
import com.linagora.obm.ui.page.CreateCalendarPage;
import com.linagora.obm.ui.page.CreateContactPage;
import com.linagora.obm.ui.page.CreateUserPage;
import com.linagora.obm.ui.page.FindUserPage;
import com.linagora.obm.ui.page.HomePage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.service.Service;
import com.linagora.obm.ui.service.Services.Logout;

@Singleton
public class ServiceUrlMapping {

	private final Map<Class<? extends Service>, URL> mapping;
	
	@Inject
	private ServiceUrlMapping(@Named(Module.SERVER_URL) URL serverUrl) throws MalformedURLException {
		mapping = ImmutableMap.<Class<? extends Service>, URL>builder()
				.put(LoginPage.class, new URL(serverUrl, "/"))
				.put(HomePage.class, new URL(serverUrl, "/obm.php"))
				.put(Logout.class, new URL(serverUrl, "/obm.php?action=logout"))
				.put(CreateUserPage.class, new URL(serverUrl, "/user/user_index.php?action=new"))
				.put(ContactPage.class, new URL(serverUrl, "/contact/contact_index.php"))
				.put(CreateContactPage.class, new URL(serverUrl, "/contact/contact_index.php"))
				.put(CalendarPage.class, new URL(serverUrl, "/calendar/calendar_index.php"))
				.put(CreateCalendarPage.class, new URL(serverUrl, "/calendar/calendar_index.php"))
				.put(FindUserPage.class, new URL(serverUrl, "/user/user_index.php?action=index"))
				.build();
	}
	
	public URL lookup(Class<? extends Service> page) {
		return mapping.get(page);
	}
}
