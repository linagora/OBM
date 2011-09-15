package org.obm.push.handler;

import javax.servlet.http.HttpServletResponse;

import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.Responder;
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
		} catch (ProcessingEmailException e) {
			sendErrorResponse(responder, e);
		}
	}

	private void sendErrorResponse(Responder responder, Exception exception) {
		logger.error(exception.getMessage(), exception);
		responder.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	private MSAttachementData getAttachment(BackendSession bs, String AttachmentName) 
			throws AttachementNotFoundException, CollectionNotFoundException, DaoException, ProcessingEmailException {
		return contentsExporter.getEmailAttachement(bs, AttachmentName);
	}
	
}
