package org.obm.push.handler;

import java.util.ArrayList;
import java.util.List;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MoveItem;
import org.obm.push.bean.MoveItemsStatus;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.PIMDataTypeNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.MoveItemsProtocol;
import org.obm.push.protocol.bean.MoveItemsRequest;
import org.obm.push.protocol.bean.MoveItemsResponse;
import org.obm.push.protocol.bean.MoveItemsResponse.MoveItemsItem;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
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
			CollectionDao collectionDao) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, stMachine, collectionDao);
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
		}
	}
	
	private void sendResponse(Responder responder, Document doc) {
		responder.sendResponse("Move", doc);
	}

	private MoveItemsResponse doTheJob(MoveItemsRequest moveItemsRequest, BackendSession bs) {
		final List<MoveItemsItem> moveItemsItems = new ArrayList<MoveItemsResponse.MoveItemsItem>();
		for (MoveItem item: moveItemsRequest.getMoveItems()) {
			
			StatusForItem statusForItem = getStatusForItem(item);
			MoveItemsItem moveItemsItem = new MoveItemsItem(statusForItem.status, item.getSourceMessageId());
			if (statusForItem.status == null) {
				try {
					PIMDataType dataClass = PIMDataType.getPIMDataType(statusForItem.srcCollection);
					String newDstId = contentsImporter.importMoveItem(bs, dataClass, statusForItem.srcCollection, statusForItem.dstCollection, item.getSourceMessageId());
					
					moveItemsItem.setStatusForItem(MoveItemsStatus.SUCCESS);
					moveItemsItem.setDstMesgId(newDstId);
				} catch (CollectionNotFoundException e) {
					moveItemsItem.setStatusForItem(MoveItemsStatus.SERVER_ERROR);
				} catch (DaoException e) {
					moveItemsItem.setStatusForItem(MoveItemsStatus.SERVER_ERROR);
				} catch (ProcessingEmailException e) {
					moveItemsItem.setStatusForItem(MoveItemsStatus.SERVER_ERROR);
				} catch (PIMDataTypeNotFoundException e) {
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
