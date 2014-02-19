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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.changes.ChangeSet;
import org.apache.commons.compress.changes.ChangeSetPerformer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.obm.StaticConfigurationService;
import org.obm.StaticLocatorConfiguration;
import org.obm.arquillian.GuiceWebXmlDescriptor;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.jdbc.H2DriverConfiguration;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.inject.Module;

public class ObmSyncArchiveUtils {

	public static WebArchive createDeployment() {
		return createDeployment(ServicesTestModule.class);
	}

	public static WebArchive createDeployment(Class<? extends Module> moduleClass) {
		return buildWebArchive(moduleClass)
				.addClasses(moduleClass)
				.addAsResource("sql/org/obm/sync/calendar/h2.sql", H2GuiceServletContextListener.INITIAL_DB_SCRIPT);
	}
	
	public static WebArchive buildWebArchive(Class<? extends Module> guiceModule)
			throws IllegalArgumentException, IllegalStateException, ResolutionException,
			CoordinateParseException, UnknownExtensionTypeException {


		return ShrinkWrap
				.create(WebArchive.class)
				.addAsWebInfResource(GuiceWebXmlDescriptor.webXml(guiceModule, H2GuiceServletContextListener.class), "web.xml")
				.addAsLibraries(serviceModule())
				.addAsLibraries(resolveArtifacts("com.h2database:h2"))
				.addClasses(
						ModuleUtils.class,
						org.obm.Configuration.class,
						org.obm.ConfigurationModule.class,
						DatabaseConfigurationFixtureH2.class,
						StaticConfigurationService.class,
						StaticLocatorConfiguration.class,
						ObmSyncStaticConfigurationService.class,
						H2DriverConfiguration.class,
						H2ConnectionProvider.class,
						H2InMemoryDatabase.class,
						H2GuiceServletContextListener.class);
	}

	private static File[] replaceServiceJar(File[] asFile) {
		return FluentIterable.from(Arrays.asList(asFile))
				.transform(new Function<File, File>() {
			
			@Override
			public File apply(File input) {
				if (input.getName().contains("services-module")) {
					ZipFile servicesZip = null;
					try {
						File outputFile = File.createTempFile("services-module", ".jar");
						servicesZip = new ZipFile(input);
						
						ChangeSet changeSet = new ChangeSet();
						changeSet.add(new JarArchiveEntry("META-INF/MANIFEST.MF"), ClassLoader.getSystemClassLoader().getResourceAsStream("MANIFEST.MF"));
						ChangeSetPerformer changeSetPerformer = new ChangeSetPerformer(changeSet);
						JarArchiveOutputStream jarArchiveOutputStream = new JarArchiveOutputStream(
								new FileOutputStream(outputFile));
						changeSetPerformer.perform(servicesZip, jarArchiveOutputStream);
						return outputFile;
					} catch (IOException e) {
						Throwables.propagate(e);
					} finally {
						try {
							if (servicesZip != null) {
								servicesZip.close();
							}
						} catch (IOException e) {
							Throwables.propagate(e);
						}
					}
				}
				return input;
			}
		}).toArray(File.class);
	}

	private static File[] serviceModule() {
		File[] asFile = resolveArtifacts("com.linagora.obm:services-module");
		return replaceServiceJar(asFile);
	}
	
	private static File[] resolveArtifacts(String artifactCoordinates) {
		return Maven.resolver()
				.offline()
				.loadPomFromFile("pom.xml")
				.resolve(artifactCoordinates)
				.withClassPathResolution(true)
				.withTransitivity()
				.asFile();
	}

}
