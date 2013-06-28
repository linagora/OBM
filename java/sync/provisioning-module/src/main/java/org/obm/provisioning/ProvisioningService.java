package org.obm.provisioning;


import org.codehaus.jackson.Version;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.BatchDaoJdbcImpl;
import org.obm.provisioning.dao.OperationDao;
import org.obm.provisioning.dao.OperationDaoJdbcImpl;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.ProfileDaoJdbcImpl;
import org.obm.provisioning.json.MultimapJsonSerializer;
import org.obm.provisioning.json.ObmDomainUuidJsonDeserializer;
import org.obm.provisioning.json.ObmDomainUuidJsonSerializer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ProvisioningService extends JerseyServletModule {

	public static String PROVISIONING_ROOT_PATH = "provisioning/v1";
	public static String PROVISIONING_URL_PREFIX = "/" + PROVISIONING_ROOT_PATH;
	public static String PROVISIONING_URL_PATTERN = PROVISIONING_URL_PREFIX + "/*";

	@Override
	protected void configureServlets() {
		serve(PROVISIONING_URL_PATTERN).with(GuiceProvisioningJerseyServlet.class, ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));

		bindRestResources();
		bindDao();

		install(new LdapModule());
	}

	private void bindDao() {
		bind(ProfileDao.class).to(ProfileDaoJdbcImpl.class);
		bind(BatchDao.class).to(BatchDaoJdbcImpl.class);
		bind(OperationDao.class).to(OperationDaoJdbcImpl.class);
	}

	private void bindRestResources() {
		bind(DomainBasedSubResource.class);

		bind(ObmDomainProvider.class);
	}

	@Provides
	@Singleton
	public static ObjectMapper createObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("Serializers", new Version(0, 0, 0, null));

		module.addSerializer(ObmDomainUuid.class, new ObmDomainUuidJsonSerializer());
		module.addDeserializer(ObmDomainUuid.class, new ObmDomainUuidJsonDeserializer());
		module.addSerializer(Multimap.class, new MultimapJsonSerializer());

		objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.registerModule(module);

		return objectMapper;
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
