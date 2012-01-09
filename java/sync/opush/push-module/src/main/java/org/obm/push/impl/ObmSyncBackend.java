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
package org.obm.push.impl;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.PIMDataType;
import org.obm.push.service.impl.MappingService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.services.ICalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Named;

public class ObmSyncBackend {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected String obmSyncHost;

	private final IAddressBook bookClient;
	private final ICalendar calendarClient;
	private final ICalendar todoClient;
	private final LoginService login;
	protected final MappingService mappingService;

	protected ObmSyncBackend(MappingService mappingService, IAddressBook bookClient, 
			@Named(CalendarType.CALENDAR) ICalendar calendarClient, 
			@Named(CalendarType.TODO) ICalendar todoClient,
			LoginService login) {
		this.mappingService = mappingService;
		this.bookClient = bookClient;
		this.calendarClient = calendarClient;
		this.todoClient = todoClient;
		this.login = login;
	}

	protected AccessToken login(BackendSession session) {
		return login.login(session.getUser().getLoginAtDomain(), session.getPassword());
	}

	protected void logout(AccessToken at) {
		login.logout(at);
	}
	
	protected String getDefaultCalendarName(BackendSession bs) {
		return "obm:\\\\" + bs.getUser().getLoginAtDomain() + "\\calendar\\"
				+ bs.getUser().getLoginAtDomain();
	}
	
	protected IAddressBook getBookClient() {
		return bookClient;
	}
	
	protected ICalendar getCalendarClient() {
		return getEventSyncClient(PIMDataType.CALENDAR);
	}

	protected ICalendar getEventSyncClient(PIMDataType type) {
		if (PIMDataType.TASKS.equals(type)) {
			return todoClient;
		} else {
			return calendarClient;
		}
	}

	public LoginService getSyncClient() {
		return login;
	}
}
