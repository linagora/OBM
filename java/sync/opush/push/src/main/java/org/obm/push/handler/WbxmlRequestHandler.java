package org.obm.push.handler;

import java.io.IOException;
import java.io.InputStream;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Abstract class for handling client requests with a Content-Type set to
 * <code>application/vnd.ms-sync.wbxml</code>
 * 
 * @author tom
 * 
 */
public abstract class WbxmlRequestHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected final IBackend backend;
	protected final EncoderFactory encoderFactory;
	protected final IContentsImporter contentsImporter;
	protected final IContentsExporter contentsExporter;
	protected final StateMachine stMachine;
	protected final CollectionDao collectionDao;

	private final WBXMLTools wbxmlTools;

	protected WbxmlRequestHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter,
			StateMachine stMachine, CollectionDao collectionDao,
			WBXMLTools wbxmlTools) {
		
		this.backend = backend;
		this.encoderFactory = encoderFactory;
		this.contentsImporter = contentsImporter;
		this.contentsExporter = contentsExporter;
		this.stMachine = stMachine;
		this.collectionDao = collectionDao;
		this.wbxmlTools = wbxmlTools;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			ActiveSyncRequest request, Responder responder) throws IOException {

		InputStream in = request.getInputStream();
		byte[] input = FileUtils.streamBytes(in, true);
		Document doc = null;

		if (input != null && input.length > 0) {
			try {
				doc = wbxmlTools.toXml(input);
			} catch (IOException e) {
				logger.error("Error parsing wbxml data.", e);
				return;
			}
		} else {
			logger.debug("empty wbxml command (valid for Ping & Sync)");
			// To reduce the amount of data sent in a Ping command request, the
			// server caches the heartbeat
			// interval and folder list. The client can omit the heartbeat
			// interval, the folder list, or both from
			// subsequent Ping requests if those parameters have not changed
			// from the previous Ping
			// request. If neither the heartbeat interval nor the folder list
			// has changed, the client can issue a
			// Ping request that does not contain an XML body. If the Ping
			// element is specified in an XML
			// request body, either the HeartbeatInterval element or the Folders
			// element or both MUST be
			// specified.
		}

		if (doc != null && logger.isInfoEnabled()) {
			DOMDumper.dumpXml(logger, doc);
		}

		process(continuation, bs, doc, request, responder);
	}

	/**
	 * Handles the client request. The wbxml was already decoded and is
	 * available in the doc parameter.
	 * 
	 * @param continuation
	 * @param bs
	 * @param doc
	 *            the decoded wbxml document.
	 * @param responder
	 */
	protected abstract void process(IContinuation continuation,
			BackendSession bs, Document doc, ActiveSyncRequest request,
			Responder responder);

	protected EncoderFactory getEncoders() {
		return encoderFactory;
	}

}
