package org.obm.push.handler;

import java.util.Random;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ProvisionStatus;
import org.obm.push.exception.InvalidPolicyKeyException;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.ProvisionProtocol;
import org.obm.push.protocol.bean.ProvisionRequest;
import org.obm.push.protocol.bean.ProvisionResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
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
			IContentsExporter contentsExporter, StateMachine stMachine, CollectionDao collectionDao, 
			ProvisionProtocol provisionProtocol) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, stMachine, collectionDao);
		this.random = random;
		this.protocol = provisionProtocol;
	}
	
	@Override
	public void process(IContinuation continuation, BackendSession bs, Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			ProvisionRequest provisionRequest = protocol.getRequest(doc);
			logger.info("required {}", provisionRequest.toString());
			ProvisionResponse provisionResponse = doTheJob(provisionRequest, bs);
			Document ret = protocol.encodeResponse(provisionResponse);
			sendResponse(responder, ret);
		} catch (InvalidPolicyKeyException e) {
			sendErrorResponse(responder, ProvisionStatus.PROTOCOL_ERROR, e);
		}
	}

	private void sendErrorResponse(Responder responder, ProvisionStatus status, InvalidPolicyKeyException e) {
		logger.error("Error creating provision response", e);
		sendResponse(responder, protocol.encodeErrorResponse(status));
	}

	private void sendResponse(Responder responder, Document ret) {
		responder.sendResponse("Provision", ret);
	}

	private ProvisionResponse doTheJob(ProvisionRequest provisionRequest, BackendSession bs) {
		ProvisionResponse provisionResponse = new ProvisionResponse(provisionRequest.getPolicyType());
		final Long nextPolicyKey = nextPolicyKey(provisionRequest.getPolicyKey());
		if (nextPolicyKey == null) {
			provisionResponse.setStatus(ProvisionStatus.THE_CLIENT_IS_ACKNOWLEDGING_THE_WRONG_POLICY_KEY);
		} else {
			provisionResponse.setStatus(ProvisionStatus.SUCCESS);
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
