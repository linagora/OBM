/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import org.apache.commons.lang.StringUtils;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.MeetingResponse;
import org.obm.push.bean.MeetingResponseStatus;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.protocol.bean.ItemChangeMeetingResponse;
import org.obm.push.protocol.bean.MeetingHandlerRequest;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class MeetingProtocol implements ActiveSyncProtocol<MeetingHandlerRequest, MeetingHandlerResponse> {

	@Override
	public MeetingHandlerRequest decodeRequest(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException("Document of MeetingResponse request is null.");
		}
		NodeList requests = doc.getDocumentElement().getElementsByTagName("Request");
		
		MeetingHandlerRequest.Builder meetingHandlerRequestBuilder = MeetingHandlerRequest.builder();
		parseNodeListAsMeetingResponses(requests, meetingHandlerRequestBuilder);

		return meetingHandlerRequestBuilder
			.build();
	}
	
	private void parseNodeListAsMeetingResponses(NodeList requests, MeetingHandlerRequest.Builder meetingHandlerRequestBuilder) {

		for (int i = 0; i < requests.getLength(); i++) {
			Element req = (Element) requests.item(i);
			MeetingResponse mr = parseElementAsMeetingResponse(req);
			meetingHandlerRequestBuilder.add(mr);
		}
	}
	
	private MeetingResponse parseElementAsMeetingResponse(Element req) {
		String userResponse = DOMUtils.getElementText(req, "UserResponse");
		String colId = DOMUtils.getElementText(req, "CollectionId");
		String reqId = DOMUtils.getElementText(req, "ReqId");
		String longId = DOMUtils.getElementText(req, "LongId");

		Integer collectionId = null;
		if (!StringUtils.isEmpty(colId)) {
			collectionId = Integer.parseInt(colId);
		}
		
		return MeetingResponse.builder()
				.collectionId(collectionId)
				.reqId(reqId)
				.longId(longId)
				.userResponse(getAttendeeStatus(userResponse))
				.build();
	}
	
	private AttendeeStatus getAttendeeStatus(String userResponse) {
		if ("1".equals(userResponse)) {
			return AttendeeStatus.ACCEPT;
		}
		if ("2".equals(userResponse)) {
			return AttendeeStatus.TENTATIVE;
		}
		if ("3".equals(userResponse)) {
			return AttendeeStatus.DECLINE;
		}
		return AttendeeStatus.TENTATIVE;
	}

	@Override
	public MeetingHandlerResponse decodeResponse(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException("Document of MeetingResponse request is null.");
		}
		
		Element mr = doc.getDocumentElement();
		NodeList results = mr.getElementsByTagName("Result");
		MeetingHandlerResponse.Builder meetingHandlerResponseBuilder = MeetingHandlerResponse.builder();
		for (int i = 0; i < results.getLength(); i++) {
			Element result = (Element) results.item(i);

			MeetingResponseStatus status = MeetingResponseStatus.fromSpecificationValue(DOMUtils.getElementText(result, "Status"));
			String calId = DOMUtils.getElementText(result, "CalId");
			String reqId = DOMUtils.getElementText(result, "ReqId");
			
			meetingHandlerResponseBuilder.add(ItemChangeMeetingResponse.builder()
					.status(status)
					.calId(calId)
					.reqId(reqId)
					.build());
		}
		
		return meetingHandlerResponseBuilder
				.build();
	}

	@Override
	public Document encodeResponse(MeetingHandlerResponse meetingResponse) {
		Document reply = DOMUtils.createDoc(null, "MeetingResponse");
		Element root = reply.getDocumentElement();
		for (ItemChangeMeetingResponse item: meetingResponse.getItemChanges()) {
			Element response = DOMUtils.createElement(root, "Result");
			DOMUtils.createElementAndText(response, "Status", item.getStatus().asSpecificationValue());
			if (item.getCalId() != null) {
				DOMUtils.createElementAndText(response, "CalId", item.getCalId());
			}
			DOMUtils.createElementAndText(response, "ReqId", item.getReqId());
		}
		return reply;
	}

	@Override
	public Document encodeRequest(MeetingHandlerRequest meetingRequest) {
		Document reply = DOMUtils.createDoc(null, "MeetingResponse");
		Element root = reply.getDocumentElement();
		
		for (MeetingResponse meetingResponse : meetingRequest.getMeetingResponses()) {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(meetingResponse.getReqId()));
			Element request = DOMUtils.createElement(root, "Request");
			
			DOMUtils.createElementAndText(request, "UserResponse", serializeAttendeeStatus(meetingResponse.getUserResponse()));
			if (meetingResponse.getCollectionId() != null) {
				DOMUtils.createElementAndText(request, "CollectionId", meetingResponse.getCollectionId());
			}
			DOMUtils.createElementAndText(request, "ReqId", meetingResponse.getReqId());
			if (meetingResponse.getLongId() != null) {
				DOMUtils.createElementAndText(request, "LongId", meetingResponse.getLongId());
			}
		}
		return reply;
	}
	
	private String serializeAttendeeStatus(AttendeeStatus userResponse) {
		if (AttendeeStatus.ACCEPT.equals(userResponse)) {
			return "1";
		}
		if (AttendeeStatus.TENTATIVE.equals(userResponse)) {
			return "2";
		}
		if (AttendeeStatus.DECLINE.equals(userResponse)) {
			return "3";
		}
		return "2";
	}
	
	public Document encodeErrorResponse(MeetingResponseStatus invalidMeetingRrequest) {
		Document ret = DOMUtils.createDoc(null, "MeetingResponse");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", invalidMeetingRrequest.asSpecificationValue());
		return ret;
	}
}
