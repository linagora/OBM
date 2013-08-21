package org.obm.provisioning;


import javax.servlet.ServletContext;

import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.obm.cyrus.imap.CyrusClientModule;
import org.obm.domain.dao.EntityRightDao;
import org.obm.domain.dao.EntityRightDaoJdbcImpl;
import org.obm.domain.dao.PGroupDao;
import org.obm.domain.dao.PGroupDaoJdbcImpl;
import org.obm.domain.dao.PUserDao;
import org.obm.domain.dao.PUserDaoJdbcImpl;
import org.obm.domain.dao.UserSystemDao;
import org.obm.domain.dao.UserSystemDaoJdbcImpl;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.conf.SystemUserLdapConfiguration;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.BatchDaoJdbcImpl;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.GroupDaoJdbcImpl;
import org.obm.provisioning.dao.OperationDao;
import org.obm.provisioning.dao.OperationDaoJdbcImpl;
import org.obm.provisioning.dao.PermissionDao;
import org.obm.provisioning.dao.PermissionDaoHardcodedImpl;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.ProfileDaoJdbcImpl;
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
import org.obm.sync.date.DateProvider;
import org.obm.utils.ObmHelper;

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
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class ProvisioningService extends ServletModule {

	public static String PROVISIONING_ROOT_PATH = "provisioning/v1";
	public static String PROVISIONING_URL_PREFIX = "/" + PROVISIONING_ROOT_PATH;
	public static String PROVISIONING_URL_PATTERN = PROVISIONING_URL_PREFIX + "/*";

	private ServletContext servletContext;

	public ProvisioningService(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	protected void configureServlets() {
		bind(GuiceContainer.class).to(GuiceProvisioningJerseyServlet.class);

		bind(ShiroFilter.class).in(Singleton.class);
		filter("/*", "").through(ShiroFilter.class);
		
		serve(PROVISIONING_URL_PATTERN)
			.with(GuiceProvisioningJerseyServlet.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));

		bindRestResources();
		bindDao();
		
		install(new BatchProcessingModule());
		install(new LdapModule());
		install(new SatelliteClientModule());
		install(new CyrusClientModule());
		install(new ShiroAopModule());
		install(new AuthorizingModule(servletContext));
	}

	private void bindDao() {
		bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		bind(BatchDao.class).to(BatchDaoJdbcImpl.class);
		bind(OperationDao.class).to(OperationDaoJdbcImpl.class);
		bind(UserSystemDao.class).to(UserSystemDaoJdbcImpl.class);
		bind(DateProvider.class).to(ObmHelper.class);
		bind(PermissionDao.class).to(PermissionDaoHardcodedImpl.class);
		bind(GroupDao.class).to(GroupDaoJdbcImpl.class);
		bind(EntityRightDao.class).to(EntityRightDaoJdbcImpl.class);
		bind(PUserDao.class).to(PUserDaoJdbcImpl.class);
		bind(PGroupDao.class).to(PGroupDaoJdbcImpl.class);
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
