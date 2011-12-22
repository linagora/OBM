package org.obm.opush;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import java.util.Collection;
import java.util.Date;

import org.easymock.EasyMock;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.push.client.OPClient;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class IntegrationTestUtils {

	public static void expectUsersHaveNoChange(CollectionDao collectionDao, Collection<OpushUser> users) throws DaoException, CollectionNotFoundException {
		Date lastSync = new Date();
		ChangedCollections changed = new ChangedCollections(lastSync, ImmutableSet.<SyncCollection>of());
		expect(collectionDao.getContactChangedCollections(anyObject(Date.class))).andReturn(changed).anyTimes();
		expect(collectionDao.getCalendarChangedCollections(anyObject(Date.class))).andReturn(changed).anyTimes();

		int randomCollectionId = anyInt();
		for (OpushUser opushUser: users) {
			String collectionPath = buildCalendarCollectionPath(opushUser);  
			expect(collectionDao.getCollectionPath(randomCollectionId)).andReturn(collectionPath).anyTimes();
		}
	}

	public static void replayMocks(Iterable<Object> toReplay) {
		EasyMock.replay(Lists.newArrayList(toReplay).toArray());
	}
	
	public static OPClient buildOpushClient(OpushUser user, int port) {
		String url = buildServiceUrl(port);
		return new OPClient(
				user.user.getLoginAtDomain(), 
				user.password, 
				user.deviceId, 
				user.deviceType, 
				user.userAgent, url, new WBXMLTools());
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
	
	private static String buildServiceUrl(int port) {
		return "http://localhost:" + port + "/";
	}
}
