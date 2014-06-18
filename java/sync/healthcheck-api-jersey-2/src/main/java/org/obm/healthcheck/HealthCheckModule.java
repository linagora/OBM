/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.healthcheck;

import javax.inject.Inject;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.obm.healthcheck.handlers.RootHandler;
import org.obm.jersey.injection.JerseyResourceConfig;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

public class HealthCheckModule extends ServletModule {

	public final static String HEALTHCHECK_URL_PREFIX = "/healthcheck";
	public final static String HEALTHCHECK_URL_PATTERN = HEALTHCHECK_URL_PREFIX + "/*";
	private final ResourceConfig application;
	
	public HealthCheckModule() {
		this(new ResourceConfig());
	}
	
	public HealthCheckModule(ResourceConfig module) {
		application = ResourceConfig.forApplication(module).register(RootHandler.class)
				.property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true)
				.register(JacksonFeature.class);
	}
	
	@Override
	protected void configureServlets() {
		bind(ResourceConfig.class).toInstance(application);
		serve(HEALTHCHECK_URL_PATTERN).with(HealthCheckGuiceContainer.class);
	}
	
	@Singleton
	public static class HealthCheckGuiceContainer extends ServletContainer {
		
		@Inject
		public HealthCheckGuiceContainer(Injector injector, ResourceConfig application) {
			super(new JerseyResourceConfig(application, injector));
		}
		
	}
	
}
