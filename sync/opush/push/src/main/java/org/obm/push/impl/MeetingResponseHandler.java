package org.obm.push.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.MSEmail;
import org.obm.push.data.EncoderFactory;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.email.MeetingResponse;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.PIMDataType;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the MeetingResponse cmd
 */
@Singleton
public class MeetingResponseHandler extends WbxmlRequestHandler {

	@Inject
	private MeetingResponseHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter,
			StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
	}

	// <?xml version="1.0" encoding="UTF-8"?>
	// <MeetingResponse>
	// <Request>
	// <UserResponse>1</UserResponse>
	// <CollectionId>62</CollectionId>
	// <ReqId>62:379</ReqId>
	// </Request>
	// </MeetingResponse>

	@Override
	protected void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType() + ")");
		
		NodeList requests = doc.getDocumentElement().getElementsByTagName("Request");

		List<MeetingResponse> items = parseNodeListAsMeetingResponses(requests);

		try {
			Document reply = DOMUtils.createDoc(null, "MeetingResponse");
			Element root = reply.getDocumentElement();
			for (MeetingResponse item : items) {

				ItemChange ic = retrieveMailWithMeetingRequest(bs, item);

				Element response = DOMUtils.createElement(root, "Result");
				if (ic == null || ic.getData() == null) {
					meetingRequestEmailNotFound(response);
				} else {
					MSEmail invitation = ((MSEmail) ic.getData());
					
					if (invitation == null) {
						meetingRequestInvitationNotFound(response);
					} else {
						handleMeetingResponse(bs, item, response, invitation);
					}
				}
				DOMUtils.createElementAndText(response, "ReqId", item.getReqId());

				responder.sendResponse("MeetingResponse", reply);
			}
		} catch (Exception e) {
			logger.info("Error creating Sync response", e);
		}
	}

	private void handleMeetingResponse(BackendSession bs, MeetingResponse item,
			Element response, MSEmail invitation) throws SQLException {
		
		String calId = contentsImporter.importCalendarUserStatus(bs,
				item.getCollectionId(), invitation, item.getUserResponse());
		
		DOMUtils.createElementAndText(response, "Status", "1");

		if (!AttendeeStatus.DECLINE.equals(item.getUserResponse())) {
			DOMUtils.createElementAndText(response, "CalId", calId);
		}
	}

	private void meetingRequestInvitationNotFound(Element response) {
		DOMUtils.createElementAndText(response, "Status", "2");
	}

	private void meetingRequestEmailNotFound(Element response) {
		DOMUtils.createElementAndText(response, "Status", "3");
	}

	private ItemChange retrieveMailWithMeetingRequest(BackendSession bs, MeetingResponse item)
		throws ActiveSyncException {
		
		List<ItemChange> lit = contentsExporter.fetch(bs, PIMDataType.EMAIL, Arrays.asList(item.getReqId()));
		if (lit.size() > 0) {
			return lit.get(0);
		} else {
			return null;
		}
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
