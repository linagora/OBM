package org.obm.push.backend;

import javax.naming.ConfigurationException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BackendFactory implements IBackendFactory {
	
	private final IBackend obmBackend;

	@Inject
	private BackendFactory(IBackend obmBackend) {
		this.obmBackend = obmBackend;
	}
	
	@Override
	public IBackend loadBackend() throws ConfigurationException {
		return obmBackend;
	}

}
