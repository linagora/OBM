package org.obm.push.backend;

import java.io.InputStream;


public interface IErrorsManager {
	
	void sendMailHandlerError(BackendSession bs, InputStream errorMail, Throwable error);

}
