package org.obm.push.backend;

import javax.naming.ConfigurationException;

public interface IBackendFactory {

	IBackend loadBackend() throws ConfigurationException;

}
