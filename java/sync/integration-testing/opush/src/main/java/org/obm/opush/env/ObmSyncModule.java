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
package org.obm.opush.env;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import org.apache.http.client.HttpClient;
import org.easymock.IMocksControl;
import org.obm.guice.AbstractOverrideModule;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.client.mailingList.MailingListClient;
import org.obm.sync.client.setting.SettingClient;
import org.obm.sync.client.user.UserClient;

import com.google.inject.name.Names;

public final class ObmSyncModule extends AbstractOverrideModule {

	public static final boolean PUSH_ENABLED = false;

	public ObmSyncModule(IMocksControl mocksControl) {
		super(mocksControl);
	}

	@Override
	protected void configureImpl() {
		bind(Boolean.class).annotatedWith(Names.named("enable-push")).toInstance(PUSH_ENABLED);
		UserClient.Factory userClientFactory = bindWithMock(UserClient.Factory.class);
		UserClient user = bindWithMock(UserClient.class);
		expect(userClientFactory.create(anyObject(HttpClient.class)))
			.andReturn(user).anyTimes();
		
		BookClient.Factory bookClientFactory = bindWithMock(BookClient.Factory.class);
		BookClient bookClient = bindWithMock(BookClient.class);
		expect(bookClientFactory.create(anyObject(HttpClient.class)))
			.andReturn(bookClient).anyTimes();
		
		CalendarClient.Factory calendarClientFactory = bindWithMock(CalendarClient.Factory.class);
		CalendarClient calendar = bindWithMock(CalendarClient.class);
		expect(calendarClientFactory.create(anyObject(HttpClient.class)))
			.andReturn(calendar).anyTimes();
		
		LoginClient.Factory loginClientFactory = bindWithMock(LoginClient.Factory.class);
		LoginClient loginClient = bindWithMock(LoginClient.class);
		expect(loginClientFactory.create(anyObject(HttpClient.class)))
			.andReturn(loginClient).anyTimes();
		
		MailingListClient.Factory mailingListClientFactory = bindWithMock(MailingListClient.Factory.class);
		MailingListClient mailingListClient = bindWithMock(MailingListClient.class);
		expect(mailingListClientFactory.create(anyObject(HttpClient.class)))
			.andReturn(mailingListClient).anyTimes();
		
		SettingClient.Factory settingClientFactory = bindWithMock(SettingClient.Factory.class);
		SettingClient settingClient = bindWithMock(SettingClient.class);
		expect(settingClientFactory.create(anyObject(HttpClient.class)))
			.andReturn(settingClient).anyTimes();
		
	}
}