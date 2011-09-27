package org.obm.push.handler;

import java.io.IOException;

import org.eclipse.jetty.http.HttpStatus;
import org.minig.imap.IMAPException;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.MailProtocol;
import org.obm.push.protocol.bean.MailRequest;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MailRequestHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final IContentsImporter contentsImporter;
	private final IErrorsManager errorManager;
	private final MailProtocol mailProtocol;
	
	protected MailRequestHandler(IContentsImporter contentsImporter, IErrorsManager errorManager, MailProtocol mailProtocol) {
		this.contentsImporter = contentsImporter;
		this.errorManager = errorManager;
		this.mailProtocol = mailProtocol;
	}

	protected abstract void doTheJob(MailRequest mailRequest, BackendSession bs) 
			throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException, 
			CollectionNotFoundException, UnknownObmSyncServerException, DaoException, IMAPException;
	
	@Override
	public void process(IContinuation continuation, BackendSession bs, ActiveSyncRequest request, Responder responder) {
		MailRequest mailRequest = null;
		try {
			mailRequest = mailProtocol.getRequest(request);
			if (logger.isDebugEnabled()) {
				logger.debug("Mail content:\n" + new String(mailRequest.getMailContent()));
			}
			doTheJob(mailRequest, bs);

		} catch (SmtpInvalidRcptException se) {
			notifyUser(bs,  mailRequest.getMailContent(), se);
		} catch (ProcessingEmailException pe) {	
			notifyUser(bs,  mailRequest.getMailContent(), pe);
		} catch (SendEmailException e) {
			handleSendEmailException(e, responder, bs,  mailRequest.getMailContent());
		} catch (IOException e) {
			responder.sendError(HttpStatus.BAD_REQUEST_400);
			return;
		} catch (CollectionNotFoundException e) {
			notifyUser(bs,  mailRequest.getMailContent(), e);
		} catch (UnknownObmSyncServerException e) {
			notifyUser(bs,  mailRequest.getMailContent(), e);
		} catch (DaoException e) {
			notifyUser(bs,  mailRequest.getMailContent(), e);
		} catch (IMAPException e) {
			notifyUser(bs,  mailRequest.getMailContent(), e);
		} catch (QuotaExceededException e) {
			notifyUserQuotaExceeded(bs, e);
		}
	}

	private void notifyUserQuotaExceeded(BackendSession bs,
			QuotaExceededException e) {
		errorManager.sendQuotaExceededError(bs, e);
	}

	private void handleSendEmailException(SendEmailException e, Responder responder, BackendSession bs, byte[] mailContent) {
		if (e.getSmtpErrorCode() >= 500) {
			notifyUser(bs, mailContent, e);
		} else {
			responder.sendError(500);
		}
	}

	private void notifyUser(BackendSession bs, byte[] mailContent, Throwable t) {
		logger.error("Error while sending mail. A mail with the error will be sent at the sender.", t);
		errorManager.sendMailHandlerError(bs, mailContent, t);
	}

}
