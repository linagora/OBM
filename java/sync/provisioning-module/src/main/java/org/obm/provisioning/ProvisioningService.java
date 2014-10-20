/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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


import java.util.TimeZone;

import javax.servlet.ServletContext;

import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.module.SimpleModule;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.EmailConfigurationImpl;
import org.obm.cyrus.imap.CyrusClientModule;
import org.obm.domain.dao.DaoModule;
import org.obm.domain.dao.UserSystemDao;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.conf.SystemUserLdapConfiguration;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.json.BatchJsonSerializer;
import org.obm.provisioning.json.ExceptionJsonSerializer;
import org.obm.provisioning.json.GroupExtIdJsonDeserializer;
import org.obm.provisioning.json.GroupExtIdJsonSerializer;
import org.obm.provisioning.json.GroupJsonDeserializer;
import org.obm.provisioning.json.GroupJsonSerializer;
import org.obm.provisioning.json.MultimapJsonSerializer;
import org.obm.provisioning.json.ObmDomainJsonSerializer;
import org.obm.provisioning.json.ObmDomainUuidJsonDeserializer;
import org.obm.provisioning.json.ObmDomainUuidJsonSerializer;
import org.obm.provisioning.json.ObmUserJsonDeserializer;
import org.obm.provisioning.json.ObmUserJsonSerializer;
import org.obm.provisioning.json.OperationJsonSerializer;
import org.obm.provisioning.json.UserExtIdJsonDeserializer;
import org.obm.provisioning.json.UserExtIdJsonSerializer;
import org.obm.provisioning.ldap.client.Configuration;
import org.obm.provisioning.ldap.client.LdapModule;
import org.obm.provisioning.resources.BatchResource;
import org.obm.provisioning.resources.DomainBasedSubResource;
import org.obm.provisioning.resources.DomainResource;
import org.obm.provisioning.resources.GroupResource;
import org.obm.provisioning.resources.GroupWriteResource;
import org.obm.provisioning.resources.ProfileResource;
import org.obm.provisioning.resources.UserResource;
import org.obm.provisioning.resources.UserWriteResource;
import org.obm.satellite.client.SatelliteClientModule;
import org.obm.sync.XTrustProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class ProvisioningService extends ServletModule {

	static {
		XTrustProvider.install();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	public final static String PROVISIONING_ROOT_PATH = "provisioning/v1";
	public final static String PROVISIONING_URL_PREFIX = "/" + PROVISIONING_ROOT_PATH;
	public final static String PROVISIONING_URL_PATTERN = PROVISIONING_URL_PREFIX + "/*";

	private final ServletContext servletContext;

	public ProvisioningService(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	protected void configureServlets() {
		bind(GuiceContainer.class).to(GuiceProvisioningJerseyServlet.class);
		bind(EmailConfiguration.class).toInstance(new EmailConfigurationImpl.Factory()
			.create(ConfigurationService.GLOBAL_OBM_CONFIGURATION_PATH));
		
		filter("/*", "").through(GuiceShiroFilter.class);

		serve(PROVISIONING_URL_PATTERN)
			.with(GuiceProvisioningJerseyServlet.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));

		bindRestResources();
		
		install(new DaoModule());
		install(new BatchProcessingModule());
		install(new LdapModule());
		install(new SatelliteClientModule());
		install(new CyrusClientModule());
		install(new ShiroAopModule());
		install(new AuthorizingModule(servletContext));
		install(new JerseyServletModule());
	}

	private void bindRestResources() {
		bind(DomainBasedSubResource.class);
		bind(DomainResource.class);
		bind(BatchResource.class);
		bind(ProfileResource.class);
		bind(UserResource.class);
		bind(UserWriteResource.class);
		bind(GroupResource.class);
		bind(GroupWriteResource.class);
		bind(ObmDomainProvider.class);
		bind(BatchProvider.class);
		bind(WebApplicationExceptionMapper.class);
		bind(GlobalExceptionMapper.class);
	}

	@Provides
	public static Configuration ldapConfiguration(UserSystemDao userSystemDao) throws DaoException {
		return new SystemUserLdapConfiguration(userSystemDao.getByLogin("ldapadmin"));
	}

	@Provides
	@RequestScoped
	public static ObmDomain domainInRequest(HttpContext context) {
		return (ObmDomain) context.getProperties().get(ObmDomainProvider.DOMAIN_KEY);
	}

	public static ObjectMapper createObjectMapper(Module... modules) {
		SimpleModule module =
				new SimpleModule("Serializers", new Version(0, 0, 0, null))
				.addSerializer(ObmDomainUuid.class, new ObmDomainUuidJsonSerializer())
				.addDeserializer(ObmDomainUuid.class, new ObmDomainUuidJsonDeserializer())
				.addSerializer(Multimap.class, new MultimapJsonSerializer())
				.addSerializer(ObmDomain.class, new ObmDomainJsonSerializer())
				.addSerializer(Operation.class, new OperationJsonSerializer())
				.addSerializer(Batch.class, new BatchJsonSerializer())
				.addSerializer(ObmUser.class, new ObmUserJsonSerializer())
				.addSerializer(UserExtId.class, new UserExtIdJsonSerializer())
				.addDeserializer(UserExtId.class, new UserExtIdJsonDeserializer())
				.addSerializer(GroupExtId.class, new GroupExtIdJsonSerializer())
				.addDeserializer(GroupExtId.class, new GroupExtIdJsonDeserializer())
				.addSerializer(Group.class, new GroupJsonSerializer())
				.addDeserializer(Group.class, new GroupJsonDeserializer())
				.addSerializer(Exception.class, new ExceptionJsonSerializer());

		ObjectMapper mapper = new ObjectMapper()
				.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
				.setSerializationInclusion(Inclusion.NON_NULL)
				.withModule(module);

		for (Module m : modules) {
			mapper.registerModule(m);
		}

		return mapper;
	}

	@Provides
	@Singleton
	public static ObjectMapper createObjectMapper(Injector injector) {
		SimpleModule module = new SimpleModule("RequestScoped", new Version(0, 0, 0, null))
			.addDeserializer(ObmUser.class, injector.getInstance(ObmUserJsonDeserializer.class));

		return createObjectMapper(module);
	}

	@Provides
	@Singleton
	public static JacksonJsonProvider jacksonJsonProvider(ObjectMapper mapper) {
		return new JacksonJsonProvider(mapper);
	}

	@Singleton
	private static class GuiceProvisioningJerseyServlet extends GuiceContainer {

		@Inject
		private GuiceProvisioningJerseyServlet(Injector injector) {
			super(injector);
		}

	}

}
