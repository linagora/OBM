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
package org.obm.opush.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.fluent.Async;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.easymock.IMocksControl;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.Configuration;
import org.obm.ConfigurationModule.PolicyConfigurationProvider;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.PendingQueriesLock;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.DefaultOpushModule;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.mail.MailBackend;
import org.obm.push.protocol.PingProtocol;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.HeartbeatDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;

import com.google.inject.Inject;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(DefaultOpushModule.class)
public class PushContinuationTest {
	
	@Inject	SingleUserFixture singleUserFixture;
	@Inject	OpushServer opushServer;
	@Inject	ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject PendingQueriesLock pendingQueries;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	@Inject PingProtocol pingProtocol;
	@Inject SyncDecoder syncDecoder;
	@Inject PolicyConfigurationProvider policyConfigurationProvider;
	
	private static final SyncKey INCOMING_SYNC_KEY = new SyncKey("132");
	private static final SyncKey NEW_SYNC_KEY = new SyncKey("456");
	private static final long HEARTBEAT = 10;
	private static final int WAIT_TO_BE_STARTED_MAX_TIME = 5;
	
	private OpushUser user;
	private int inboxCollectionId;
	private String inboxCollectionIdAsString;
	
	private final static int TIMEOUT_IN_SECONDS = 15;
	private ExecutorService threadpool;
	private Async async;
	private CloseableHttpClient httpClient;

	@Before
	public void setup() {
		threadpool = Executors.newFixedThreadPool(4);
		async = Async.newInstance().use(threadpool);
		httpClient = HttpClientBuilder.create().build();
		
		user = singleUserFixture.jaures;
		inboxCollectionId = 1234;
		inboxCollectionIdAsString = String.valueOf(inboxCollectionId);

		expect(policyConfigurationProvider.get()).andReturn("fakeConfiguration");
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		threadpool.shutdown();
		httpClient.close();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testSyncAfterPing() throws Exception {
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		expectPing();
		expectSyncWithoutChanges();
		
		mocksControl.replay();
		opushServer.start();
		
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort(), httpClient);
		Future<PingResponse> pingFuture = opClient.pingASync(async, pingProtocol, inboxCollectionIdAsString, HEARTBEAT);
		
		// We have to wait for Ping request really arrived in OPush
		assertThat(pendingQueries.waitingClose(WAIT_TO_BE_STARTED_MAX_TIME, TimeUnit.SECONDS)).isTrue();
		SyncResponse syncResponse = opClient.syncEmail(syncDecoder, INCOMING_SYNC_KEY, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);
		
		PingResponse pingResponse = pingFuture.get(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
		
		mocksControl.verify();
		
		assertThat(syncResponse.getStatus()).isEqualTo(SyncStatus.OK);
		assertThat(pingResponse.getPingStatus()).isEqualTo(PingStatus.NO_CHANGES);
	}

	private void expectSyncWithoutChanges() {
		Credentials credentials = new Credentials(user.user, user.password);
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expect(collectionDao.getCollectionPath(inboxCollectionId))
			.andReturn("obm:\\\\" + user.user.getLoginAtDomain() + "\\email\\INBOX").anyTimes();
		ItemSyncState itemSyncState = ItemSyncState.builder()
				.syncKey(INCOMING_SYNC_KEY)
				.syncDate(DateUtils.getCurrentDate())
				.build();
		expect(collectionDao.findItemStateForKey(INCOMING_SYNC_KEY))
			.andReturn(itemSyncState).anyTimes();
		expect(collectionDao.updateState(eq(user.device), eq(inboxCollectionId), anyObject(SyncKey.class), anyObject(Date.class)))
			.andReturn(ItemSyncState.builder()
				.syncKey(NEW_SYNC_KEY)
				.syncDate(DateUtils.getCurrentDate())
				.build()).anyTimes();
		
		MailBackend mailBackend = classToInstanceMap.get(MailBackend.class);
		expect(mailBackend.getChanged(eq(new UserDataRequest(credentials, "Sync", user.device)), 
				eq(itemSyncState), 
				anyObject(AnalysedSyncCollection.class), 
				anyObject(SyncClientCommands.class), 
				anyObject(SyncKey.class)))
			.andReturn(DataDelta.builder()
				.syncDate(DateUtils.getCurrentDate())
				.syncKey(NEW_SYNC_KEY)
				.build()).anyTimes();
	}
	
	private void expectPing() {
		HeartbeatDao heartbeatDao = classToInstanceMap.get(HeartbeatDao.class);
		heartbeatDao.updateLastHeartbeat(user.device, HEARTBEAT);
		expectLastCall();
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expect(collectionDao.lastKnownState(user.device, inboxCollectionId))
			.andReturn(ItemSyncState.builder()
				.syncKey(NEW_SYNC_KEY)
				.syncDate(DateUtils.getCurrentDate())
				.build());
	}
}
