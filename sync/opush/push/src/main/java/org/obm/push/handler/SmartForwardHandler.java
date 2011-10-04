package org.obm.push.handler;

import org.minig.imap.IMAPException;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.protocol.MailProtocol;
import org.obm.push.protocol.bean.MailRequest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SmartForwardHandler extends MailRequestHandler {

	@Inject
	protected SmartForwardHandler(IContentsImporter contentsImporter, 
			IErrorsManager errorManager, MailProtocol mailProtocol) {
		
		super(contentsImporter, errorManager, mailProtocol);
	}

	@Override
	public void doTheJob(MailRequest mailRequest, BackendSession bs) throws ProcessingEmailException, CollectionNotFoundException, 
	SendEmailException, SmtpInvalidRcptException, UnknownObmSyncServerException, DaoException, IMAPException {

		contentsImporter.forwardEmail(bs, mailRequest.getMailContent(), mailRequest.isSaveInSent(), 
				mailRequest.getCollectionId(), mailRequest.getServerId());
	}
	
}
