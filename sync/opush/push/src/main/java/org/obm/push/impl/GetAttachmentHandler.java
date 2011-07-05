package org.obm.push.impl;

import java.io.IOException;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.MSAttachementData;
import org.obm.push.exception.ObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the search cmd
 * 
 */
@Singleton
public class GetAttachmentHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private final IContentsExporter contentsExporter;

	@Inject
	private GetAttachmentHandler(IContentsExporter contentsExporter) {
		this.contentsExporter = contentsExporter;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		String AttachmentName = request.getParameter("AttachmentName");

		MSAttachementData attachment;
		try {
			attachment = contentsExporter.getEmailAttachement(
					bs, AttachmentName);
			responder.sendResponseFile(attachment.getContentType(),
					attachment.getFile());
		} catch (ObjectNotFoundException e) {
			responder.sendError(500);
		}
	}
}
