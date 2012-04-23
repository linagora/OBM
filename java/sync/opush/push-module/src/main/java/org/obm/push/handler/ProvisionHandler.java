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

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.ProvisionPolicyStatus;
import org.obm.push.bean.ProvisionStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
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
import org.obm.push.store.DeviceDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProvisionHandler extends WbxmlRequestHandler {

	private static final int INITIAL_POLICYKEY = 0;
	private final ProvisionProtocol protocol;
	private final DeviceDao deviceDao;

	@Inject
	protected ProvisionHandler(IBackend backend, EncoderFactory encoderFactory,
			DeviceDao deviceDao, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter, StateMachine stMachine, CollectionDao collectionDao, 
			ProvisionProtocol provisionProtocol, WBXMLTools wbxmlTools, DOMDumper domDumper) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, 
				stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.deviceDao = deviceDao;
		this.protocol = provisionProtocol;
	}
	
	@Override
	public void process(IContinuation continuation, UserDataRequest udr, Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			ProvisionRequest provisionRequest = protocol.getRequest(doc);
			logger.info("required {}", provisionRequest.toString());
			ProvisionResponse provisionResponse = doTheJob(provisionRequest, udr);
			Document ret = protocol.encodeResponse(provisionResponse);
			sendResponse(responder, ret);
		} catch (InvalidPolicyKeyException e) {
			sendErrorResponse(responder, ProvisionStatus.PROTOCOL_ERROR, e);
		} catch (DaoException e) {
			sendErrorResponse(responder, ProvisionStatus.GENERAL_SERVER_ERROR, e);
		}
	}

	private void sendErrorResponse(Responder responder, ProvisionStatus status, Exception e) {
		logger.error("Error creating provision response", e);
		sendResponse(responder, protocol.encodeErrorResponse(status));
	}

	private void sendResponse(Responder responder, Document ret) {
		responder.sendWBXMLResponse("Provision", ret);
	}

	private ProvisionResponse doTheJob(ProvisionRequest provisionRequest, UserDataRequest udr) throws DaoException {
		ProvisionResponse provisionResponse = new ProvisionResponse(provisionRequest.getPolicyType());
		provisionResponse.setStatus(ProvisionStatus.SUCCESS);
		long policyKey = provisionRequest.getPolicyKey();
		
		if (policyKey == INITIAL_POLICYKEY) {
			setUpNewPolicyKey(udr, provisionResponse);
			provisionResponse.setPolicy(backend.getDevicePolicy(udr));
		} else if (isCurrentPolicyKey(udr, policyKey)) {
			setUpNewPolicyKey(udr, provisionResponse);
		} else {
			provisionResponse.setPolicyStatus(ProvisionPolicyStatus.THE_CLIENT_IS_ACKNOWLEDGING_THE_WRONG_POLICY_KEY);
		}
		return provisionResponse;
	}

	private void setUpNewPolicyKey(UserDataRequest udr, ProvisionResponse provisionResponse) throws DaoException {
		provisionResponse.setPolicyKey(allocateNewPolicyKey(udr));
		provisionResponse.setPolicyStatus(ProvisionPolicyStatus.SUCCESS);
	}

	private boolean isCurrentPolicyKey(UserDataRequest bs, long policyKey) throws DaoException {
		Long actualPolicyKey = deviceDao.getPolicyKey(bs.getUser(), bs.getDevId());
		return Objects.equal(actualPolicyKey, policyKey);
	}

	private long allocateNewPolicyKey(UserDataRequest udr) throws DaoException {
		deviceDao.removePolicyKey(udr.getUser(), udr.getDevice());
		return deviceDao.allocateNewPolicyKey(udr.getUser(), udr.getDevId());
	}
}
