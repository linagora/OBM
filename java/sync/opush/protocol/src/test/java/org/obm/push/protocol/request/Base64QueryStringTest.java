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
package org.obm.push.protocol.request;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;


public class Base64QueryStringTest {

	@Test
	public void testBase64Query() {
		HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
		expect(httpRequest.getQueryString()).andReturn("eQkMBBCCQ5n2qr7U1ExezUo9UusJBDkAAAACV1A=");
		
		replay(httpRequest);
		ActiveSyncRequest asRequest = new Base64QueryString(httpRequest, null);
		verify(httpRequest);

		assertThat(asRequest.getMsPolicyKey()).isEqualTo("956301312");
		assertThat(asRequest.getMSASProtocolVersion()).isEqualTo("12.1");
		assertThat(asRequest.getCommand()).isEqualTo("FolderSync");

		assertThat(asRequest.getParameter("Cmd")).isEqualTo("FolderSync");
		assertThat(asRequest.getParameter("DeviceId")).isEqualTo("gkOZ9qq+1NRMXs1KPVLrCQ==");
		assertThat(asRequest.getParameter("DeviceType")).isEqualTo("WP");
		assertThat(asRequest.getParameter("AttachmentName")).isNull();
		assertThat(asRequest.getParameter("CollectionId")).isNull();
		assertThat(asRequest.getParameter("CollectionName")).isNull();
		assertThat(asRequest.getParameter("AttachmentName")).isNull();
		assertThat(asRequest.getParameter("ItemId")).isNull();
		assertThat(asRequest.getParameter("LongId")).isNull();
		assertThat(asRequest.getParameter("ParentId")).isNull();
		assertThat(asRequest.getParameter("Occurrence")).isNull();
		assertThat(asRequest.getParameter("SaveInSent")).isNull();
		assertThat(asRequest.getParameter("AcceptMultiPart")).isNull();
	}
}
