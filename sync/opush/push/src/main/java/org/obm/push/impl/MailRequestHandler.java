package org.obm.push.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.exception.ProcessingEmailException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MailRequestHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final IContentsImporter contentsImporter;
	private final IErrorsManager errorManager;
	
	protected MailRequestHandler(IContentsImporter contentsImporter,
			IErrorsManager errorManager) {
		
		this.contentsImporter = contentsImporter;
		this.errorManager = errorManager;
	}

	@Override
	@Transactional
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException {
		InputStream mailContent = null;
		try {
			mailContent = new BufferedInputStream(request.getInputStream());
			mailContent.mark(mailContent.available());
		} catch (IOException e) {
			responder.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			Boolean saveInSent = false;
			String sis = request.getParameter("SaveInSent");
			if (sis != null) {
				saveInSent = sis.equalsIgnoreCase("T");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Mail content:\n"
						+ new String(FileUtils.streamBytes(mailContent, false)));
				mailContent.reset();
			}
			this.process(continuation, bs, mailContent, saveInSent, request,
					responder);

		} catch (SmtpInvalidRcptException se) {
			notifyUser(bs, mailContent, se);
		} catch (ProcessingEmailException pe) {
			notifyUser(bs, mailContent, pe);
		} catch (SendEmailException e) {
			handleSendEmailException(e, responder, bs, mailContent);
		} catch (Throwable t) {
			notifyUser(bs, mailContent, t);
		}
	}

	private void handleSendEmailException(SendEmailException e,
			Responder responder, BackendSession bs, InputStream mailContent)
			throws IOException {
		if (e.getSmtpErrorCode() >= 500) {
			notifyUser(bs, mailContent, e);
		} else {
			logger.error(
					"Error while sending mail. HTTP error[500] will send to the pda.",
					e);
			responder.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void notifyUser(BackendSession bs, InputStream mailContent,
			Throwable t) {
		logger.error(
				"Error while sending mail. A mail with the error will be sent at the sender.",
				t);
		try {
			mailContent.reset();
		} catch (IOException e) {
		}
		errorManager.sendMailHandlerError(bs, mailContent, t);
	}

	public abstract void process(IContinuation continuation, BackendSession bs,
			InputStream mailContent, Boolean saveInSent,
			ActiveSyncRequest request, Responder responder)
			throws SendEmailException, ProcessingEmailException,
			SmtpInvalidRcptException;

}
