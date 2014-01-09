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
package org.obm.push.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;


public class ClientIdServiceImplTest {

	private UserDataRequest udr;

	@Before
	public void setup() {
		User user = User.Factory.create().createUser("test@test", "test@domain", "displayName");
		Device device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		udr = new UserDataRequest(new Credentials(user, "password"), "noCommand", device);
	}
	
	@Test
	public void testGetHashClientId() {
		String clientId = "123";
		String expectedHashClientId = "429ceb34c4b664d63da6cee8fd40ce0d4b2532aa";
		
		String hashClientId = new ClientIdServiceImpl().hash(udr, clientId);

		assertThat(hashClientId).isEqualTo(expectedHashClientId);
	}

	@Test
	public void testGetHashClientIdOtherValue() {
		String clientId = "456";
		String expectedHashClientId = "574394a967e7c3db37d5b74b649fba31f899c7a2";
		
		String hashClientId = new ClientIdServiceImpl().hash(udr, clientId);

		assertThat(hashClientId).isEqualTo(expectedHashClientId);
	}
}
