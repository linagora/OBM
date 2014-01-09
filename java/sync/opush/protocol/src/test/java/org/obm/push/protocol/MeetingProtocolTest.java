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

import static org.assertj.core.api.Assertions.assertThat;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.MeetingResponse;
import org.obm.push.protocol.bean.MeetingHandlerRequest;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;


public class MeetingProtocolTest {
	
	private MeetingProtocol meetingProtocol;
	
	@Before
	public void init() {
		meetingProtocol = new MeetingProtocol();
	}
	
	@Test
	public void testLoopWithinRequestProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<MeetingResponse>" +
				"<Request>" +
				"<UserResponse>1</UserResponse>" +
				"<CollectionId>11</CollectionId>" +
				"<ReqId>reqId</ReqId>" +
				"<LongId>londId</LongId>" +
				"</Request>" +
				"<Request>" +
				"<UserResponse>3</UserResponse>" +
				"<ReqId>reqId2</ReqId>" +
				"<LongId>londId2</LongId>" +
				"</Request>" +
				"</MeetingResponse>";
		
		MeetingHandlerRequest meetingRequest = meetingProtocol.decodeRequest(DOMUtils.parse(initialDocument));
		Document encodeRequest = meetingProtocol.encodeRequest(meetingRequest);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeRequest));
	}
	
	@Test
	public void testLoopWithinResponseProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<MeetingResponse>" +
				"<Result>" +
				"<Status>1</Status>" +
				"<CalId>11:2</CalId>" +
				"<ReqId>reqId</ReqId>" +
				"</Result>" +
				"<Result>" +
				"<Status>2</Status>" +
				"<ReqId>reqId2</ReqId>" +
				"</Result>" +
				"</MeetingResponse>";
		
		MeetingHandlerResponse meetingResponse = meetingProtocol.decodeResponse(DOMUtils.parse(initialDocument));
		Document encodeResponse = meetingProtocol.encodeResponse(meetingResponse);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
	
	@Test
	public void testEncodeValues() throws Exception {
		MeetingHandlerRequest request = MeetingHandlerRequest.builder().add(MeetingResponse.builder()
					.collectionId(1)
					.longId("2")
					.reqId("3")
					.userResponse(AttendeeStatus.ACCEPT)
					.build()).build();
		Document encodedRequest = meetingProtocol.encodeRequest(request);
		
		Document expectedRequest = DOMUtils.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<MeetingResponse>" +
				"<Request>" +
				"<UserResponse>1</UserResponse>" +
				"<CollectionId>1</CollectionId>" +
				"<ReqId>3</ReqId>" +
				"<LongId>2</LongId>" +
				"</Request>" +
				"</MeetingResponse>");
		
		XMLAssert.assertXMLEqual(encodedRequest, expectedRequest);
	}
	
	@Test
	public void testEncodeLongIdIsNotRequired() throws Exception {
		MeetingHandlerRequest request = MeetingHandlerRequest.builder().add(MeetingResponse.builder()
					.collectionId(1)
					.reqId("3")
					.userResponse(AttendeeStatus.ACCEPT)
					.build()).build();
		Document encodedRequest = meetingProtocol.encodeRequest(request);
		
		Document expectedRequest = DOMUtils.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<MeetingResponse>" +
				"<Request>" +
				"<UserResponse>1</UserResponse>" +
				"<CollectionId>1</CollectionId>" +
				"<ReqId>3</ReqId>" +
				"</Request>" +
				"</MeetingResponse>");
		
		XMLAssert.assertXMLEqual(encodedRequest, expectedRequest);
	}
	
	@Test
	public void testEncodeCollectionIdIsNotRequired() throws Exception {
		MeetingHandlerRequest request = MeetingHandlerRequest.builder().add(MeetingResponse.builder()
					.longId("2")
					.reqId("3")
					.userResponse(AttendeeStatus.ACCEPT)
					.build()).build();
		Document encodedRequest = meetingProtocol.encodeRequest(request);
		
		Document expectedRequest = DOMUtils.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<MeetingResponse>" +
				"<Request>" +
				"<UserResponse>1</UserResponse>" +
				"<ReqId>3</ReqId>" +
				"<LongId>2</LongId>" +
				"</Request>" +
				"</MeetingResponse>");
		
		XMLAssert.assertXMLEqual(encodedRequest, expectedRequest);
	}

	@Test
	public void testEncodeAttendeeStatusIsTentativeByDefault() throws Exception {
		MeetingHandlerRequest request = MeetingHandlerRequest.builder().add(MeetingResponse.builder()
					.collectionId(1)
					.longId("2")
					.reqId("3")
					.build()).build();
		Document encodedRequest = meetingProtocol.encodeRequest(request);
		
		Document expectedRequest = DOMUtils.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<MeetingResponse>" +
				"<Request>" +
				"<UserResponse>2</UserResponse>" +
				"<CollectionId>1</CollectionId>" +
				"<ReqId>3</ReqId>" +
				"<LongId>2</LongId>" +
				"</Request>" +
				"</MeetingResponse>");
		
		XMLAssert.assertXMLEqual(encodedRequest, expectedRequest);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEncodeReqIdIsRequired() {
		MeetingHandlerRequest request = MeetingHandlerRequest.builder().add(MeetingResponse.builder()
					.collectionId(1)
					.longId("2")
					.userResponse(AttendeeStatus.ACCEPT)
					.build()).build();
		meetingProtocol.encodeRequest(request);
	}
}
