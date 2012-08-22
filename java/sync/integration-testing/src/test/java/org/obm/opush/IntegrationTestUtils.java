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
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollection;
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
		ItemSyncState syncState = new ItemSyncState("sync state");
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

	public static void expectAllocateFolderState(CollectionDao collectionDao, FolderSyncState folderSyncState) throws DaoException {
		expect(collectionDao.allocateNewFolderSyncState(anyObject(Device.class), anyObject(String.class)))
			.andReturn(folderSyncState);
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
		return buildCollectionPath(opushUser, "calendar", opushUser.user.getLoginAtDomain());
	}
	
	public static String buildEmailInboxCollectionPath(OpushUser opushUser) {
		return buildCollectionPath(opushUser, "email", "INBOX");
	}
	
	private static String buildCollectionPath(OpushUser opushUser, String dataType, String relativePath) {
		return "obm:\\\\" + opushUser.user.getLoginAtDomain() + "\\" + dataType + "\\" + relativePath;
	}
}
