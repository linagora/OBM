package org.obm.push.impl;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.data.EncoderFactory;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ISyncStorage;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the Provision cmd
 * 
 */
@Singleton
public class SettingsHandler extends WbxmlRequestHandler {

	@Inject
	private SettingsHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, ISyncStorage storage,
			IContentsExporter contentsExporter, StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		try {
			// send back the original document
			responder.sendResponse("Settings", doc);
		} catch (Exception e) {
			logger.error("Error creating provision response");
		}

	}

}
