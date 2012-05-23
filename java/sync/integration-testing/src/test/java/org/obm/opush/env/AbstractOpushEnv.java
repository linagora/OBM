package org.obm.opush.env;

import org.obm.opush.ActiveSyncServletModule;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Modules;

public abstract class AbstractOpushEnv extends ActiveSyncServletModule {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private final ClassToInstanceAgregateView<Object> mockMap;
	
	public AbstractOpushEnv() {
		mockMap = new ClassToInstanceAgregateView<Object>();
	}
	
	@Provides
	public ClassToInstanceAgregateView<Object> makeInstanceMapInjectable() {
		return mockMap;
	}
	
	@Override
	protected Module overrideModule() throws Exception {
		AbstractOverrideModule[] modules = new AbstractOverrideModule[] {
			configuration(),
			dao(),
			email(),
			obmSync()
		};
		for (AbstractOverrideModule module: modules) {
			mockMap.addMap(module.getMockMap());
		}
		return Modules.combine(modules);
	}

	protected ObmSyncModule obmSync() {
		return new ObmSyncModule();
	}

	protected EmailModule email() {
		return new EmailModule();
	}

	protected DaoModule dao() {
		return new DaoModule();
	}

	protected ConfigurationModule configuration() {
		return new ConfigurationModule(new Configuration());
	}
	
	public ClassToInstanceAgregateView<Object> getMockMap() {
		return mockMap;
	}
}
