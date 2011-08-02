package org.obm.push.protocol;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.MoveItem;
import org.obm.push.bean.MoveItemsRequest;
import org.obm.push.bean.MoveItemsResponse;
import org.obm.push.bean.MoveItemsResponse.MoveItemsItem;
import org.obm.push.exception.NoDocumentException;
import org.obm.push.impl.MoveItemsStatus;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MoveItemsProtocol {

	public MoveItemsProtocol() {
	}
	
	public MoveItemsRequest getRequest(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException();
		}
		
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
		
		return new MoveItemsRequest(moveItems);
	}

	public Document encodeResponse(MoveItemsResponse moveItemsResponse) {
		Document reply = DOMUtils.createDoc(null, "MoveItems");
		Element root = reply.getDocumentElement();
		
		for (MoveItemsItem moveItemsItem: moveItemsResponse.getMoveItemsItem()) {
		
			Element response = DOMUtils.createElement(root, "Response");
			
			switch (moveItemsItem.getItemStatus()) {
			case SUCCESS:
				DOMUtils.createElementAndText(response, "Status", MoveItemsStatus.SUCCESS.asXmlValue());
				DOMUtils.createElementAndText(response, "SrcMsgId",	moveItemsItem.getSourceMessageId());
				DOMUtils.createElementAndText(response, "DstMsgId",	moveItemsItem.getNewDstId());
				break;
			case SERVER_ERROR:
				DOMUtils.createElementAndText(response, "SrcMsgId", moveItemsItem.getSourceMessageId());
				DOMUtils.createElementAndText(response, "Status", MoveItemsStatus.SERVER_ERROR.asXmlValue());
				break;
			default:
				DOMUtils.createElementAndText(response, "SrcMsgId", moveItemsItem.getSourceMessageId());
				DOMUtils.createElementAndText(response, "Status", moveItemsItem.getItemStatus().asXmlValue());
				break;
			}			
		}
		return reply;
	}
	
}
