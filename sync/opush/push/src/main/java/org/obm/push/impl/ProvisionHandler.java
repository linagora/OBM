package org.obm.push.impl;

import java.io.IOException;
import java.util.Random;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.ProvisionRequest;
import org.obm.push.bean.ProvisionResponse;
import org.obm.push.data.EncoderFactory;
import org.obm.push.exception.InvalidPolicyKeyException;
import org.obm.push.protocol.ProvisionProtocol;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ISyncStorage;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProvisionHandler extends WbxmlRequestHandler {

	private static final long DEFAULT_PKEY = 3378841480L;
	private final Random random;
	private final ProvisionProtocol protocol;

	@Inject
	protected ProvisionHandler(IBackend backend, EncoderFactory encoderFactory,
			Random random, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter,
			StateMachine stMachine, CollectionDao collectionDao, 
			ProvisionProtocol provisionProtocol) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine, collectionDao);
		
		this.random = random;
		this.protocol = provisionProtocol;
	}
	
	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {

		try {
			ProvisionRequest provisionRequest = protocol.getRequest(doc);
			
			logger.info("required {}", provisionRequest.toString());
			
			ProvisionResponse provisionResponse = doTheJob(provisionRequest, bs);
			Document ret = protocol.encodeResponse(provisionResponse);

			responder.sendResponse("Provision", ret);

		} catch (InvalidPolicyKeyException e) {
			logger.error("Error creating provision response", e);
		} catch (IOException e) {
			logger.error("Error creating provision response", e);
		}
	}

	@Transactional
	private ProvisionResponse doTheJob(ProvisionRequest provisionRequest, BackendSession bs) {
		ProvisionResponse provisionResponse = new ProvisionResponse(provisionRequest.getPolicyType());
		
		final Long nextPolicyKey = nextPolicyKey(provisionRequest.getPolicyKey());
		if (nextPolicyKey == null) {
			// The client is acknowledging the wrong policy key.
			provisionResponse.setStatus(5);
		} else {
			provisionResponse.setStatus(1);
			provisionResponse.setPolicyKey(nextPolicyKey);
		}
		if (provisionRequest.getPolicyKey() == 0) {
			provisionResponse.setPolicy(backend.getDevicePolicy(bs));
		}
		return provisionResponse;
	}

	private Long nextPolicyKey(long policyKey) {
		if (policyKey == 0) {
			return DEFAULT_PKEY;
		} else if (policyKey == DEFAULT_PKEY) {
			return Long.valueOf(Math.abs((random.nextInt() >> 2)));
		} else {
			return null;
		}
	}
	
}
