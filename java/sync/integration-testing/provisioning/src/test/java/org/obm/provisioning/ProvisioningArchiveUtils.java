/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.provisioning;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletContextListener;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.obm.DependencyResolverHelper;
import org.obm.StaticConfigurationService;
import org.obm.configuration.TestTransactionConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.jdbc.H2DriverConfiguration;

import com.google.inject.Module;

public class ProvisioningArchiveUtils {

	public static WebArchive buildWebArchive(File initialSqlScript)
			throws IllegalArgumentException, IllegalStateException, ResolutionException,
			CoordinateParseException, UnknownExtensionTypeException, URISyntaxException {

		JavaArchive wholeProvisioningModuleArchive = ShrinkWrap
				.create(JavaArchive.class, "provisioning-integration-testing-classes.jar")
				.addClass(ProvisioningService.class)
				.addClass(BatchResource.class)
				.addClass(ProfileResource.class)
				.addClass(ProvisioningContextListener.class)
				.addClasses(DependencyResolverHelper.projectAnnotationsClasses())
				.addClasses(DependencyResolverHelper.projectConfigurationClasses())
				.addClasses(DependencyResolverHelper.projectDBCPClasses())
				.addClasses(DependencyResolverHelper.projectUtilsClasses())
				.addClasses(DependencyResolverHelper.projectLdapClientClasses())
				.addClasses(DependencyResolverHelper.projectObmDaoClasses());
			
		URL pomXmlUrl = ClassLoader.getSystemResource("pom.xml");
		return ShrinkWrap
				.create(WebArchive.class)
				.addAsWebInfResource(webXml(TestingProvisioningContextListener.class, TestingProvisioningModule.class), "web.xml")
				.addAsResource(initialSqlScript, "dbInitialScript.sql")
				.addAsLibraries(DependencyResolverHelper.projectDependencies(new File(pomXmlUrl.toURI())))
				.addAsLibraries(wholeProvisioningModuleArchive)
				.addClasses(
						H2Initializer.class,
						TestingProvisioningModule.class,
						TestingProvisioningContextListener.class,
						org.obm.Configuration.class,
						org.obm.ConfigurationModule.class,
						DatabaseConfigurationFixtureH2.class,
						StaticConfigurationService.class,
						TestTransactionConfiguration.class,
						H2DriverConfiguration.class);
	}

	private static Asset webXml(
			Class<? extends ServletContextListener> classContextListener,
			Class<? extends Module> guiceModule) {
		return new StringAsset(
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\">" +
			"<web-app>" +
			
	        	"<display-name>OBM Provisioning integration testing</display-name>" +

				"<listener>" +
					"<listener-class>" + classContextListener.getName() + "</listener-class>" +
				"</listener>" +
                
	            "<context-param>" +
	            	"<param-name>guiceModule</param-name>" +
	            	"<param-value>" + guiceModule.getName() +"</param-value>" +
	        	"</context-param>" +
	
                "<filter>" +
	                "<filter-name>guiceFilter</filter-name>" +
	                "<filter-class>com.google.inject.servlet.GuiceFilter</filter-class>" +
                "</filter>" +
	                
                "<filter-mapping>" +
	                "<filter-name>guiceFilter</filter-name>" +
	                "<url-pattern>/*</url-pattern>" +
                "</filter-mapping>" +
	                
			"</web-app>");
	}
}
