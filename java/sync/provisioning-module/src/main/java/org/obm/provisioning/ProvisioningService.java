package org.obm.provisioning;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class ProvisioningService extends JerseyServletModule {

	public static String PROVISIONING_URL_PREFIX = "/provisioning/v1";
	public static String PROVISIONING_URL_PATTERN = PROVISIONING_URL_PREFIX + "/*";
	
	@Override
	protected void configureServlets() {
		serve(PROVISIONING_URL_PATTERN).with(GuiceProvisioningJerseyServlet.class, 
				ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));
		
		bind(BatchResource.class);
		bind(UserResource.class);

		install(new LdapModule());
	}

	@Provides
	@Singleton
	public static ObjectMapper createObjectMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
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
