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

import java.util.Date;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.InvalidSyncKeyException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.FolderSyncProtocol;
import org.obm.push.protocol.bean.FolderSyncRequest;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.push.utils.DateUtils;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FolderSyncHandler extends WbxmlRequestHandler {

	private final IHierarchyExporter hierarchyExporter;
	private final FolderSyncProtocol protocol;
	
	@Inject
	protected FolderSyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IHierarchyExporter hierarchyExporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			CollectionDao collectionDao, FolderSyncProtocol protocol,
			WBXMLTools wbxmlTools) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools);
		
		this.hierarchyExporter = hierarchyExporter;
		this.protocol = protocol;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		
		try {
			FolderSyncRequest folderSyncRequest = protocol.getRequest(doc);
			FolderSyncResponse folderSyncResponse = doTheJob(bs, folderSyncRequest);
			Document ret = protocol.encodeResponse(folderSyncResponse);
			sendResponse(responder, ret);
			
		} catch (InvalidSyncKeyException e) {
			logger.warn(e.getMessage(), e);
			sendResponse(responder, protocol.encodeErrorResponse(FolderSyncStatus.INVALID_SYNC_KEY));
		} catch (NoDocumentException e) {
			sendError(responder, FolderSyncStatus.INVALID_REQUEST, e);
		} catch (DaoException e) {
			sendError(responder, FolderSyncStatus.SERVER_ERROR, e);
		} catch (UnknownObmSyncServerException e) {
			sendError(responder, FolderSyncStatus.SERVER_ERROR, e);
		} catch (InvalidServerId e) {
			sendError(responder, FolderSyncStatus.INVALID_REQUEST, e);
		} catch (CollectionNotFoundException e) {
			sendError(responder, FolderSyncStatus.INVALID_REQUEST, e);
		}
	}

	private void sendResponse(Responder responder, Document ret) {
		responder.sendResponse("FolderHierarchy", ret);
	}
	
	private void sendError(Responder responder, FolderSyncStatus status, Exception exception) {
		logger.error(exception.getMessage(), exception);
		sendResponse(responder, protocol.encodeErrorResponse(status));
	}
	
	private FolderSyncResponse doTheJob(BackendSession bs, FolderSyncRequest folderSyncRequest) throws InvalidSyncKeyException, 
		DaoException, UnknownObmSyncServerException, InvalidServerId, CollectionNotFoundException {
		
		if (isFirstSync(folderSyncRequest)) {

			return getFolderSyncResponse(bs, 
					DateUtils.getEpochPlusOneSecondCalendar().getTime());
		} else {
			
			String syncKey = folderSyncRequest.getSyncKey();
			SyncState syncState = stMachine.getSyncState(syncKey);
			if (syncState == null) {
				throw new InvalidSyncKeyException(syncKey);
			}
			return getFolderSyncResponse(bs, syncState.getLastSync());
		}
	}

	private boolean isFirstSync(FolderSyncRequest folderSyncRequest) {
		return folderSyncRequest.getSyncKey().equals("0");
	}

	private FolderSyncResponse getFolderSyncResponse(BackendSession bs, Date lastSync) throws DaoException, CollectionNotFoundException, 
			UnknownObmSyncServerException, InvalidServerId {
		
		HierarchyItemsChanges hierarchyItemsChanges = getFolderChanges(bs, lastSync);
		String newSyncKey = stMachine.allocateNewSyncKey(bs, getCollectionId(bs), hierarchyItemsChanges.getLastSync(), 
				hierarchyItemsChanges.getItemsAddedOrUpdated(),
				hierarchyItemsChanges.getItemsDeleted());
		
		return new FolderSyncResponse(
				hierarchyItemsChanges.getItemsAddedOrUpdated(), hierarchyItemsChanges.getItemsDeleted(), newSyncKey);
	}

	private HierarchyItemsChanges getFolderChanges(BackendSession bs, Date lastSync) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException {
		
		return hierarchyExporter.getChanged(bs, lastSync);
	}
	
	private int getCollectionId(BackendSession bs) throws DaoException {
		return getOrAddCollectionId(bs.getDevice(), hierarchyExporter.getRootFolderUrl(bs));
	}

	private int getOrAddCollectionId(Device deviceId, String rootFolderUrl) throws DaoException {
		try {
			return collectionDao.getCollectionMapping(deviceId, rootFolderUrl);
		} catch (CollectionNotFoundException e) {
			return collectionDao.addCollectionMapping(deviceId, rootFolderUrl);
		}
	}

}