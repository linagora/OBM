package org.obm.push.backend;

import org.obm.push.bean.BackendSession;
import org.obm.push.exception.QuotaExceededException;


public interface IErrorsManager {
	
	void sendMailHandlerError(BackendSession bs, byte[] errorMail, Throwable error);

	void sendQuotaExceededError(BackendSession bs, QuotaExceededException e);

}
