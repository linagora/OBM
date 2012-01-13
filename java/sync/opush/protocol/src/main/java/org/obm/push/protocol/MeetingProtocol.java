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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.MeetingResponse;
import org.obm.push.bean.MeetingResponseStatus;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.protocol.bean.MeetingHandlerRequest;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.bean.MeetingHandlerResponse.ItemChangeMeetingResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MeetingProtocol {

	public MeetingHandlerRequest getRequest(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException("Document of MeetingResponse request is null.");
		}
		NodeList requests = doc.getDocumentElement().getElementsByTagName("Request");
		List<MeetingResponse> items = parseNodeListAsMeetingResponses(requests);
		return new MeetingHandlerRequest(items);
	}
	
	public Document encodeResponses(MeetingHandlerResponse meetingResponse) {
		Document reply = DOMUtils.createDoc(null, "MeetingResponse");
		Element root = reply.getDocumentElement();
		for (ItemChangeMeetingResponse item: meetingResponse.getItemChanges()) {
			Element response = DOMUtils.createElement(root, "Result");
			DOMUtils.createElementAndText(response, "Status", item.getStatus().asXmlValue());
			if (item.getCalId() != null) {
				DOMUtils.createElementAndText(response, "CalId", item.getCalId());
			}
			DOMUtils.createElementAndText(response, "ReqId", item.getReqId());
		}
		return reply;
	}
	
	public Document encodeErrorResponse(MeetingResponseStatus invalidMeetingRrequest) {
		Document ret = DOMUtils.createDoc(null, "MeetingResponse");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", invalidMeetingRrequest.asXmlValue());
		return ret;
	}
	
	private List<MeetingResponse> parseNodeListAsMeetingResponses(
			NodeList requests) {
		List<MeetingResponse> items = new ArrayList<MeetingResponse>();

		for (int i = 0; i < requests.getLength(); i++) {
			Element req = (Element) requests.item(i);
			MeetingResponse mr = parseElementAsMeetingResponse(req);
			items.add(mr);
		}
		return items;
	}
	
	private MeetingResponse parseElementAsMeetingResponse(Element req) {
		String userResponse = DOMUtils.getElementText(req, "UserResponse");
		String collectionId = DOMUtils.getElementText(req, "CollectionId");
		String reqId = DOMUtils.getElementText(req, "ReqId");
		String longId = DOMUtils.getElementText(req, "LongId");

		MeetingResponse mr = new MeetingResponse();

		if (!StringUtils.isEmpty(collectionId)) {
			mr.setCollectionId(Integer.parseInt(collectionId));
		}
		mr.setReqId(reqId);
		mr.setLongId(longId);
		mr.setUserResponse(getAttendeeStatus(userResponse));
		return mr;
	}
	
	private AttendeeStatus getAttendeeStatus(String userResponse) {
		if ("1".equals(userResponse)) {
			return AttendeeStatus.ACCEPT;
		} else if ("2".equals(userResponse)) {
			return AttendeeStatus.TENTATIVE;
		} else if ("3".equals(userResponse)) {
			return AttendeeStatus.DECLINE;
		} else {
			return AttendeeStatus.TENTATIVE;
		}
	}
	
}
