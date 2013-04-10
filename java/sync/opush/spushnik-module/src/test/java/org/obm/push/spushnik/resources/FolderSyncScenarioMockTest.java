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
package org.obm.push.spushnik.resources;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.push.spushnik.SpushnikTestUtils.buildServiceUrl;

import java.util.Properties;

import org.easymock.IMocksControl;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.Configuration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.IntegrationTestUtils;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.spushnik.SpushnikScenarioTestUtils;
import org.obm.push.spushnik.bean.CheckResult;
import org.obm.push.spushnik.bean.CheckStatus;
import org.obm.push.spushnik.bean.Credentials;
import org.obm.push.state.SyncKeyFactory;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.DeviceDao.PolicyStatus;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.client.login.LoginService;

import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class FolderSyncScenarioMockTest {
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(ScenarioTestModule.class);

	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	@Inject FolderSyncScenario folderSyncScenario;

	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testScenarii() throws Exception {
		SpushnikScenarioTestUtils.mockWorkingFolderSync(classToInstanceMap, singleUserFixture.jaures);
		mocksControl.replay();
		opushServer.start();

		CheckResult checkResult = folderSyncScenario.run(
				buildServiceUrl(opushServer.getPort()),
				Credentials.builder()
					.loginAtDomain(singleUserFixture.jaures.user.getLoginAtDomain())
					.password(singleUserFixture.jaures.password)
					.build());
		
		assertThat(checkResult.getStatus()).isEqualTo(CheckStatus.OK.asSpecificationValue());
	}

	@Test
	public void testErrorInBackend() throws Exception {
		IntegrationTestUtils.expectUserLoginFromOpush(classToInstanceMap.get(LoginService.class), 
				singleUserFixture.jaures);
		User user = singleUserFixture.jaures.user;
		DeviceId deviceId = new DeviceId("spushnik");
		Device device = new Device(user.hashCode(), 
				"spushnikProbe", 
				deviceId, 
				new Properties(), 
				singleUserFixture.jaures.deviceProtocolVersion);
		// First provisionning
		DeviceDao deviceDao = classToInstanceMap.get(DeviceDao.class);
		expect(deviceDao.getDevice(user, 
				deviceId, 
				"spushnikAgent",
				singleUserFixture.jaures.deviceProtocolVersion))
			.andReturn(device).anyTimes();
		expect(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING))
			.andReturn(null).once();
		Long policyKey = new Long(1);
		expect(deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING))
			.andReturn(policyKey).once();
		
		// Second provisionning
		expect(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING))
			.andReturn(policyKey).once();
		deviceDao.removePolicyKey(user, device);
		expectLastCall().once();
		expect(deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED))
			.andReturn(policyKey).once();
		expect(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED))
			.andReturn(policyKey).once();
		
		// FolderSync
		SyncKey syncKey = new SyncKey("123");
		expect(classToInstanceMap.get(SyncKeyFactory.class).randomSyncKey())
			.andReturn(syncKey).once();
		FolderSyncState syncState = FolderSyncState.builder()
				.syncKey(syncKey)
				.id(1)
				.build();
		expect(classToInstanceMap.get(CollectionDao.class).allocateNewFolderSyncState(device, syncKey))
			.andReturn(syncState).once();
		
		mocksControl.replay();
		opushServer.start();

		CheckResult checkResult = folderSyncScenario.run(
				buildServiceUrl(opushServer.getPort()),
				Credentials.builder()
					.loginAtDomain(user.getLoginAtDomain())
					.password(singleUserFixture.jaures.password)
					.build());
		
		assertThat(checkResult.getStatus()).isEqualTo(CheckStatus.ERROR.asSpecificationValue());
	}

	@Test
	public void testBadOpushPort() throws Exception {
		IntegrationTestUtils.expectUserLoginFromOpush(classToInstanceMap.get(LoginService.class), 
				singleUserFixture.jaures);
		
		mocksControl.replay();
		opushServer.start();

		CheckResult checkResult = folderSyncScenario.run(
				buildServiceUrl(opushServer.getPort() +1),
				Credentials.builder()
					.loginAtDomain(singleUserFixture.jaures.user.getLoginAtDomain())
					.password(singleUserFixture.jaures.password)
					.build());
		
		assertThat(checkResult.getStatus()).isEqualTo(CheckStatus.ERROR.asSpecificationValue());
	}

	@Test
	public void testBadOpushAddress() throws Exception {
		IntegrationTestUtils.expectUserLoginFromOpush(classToInstanceMap.get(LoginService.class), 
				singleUserFixture.jaures);
		
		mocksControl.replay();
		opushServer.start();

		CheckResult checkResult = folderSyncScenario.run(
				buildServiceUrl("123.456.0.1", opushServer.getPort()),
				Credentials.builder()
					.loginAtDomain(singleUserFixture.jaures.user.getLoginAtDomain())
					.password(singleUserFixture.jaures.password)
					.build()); 
		
		assertThat(checkResult.getStatus()).isEqualTo(CheckStatus.ERROR.asSpecificationValue());
	}

	@Test
	public void testBadWebApp() throws Exception {
		IntegrationTestUtils.expectUserLoginFromOpush(classToInstanceMap.get(LoginService.class), 
				singleUserFixture.jaures);
		
		mocksControl.replay();
		opushServer.start();

		CheckResult checkResult = folderSyncScenario.run(
				buildServiceUrl("/VeryBad/", "127.0.0.1", opushServer.getPort()),
				Credentials.builder()
					.loginAtDomain(singleUserFixture.jaures.user.getLoginAtDomain())
					.password(singleUserFixture.jaures.password)
					.build());  
				
		assertThat(checkResult.getStatus()).isEqualTo(CheckStatus.ERROR.asSpecificationValue());
	}
}
