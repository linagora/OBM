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

import java.util.ArrayList;
import java.util.List;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathUtils;
import org.obm.push.bean.MoveItem;
import org.obm.push.bean.MoveItemsStatus;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.MoveItemsProtocol;
import org.obm.push.protocol.bean.MoveItemsRequest;
import org.obm.push.protocol.bean.MoveItemsResponse;
import org.obm.push.protocol.bean.MoveItemsResponse.MoveItemsItem;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the MoveItems cmd
 * 
 * 
 */
@Singleton
public class MoveItemsHandler extends WbxmlRequestHandler {

	private final MoveItemsProtocol moveItemsProtocol;

	@Inject
	protected MoveItemsHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IContentsExporter contentsExporter, 
			StateMachine stMachine, MoveItemsProtocol moveItemsProtocol,
			CollectionDao collectionDao, WBXMLTools wbxmlTools, DOMDumper domDumper) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, 
				stMachine, collectionDao, wbxmlTools, domDumper);
		this.moveItemsProtocol = moveItemsProtocol;
	}

	// <?xml version="1.0" encoding="UTF-8"?>
	// <MoveItems>
	// <Move>
	// <SrcMsgId>56:340</SrcMsgId>
	// <SrcFldId>56</SrcFldId>
	// <DstFldId>57</DstFldId>
	// </Move>
	// <Move>
	// <SrcMsgId>56:339</SrcMsgId>
	// <SrcFldId>56</SrcFldId>
	// <DstFldId>57</DstFldId>
	// </Move>
	// </MoveItems>
	@Override
	protected void process(IContinuation continuation, BackendSession bs, Document doc, 
			ActiveSyncRequest request, Responder responder) {
		
		logger.info("process(" + bs.getUser().getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		try {
		
			MoveItemsRequest moveItemsRequest = moveItemsProtocol.getRequest(doc);
			MoveItemsResponse moveItemsResponse = doTheJob(moveItemsRequest, bs);
			Document reply = moveItemsProtocol.encodeResponse(moveItemsResponse);
			sendResponse(responder, reply);

		} catch (NoDocumentException e) {
			logger.error(e.getMessage(), e);
			sendResponse(responder, 
					moveItemsProtocol.encodeErrorResponse(MoveItemsStatus.SERVER_ERROR));
		} catch (CollectionPathException e) {
			logger.error(e.getMessage(), e);
			sendResponse(responder, 
					moveItemsProtocol.encodeErrorResponse(MoveItemsStatus.SERVER_ERROR));
		}
	}
	
	private void sendResponse(Responder responder, Document doc) {
		responder.sendResponse("Move", doc);
	}

	private MoveItemsResponse doTheJob(MoveItemsRequest moveItemsRequest, BackendSession bs) throws CollectionPathException {
		final List<MoveItemsItem> moveItemsItems = new ArrayList<MoveItemsResponse.MoveItemsItem>();
		for (MoveItem item: moveItemsRequest.getMoveItems()) {
			
			StatusForItem statusForItem = getStatusForItem(item);
			MoveItemsItem moveItemsItem = new MoveItemsItem(statusForItem.status, item.getSourceMessageId());
			if (statusForItem.status == null) {
				try {
					PIMDataType dataClass =  CollectionPathUtils.recognizePIMDataType(bs, statusForItem.srcCollection);
					String newDstId = contentsImporter.importMoveItem(bs, dataClass, statusForItem.srcCollection, statusForItem.dstCollection, item.getSourceMessageId());
					
					moveItemsItem.setStatusForItem(MoveItemsStatus.SUCCESS);
					moveItemsItem.setDstMesgId(newDstId);
				} catch (CollectionNotFoundException e) {
					moveItemsItem.setStatusForItem(MoveItemsStatus.SERVER_ERROR);
				} catch (DaoException e) {
					moveItemsItem.setStatusForItem(MoveItemsStatus.SERVER_ERROR);
				} catch (ProcessingEmailException e) {
					moveItemsItem.setStatusForItem(MoveItemsStatus.SERVER_ERROR);
				}
			}
			moveItemsItems.add(moveItemsItem);
		}
		return new MoveItemsResponse(moveItemsItems);
	}

	private static class StatusForItem {
		public String srcCollection;
		public String dstCollection;
		public Integer srcCollectionId;
		public Integer dstCollectionId;
		public MoveItemsStatus status;
	}
	
	private StatusForItem getStatusForItem(MoveItem item) {
		StatusForItem status = new StatusForItem();
		try {
			try {
				status.dstCollectionId = Integer.parseInt(item.getDestinationFolderId());
				status.dstCollection = collectionDao.getCollectionPath(status.dstCollectionId);
			} catch (CollectionNotFoundException ex) {
				status.status = MoveItemsStatus.INVALID_DESTINATION_COLLECTION_ID;
			}

			try {
				status.srcCollectionId = Integer.parseInt(item.getSourceFolderId());
				status.srcCollection = collectionDao.getCollectionPath(status.srcCollectionId);
			} catch (CollectionNotFoundException ex) {
				status.status = MoveItemsStatus.INVALID_SOURCE_COLLECTION_ID;
			}

			if (status.status == null && status.srcCollectionId.equals(status.dstCollectionId)) {
				status.status = MoveItemsStatus.SAME_SOURCE_AND_DESTINATION_COLLECTION_ID;
			}
		} catch (DaoException ex) {
			status.status = MoveItemsStatus.SERVER_ERROR;
		}
		return status;
	}
	
}
