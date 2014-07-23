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
package org.obm.push.bean;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.User.Factory;

@RunWith(SlowFilterRunner.class)
public class UserDataRequestTest {

	private User user;
	private Credentials credentials;
	private String command;
	private Device device;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		credentials = new Credentials(user, "test");
		command = "command";
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), null);
	}
	
	@Test
	public void testGetUser() {
		UserDataRequest userDataRequest = createUserDataRequest();
		assertThat(userDataRequest.getUser()).isEqualTo(credentials.getUser());
	}
	
	@Test
	public void testGetPasword() {
		UserDataRequest userDataRequest = createUserDataRequest();
		assertThat(userDataRequest.getPassword()).isEqualTo(credentials.getPassword());
	}
	
	@Test
	public void testGetDevId() {
		UserDataRequest userDataRequest = createUserDataRequest();
		assertThat(userDataRequest.getDevId()).isEqualTo(device.getDevId());
	}
	
	@Test
	public void testGetDevType() {
		UserDataRequest userDataRequest = createUserDataRequest();
		assertThat(userDataRequest.getDevType()).isEqualTo(device.getDevType());
	}
	
	@Test
	public void testGetCommand() {
		UserDataRequest userDataRequest = createUserDataRequest();
		assertThat(userDataRequest.getCommand()).isEqualTo(command);
	}
	
	@Test
	public void testGetCredentials() {
		UserDataRequest userDataRequest = createUserDataRequest();
		assertThat(userDataRequest.getCredentials()).isEqualTo(credentials);
	}
	
	@Test
	public void testGetDevice() {
		UserDataRequest userDataRequest = createUserDataRequest();
		assertThat(userDataRequest.getDevice()).isEqualTo(device);
	}
	
	private UserDataRequest createUserDataRequest() {
		return new UserDataRequest(credentials, command, device, new ResourcesHolder());
	}
}
