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
package org.obm.push.protocol;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.bean.MoveItem;
import org.obm.push.bean.MoveItemsStatus;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.protocol.bean.MoveItemsRequest;
import org.obm.push.protocol.bean.MoveItemsResponse;
import org.obm.push.protocol.bean.MoveItemsResponse.MoveItemsItem;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MoveItemsProtocol implements ActiveSyncProtocol<MoveItemsRequest, MoveItemsResponse> {

	public MoveItemsProtocol() {
	}
	
	@Override
	public MoveItemsRequest decodeRequest(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException("Document of MoveItems request is null.");
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

	@Override
	public Document encodeResponse(MoveItemsResponse moveItemsResponse) {
		Document reply = DOMUtils.createDoc(null, "MoveItems");
		Element root = reply.getDocumentElement();
		
		for (MoveItemsItem moveItemsItem: moveItemsResponse.getMoveItemsItem()) {
		
			Element response = DOMUtils.createElement(root, "Response");
			
			switch (moveItemsItem.getItemStatus()) {
			case SUCCESS:
				DOMUtils.createElementAndText(response, "Status", MoveItemsStatus.SUCCESS.asSpecificationValue());
				DOMUtils.createElementAndText(response, "SrcMsgId",	moveItemsItem.getSourceMessageId());
				DOMUtils.createElementAndText(response, "DstMsgId",	moveItemsItem.getNewDstId());
				break;
			case SERVER_ERROR:
				DOMUtils.createElementAndText(response, "SrcMsgId", moveItemsItem.getSourceMessageId());
				DOMUtils.createElementAndText(response, "Status", MoveItemsStatus.SERVER_ERROR.asSpecificationValue());
				break;
			default:
				DOMUtils.createElementAndText(response, "SrcMsgId", moveItemsItem.getSourceMessageId());
				DOMUtils.createElementAndText(response, "Status", moveItemsItem.getItemStatus().asSpecificationValue());
				break;
			}			
		}
		return reply;
	}
	
	public Document encodeErrorResponse(MoveItemsStatus moveItemsStatus) {
		Document document = DOMUtils.createDoc(null, "Move");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", moveItemsStatus.asSpecificationValue());
		return document;
	}
	
}
