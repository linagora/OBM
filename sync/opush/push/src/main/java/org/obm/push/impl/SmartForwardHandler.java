package org.obm.push.impl;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.bean.MailRequest;
import org.obm.push.exception.ProcessingEmailException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.protocol.MailProtocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SmartForwardHandler extends MailRequestHandler {

	@Inject
	protected SmartForwardHandler(IContentsImporter contentsImporter, 
			IErrorsManager errorManager, MailProtocol mailProtocol) {
		
		super(contentsImporter, errorManager, mailProtocol);
	}

	@Transactional
	@Override
	public void doTheJob(MailRequest mailRequest, BackendSession bs) 
			throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		
		contentsImporter.forwardEmail(bs, mailRequest.getMailContent(), mailRequest.isSaveInSent(), 
				mailRequest.getCollectionId(), mailRequest.getServerId());
	}
	
}
