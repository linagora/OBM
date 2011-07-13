package org.obm.push.backend;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obm.push.impl.ListenerRegistration;
import org.obm.push.mail.IEmailManager;
import org.obm.push.mail.MailBackend;
import org.obm.push.monitor.CalendarMonitoringThread;
import org.obm.push.monitor.ContactsMonitoringThread;
import org.obm.push.monitor.EmailMonitoringThread;
import org.obm.push.provisioning.MSEASProvisioingWBXML;
import org.obm.push.provisioning.MSWAPProvisioningXML;
import org.obm.push.provisioning.Policy;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.SyncCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OBMBackend implements IBackend {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ISyncStorage store;
	private final IEmailManager	emailManager;
	private final IContentsExporter contentsExporter;
	private final MailBackend mailBackend;
	private final CalendarMonitoringThread calendarPushMonitor;
	private final ContactsMonitoringThread contactsPushMonitor;
	
	private final Set<ICollectionChangeListener> registeredListeners;
	private final Map<Integer, EmailMonitoringThread> emailPushMonitors;
	
	@Inject
	private OBMBackend(ISyncStorage store, IEmailManager emailManager,
			IContentsExporter contentsExporter, MailBackend mailBackend,
			CalendarMonitoringThread.Factory calendarMonitoringThreadFactory,
			ContactsMonitoringThread.Factory contactsMonitoringThreadFactory) {
		
		this.store = store;
		this.emailManager = emailManager;
		this.contentsExporter = contentsExporter;
		this.mailBackend = mailBackend;
		
		this.registeredListeners = Collections
				.synchronizedSet(new HashSet<ICollectionChangeListener>());
		
		this.emailPushMonitors = Collections
				.synchronizedMap(new HashMap<Integer, EmailMonitoringThread>());
		
		this.calendarPushMonitor = calendarMonitoringThreadFactory
				.createClient(5000, this.registeredListeners);
		
		this.contactsPushMonitor = contactsMonitoringThreadFactory
				.createClient(5000, this.registeredListeners);
		
		startMonitoringThreads(calendarPushMonitor, contactsPushMonitor);
	}

	private void startMonitoringThreads(
			CalendarMonitoringThread calendarPushMonitor,
			ContactsMonitoringThread contactsPushMonitor) {
		
		Thread calThread = new Thread(calendarPushMonitor);
		calThread.setDaemon(true);
		calThread.start();

		Thread contactThread = new Thread(contactsPushMonitor);
		contactThread.setDaemon(true);
		contactThread.start();
	}

	public void startEmailMonitoring(BackendSession bs, Integer collectionId)
			throws ActiveSyncException {
		EmailMonitoringThread emt = null;
		synchronized (emailPushMonitors) {
			emt = emailPushMonitors.get(collectionId);
		}

		if (emt != null) {
			emt.stopIdle();
		} else {
			emt = new EmailMonitoringThread(mailBackend, registeredListeners,
					bs, collectionId, emailManager, contentsExporter);
		}
		try {
			emt.startIdle();
		} catch (Exception e) {
			logger.error("Error while starting idle on collection["
					+ collectionId + "]", e);
			emt.stopIdle();
		}
		synchronized (emailPushMonitors) {
			emailPushMonitors.put(collectionId, emt);
		}
	}

	@Override
	public String getWasteBasket() {
		return "Trash";
	}

	@Override
	public Policy getDevicePolicy(BackendSession bs) {
		if (bs.getProtocolVersion() <= 2.5) {
			return new MSWAPProvisioningXML();
		} else {
			return new MSEASProvisioingWBXML(bs.getProtocolVersion());
		}
	}

	@Override
	public IListenerRegistration addChangeListener(ICollectionChangeListener ccl) {
		ListenerRegistration ret = new ListenerRegistration(ccl,
				registeredListeners);
		synchronized (registeredListeners) {
			registeredListeners.add(ccl);
		}
		logger.info("[" + ccl.getSession().getLoginAtDomain()
				+ "] change listener registered on backend");
		return ret;
	}

	@Override
	public void resetCollection(BackendSession bs, Integer collectionId) throws SQLException {
		logger.info("reset Collection " + collectionId
				+ " For Full Sync devId: " + bs.getDevId());
		try {
			Set<Integer> colIds = ImmutableSet.of(collectionId);
			emailManager.resetForFullSync(colIds);
			store.resetCollection(bs.getLoginAtDomain(), bs.getDevId(), collectionId);
			bs.clear(collectionId);
		} catch (RuntimeException re) {
			logger.error(re.getMessage(), re);
			throw re;
		}
	}

	@Override
	public boolean validatePassword(String userID, String password) {
		return contentsExporter.validatePassword(userID, password);
	}

	@Override
	public Set<SyncCollection> getChangesSyncCollections(
			CollectionChangeListener collectionChangeListener) throws SQLException {
		
		final Set<SyncCollection> syncCollectionsChanged = new HashSet<SyncCollection>();
		final BackendSession backendSession = collectionChangeListener.getSession();
		
		for (SyncCollection syncCollection: collectionChangeListener.getMonitoredCollections()) {
			
			int count = getCount(backendSession, syncCollection);
			if (count > 0) {
				syncCollectionsChanged.add(syncCollection);
			}
		}
		
		return syncCollectionsChanged;
	}
	
	private int getCount(BackendSession backendSession,
			SyncCollection syncCollection) throws SQLException {
		
		try {
			
			return contentsExporter.getCount(backendSession,
					syncCollection.getSyncState(),
					syncCollection.getFilterType(),
					syncCollection.getCollectionId());
		} catch (ActiveSyncException e) {
			logger.error(e.getMessage(), e);
		}
		
		return 0;
	}
	
}
