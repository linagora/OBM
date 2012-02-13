/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.handler;

import java.util.Random;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ProvisionStatus;
import org.obm.push.exception.InvalidPolicyKeyException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.ProvisionProtocol;
import org.obm.push.protocol.bean.ProvisionRequest;
import org.obm.push.protocol.bean.ProvisionResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
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
			ProvisionProtocol provisionProtocol, WBXMLTools wbxmlTools, DOMDumper domDumper) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, 
				stMachine, collectionDao, wbxmlTools, domDumper);
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
