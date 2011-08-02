package org.obm.push.impl;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.obm.configuration.ConfigurationService;
import org.obm.locator.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.ISyncClient;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.AddressBookLocator;
import org.obm.sync.locators.CalendarLocator;
import org.obm.sync.locators.TaskLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ObmSyncBackend {

	public static final String OBM_SYNC_ORIGIN = "o-push";
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected String obmSyncHost;
	private final DeviceDao deviceDao;
	private final CollectionDao collectionDao;
	private final LocatorClient locatorClient;

	protected ObmSyncBackend(DeviceDao deviceDao, ConfigurationService configurationService, 
			CollectionDao collectionDao)
			throws ConfigurationException {

		this.locatorClient = new LocatorClient(
				configurationService.getLocatorUrl());
		this.deviceDao = deviceDao;
		this.collectionDao = collectionDao;
	}

	protected void locateObmSync(String loginAtDomain) {
		obmSyncHost = locatorClient.getServiceLocation("sync/obm_sync",
				loginAtDomain);
		logger.info("Using " + obmSyncHost + " as obm_sync host.");
	}

	protected AccessToken login(ISyncClient client, BackendSession session) {
		return client.login(session.getLoginAtDomain(), session.getPassword(), OBM_SYNC_ORIGIN);
	}
	
	protected AbstractEventSyncClient getCalendarClient(BackendSession bs) {
		return getCalendarClient(bs, PIMDataType.CALENDAR);
	}

	protected AbstractEventSyncClient getCalendarClient(BackendSession bs,
			PIMDataType type) {

		if (obmSyncHost == null) {
			locateObmSync(bs.getLoginAtDomain());
		}

		AbstractEventSyncClient cli = null;
		if (PIMDataType.TASKS.equals(type)) {
			TaskLocator tl = new TaskLocator();
			cli = tl.locate("http://" + obmSyncHost + ":8080/obm-sync/services");
		} else {
			CalendarLocator cl = new CalendarLocator();
			cli = cl.locate("http://" + obmSyncHost + ":8080/obm-sync/services");
		}

		return cli;
	}

	protected BookClient getBookClient(BackendSession bs) {
		AddressBookLocator abl = new AddressBookLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs.getLoginAtDomain());
		}
		BookClient bookCli = abl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return bookCli;
	}

	protected ItemChange createItemChangeToRemove(Integer collectionId,
			String del) {
		
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(collectionId, del));
		return ic;
	}

	protected List<ItemChange> getDeletions(Integer collectionId,
			Collection<? extends Object> uids) {
		final List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (final Object uid : uids) {
			deletions
					.add(createItemChangeToRemove(collectionId, uid.toString()));
		}
		return deletions;
	}

	public boolean validatePassword(String loginAtDomain, String password) {
		CalendarLocator cl = new CalendarLocator();
		if (obmSyncHost == null) {
			locateObmSync(loginAtDomain);
		}
		CalendarClient cc = cl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		AccessToken token = cc.login(loginAtDomain, password, OBM_SYNC_ORIGIN);
		try {
			Boolean valid = true;
			if (token == null || token.getSessionId() == null) {
				logger.info(loginAtDomain
						+ " can't log on obm-sync. The username or password isn't valid");
				valid = false;
			}
			return valid;
		} finally {
			cc.logout(token);
		}
	}
	
	protected String getDefaultCalendarName(BackendSession bs) {
		return "obm:\\\\" + bs.getLoginAtDomain() + "\\calendar\\"
				+ bs.getLoginAtDomain();
	}

	public Integer getCollectionIdFor(String loginAtDomain, String deviceId, String collection)
			throws CollectionNotFoundException, SQLException {
		return collectionDao.getCollectionMapping(loginAtDomain, deviceId, collection);
	}

	public String getCollectionPathFor(Integer collectionId)
			throws ActiveSyncException {
		return collectionDao.getCollectionPath(collectionId);
	}

	public String getServerIdFor(Integer collectionId) {
		if (collectionId == null) {
			return null;
		}
		return collectionId.toString();
	}

	public String getServerIdFor(Integer collectionId, String clientId) {
		if (collectionId == null || Strings.isNullOrEmpty(clientId)) {
			return null;
		}
		StringBuilder sb = new StringBuilder(10);
		sb.append(collectionId);
		sb.append(':');
		sb.append(clientId);
		return sb.toString();
	}

	protected Integer getItemIdFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return Integer.parseInt(serverId.substring(idx + 1));
	}

	public int getDevId(String loginAtDomain, String deviceId) throws SQLException {
		return deviceDao.findDevice(loginAtDomain, deviceId);
	}

	public String createCollectionMapping(String loginAtDomain, String devId, String col) throws SQLException {
		return collectionDao.addCollectionMapping(loginAtDomain, devId, col).toString();
	}
}
