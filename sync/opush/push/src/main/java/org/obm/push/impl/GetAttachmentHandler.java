package org.obm.push.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.exception.ObjectNotFoundException;
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
			ActiveSyncRequest request, Responder responder) throws IOException {

		String AttachmentName = request.getParameter("AttachmentName");

		try {
			MSAttachementData attachment = getAttachment(bs, AttachmentName);
			responder.sendResponseFile(attachment.getContentType(),	attachment.getFile());
		} catch (ObjectNotFoundException e) {
			responder.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	private MSAttachementData getAttachment(BackendSession bs,
			String AttachmentName) throws ObjectNotFoundException {
		return contentsExporter.getEmailAttachement(bs, AttachmentName);
	}
}
