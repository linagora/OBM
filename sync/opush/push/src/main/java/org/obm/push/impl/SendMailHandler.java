package org.obm.push.impl;

import java.io.InputStream;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.exception.ProcessingEmailException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the SendMail cmd
 * 
 */
@Singleton
public class SendMailHandler extends MailRequestHandler {

	@Inject
	protected SendMailHandler(IContentsImporter contentsImporter,
			IErrorsManager errorManager) {
		
		super(contentsImporter, errorManager);
	}

	@Override
	@Transactional
	public void process(IContinuation continuation, BackendSession bs,
			InputStream mailContent, Boolean saveInSent, ActiveSyncRequest request,
			Responder responder) throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		contentsImporter.sendEmail(bs, mailContent, saveInSent);
	}

}
