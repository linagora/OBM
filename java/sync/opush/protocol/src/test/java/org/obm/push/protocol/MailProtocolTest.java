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

import java.io.IOException;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.EmailConfigurationImpl;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.protocol.request.ActiveSyncRequest;


public class MailProtocolTest {
	
	@Test
	public void testWithBigMessageMaxSize() throws IOException, QuotaExceededException {
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfigurationImpl.class);
		ActiveSyncRequest request = EasyMock.createMock(ActiveSyncRequest.class);
		
		EasyMock.expect(request.getParameter("CollectionId")).andReturn("1").once();
		EasyMock.expect(request.getParameter("ItemId")).andReturn("1").once();
		EasyMock.expect(request.getInputStream()).andReturn(loadDataFile("bigEml.eml")).once();
		EasyMock.expect(request.getParameter("SaveInSent")).andReturn("T").once();
		
		EasyMock.expect(emailConfiguration.getMessageMaxSize()).andReturn(10485760).once();
		EasyMock.replay(request, emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		mailProtocol.getRequest(request);
		EasyMock.verify(request, emailConfiguration);
		
	}
	
	@Test(expected=QuotaExceededException.class)
	public void testWithSmallMessageMaxSize() throws IOException, QuotaExceededException {
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfigurationImpl.class);
		ActiveSyncRequest request = EasyMock.createMock(ActiveSyncRequest.class);
		
		EasyMock.expect(request.getParameter("CollectionId")).andReturn("1").once();
		EasyMock.expect(request.getParameter("ItemId")).andReturn("1").once();
		EasyMock.expect(request.getInputStream()).andReturn(loadDataFile("bigEml.eml")).once();
		EasyMock.expect(request.getParameter("SaveInSent")).andReturn("T").once();
		
		EasyMock.expect(emailConfiguration.getMessageMaxSize()).andReturn(1024).once();
		EasyMock.replay(request, emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		mailProtocol.getRequest(request);
		EasyMock.verify(request, emailConfiguration);
		
	}

	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"file/" + name);
	}
}
