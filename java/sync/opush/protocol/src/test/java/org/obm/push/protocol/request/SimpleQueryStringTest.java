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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;


public class SimpleQueryStringTest {

	@Test
	public void testSimpleQuery() {
		HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
		expect(httpRequest.getHeader("X-Ms-PolicyKey")).andReturn("956301312");
		expect(httpRequest.getHeader("MS-ASProtocolVersion")).andReturn("12.1");
		expect(httpRequest.getParameter("Cmd")).andReturn("Autodiscover");
		expect(httpRequest.getParameter("DeviceId")).andReturn("gkOZ9qq+1NRMXs1KPVLrCQ==");
		expect(httpRequest.getParameter("DeviceType")).andReturn("WP");
		
		replay(httpRequest);
		ActiveSyncRequest asRequest = new SimpleQueryString(httpRequest);
		assertThat(asRequest.getMsPolicyKey()).isEqualTo("956301312");
		assertThat(asRequest.getMSASProtocolVersion()).isEqualTo("12.1");
		assertThat(asRequest.getCommand()).isEqualTo("Autodiscover");
		assertThat(asRequest.getParameter("DeviceId")).isEqualTo("gkOZ9qq+1NRMXs1KPVLrCQ==");
		assertThat(asRequest.getParameter("DeviceType")).isEqualTo("WP");
		verify(httpRequest);
	}
}
