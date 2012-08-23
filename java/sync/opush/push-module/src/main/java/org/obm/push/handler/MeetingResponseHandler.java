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
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MeetingResponse;
import org.obm.push.bean.MeetingResponseStatus;
import org.obm.push.bean.PIMDataType;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.mail.MailBackend;
import org.obm.push.protocol.MeetingProtocol;
import org.obm.push.protocol.bean.MeetingHandlerRequest;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.bean.MeetingHandlerResponse.ItemChangeMeetingResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the MeetingResponse cmd
 */
@Singleton
public class MeetingResponseHandler extends WbxmlRequestHandler {

	private final MeetingProtocol meetingProtocol;
	private final MailBackend mailBackend;
	private final CalendarBackend calendarBackend;
	
	@Inject
	protected MeetingResponseHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter,	StateMachine stMachine, 
			MeetingProtocol meetingProtocol, CollectionDao collectionDao,
			MailBackend mailBackend, WBXMLTools wbxmlTools,
			CalendarBackend calendarBackend, DOMDumper domDumper) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.meetingProtocol = meetingProtocol;
		this.mailBackend = mailBackend;
		this.calendarBackend = calendarBackend;
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
	protected void process(IContinuation continuation, UserDataRequest udr,
			Document doc, ActiveSyncRequest request, Responder responder) {
		
		MeetingHandlerRequest meetingRequest;
		try {
			
			meetingRequest = meetingProtocol.decodeRequest(doc);
			MeetingHandlerResponse meetingResponse = doTheJob(meetingRequest, udr);
			Document document = meetingProtocol.encodeResponse(meetingResponse);
			sendResponse(responder, document);
			
		} catch (NoDocumentException e) {
			sendErrorResponse(responder, MeetingResponseStatus.INVALID_MEETING_RREQUEST);
		} catch (CollectionNotFoundException e) {
			sendErrorResponse(responder, MeetingResponseStatus.INVALID_MEETING_RREQUEST);
		} catch (ProcessingEmailException e) {
			logger.error(e.getMessage(), e);
			sendErrorResponse(responder, MeetingResponseStatus.SERVER_ERROR);
		} catch (ConversionException e) {
			logger.error(e.getMessage(), e);
			sendErrorResponse(responder, MeetingResponseStatus.SERVER_ERROR);
		}
	}
	
	private void sendErrorResponse(Responder responder, MeetingResponseStatus status) {
		sendResponse(responder, meetingProtocol.encodeErrorResponse(status));
	}
	
	private void sendResponse(Responder responder, Document document) {
		responder.sendWBXMLResponse("MeetingResponse", document);
	}

	private MeetingHandlerResponse doTheJob(MeetingHandlerRequest meetingRequest, UserDataRequest udr) 
			throws CollectionNotFoundException, ProcessingEmailException, ConversionException {
		
		List<ItemChangeMeetingResponse> meetingResponses =  new ArrayList<ItemChangeMeetingResponse>();
		for (MeetingResponse item : meetingRequest.getMeetingResponses()) {
			ItemChangeMeetingResponse meetingResponse = handleSingleResponse(udr, item);
			meetingResponses.add(meetingResponse);
		}
		return new MeetingHandlerResponse(meetingResponses);
	}

	private ItemChangeMeetingResponse handleSingleResponse(UserDataRequest udr, MeetingResponse item) 
			throws CollectionNotFoundException, ProcessingEmailException, ConversionException {
		
		MSEmail email = retrieveMailWithMeetingRequest(udr, item);
	
		ItemChangeMeetingResponse meetingResponse = new ItemChangeMeetingResponse();
		
		if (email != null) {
			handle(meetingResponse, udr, email, item.getUserResponse());
			deleteInvitationEmail(udr, item);
		} else {
			meetingResponse.setStatus(MeetingResponseStatus.INVALID_MEETING_RREQUEST);
		}
		
		meetingResponse.setReqId(item.getReqId());
		return meetingResponse;
	}

	private void handle(ItemChangeMeetingResponse meetingResponse, UserDataRequest udr, MSEmail email,
			AttendeeStatus userResponse) throws ConversionException {
		
		meetingResponse.setStatus(MeetingResponseStatus.SUCCESS);
		try {
			String calId = calendarBackend.handleMeetingResponse(udr, email,	userResponse);
			if (!AttendeeStatus.DECLINE.equals(userResponse)) {
				meetingResponse.setCalId(calId);
			}
		} catch (ItemNotFoundException e) {
			logger.error(e.getMessage(), e);
			meetingResponse.setStatus(MeetingResponseStatus.SERVER_ERROR);
		} catch (UnexpectedObmSyncServerException e) {
			logger.error(e.getMessage(), e);
			meetingResponse.setStatus(MeetingResponseStatus.SERVER_ERROR);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
			meetingResponse.setStatus(MeetingResponseStatus.SERVER_ERROR);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			meetingResponse.setStatus(MeetingResponseStatus.INVALID_MEETING_RREQUEST);
		}		
	}

	private void deleteInvitationEmail(UserDataRequest udr, MeetingResponse item) {

		try {
			contentsImporter.importMessageDeletion(udr, PIMDataType.EMAIL, item.getCollectionId(), item.getReqId(), false);
		} catch (ItemNotFoundException e) {
			logger.warn(e.getMessage(), e);
		} catch (UnexpectedObmSyncServerException e) {
			logger.warn(e.getMessage(), e);
		} catch (ProcessingEmailException e) {
			logger.warn(e.getMessage(), e);
		} catch (DaoException e) {
			logger.warn(e.getMessage(), e);
		} catch (CollectionNotFoundException e) {
			logger.warn(e.getMessage(), e);
		} catch (UnsupportedBackendFunctionException e) {
			logger.warn(e.getMessage(), e);
		}
	}
	
	private MSEmail retrieveMailWithMeetingRequest(UserDataRequest udr, MeetingResponse item)
		throws CollectionNotFoundException, ProcessingEmailException {

		MSEmail email = mailBackend.getEmail(udr, item.getCollectionId(), item.getReqId());
		return email;
	}
	
}
