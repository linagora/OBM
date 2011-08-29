package org.obm.push.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.LocatorClient;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
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
	private final CollectionDao collectionDao;
	private final LocatorClient locatorClient;

	protected ObmSyncBackend(ObmConfigurationService configurationService, 
			CollectionDao collectionDao)
			throws ConfigurationException {

		this.locatorClient = new LocatorClient(
				configurationService.getLocatorUrl());
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

	public boolean validatePassword(String loginAtDomain, String password) {
		CalendarLocator cl = new CalendarLocator();
		if (obmSyncHost == null) {
			locateObmSync(loginAtDomain);
		}
		CalendarClient cc = cl.locate("http://" + obmSyncHost + ":8080/obm-sync/services");
		AccessToken token = cc.login(loginAtDomain, password, OBM_SYNC_ORIGIN);
		try {
			if (token == null || token.getSessionId() == null) {
				logger.info(loginAtDomain + " can't log on obm-sync. The username or password isn't valid");
				return false;
			}
		} finally {
			cc.logout(token);
		}
		return true;
	}
	
	protected String getDefaultCalendarName(BackendSession bs) {
		return "obm:\\\\" + bs.getLoginAtDomain() + "\\calendar\\"
				+ bs.getLoginAtDomain();
	}

	public Integer getCollectionIdFor(Device device, String collection)
			throws CollectionNotFoundException, DaoException {
		return collectionDao.getCollectionMapping(device, collection);
	}

	public String getCollectionPathFor(Integer collectionId) throws CollectionNotFoundException, DaoException {
		return collectionDao.getCollectionPath(collectionId);
	}

	protected List<ItemChange> getDeletions(Integer collectionId, Collection<Long> uids) {
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (Long uid: uids) {
			deletions.add( getItemChange(collectionId, uid.toString()) );
		}
		return deletions;
	}
	
	protected ItemChange getItemChange(Integer collectionId, String clientId) {
		return new ItemChange( getServerIdFor(collectionId, clientId) );
	}
	
	protected String collectionIdToString(Integer collectionId) {
		return String.valueOf(collectionId);
	}

	protected String getServerIdFor(Integer collectionId, String clientId) {
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

	protected String createCollectionMapping(Device device, String col) throws DaoException {
		return collectionDao.addCollectionMapping(device, col).toString();
	}
	
}
