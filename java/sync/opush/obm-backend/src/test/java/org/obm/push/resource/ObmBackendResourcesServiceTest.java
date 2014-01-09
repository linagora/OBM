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
package org.obm.push.resource;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Map;
import java.util.Properties;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.Resource;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;

import com.google.common.collect.Maps;


public class ObmBackendResourcesServiceTest {

	private User user;
	private Credentials credentials;
	private String command;
	private Device device;
	private Resource resource1, resource2;
	private ResourceCloser resourceCloser;
	private ObmBackendResourcesService resourcesServiceImpl;
	
	private IMocksControl mocksControl;
	
	@Before
	public void setup() {
		mocksControl = createControl();
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		credentials = new Credentials(user, "test");
		command = "command";
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), null);

		resourceCloser = mocksControl.createMock(ResourceCloser.class);
		resourcesServiceImpl = new ObmBackendResourcesService(resourceCloser);
	}
	
	@Test
	public void testCloseResourcesWhenNoResourcesIncluded() {
		UserDataRequest userDataRequest = createUserDataRequest();
		resourcesServiceImpl.closeResources(userDataRequest);
	}

	@Test
	public void testCloseResources() {
		Resource resource = createMock(Resource.class);
		UserDataRequest userDataRequest = createUserDataRequest();
		userDataRequest.putResource(ResourceCloseOrder.ACCESS_TOKEN.name(), resource);
		resourceCloser.closeResources(userDataRequest, ObmBackendResource.class);
		expectLastCall().once();
		
		mocksControl.replay();
		
		resourcesServiceImpl.closeResources(userDataRequest);
		
		mocksControl.verify();
	}

	@Test
	public void testCloseMultipleResources() {
		UserDataRequest userDataRequest = createUserDataRequest();
		userDataRequest.putAllResources(createResourcesMap());
		
		resourceCloser.closeResources(userDataRequest, ObmBackendResource.class);
		expectLastCall().once();
		
		mocksControl.replay();
		resourcesServiceImpl.closeResources(userDataRequest);
		
		mocksControl.verify();
	}

	@Test
	public void testCloseMultipleResourcesWithOnlyOneFailure() {
		UserDataRequest userDataRequest = createUserDataRequest();
		
		Resource resource1 = createMock(Resource.class);
		resource1.close();
		expectLastCall().once();
		
		Resource resource2 = createMock(Resource.class);
		resource2.close();
		expectLastCall().andThrow(new RuntimeException("runtime")).once();
		
		resourceCloser.closeResources(userDataRequest, ObmBackendResource.class);
		expectLastCall().once();
		
		mocksControl.replay();
		
		Map<String, Resource> resources = Maps.newTreeMap();
		resources.put(ResourceCloseOrder.ACCESS_TOKEN.name(), resource1);
		resources.put(ResourceCloseOrder.HTTP_CLIENT.name(), resource2);
		userDataRequest.putAllResources(resources);
		
		resourcesServiceImpl.closeResources(userDataRequest);
		
		mocksControl.verify();
	}

	private Map<String, Resource> createResourcesMap() {
		resource1 = mocksControl.createMock(Resource.class);
		resource2 = mocksControl.createMock(Resource.class);
		
		Map<String, Resource> resources = Maps.newHashMap();
		resources.put(ResourceCloseOrder.ACCESS_TOKEN.name(), resource1);
		resources.put(ResourceCloseOrder.HTTP_CLIENT.name(), resource2);
		return resources;
	}
	
	private UserDataRequest createUserDataRequest() {
		return new UserDataRequest(credentials, command, device);
	}
}
