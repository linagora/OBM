package org.obm.caldav.obmsync;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.IBackendFactory;
import org.obm.caldav.server.share.Token;

public class BackendFactory implements IBackendFactory {

	private static final Log logger = LogFactory.getLog(BackendFactory.class);

	@Override
	public IBackend loadBackend(Token token) throws Exception {
		logger.info("Loading OBM backend");
		return new OBMBackend(token);
	}

}
