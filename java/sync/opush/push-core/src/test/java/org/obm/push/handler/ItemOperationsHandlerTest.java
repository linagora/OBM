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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.bean.ItemOperationsResponse;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult.FetchAttachmentResult;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.collect.Lists;


public class ItemOperationsHandlerTest {

	private ItemOperationsHandler itemOperationsHandler;

	@Before
	public void setUp() {
		itemOperationsHandler = new ItemOperationsHandler(null, null, null, null, null, null, null, null, null, null, null);
	}
	
	@Test
	public void testSendResponseChooseMultipartIfMultipartAndFileReference() {
		Document document = DOMUtils.createDoc(null, "root");

		boolean isMultipart = true;
		boolean isGzip = true;
		byte[] expectedData = new byte[]{1, 2, 3, 4};
		FetchAttachmentResult fileReferenceFetch = new FetchAttachmentResult();
		fileReferenceFetch.setAttch(expectedData);
		MailboxFetchResult mailboxFetchResult = new MailboxFetchResult();
		mailboxFetchResult.setFetchAttachmentResult(fileReferenceFetch);
		ItemOperationsResponse response = new ItemOperationsResponse();
		response.setMailboxFetchResult(mailboxFetchResult);
		
		Responder responder = createMock(Responder.class);
		responder.sendMSSyncMultipartResponse("ItemOperations", document, Lists.newArrayList(expectedData), true);
		expectLastCall();
		
		replay(responder);
		itemOperationsHandler.sendResponse(responder, document, response, isGzip, isMultipart);
		verify(responder);
	}
	
	@Test
	public void testSendResponseDoesntChooseMultipartIfMultipartButNoFileReference() {
		Document document = DOMUtils.createDoc(null, "root");

		boolean isMultipart = true;
		boolean isGzip = true;
		MailboxFetchResult mailboxFetchResult = new MailboxFetchResult();
		ItemOperationsResponse response = new ItemOperationsResponse();
		response.setMailboxFetchResult(mailboxFetchResult);
		
		Responder responder = createMock(Responder.class);
		responder.sendWBXMLResponse("ItemOperations", document);
		expectLastCall();
		
		replay(responder);
		itemOperationsHandler.sendResponse(responder, document, response, isGzip, isMultipart);
		verify(responder);
	}
	
	@Test
	public void testSendResponseDoesntChooseMultipartIfNotMultipart() {
		Document document = DOMUtils.createDoc(null, "root");

		boolean isMultipart = false;
		boolean isGzip = true;
		byte[] expectedData = new byte[]{1, 2, 3, 4};
		FetchAttachmentResult fileReferenceFetch = new FetchAttachmentResult();
		fileReferenceFetch.setAttch(expectedData);
		MailboxFetchResult mailboxFetchResult = new MailboxFetchResult();
		mailboxFetchResult.setFetchAttachmentResult(fileReferenceFetch);
		ItemOperationsResponse response = new ItemOperationsResponse();
		response.setMailboxFetchResult(mailboxFetchResult);
		
		Responder responder = createMock(Responder.class);
		responder.sendWBXMLResponse("ItemOperations", document);
		expectLastCall();
		
		replay(responder);
		itemOperationsHandler.sendResponse(responder, document, response, isGzip, isMultipart);
		verify(responder);
	}
	
	@Test
	public void testMSAcceptMultipartWhenExpectedValue() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn("T").anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn(null).anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isTrue();
		verify(request);
	}
	
	@Test
	public void testMSAcceptMultipartWhenTrue() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn("true").anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn(null).anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testMSAcceptMultipartWhenNull() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn(null).anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn(null).anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testMSAcceptMultipartWhenEmpty() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn("").anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn(null).anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isFalse();
		verify(request);
	}

	@Test
	public void testAcceptMultipartWhenExpectedValue() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn(null).anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn("T").anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isTrue();
		verify(request);
	}
	
	@Test
	public void testAcceptMultipartWhenTrue() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn(null).anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn("true").anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testAcceptMultipartWhenNull() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn(null).anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn(null).anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testAcceptMultipartWhenEmpty() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn(null).anyTimes();
		expect(request.getParameter("AcceptMultiPart")).andReturn("").anyTimes();
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptMultipart(request)).isFalse();
		verify(request);
	}

	@Test
	public void testAcceptGzipWhenNull() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).andReturn(null);
		replay(request);
		
		assertThat(ItemOperationsHandler.isAcceptGZip(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testAcceptGzipWhenEmpty() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).andReturn("");
		replay(request);

		assertThat(ItemOperationsHandler.isAcceptGZip(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testAcceptGzipWhenTrue() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).andReturn("true");
		replay(request);

		assertThat(ItemOperationsHandler.isAcceptGZip(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testAcceptGzipWhenFalse() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).andReturn("false");
		replay(request);

		assertThat(ItemOperationsHandler.isAcceptGZip(request)).isFalse();
		verify(request);
	}
	
	@Test
	public void testAcceptGzipWhenExpectedValue() {
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		expect(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).andReturn("gzip");
		replay(request);

		assertThat(ItemOperationsHandler.isAcceptGZip(request)).isTrue();
		verify(request);
	}
}
