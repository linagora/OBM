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
package org.obm.push.spushnik;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;
import org.obm.push.spushnik.bean.CheckResult;
import org.obm.push.spushnik.bean.CheckStatus;
import org.obm.push.spushnik.resources.FolderSyncScenario;
import org.obm.push.spushnik.resources.Scenario;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;

public class SpushnikWebArchive {

	public static WebArchive buildInstance() throws IllegalArgumentException,
			IllegalStateException, ResolutionException,
			CoordinateParseException, UnknownExtensionTypeException {

		return ShrinkWrap
				.create(WebArchive.class)
				.addClass(GuiceModule.class)
				.addClass(CheckResult.class)
				.addClass(CheckStatus.class)
				.addClass(FolderSyncScenario.class)
				.addClass(Scenario.class)
				.addAsLibraries(
					Maven.resolver()
						.addDependencies(
							MavenDependencies.createDependency(getRestEasyMavenJaxRsCoordinate(), ScopeType.COMPILE, false))
						.addDependencies(
							MavenDependencies.createDependency(getRestEasyMavenJacksonCoordinate(), ScopeType.COMPILE, false))
						.addDependencies(
							MavenDependencies.createDependency(getRestEasyMavenGuiceCoordinate(), ScopeType.COMPILE, false))
						.resolve().withTransitivity().asFile())
				.setWebXML(Resources.getResource("web.xml"));
	}

	private static String getRestEasyMavenJaxRsCoordinate() {
		return "org.jboss.resteasy:resteasy-jaxrs:" + restEasyVersion;
	}

	private static String getRestEasyMavenJacksonCoordinate() {
		return "org.jboss.resteasy:resteasy-jackson-provider:" + restEasyVersion;
	}
	
	private static String getRestEasyMavenGuiceCoordinate() {
		return "org.jboss.resteasy:resteasy-guice:" + restEasyVersion;
	}

	private static String restEasyVersion = getRestEasyVersion();
	private static String getRestEasyVersion() {
		Properties props = new Properties();
		InputStream in = GuiceResteasyBootstrapServletContextListener.class
				.getClassLoader()
				.getResourceAsStream(
						"META-INF/maven/org.jboss.resteasy/resteasy-guice/pom.properties");
		try {
			props.load(in);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		return props.getProperty("version");
	}

}
