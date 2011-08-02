package org.obm.push.backend;

import java.io.InputStream;

import org.obm.push.bean.BackendSession;


public interface IErrorsManager {
	
	void sendMailHandlerError(BackendSession bs, InputStream errorMail, Throwable error);

}
