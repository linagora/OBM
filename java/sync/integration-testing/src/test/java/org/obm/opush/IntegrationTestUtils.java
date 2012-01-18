package org.obm.opush;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.easymock.EasyMock;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.WBXMLOPClient;
import org.obm.sync.push.client.XMLOPClient;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class IntegrationTestUtils {

	public static void expectUserLoginFromOpush(LoginService loginService, Collection<OpushUser> users) throws AuthFault {
		for (OpushUser user : users) {
			expectUserLoginFromOpush(loginService, user);
		}
	}
	
	public static void expectUserLoginFromOpush(LoginService loginService, OpushUser user) throws AuthFault {
		expect(loginService.authenticate(user.user.getLoginAtDomain(), user.password)).andReturn(user.accessToken).anyTimes();
		loginService.logout(user.accessToken);
		expectLastCall().anyTimes();
	}


	public static void expectUserDeviceAccess(DeviceDao deviceDao, Collection<OpushUser> users) throws DaoException {
		for (OpushUser user : users) {
			expectUserDeviceAccess(deviceDao, user);
		}
	}
	
	public static void expectUserDeviceAccess(DeviceDao deviceDao, OpushUser user) throws DaoException {
		expect(deviceDao.getDevice(user.user, 
				user.deviceId, 
				user.userAgent))
				.andReturn(
						new Device(user.hashCode(), user.deviceType, user.deviceId, new Properties()))
						.anyTimes();
	}
	
	public static void expectUserCollectionsNeverChange(CollectionDao collectionDao, Collection<OpushUser> users) throws DaoException, CollectionNotFoundException {
		Date lastSync = new Date();
		SyncState syncState = new SyncState("sync state");
		expect(collectionDao.lastKnownState(anyObject(Device.class), anyInt())).andReturn(syncState).anyTimes();
		ChangedCollections changed = new ChangedCollections(lastSync, ImmutableSet.<SyncCollection>of());
		expect(collectionDao.getContactChangedCollections(anyObject(Date.class))).andReturn(changed).anyTimes();
		expect(collectionDao.getCalendarChangedCollections(anyObject(Date.class))).andReturn(changed).anyTimes();

		int randomCollectionId = anyInt();
		for (OpushUser opushUser: users) {
			String collectionPath = IntegrationTestUtils.buildCalendarCollectionPath(opushUser);  
			expect(collectionDao.getCollectionPath(randomCollectionId)).andReturn(collectionPath).anyTimes();
		}
	}

	public static void replayMocks(Iterable<Object> toReplay) {
		EasyMock.replay(Lists.newArrayList(toReplay).toArray());
	}
	
	public static OPClient buildOpushClient(OpushUser user, int port) {
		return new XMLOPClient(user.user.getLoginAtDomain(), 
				user.password, 
				user.deviceId, 
				user.deviceType, 
				user.userAgent, port
			);
	}
	
	public static WBXMLOPClient buildWBXMLOpushClient(OpushUser user, int port) {
		return new WBXMLOPClient(
				user.user.getLoginAtDomain(), 
				user.password, 
				user.deviceId, 
				user.deviceType, 
				user.userAgent, port, new WBXMLTools());
	}

	public static String buildCalendarCollectionPath(OpushUser opushUser) {
		return buildCollectionPath(opushUser, "calendar");
	}
	
	public static String buildMailCollectionPath(OpushUser opushUser) {
		return buildCollectionPath(opushUser, "mail");
	}
	
	private static String buildCollectionPath(OpushUser opushUser, String dataType) {
		return opushUser.user.getLoginAtDomain() + "\\" + dataType + "\\" + opushUser.user.getLoginAtDomain();
	}
}
