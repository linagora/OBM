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
package org.obm.push.protocol.data;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionRequest;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.protocol.bean.AnalysedPingRequest;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.HeartbeatDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.utils.DateUtils;


public class PingAnalyserTest {
	
	private Device device;
	private UserDataRequest udr;
	private User user;
	private Credentials credentials;
	private String collectionPath;
	private int collectionId;
	
	private IMocksControl mocks;
	private CollectionDao collectionDao;
	private HeartbeatDao heartbeatDao;
	private MonitoredCollectionDao monitoredCollectionDao;
	private ICollectionPathHelper collectionPathHelper;
	private StateMachine stateMachine;
	
	private PingAnalyser pingAnalyser;
	
	@Before
	public void setup() {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), null);
		user = Factory.create().createUser("adrien@test.tlse.lngr", "email@test.tlse.lngr", "Adrien");
		credentials = new Credentials(user, "test");
		udr = new UserDataRequest(credentials, "Sync", device);
		collectionPath = "INBOX";
		collectionId = 5;


		mocks = createControl();
		collectionDao = mocks.createMock(CollectionDao.class);
		heartbeatDao = mocks.createMock(HeartbeatDao.class);
		monitoredCollectionDao = mocks.createMock(MonitoredCollectionDao.class);
		collectionPathHelper = mocks.createMock(ICollectionPathHelper.class);
		stateMachine = mocks.createMock(StateMachine.class);
		
		pingAnalyser = new PingAnalyser(collectionDao, heartbeatDao, monitoredCollectionDao, collectionPathHelper, stateMachine);
	}
	
	@Test
	public void testAnalysePingWithoutHeartbeat() throws Exception {
		SyncKey syncKey = new SyncKey("123");
		PingRequest pingRequest = PingRequest.builder()
				.add(SyncCollectionRequest.builder()
						.collectionId(collectionId)
						.syncKey(syncKey)
						.build())
				.build();
		
		long heartbeat = 100;
		expect(heartbeatDao.findLastHeartbeat(device))
			.andReturn(heartbeat).once();

		expect(collectionDao.getCollectionPath(collectionId))
			.andReturn(collectionPath).once();
		
		expect(collectionPathHelper.recognizePIMDataType(collectionPath))
			.andReturn(PIMDataType.EMAIL).once();

		expect(stateMachine.lastKnownState(device, collectionId))
			.andReturn(null).once();
		
		mocks.replay();
		
		AnalysedPingRequest analysedPingRequest = pingAnalyser.analysePing(udr, pingRequest);
		
		mocks.verify();
		assertThat(analysedPingRequest).isEqualTo(AnalysedPingRequest.builder()
				.heartbeatInterval(heartbeat)
				.add(AnalysedSyncCollection.builder()
						.collectionId(collectionId)
						.collectionPath(collectionPath)
						.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
						.dataType(PIMDataType.EMAIL)
						.build())
				.build());
	}
	
	@Test (expected=MissingRequestParameterException.class)
	public void testAnalysePingWithoutHeartbeatAndNoneStored() throws Exception {
		SyncKey syncKey = new SyncKey("123");
		PingRequest pingRequest = PingRequest.builder()
				.add(SyncCollectionRequest.builder()
						.collectionId(collectionId)
						.syncKey(syncKey)
						.build())
				.build();
		
		expect(heartbeatDao.findLastHeartbeat(device))
			.andReturn(null).once();
		
		mocks.replay();
		
		try {
			pingAnalyser.analysePing(udr, pingRequest);
		} catch (MissingRequestParameterException e) {
			mocks.verify();
			throw e;
		}
	}
	
	@Test
	public void testAnalysePingUseMinHeartbeat() throws Exception {
		SyncKey syncKey = new SyncKey("123");
		long heartbeat = 1;
		PingRequest pingRequest = PingRequest.builder()
				.add(SyncCollectionRequest.builder()
						.collectionId(collectionId)
						.syncKey(syncKey)
						.build())
				.heartbeatInterval(heartbeat)
				.build();
		
		heartbeatDao.updateLastHeartbeat(device, PingAnalyser.MIN_SANE_HEARTBEAT_VALUE);
		expectLastCall().once();
		
		expect(collectionDao.getCollectionPath(collectionId))
			.andReturn(collectionPath).once();
		
		expect(collectionPathHelper.recognizePIMDataType(collectionPath))
			.andReturn(PIMDataType.EMAIL).once();

		expect(stateMachine.lastKnownState(device, collectionId))
			.andReturn(null).once();
		
		mocks.replay();
		
		AnalysedPingRequest analysedPingRequest = pingAnalyser.analysePing(udr, pingRequest);
		
		mocks.verify();
		assertThat(analysedPingRequest).isEqualTo(AnalysedPingRequest.builder()
				.heartbeatInterval(PingAnalyser.MIN_SANE_HEARTBEAT_VALUE)
				.add(AnalysedSyncCollection.builder()
						.collectionId(collectionId)
						.collectionPath(collectionPath)
						.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
						.dataType(PIMDataType.EMAIL)
						.build())
				.build());
	}
	
	@Test
	public void testAnalysePingWithKnownState() throws Exception {
		SyncKey syncKey = new SyncKey("123");
		PingRequest pingRequest = PingRequest.builder()
				.add(SyncCollectionRequest.builder()
						.collectionId(collectionId)
						.syncKey(syncKey)
						.build())
				.build();
		
		long heartbeat = 100;
		expect(heartbeatDao.findLastHeartbeat(device))
			.andReturn(heartbeat).once();

		expect(collectionDao.getCollectionPath(collectionId))
			.andReturn(collectionPath).once();
		
		expect(collectionPathHelper.recognizePIMDataType(collectionPath))
			.andReturn(PIMDataType.EMAIL).once();

		SyncKey knownSyncKey = new SyncKey("456");
		ItemSyncState itemSyncState = ItemSyncState.builder()
				.syncKey(knownSyncKey)
				.syncDate(DateUtils.getCurrentDate())
				.build();
		expect(stateMachine.lastKnownState(device, collectionId))
			.andReturn(itemSyncState).once();
		
		mocks.replay();
		
		AnalysedPingRequest analysedPingRequest = pingAnalyser.analysePing(udr, pingRequest);
		
		mocks.verify();
		assertThat(analysedPingRequest).isEqualTo(AnalysedPingRequest.builder()
				.heartbeatInterval(heartbeat)
				.add(AnalysedSyncCollection.builder()
						.collectionId(collectionId)
						.collectionPath(collectionPath)
						.syncKey(knownSyncKey)
						.dataType(PIMDataType.EMAIL)
						.build())
				.build());
	}
	
	@Test
	public void testAnalysePingTwoSyncCollections() throws Exception {
		SyncKey syncKey = new SyncKey("123");
		Integer collectionId2 = 785;
		PingRequest pingRequest = PingRequest.builder()
				.add(SyncCollectionRequest.builder()
						.collectionId(collectionId)
						.syncKey(syncKey)
						.build())
				.add(SyncCollectionRequest.builder()
						.collectionId(collectionId2)
						.syncKey(syncKey)
						.build())
				.build();
		
		long heartbeat = 100;
		expect(heartbeatDao.findLastHeartbeat(device))
			.andReturn(heartbeat).once();

		expect(collectionDao.getCollectionPath(collectionId))
			.andReturn(collectionPath).once();
		String collectionPath2 = "CONTACT";
		expect(collectionDao.getCollectionPath(collectionId2))
			.andReturn(collectionPath2 ).once();
		
		expect(collectionPathHelper.recognizePIMDataType(collectionPath))
			.andReturn(PIMDataType.EMAIL).once();
		expect(collectionPathHelper.recognizePIMDataType(collectionPath2))
			.andReturn(PIMDataType.CONTACTS).once();

		SyncKey knownSyncKey = new SyncKey("456");
		ItemSyncState itemSyncState = ItemSyncState.builder()
				.syncKey(knownSyncKey)
				.syncDate(DateUtils.getCurrentDate())
				.build();
		expect(stateMachine.lastKnownState(device, collectionId))
			.andReturn(itemSyncState).once();
		expect(stateMachine.lastKnownState(device, collectionId2))
			.andReturn(null).once();
		
		mocks.replay();
		
		AnalysedPingRequest analysedPingRequest = pingAnalyser.analysePing(udr, pingRequest);
		
		mocks.verify();
		assertThat(analysedPingRequest).isEqualTo(AnalysedPingRequest.builder()
				.heartbeatInterval(heartbeat)
				.add(AnalysedSyncCollection.builder()
						.collectionId(collectionId)
						.collectionPath(collectionPath)
						.syncKey(knownSyncKey)
						.dataType(PIMDataType.EMAIL)
						.build())
				.add(AnalysedSyncCollection.builder()
						.collectionId(collectionId2)
						.collectionPath(collectionPath2)
						.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
						.dataType(PIMDataType.CONTACTS)
						.build())
				.build());
	}
}
