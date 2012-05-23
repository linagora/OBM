package org.obm.opush.env;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.AbstractModule;

public abstract class AbstractOverrideModule extends AbstractModule {

	private ClassToInstanceMap<Object> mockMap;

	public AbstractOverrideModule() {
		mockMap = MutableClassToInstanceMap.create();
	}
	
	protected <T extends Object> void bindWithMock(Class<T> clazz) {
		T mock = createAndRegisterMock(clazz);
		bind(clazz).toInstance(mock);
	}

	protected <T> T createAndRegisterMock(Class<T> clazz) {
		T mock = createMock(clazz);
		mockMap.put(clazz, mock);
		return mock;
	}
	
	public ClassToInstanceMap<Object> getMockMap() {
		return mockMap;
	}
	
	public <T> T getMock(Class<T> clazz) {
		return getMockMap().getInstance(clazz);
	}
	
	@Override
	protected final void configure() {
		configureImpl();
	}

	public void replayMocks() {
		replay(mockMap.values().toArray());
	}
	
	protected abstract void configureImpl();
}
