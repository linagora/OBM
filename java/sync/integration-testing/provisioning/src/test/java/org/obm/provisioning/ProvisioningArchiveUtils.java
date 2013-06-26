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
package org.obm.provisioning;

import java.io.File;
import java.util.Arrays;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.obm.StaticConfigurationService;
import org.obm.annotations.database.AutoTruncate;
import org.obm.annotations.database.DatabaseEntity;
import org.obm.annotations.database.DatabaseField;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.annotations.transactional.Propagation;
import org.obm.annotations.transactional.TransactionException;
import org.obm.annotations.transactional.TransactionProvider;
import org.obm.annotations.transactional.Transactional;
import org.obm.annotations.transactional.TransactionalBinder;
import org.obm.annotations.transactional.TransactionalInterceptor;
import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.TestTransactionConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.jdbc.H2DriverConfiguration;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Module;

public class ProvisioningArchiveUtils {

	public static WebArchive buildWebArchive(Class<? extends Module> guiceModule)
			throws IllegalArgumentException, IllegalStateException, ResolutionException,
			CoordinateParseException, UnknownExtensionTypeException {

		JavaArchive wholeObmSyncArchive = ShrinkWrap
				.create(JavaArchive.class, "provisioning-integration-testing-classes.jar")
				.addClass(ProvisioningService.class);
			
		return ShrinkWrap
				.create(WebArchive.class)
				.addAsWebInfResource(webXml(), "web.xml")
				.addAsLibraries(projectDependencies())
				.addAsLibraries(wholeObmSyncArchive)
				.addClasses(
						org.obm.Configuration.class,
						org.obm.ConfigurationModule.class,
						DatabaseConfigurationFixtureH2.class,
						StaticConfigurationService.class,
						TestTransactionConfiguration.class,
						H2DriverConfiguration.class);
	}

	private static File[] projectDependencies() {
		return filterObmDependencies(allObmSyncDependencies());
	}

	private static MavenResolvedArtifact[] allObmSyncDependencies() {
		return Maven.resolver()
			.offline()
			.loadPomFromFile("pom.xml")
			.importRuntimeDependencies()
			.resolve()
			.withTransitivity()
			.asResolvedArtifact();
	}

	private static File[] filterObmDependencies(MavenResolvedArtifact[] allObmSyncDependencies) {
		return FluentIterable.from(Arrays.asList(
				allObmSyncDependencies))
				.filter(obmDependencyPredicate())
				.transform(artifactAsFile()).toArray(File.class);
	}

	private static Function<MavenResolvedArtifact, File> artifactAsFile() {
		return new Function<MavenResolvedArtifact, File>() {
			@Override
			public File apply(MavenResolvedArtifact input) {
				return input.asFile();
			}
		};
	}

	private static Predicate<MavenResolvedArtifact> obmDependencyPredicate() {
		return new Predicate<MavenResolvedArtifact>() {

			@Override
			public boolean apply(MavenResolvedArtifact input) {
				String groupId = input.getCoordinate().getGroupId();
				return !(groupId.startsWith("com.linagora") || groupId.startsWith("org.obm"));
			}
		};
	}

	private static Asset webXml() {
		return new StringAsset(
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\">" +
			"<web-app>" +
			
	        	"<display-name>OBM Sync integration testing</display-name>" +
	        	
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

	public static Class<?>[] projectAnnotationsClasses() {
		return new Class<?>[] {
				ITransactionAttributeBinder.class,
				Propagation.class,
				TransactionalBinder.class,
				TransactionalInterceptor.class,
				Transactional.class,
				TransactionalModule.class,
				TransactionException.class,
				TransactionProvider.class,
				AutoTruncate.class,
				DatabaseField.class,
				DatabaseEntity.class
		};
	}

}
