package org.obm.caldav.server;

import org.obm.caldav.server.share.Token;


public interface IBackendFactory {

	IBackend loadBackend(Token token) throws Exception;

}
