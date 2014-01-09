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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.EmailConfigurationImpl;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.protocol.bean.MailRequest;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.protocol.request.SendEmailSyncRequest;

import com.google.common.collect.ImmutableMap;


public class MailProtocolTest {
	
	@Test
	public void testWithBigMessageMaxSize() throws IOException, QuotaExceededException {
		EmailConfiguration emailConfiguration = createMock(EmailConfigurationImpl.class);
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		
		expect(request.getParameter("CollectionId")).andReturn("1").once();
		expect(request.getParameter("ItemId")).andReturn("1").once();
		expect(request.getInputStream()).andReturn(loadDataFile("bigEml.eml")).once();
		expect(request.getParameter("SaveInSent")).andReturn("T").once();
		
		expect(emailConfiguration.getMessageMaxSize()).andReturn(10485760).once();
		replay(request, emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		mailProtocol.getRequest(request);
		verify(request, emailConfiguration);
	}
	
	@Test(expected=QuotaExceededException.class)
	public void testWithSmallMessageMaxSize() throws IOException, QuotaExceededException {
		EmailConfiguration emailConfiguration = createMock(EmailConfigurationImpl.class);
		ActiveSyncRequest request = createMock(ActiveSyncRequest.class);
		
		expect(request.getParameter("CollectionId")).andReturn("1").once();
		expect(request.getParameter("ItemId")).andReturn("1").once();
		expect(request.getInputStream()).andReturn(loadDataFile("bigEml.eml")).once();
		expect(request.getParameter("SaveInSent")).andReturn("T").once();
		
		expect(emailConfiguration.getMessageMaxSize()).andReturn(1024).once();
		replay(request, emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		mailProtocol.getRequest(request);
		verify(request, emailConfiguration);
	}
	
	private InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"file/" + name);
	}

	@Test
	public void testEncodeRequest() throws Exception {
		EmailConfiguration emailConfiguration = createMock(EmailConfigurationImpl.class);
		byte[] mailContent = new byte[] {123, 54, 23, 87, 10, 23, 10, 23 };
		MailRequest sendSimpleEmailRequest = new MailRequest("23", "12", true, mailContent);
		
		ActiveSyncRequest expectedActiveSyncRequest = new SendEmailSyncRequest.Builder()
			.parameters(ImmutableMap.<String, String> of("CollectionId", "23", "ItemId", "12", "SaveInSent", "T"))
			.inputStream(new ByteArrayInputStream(mailContent))
			.build();
		
		expect(emailConfiguration.getMessageMaxSize()).andReturn(1024).once();
		replay(emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		ActiveSyncRequest encodedRequest = mailProtocol.encodeRequest(sendSimpleEmailRequest);
		
		verify(emailConfiguration);
		
		assertThat(encodedRequest).isEqualTo(expectedActiveSyncRequest);
	}

	@Test(expected=QuotaExceededException.class)
	public void testEncodeRequestMaxSizeException() throws Exception {
		EmailConfiguration emailConfiguration = createMock(EmailConfigurationImpl.class);
		MailRequest sendSimpleEmailRequest = new MailRequest("23", "12", true, new byte[] {123, 54, 23, 87, 10, 23, 10, 23 });
		
		expect(emailConfiguration.getMessageMaxSize()).andReturn(2).once();
		replay(emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		mailProtocol.encodeRequest(sendSimpleEmailRequest);
	}

	@Test
	public void testEncodeRequestEmptyMailContent() throws Exception {
		MailRequest sendSimpleEmailRequest = new MailRequest("23", "12", true, null);
		
		MailProtocol mailProtocol = new MailProtocol(null);
		ActiveSyncRequest encodedRequest = mailProtocol.encodeRequest(sendSimpleEmailRequest);
		
		ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) encodedRequest.getInputStream();
		byte[] bytes = new byte[byteArrayInputStream.available()];
		byteArrayInputStream.read(bytes);
		assertThat(bytes).isEmpty();
	}
}
