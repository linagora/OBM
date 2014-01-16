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
package org.obm.opush;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import org.apache.http.client.HttpClient;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.ProtocolVersion;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.imap.LinagoraMailboxService;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.WBXMLOPClient;
import org.obm.sync.push.client.XMLOPClient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.icegreen.greenmail.user.GreenMailUser;

public class IntegrationTestUtils {

	public static void expectSyncState(StateMachine stateMachine, SyncKey syncKey, ItemSyncState syncState) throws DaoException {
		expect(stateMachine.getItemSyncState(syncKey)).andReturn(syncState).anyTimes();
	}
	
	public static void expectUserLoginFromOpush(LoginClient loginClient, Collection<OpushUser> users) throws AuthFault {
		for (OpushUser user : users) {
			expectUserLoginFromOpush(loginClient, user);
		}
	}
	
	public static void expectUserLoginFromOpush(LoginClient loginClient, OpushUser user) throws AuthFault {
		expect(loginClient.authenticate(user.user.getLoginAtDomain(), user.password)).andReturn(user.accessToken).anyTimes();
		loginClient.logout(user.accessToken);
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
				user.userAgent,
				user.deviceProtocolVersion))
				.andReturn(
						new Device(user.hashCode(), user.deviceType, user.deviceId, new Properties(), user.deviceProtocolVersion))
						.anyTimes();
	}
	
	public static void expectUserCollectionsNeverChange(CollectionDao collectionDao,
			Collection<OpushUser> users, Collection<Integer> unchangedCollectionsIds)
			throws DaoException, CollectionNotFoundException {
		
		Date lastSync = new Date();
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(lastSync)
				.syncKey(new SyncKey("sync state"))
				.build();
		expect(collectionDao.lastKnownState(anyObject(Device.class), anyInt())).andReturn(syncState).anyTimes();
		ChangedCollections changed = new ChangedCollections(lastSync, ImmutableSet.<String>of());
		expect(collectionDao.getContactChangedCollections(anyObject(Date.class))).andReturn(changed).anyTimes();
		expect(collectionDao.getCalendarChangedCollections(anyObject(Date.class))).andReturn(changed).anyTimes();

		int otherCollectionId = anyInt();
		for (OpushUser opushUser: users) {
			for (Integer unchangedCollectionId : unchangedCollectionsIds) {
				String collectionPath = IntegrationTestUtils.buildEmailInboxCollectionPath(opushUser);  
				expect(collectionDao.getCollectionPath(unchangedCollectionId)).andReturn(collectionPath).anyTimes();
			}
			expect(collectionDao.getCollectionPath(otherCollectionId)).andThrow(new CollectionNotFoundException()).anyTimes();
		}
	}

	public static void expectAllocateFolderState(CollectionDao collectionDao, FolderSyncState folderSyncState) throws DaoException {
		expect(collectionDao.allocateNewFolderSyncState(anyObject(Device.class), anyObject(SyncKey.class)))
			.andReturn(folderSyncState);
	}
	
	public static void expectGetCollectionPath(CollectionDao collectionDao, Integer collectionId, String serverId) throws CollectionNotFoundException, DaoException {
		expect(collectionDao.getCollectionPath(collectionId))
			.andReturn(serverId);
	}

	public static void expectCreateFolderMappingState(FolderSyncStateBackendMappingDao folderSyncStateBackendMappingDao) throws DaoException {
		folderSyncStateBackendMappingDao.createMapping(anyObject(PIMDataType.class), anyObject(FolderSyncState.class));
		expectLastCall().anyTimes();
	}

	public static OPClient buildOpushClient(OpushUser user, int port, HttpClient httpClient) {
		return new XMLOPClient(httpClient, 
				user.user.getLoginAtDomain(), 
				user.password, 
				user.deviceId, 
				user.deviceType, 
				user.userAgent, port
			);
	}
	
	public static WBXMLOPClient buildWBXMLOpushClient(OpushUser user, int port, ProtocolVersion protocolVersion, HttpClient httpClient) {
		return new WBXMLOPClient(
				httpClient,
				user.user.getLoginAtDomain(), 
				user.password, 
				user.deviceId, 
				user.deviceType, 
				user.userAgent, "localhost", port, "/ActiveSyncServlet/",
				new WBXMLTools(),
				protocolVersion);
	}
	
	public static WBXMLOPClient buildWBXMLOpushClient(OpushUser user, int port, HttpClient httpClient) {
		return buildWBXMLOpushClient(user, port, ProtocolVersion.V121, httpClient);
	}

	public static String buildCalendarCollectionPath(OpushUser opushUser) {
		return buildCollectionPath(opushUser, "calendar", opushUser.user.getLoginAtDomain());
	}

	public static String buildContactCollectionPath(OpushUser opushUser, String contactCollectionId) {
		return buildCollectionPath(opushUser, "contacts", contactCollectionId);
	}
	
	public static String buildEmailInboxCollectionPath(OpushUser opushUser) {
		return buildCollectionPath(opushUser, "email", "INBOX");
	}
	
	public static String buildEmailSentCollectionPath(OpushUser opushUser) {
		return buildCollectionPath(opushUser, "email", "Sent");
	}
	
	public static String buildEmailTrashCollectionPath(OpushUser opushUser) {
		return buildCollectionPath(opushUser, "email", "Trash");
	}
	
	private static String buildCollectionPath(OpushUser opushUser, String dataType, String relativePath) {
		return "obm:\\\\" + opushUser.user.getLoginAtDomain() + "\\" + dataType + "\\" + relativePath;
	}

	public static void appendToINBOX(GreenMailUser greenMailUser, String emailPath) throws Exception {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		javax.mail.internet.MimeMessage mimeMessage = new javax.mail.internet.MimeMessage(session, streamEmail(emailPath));
		greenMailUser.deliver(mimeMessage);
	}

	public static byte[] loadEmail(String emailPath) throws IOException {
		return ByteStreams.toByteArray(streamEmail(emailPath));
	}

	public static InputStream streamEmail(String emailPath) {
		return ClassLoader.getSystemResourceAsStream(emailPath);
	}

	public static void expectFetchFlags(LinagoraMailboxService mailboxService, UserDataRequest udr, String collectionName, long uid, FlagsList value) {
		expect(mailboxService.fetchFlags(udr, collectionName, MessageSet.singleton(uid))).andReturn(ImmutableMap.of(uid, value));
	}

	public static void expectFetchEnvelope(LinagoraMailboxService mailboxService, UserDataRequest udr, String collectionName, int uid, UIDEnvelope envelope) {
		expect(mailboxService.fetchEnvelope(udr, collectionName, MessageSet.singleton(uid)))
			.andReturn(ImmutableList.of(envelope));
	}

	public static void expectFetchBodyStructure(LinagoraMailboxService mailboxService, UserDataRequest udr, String collectionName, int uid, MimeMessage mimeMessage) {
		expect(mailboxService.fetchBodyStructure(udr, collectionName, MessageSet.singleton(uid)))
			.andReturn(ImmutableList.of(mimeMessage));
	}

	public static void expectFetchMailStream(LinagoraMailboxService mailboxService, UserDataRequest udr, String collectionName, int uid, InputStream mailStream) {
		expect(mailboxService.fetchMailStream(udr, collectionName, uid))
				.andReturn(mailStream);
	}

	public static void expectFetchMimePartStream(LinagoraMailboxService mailboxService, UserDataRequest udr, String collectionName, int uid, InputStream mailStream, MimeAddress partAddress) {
		expect(mailboxService.fetchMimePartStream(udr, collectionName, uid, partAddress))
			.andReturn(mailStream);
	}
	
	public static void expectContentExporterFetching(IContentsExporter iContentsExporter, UserDataRequest userDataRequest, List<ItemChange> itemChanges) throws Exception {
		expect(iContentsExporter.fetch(eq(userDataRequest), anyObject(ItemSyncState.class), anyObject(AnalysedSyncCollection.class)))
			.andReturn(itemChanges);
	}
}
