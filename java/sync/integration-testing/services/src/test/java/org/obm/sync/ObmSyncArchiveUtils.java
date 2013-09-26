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

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.obm.DependencyResolverHelper;
import org.obm.StaticConfigurationService;
import org.obm.arquillian.GuiceWebXmlDescriptor;
import org.obm.configuration.TestTransactionConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.jdbc.H2DriverConfiguration;

import com.google.inject.Module;

public class ObmSyncArchiveUtils {

	public static WebArchive buildWebArchive(Class<? extends Module> guiceModule)
			throws IllegalArgumentException, IllegalStateException, ResolutionException,
			CoordinateParseException, UnknownExtensionTypeException {

		JavaArchive wholeObmSyncArchive = ShrinkWrap
				.create(JavaArchive.class, "services-integration-testing-classes.jar")
				.addAsManifestResource("MANIFEST.MF")
				.addAsResource("bitronix-default-config.properties")
				.addAsResource("ical4j.properties")
				.addAsResource("logback.xml")
				.addAsResource("Messages_en.properties")
				.addAsResource("Messages_fr.properties")
				.addClasses(LifecycleListener.class, LifecycleListenerHelper.class)
				.addClasses(DependencyResolverHelper.projectObmDaoClasses())
				.addClasses(DependencyResolverHelper.projectAnnotationsClasses())
				.addClasses(DependencyResolverHelper.projectConfigurationClasses())
				.addClasses(DependencyResolverHelper.projectDBCPClasses())
				.addClasses(DependencyResolverHelper.projectICalendarClasses())
				.addClasses(DependencyResolverHelper.projectMessageQueueClasses())
				.addClasses(DependencyResolverHelper.projectUtilsClasses())
				.addClasses(DependencyResolverHelper.projectLocatorClasses())
				.addClasses(DependencyResolverHelper.projectServicesCommonClasses())
				.addClasses(DependencyResolverHelper.projectCommonClasses())
				.addClasses(DependencyResolverHelper.projectDatabaseMetadataClasses());
			
		return ShrinkWrap
				.create(WebArchive.class)
				.addAsWebInfResource(GuiceWebXmlDescriptor.webXml(guiceModule, H2GuiceServletContextListener.class), "web.xml")
				.addAsLibraries(DependencyResolverHelper.projectDependencies(new File("pom.xml")))
				.addAsLibraries(wholeObmSyncArchive)
				.addClasses(
						ModuleUtils.class,
						org.obm.Configuration.class,
						org.obm.ConfigurationModule.class,
						DatabaseConfigurationFixtureH2.class,
						StaticConfigurationService.class,
						ObmSyncStaticConfigurationService.class,
						TestTransactionConfiguration.class,
						H2DriverConfiguration.class,
						H2GuiceServletContextListener.class);
	}

}
