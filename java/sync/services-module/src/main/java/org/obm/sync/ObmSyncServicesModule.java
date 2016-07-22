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
package org.obm.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.obm.configuration.module.LoggerModule;
import org.obm.service.ObmServicesModule;
import org.obm.sync.server.template.ITemplateLoader;
import org.obm.sync.server.template.TemplateLoaderFreeMarkerImpl;
import org.obm.sync.services.ICalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import fr.aliacom.obm.common.calendar.AnonymizerService;
import fr.aliacom.obm.common.calendar.AnonymizerServiceImpl;
import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.common.calendar.EventNotificationService;
import fr.aliacom.obm.common.calendar.EventNotificationServiceImpl;
import fr.aliacom.obm.common.calendar.MessageQueueService;
import fr.aliacom.obm.common.calendar.MessageQueueServiceImpl;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.setting.SettingsServiceImpl;
import fr.aliacom.obm.freebusy.DatabaseFreeBusyProvider;
import fr.aliacom.obm.freebusy.FreeBusyPluginModule;
import fr.aliacom.obm.freebusy.LocalFreeBusyProvider;
import fr.aliacom.obm.utils.HelperService;
import fr.aliacom.obm.utils.HelperServiceImpl;

public class ObmSyncServicesModule extends AbstractModule {

	private static final String APPLICATION_NAME = "obm-sync";

	@Override
	protected void configure() {
		install(new ObmServicesModule());
		
		bind(SettingsService.class).to(SettingsServiceImpl.class);
		bind(HelperService.class).to(HelperServiceImpl.class);
		bind(AnonymizerService.class).to(AnonymizerServiceImpl.class);
		bind(LocalFreeBusyProvider.class).to(DatabaseFreeBusyProvider.class);
		bind(MessageQueueService.class).to(MessageQueueServiceImpl.class);
		bind(EventNotificationService.class).to(EventNotificationServiceImpl.class);

		bind(ITemplateLoader.class).to(TemplateLoaderFreeMarkerImpl.class);
		bind(ICalendar.class).to(CalendarBindingImpl.class);
		ServiceLoader<FreeBusyPluginModule> pluginModules = ServiceLoader.load(FreeBusyPluginModule.class);

		List<FreeBusyPluginModule> pluginModulesList = new ArrayList<FreeBusyPluginModule>();
		for (FreeBusyPluginModule pluginModule : pluginModules) {
			pluginModulesList.add(pluginModule);
		}

		Collections.sort(pluginModulesList, Collections.reverseOrder());
		for (FreeBusyPluginModule pluginModule : pluginModulesList) {
			this.install(pluginModule);
		}

		bind(Boolean.class).annotatedWith(Names.named("queueIsRemote")).toInstance(false);
		bind(String.class).annotatedWith(Names.named("application-name")).toInstance(APPLICATION_NAME);
		bind(Logger.class).annotatedWith(Names.named(LoggerModule.CONFIGURATION)).toInstance(LoggerFactory.getLogger(LoggerModule.CONFIGURATION));
    }
}
