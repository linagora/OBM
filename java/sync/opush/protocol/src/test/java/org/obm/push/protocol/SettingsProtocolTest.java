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

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.obm.push.bean.User;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class SettingsProtocolTest {

	@Test
	public void testResponseWithUser() throws TransformerException {
		User user = User.Factory.create().createUser("domain\\userId", "useremail@domain", "user");
		
		String responseAsText = new StringBuilder() 
			.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
			.append("<Settings>")
			.append("<Status>1</Status>")
			.append("<DeviceInformaton><Status>1</Status></DeviceInformaton>")
			
			.append("<UserInformation><Status>1</Status>")
			.append("<Get><EmailAddresses>")
			.append("<SmtpAddress>")
			.append(user.getEmail())
			.append("</SmtpAddress>")
			.append("</EmailAddresses></Get>")
			.append("</UserInformation>")
			
			.append("</Settings>").toString();
		
		Document response = new SettingsProtocol().encodeResponse(user);
		
		assertThat(DOMUtils.serialize(response)).isEqualTo(responseAsText);
	}

	@Test(expected=NullPointerException.class)
	public void testResponseWithNullUser() {
		User user = null;

		new SettingsProtocol().encodeResponse(user);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testResponseWithNullUserEmail() {
		User user = User.Factory.create().createUser("userId", null, "user");

		new SettingsProtocol().encodeResponse(user);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testResponseWithEmptyUserEmail() {
		User user = User.Factory.create().createUser("userId", "", "user");

		new SettingsProtocol().encodeResponse(user);
	}
}
