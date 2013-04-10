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
package org.obm.opush.env;

import org.easymock.IMocksControl;
import org.obm.guice.AbstractOverrideModule;
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.services.ICalendar;
import org.obm.sync.services.IMailingList;
import org.obm.sync.services.ISetting;

import com.google.inject.name.Names;

public final class ObmSyncModule extends AbstractOverrideModule {

	public static final boolean PUSH_ENABLED = false;

	public ObmSyncModule(IMocksControl mocksControl) {
		super(mocksControl);
	}

	@Override
	protected void configureImpl() {
		bind(Boolean.class).annotatedWith(Names.named("enable-push")).toInstance(PUSH_ENABLED);
		bindWithMock(IAddressBook.class);
		ICalendar calendar = createAndRegisterMock(ICalendar.class);
		bind(ICalendar.class)
			.annotatedWith(Names.named(CalendarType.CALENDAR))
			.toInstance(calendar);
		bind(ICalendar.class)
			.annotatedWith(Names.named(CalendarType.TODO))
			.toInstance(calendar);
		bindWithMock(LoginService.class);
		bindWithMock(IMailingList.class);
		bindWithMock(ISetting.class);
	}
}