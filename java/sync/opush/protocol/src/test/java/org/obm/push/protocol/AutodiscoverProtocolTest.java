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

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.autodiscover.AutodiscoverRequest;
import org.obm.push.bean.autodiscover.AutodiscoverResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;


public class AutodiscoverProtocolTest {
	
	private AutodiscoverProtocol autodiscoverProtocol;
	
	@Before
	public void init() {
		autodiscoverProtocol = new AutodiscoverProtocol();
	}

	@Test
	public void testLoopWithinResponseProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Autodiscover xmlns:Autodiscover=\"http://schemas.microsoft.com/exchange/autodiscover/responseschema/2006\">" +
				"<Response xmlns=\"http://schemas.microsoft.com/exchange/autodiscover/mobilesync/responseschema/2006\">" +
				"<Culture>culture</Culture>" +
				"<User>" +
				"<DisplayName>displayName</DisplayName>" +
				"<EMailAddress>user@test.org</EMailAddress>" +
				"</User>" +
				"<Action>" +
				"<Redirect>actionRedirect</Redirect>" +
				"<Settings>" +
				"<Server>" +
				"<Type>type</Type>" +
				"<Url>url</Url>" +
				"<Name>name</Name>" +
				"<ServerData>serverData</ServerData>" +
				"</Server>" +
				"<Server>" +
				"<Type>type2</Type>" +
				"<Url>url2</Url>" +
				"<Name>name2</Name>" +
				"<ServerData>serverData2</ServerData>" +
				"</Server>" +
				"</Settings>" +
				"<Error>" +
				"<Status>1</Status>" +
				"<Message>messageAction</Message>" +
				"<DebugData>debugAction</DebugData>" +
				"</Error>" +
				"</Action>" +
				"<Error>" +
				"<ErrorCode>2</ErrorCode>" +
				"<Message>messageResponse</Message>" +
				"<DebugData>debugReponse</DebugData>" +
				"</Error>" +
				"</Response>" +
				"</Autodiscover>";
		
		AutodiscoverResponse autodiscoverResponse = autodiscoverProtocol.decodeResponse(DOMUtils.parse(initialDocument));
		Document encodeResponse = autodiscoverProtocol.encodeResponse(autodiscoverResponse);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
	
	@Test
	public void testLoopWithinRequestProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Autodiscover>" +
				"<EMailAddress>user@test.org</EMailAddress>" +
				"<AcceptableResponseSchema>schema</AcceptableResponseSchema>" +
				"</Autodiscover>";
		
		AutodiscoverRequest autodiscoverRequest = autodiscoverProtocol.decodeRequest(DOMUtils.parse(initialDocument));
		Document encodeResponse = autodiscoverProtocol.encodeRequest(autodiscoverRequest);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
}
