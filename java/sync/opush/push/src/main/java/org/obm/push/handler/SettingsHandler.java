package org.obm.push.handler;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SettingsHandler extends WbxmlRequestHandler {

	@Inject
	protected SettingsHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			CollectionDao collectionDao, WBXMLTools wbxmlTools) {
		
		super(backend, encoderFactory, contentsImporter, 
				contentsExporter, stMachine, collectionDao, wbxmlTools);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {

		try {
			// send back the original document
			responder.sendResponse("Settings", doc);
		} catch (Exception e) {
			logger.error("Error creating provision response");
		}

	}

}
