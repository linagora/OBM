package org.obm.push.impl;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.MoveItem;
import org.obm.push.data.EncoderFactory;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.PIMDataType;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the MoveItems cmd
 * 
 * 
 */
@Singleton
public class MoveItemsHandler extends WbxmlRequestHandler {

	@Inject
	private MoveItemsHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, ISyncStorage storage,
			IContentsExporter contentsExporter, StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
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
	protected void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		NodeList moves = doc.getDocumentElement().getElementsByTagName("Move");
		List<MoveItem> moveItems = new LinkedList<MoveItem>();
		for (int i = 0; i < moves.getLength(); i++) {
			Element mv = (Element) moves.item(i);

			String srcMsgId = DOMUtils.getElementText(mv, "SrcMsgId");
			String srcFldId = DOMUtils.getElementText(mv, "SrcFldId");
			String dstFldId = DOMUtils.getElementText(mv, "DstFldId");

			MoveItem mi = new MoveItem(srcMsgId, srcFldId, dstFldId);
			moveItems.add(mi);
		}
		try {
			Document reply = DOMUtils.createDoc(null, "MoveItems");
			Element root = reply.getDocumentElement();
			for (MoveItem item : moveItems) {
				Element response = DOMUtils.createElement(root, "Response");

				StatusForItem statusForItem = getStatusForItem(item);

				if (statusForItem.status == null) {
					try {
						PIMDataType dataClass = storage.getDataClass(statusForItem.srcCollection);
						String newDstId = contentsImporter.importMoveItem(bs,
								dataClass, statusForItem.srcCollection, statusForItem.dstCollection, item
										.getSourceMessageId());
						DOMUtils.createElementAndText(response, "Status",
								MoveItemsStatus.SUCCESS.asXmlValue());
						DOMUtils.createElementAndText(response, "SrcMsgId",
								item.getSourceMessageId());
						DOMUtils.createElementAndText(response, "DstMsgId",
								newDstId);
					} catch (Exception e) {
						DOMUtils.createElementAndText(response, "SrcMsgId",
								item.getSourceMessageId());
						DOMUtils.createElementAndText(response, "Status",
								MoveItemsStatus.SERVER_ERROR.asXmlValue());
					}
				} else {
					DOMUtils.createElementAndText(response, "SrcMsgId", item
							.getSourceMessageId());
					DOMUtils.createElementAndText(response, "Status", statusForItem.status
							.asXmlValue());
				}
			}
			responder.sendResponse("Move", reply);
		} catch (Throwable e) {
			logger.info("Error creating Sync response", e);
		}
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
			status.dstCollectionId = Integer.parseInt(item.getDestinationFolderId());
			status.dstCollection = storage.getCollectionPath(status.dstCollectionId);
		} catch (Throwable nfe) {
			status.status = MoveItemsStatus.INVALID_DESTINATION_COLLECTION_ID;
		}
		
		try {
			status.srcCollectionId = Integer
					.parseInt(item.getSourceFolderId());
			status.srcCollection = storage.getCollectionPath(
					status.srcCollectionId);
		} catch (Throwable nfe) {
			status.status = MoveItemsStatus.INVALID_SOURCE_COLLECTION_ID;
		}
		if (status.status == null && status.srcCollectionId.equals(status.dstCollectionId)) {
			status.status = MoveItemsStatus.SAME_SOURCE_AND_DESTINATION_COLLECTION_ID;
		}
		return status;
	}
}
