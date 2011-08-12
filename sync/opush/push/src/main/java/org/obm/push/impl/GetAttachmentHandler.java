package org.obm.push.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.obm.annotations.transactional.Propagation;
import org.minig.imap.IMAPException;
import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetAttachmentHandler implements IRequestHandler {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final IContentsExporter contentsExporter;

	@Inject
	protected GetAttachmentHandler(IContentsExporter contentsExporter) {
		this.contentsExporter = contentsExporter;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) {

		String AttachmentName = request.getParameter("AttachmentName");

		try {
			MSAttachementData attachment = getAttachment(bs, AttachmentName);
			responder.sendResponseFile(attachment.getContentType(),	attachment.getFile());
		} catch (AttachementNotFoundException e) {
			sendErrorResponse(responder, e);
		} catch (CollectionNotFoundException e) {
			sendErrorResponse(responder, e);
		} catch (DaoException e) {
			sendErrorResponse(responder, e);
		} catch (IMAPException e) {
			sendErrorResponse(responder, e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sendErrorResponse(Responder responder, Exception exception) {
		logger.error(exception.getMessage(), exception);
		try {
			responder.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional(propagation=Propagation.NESTED)
	private MSAttachementData getAttachment(BackendSession bs,
			String AttachmentName) throws AttachementNotFoundException, CollectionNotFoundException, DaoException, IMAPException {
		return contentsExporter.getEmailAttachement(bs, AttachmentName);
	}
}
