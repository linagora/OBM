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
package org.obm.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.ConfigurationServiceImpl;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.DatabaseConfigurationImpl;
import org.obm.configuration.DefaultTransactionConfiguration;
import org.obm.configuration.TransactionConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.dbcp.DatabaseConnectionProviderImpl;
import org.obm.locator.store.LocatorCache;
import org.obm.locator.store.LocatorService;
import org.obm.sync.date.DateProvider;
import org.obm.sync.server.template.ITemplateLoader;
import org.obm.sync.server.template.TemplateLoaderFreeMarkerImpl;
import org.obm.sync.services.AttendeeService;
import org.obm.sync.services.ICalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;

import fr.aliacom.obm.common.addition.CommitedOperationDao;
import fr.aliacom.obm.common.addition.CommitedOperationDaoJdbcImpl;
import fr.aliacom.obm.common.calendar.AttendeeServiceJdbcImpl;
import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.calendar.CalendarDaoJdbcImpl;
import fr.aliacom.obm.common.calendar.EventNotificationService;
import fr.aliacom.obm.common.calendar.EventNotificationServiceImpl;
import fr.aliacom.obm.common.calendar.MessageQueueService;
import fr.aliacom.obm.common.calendar.MessageQueueServiceImpl;
import fr.aliacom.obm.common.domain.DomainCache;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.setting.SettingsServiceImpl;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserServiceImpl;
import fr.aliacom.obm.freebusy.DatabaseFreeBusyProvider;
import fr.aliacom.obm.freebusy.FreeBusyPluginModule;
import fr.aliacom.obm.freebusy.LocalFreeBusyProvider;
import fr.aliacom.obm.utils.HelperService;
import fr.aliacom.obm.utils.HelperServiceImpl;
import fr.aliacom.obm.utils.ObmHelper;

public class GuiceServletContextListener implements ServletContextListener { 

	public static final String ATTRIBUTE_NAME = "GuiceInjecter";
	private static final String APPLICATION_NAME = "obm-sync";
	
    @Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
    	XTrustProvider.install();
    	
        final ServletContext servletContext = servletContextEvent.getServletContext(); 
        
        try {
        	Injector injector = createInjector();
        	if (injector == null) { 
        		failStartup("Could not create injector: createInjector() returned null"); 
        	} 
        	servletContext.setAttribute(ATTRIBUTE_NAME, injector);
        	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        } catch (Exception e) {
        	failStartup(e.getMessage());
        } 
    } 
    
    private Injector createInjector() throws Exception {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DomainService.class).to(DomainCache.class);
                bind(UserService.class).to(UserServiceImpl.class);
                bind(SettingsService.class).to(SettingsServiceImpl.class);
                bind(ObmSmtpConf.class).to(ObmSmtpConfImpl.class);
                bind(CalendarDao.class).to(CalendarDaoJdbcImpl.class);
                bind(CommitedOperationDao.class).to(CommitedOperationDaoJdbcImpl.class);
                bind(ITemplateLoader.class).to(TemplateLoaderFreeMarkerImpl.class);
                bind(LocalFreeBusyProvider.class).to(DatabaseFreeBusyProvider.class);
                bind(DatabaseConnectionProvider.class).to(DatabaseConnectionProviderImpl.class);
                bind(LocatorService.class).to(LocatorCache.class);
                bind(HelperService.class).to(HelperServiceImpl.class);
                bind(ConfigurationService.class).to(ConfigurationServiceImpl.class);
                bind(DatabaseConfiguration.class).to(DatabaseConfigurationImpl.class);
                bind(TransactionConfiguration.class).to(DefaultTransactionConfiguration.class);
                bind(MessageQueueService.class).to(MessageQueueServiceImpl.class);
                bind(EventNotificationService.class).to(EventNotificationServiceImpl.class);
                bind(ICalendar.class).to(CalendarBindingImpl.class);

                ServiceLoader<FreeBusyPluginModule> pluginModules = ServiceLoader
                        .load(FreeBusyPluginModule.class);

                List<FreeBusyPluginModule> pluginModulesList = new ArrayList<FreeBusyPluginModule>();
                for (FreeBusyPluginModule pluginModule : pluginModules) {
                    pluginModulesList.add(pluginModule);
                }

                Collections.sort(pluginModulesList, Collections.reverseOrder());
                for (FreeBusyPluginModule pluginModule : pluginModulesList) {
                    this.install(pluginModule);
                }
                
        		bind(String.class).annotatedWith(Names.named("application-name")).toInstance(APPLICATION_NAME);
        		bind(Logger.class).annotatedWith(Names.named(LoggerModule.CONFIGURATION)).toInstance(LoggerFactory.getLogger(LoggerModule.CONFIGURATION));
        		bind(DateProvider.class).to(ObmHelper.class);
        		bind(AttendeeService.class).to(AttendeeServiceJdbcImpl.class);
            }
        }, new MessageQueueModule(), new TransactionalModule(), new SolrJmsModule());
    }
    
    private void failStartup(String message) { 
        throw new CreationException(Collections.nCopies(1, new Message(this, message))); 
    }
    
    @Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) { 
    	servletContextEvent.getServletContext().setAttribute(ATTRIBUTE_NAME, null); 
    }
}