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
package org.obm.push;

import org.obm.locator.store.LocatorCache;
import org.obm.locator.store.LocatorService;
import org.obm.push.auth.AuthenticationServiceImpl;
import org.obm.push.backend.IAccessTokenResource;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.OBMBackend;
import org.obm.push.backend.PIMBackend;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.calendar.EventConverter;
import org.obm.push.calendar.EventConverterImpl;
import org.obm.push.calendar.EventServiceImpl;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.impl.OpushLocatorServiceImpl;
import org.obm.push.resource.AccessTokenResource;
import org.obm.push.search.ISearchSource;
import org.obm.push.search.ObmSearchContact;
import org.obm.push.service.AuthenticationService;
import org.obm.push.service.EventService;
import org.obm.push.service.OpushLocatorService;
import org.obm.push.task.TaskBackend;
import org.obm.sync.ObmSyncHttpClientModule;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.services.AttendeeService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ObmBackendModule extends AbstractModule {

    @Override
    protected void configure() {
		install(new ObmSyncHttpClientModule());
		bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
		bind(IBackend.class).to(OBMBackend.class);
		bind(ICalendarBackend.class).to(CalendarBackend.class);
		bind(EventService.class).to(EventServiceImpl.class);
		bind(EventConverter.class).to(EventConverterImpl.class);
		bind(LocatorService.class).to(LocatorCache.class);
		bind(OpushLocatorService.class).to(OpushLocatorServiceImpl.class);
		bind(AttendeeService.class).to(SimpleAttendeeService.class);
		bind(IAccessTokenResource.Factory.class).to(AccessTokenResource.Factory.class);
		
		Multibinder<PIMBackend> pimBackends = Multibinder.newSetBinder(binder(), PIMBackend.class);
		pimBackends.addBinding().to(CalendarBackend.class);
		pimBackends.addBinding().to(ContactsBackend.class);
		pimBackends.addBinding().to(TaskBackend.class);
		Multibinder<ISearchSource> searchSources = Multibinder.newSetBinder(binder(), ISearchSource.class);
		searchSources.addBinding().to(ObmSearchContact.class);
    }
}
