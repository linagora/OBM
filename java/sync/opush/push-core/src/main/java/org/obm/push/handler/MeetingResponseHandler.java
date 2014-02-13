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
package org.obm.push.handler;

import org.obm.push.ICalendarBackend;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.MeetingResponse;
import org.obm.push.bean.MeetingResponseStatus;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.ICalendarConverterException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.TimeoutException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.mail.MailBackend;
import org.obm.push.protocol.MeetingProtocol;
import org.obm.push.protocol.bean.ItemChangeMeetingResponse;
import org.obm.push.protocol.bean.MeetingHandlerRequest;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the MeetingResponse cmd
 */
@Singleton
public class MeetingResponseHandler extends WbxmlRequestHandler {

	private final MeetingProtocol meetingProtocol;
	private final MailBackend mailBackend;
	private final ICalendarBackend calendarBackend;
	
	@Inject
	protected MeetingResponseHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter,	StateMachine stMachine, 
			MeetingProtocol meetingProtocol, CollectionDao collectionDao,
			MailBackend mailBackend, WBXMLTools wbxmlTools,
			ICalendarBackend calendarBackend, DOMDumper domDumper) {
		
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

		MeetingHandlerResponse.Builder meetingHandlerResponseBuilder = MeetingHandlerResponse.builder();
		for (MeetingResponse item : meetingRequest.getMeetingResponses()) {
			ItemChangeMeetingResponse meetingResponse = handleSingleResponse(udr, item);
			meetingHandlerResponseBuilder.add(meetingResponse);
		}
		return meetingHandlerResponseBuilder
			.build();
	}

	private ItemChangeMeetingResponse handleSingleResponse(UserDataRequest udr, MeetingResponse item) 
			throws CollectionNotFoundException, ProcessingEmailException, ConversionException {
	
		ItemChangeMeetingResponse.Builder builder = ItemChangeMeetingResponse.builder()
				.reqId(item.getReqId());
		try {
			UidMSEmail email = retrieveMailWithMeetingRequest(udr, item);
		
			if (email != null) {
				handleEmail(udr, item, builder);
			} else {
				builder.status(MeetingResponseStatus.INVALID_MEETING_RREQUEST);
			}
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
			builder.status(MeetingResponseStatus.SERVER_ERROR);
		}
		
		return builder.build();
	}

	private void handleEmail(UserDataRequest udr, MeetingResponse item, ItemChangeMeetingResponse.Builder builder) {
		try {
			String serverId = handle(udr, item);
			builder.status(MeetingResponseStatus.SUCCESS);
			
			if (!AttendeeStatus.DECLINE.equals(item.getUserResponse())) {
				builder.calId(serverId);
			}
			
			deleteInvitationEmail(udr, item);
			
		} catch (ItemNotFoundException e) {
			logger.error(e.getMessage(), e);
			builder.status(MeetingResponseStatus.SERVER_ERROR);
		} catch (UnexpectedObmSyncServerException e) {
			logger.error(e.getMessage(), e);
			builder.status(MeetingResponseStatus.SERVER_ERROR);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
			builder.status(MeetingResponseStatus.SERVER_ERROR);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			builder.status(MeetingResponseStatus.INVALID_MEETING_RREQUEST);
		} catch (HierarchyChangedException e) {
			logger.error(e.getMessage(), e);
			builder.status(MeetingResponseStatus.SERVER_ERROR);
		} catch (ICalendarConverterException e) {
			logger.error(e.getMessage(), e);
			builder.status(MeetingResponseStatus.SERVER_ERROR);
		}
	}

	private String handle(UserDataRequest udr, MeetingResponse item) 
			throws ConversionException, CollectionNotFoundException, ItemNotFoundException, UnexpectedObmSyncServerException, DaoException, HierarchyChangedException, ICalendarConverterException {
		
		Object invitation = mailBackend.getInvitation(udr, item.getCollectionId(), item.getReqId());
		return calendarBackend.handleMeetingResponse(udr, invitation, item.getUserResponse());
	}

	@VisibleForTesting void deleteInvitationEmail(UserDataRequest udr, MeetingResponse item) {

		try {
			contentsImporter.importMessageDeletion(udr, PIMDataType.EMAIL, item.getCollectionId(), item.getReqId(), true);
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
	
	private UidMSEmail retrieveMailWithMeetingRequest(UserDataRequest udr, MeetingResponse item)
			throws CollectionNotFoundException, ProcessingEmailException {

		return mailBackend.getEmail(udr, item.getCollectionId(), item.getReqId());
	}
	
}
