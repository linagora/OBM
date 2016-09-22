/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2016 Linagora
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
package org.obm.service;

import org.obm.domain.dao.EntityDaoListener;
import org.obm.icalendar.Ical4jHelper;
import org.obm.locator.store.LocatorCache;
import org.obm.locator.store.LocatorService;
import org.obm.service.attendee.AttendeeServiceJdbcImpl;
import org.obm.service.domain.DomainCache;
import org.obm.service.domain.DomainService;
import org.obm.service.solr.SolrEntityDaoListener;
import org.obm.service.user.UserService;
import org.obm.service.user.UserServiceImpl;
import org.obm.sync.services.AttendeeService;
import org.obm.sync.utils.RecurrenceHelper;

import com.google.inject.AbstractModule;

public class ObmServicesModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MessageQueueClientModule());
		
		bind(DomainService.class).to(DomainCache.class);
		bind(UserService.class).to(UserServiceImpl.class);
		bind(LocatorService.class).to(LocatorCache.class);
		bind(AttendeeService.class).to(AttendeeServiceJdbcImpl.class);
		bind(RecurrenceHelper.class).to(Ical4jHelper.class);
		bind(EntityDaoListener.class).to(SolrEntityDaoListener.class);
	}

}
