package org.obm.push.backend;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obm.push.calendar.CalendarMonitoringThread;
import org.obm.push.calendar.CalendarMonitoringThread.Factory;
import org.obm.push.contacts.ContactsMonitoringThread;
import org.obm.push.impl.ListenerRegistration;
import org.obm.push.mail.EmailMonitoringThread;
import org.obm.push.mail.IEmailManager;
import org.obm.push.mail.MailBackend;
import org.obm.push.provisioning.MSEASProvisioingWBXML;
import org.obm.push.provisioning.MSWAPProvisioningXML;
import org.obm.push.provisioning.Policy;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.ISyncStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OBMBackend implements IBackend {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ISyncStorage store;
	private final IEmailManager	emailManager;
	private final IContentsExporter contentsExporter;
	private final MailBackend mailBackend;
	private final Set<ICollectionChangeListener> registeredListeners;
	private final Map<Integer, EmailMonitoringThread> emailPushMonitors;

	private CalendarMonitoringThread calendarPushMonitor;
	private ContactsMonitoringThread contactsPushMonitor;
	
	@Inject
	private OBMBackend(ISyncStorage store, IEmailManager emailManager,
			IContentsExporter contentsExporter, MailBackend mailBackend,
			CalendarMonitoringThread.Factory calendarMonitoringThreadFactory,
			ContactsMonitoringThread.Factory contactsMonitoringThreadFactory) {
		
		this.store = store;
		this.emailManager = emailManager;
		this.contentsExporter = contentsExporter;
		this.mailBackend = mailBackend;
		
		registeredListeners = Collections
				.synchronizedSet(new HashSet<ICollectionChangeListener>());
		
		emailPushMonitors = Collections
				.synchronizedMap(new HashMap<Integer, EmailMonitoringThread>());
		
		startOBMMonitoringThreads(calendarMonitoringThreadFactory,
				contactsMonitoringThreadFactory);
	}

	private void startOBMMonitoringThreads(
			Factory calendarMonitoringThreadFactory,
			org.obm.push.contacts.ContactsMonitoringThread.Factory contactsMonitoringThreadFactory) {
		
		calendarPushMonitor = calendarMonitoringThreadFactory.createClient(5000, registeredListeners);
		Thread calThread = new Thread(calendarPushMonitor);
		calThread.setDaemon(true);
		calThread.start();

		contactsPushMonitor = contactsMonitoringThreadFactory.createClient(5000, registeredListeners);
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
					bs, collectionId, emailManager);
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
	public void resetCollection(BackendSession bs, Integer collectionId) {
		logger.info("reset Collection " + collectionId
				+ " For Full Sync devId: " + bs.getDevId());
		try {
			Set<Integer> colIds = new HashSet<Integer>();
			colIds.add(collectionId);
			emailManager.resetForFullSync(colIds);
			store.resetCollection(bs.getDevId(), collectionId);
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
	
}
