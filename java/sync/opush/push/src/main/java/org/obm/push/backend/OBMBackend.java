package org.obm.push.backend;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.minig.imap.IMAPException;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SyncCollection;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.ListenerRegistration;
import org.obm.push.mail.IEmailManager;
import org.obm.push.mail.MailBackend;
import org.obm.push.monitor.CalendarMonitoringThread;
import org.obm.push.monitor.ContactsMonitoringThread;
import org.obm.push.monitor.EmailMonitoringThread;
import org.obm.push.protocol.provisioning.MSEASProvisioingWBXML;
import org.obm.push.protocol.provisioning.MSWAPProvisioningXML;
import org.obm.push.protocol.provisioning.Policy;
import org.obm.push.store.CollectionDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OBMBackend implements IBackend {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final CollectionDao collectionDao;
	private final IEmailManager	emailManager;
	private final IContentsExporter contentsExporter;
	private final MailBackend mailBackend;
	private final CalendarMonitoringThread calendarPushMonitor;
	private final ContactsMonitoringThread contactsPushMonitor;
	
	private final Set<ICollectionChangeListener> registeredListeners;
	private final Map<Integer, EmailMonitoringThread> emailPushMonitors;

	private final CalendarBackend calendarBackend;
	
	@Inject
	private OBMBackend(CollectionDao collectionDao, IEmailManager emailManager,
			IContentsExporter contentsExporter, MailBackend mailBackend,
			CalendarMonitoringThread.Factory calendarMonitoringThreadFactory,
			ContactsMonitoringThread.Factory contactsMonitoringThreadFactory, CalendarBackend calendarBackend) {
		
		this.collectionDao = collectionDao;
		this.emailManager = emailManager;
		this.contentsExporter = contentsExporter;
		this.mailBackend = mailBackend;
		this.calendarBackend = calendarBackend;
		
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

	@Override
	public void startEmailMonitoring(BackendSession bs, Integer collectionId) throws CollectionNotFoundException {
		EmailMonitoringThread emt = null;
		synchronized (emailPushMonitors) {
			emt = emailPushMonitors.get(collectionId);
		}
		try {
			if (emt != null) {
				emt.stopIdle();
			} else {
				emt = new EmailMonitoringThread(mailBackend, registeredListeners,
					bs, collectionId, emailManager, contentsExporter);
			}
		
			emt.startIdle();
			
			synchronized (emailPushMonitors) {
				emailPushMonitors.put(collectionId, emt);
			}
		} catch (DaoException e) {
			stopIdle(emt, collectionId, e);
		} catch (IMAPException e) {
			stopIdle(emt, collectionId, e);
		} catch (LocatorClientException e) {
			stopIdle(emt, collectionId, e);
		}
	}

	private void stopIdle(EmailMonitoringThread emt, Integer collectionId, Exception exception) {
		logger.error("Error while starting idle on collection [ " + collectionId + " ]", exception);
		if (emt != null) {
			emt.stopIdle();	
		}
	}
	
	@Override
	public String getWasteBasket() {
		return "Trash";
	}

	@Override
	public Policy getDevicePolicy(BackendSession bs) {
		if (bs.getProtocolVersion().compareTo(new BigDecimal("2.5")) <= 0) {
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
	public void resetCollection(BackendSession bs, Integer collectionId) throws DaoException {
		logger.info("reset Collection {} For Full Sync devId {}", 
				new Object[]{collectionId, bs.getDevId()});
		try {
			collectionDao.resetCollection(bs.getDevice(), collectionId);
		} catch (RuntimeException re) {
			logger.error(re.getMessage(), re);
			throw re;
		}
	}

	@Override
	public AccessToken login(String userID, String password) throws AuthFault {
		return calendarBackend.login(userID, password);
	}

	@Override
	public Set<SyncCollection> getChangesSyncCollections(CollectionChangeListener collectionChangeListener) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException {
		
		final Set<SyncCollection> syncCollectionsChanged = new HashSet<SyncCollection>();
		final BackendSession backendSession = collectionChangeListener.getSession();
		
		for (SyncCollection syncCollection: collectionChangeListener.getMonitoredCollections()) {
			
			int count = getItemEstimateSize(backendSession, syncCollection);
			if (count > 0) {
				syncCollectionsChanged.add(syncCollection);
			}
		}
		
		return syncCollectionsChanged;
	}
	
	private int getItemEstimateSize(BackendSession backendSession, SyncCollection syncCollection) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException {
		
		return contentsExporter.getItemEstimateSize(backendSession, syncCollection.getOptions().getFilterType(),
				syncCollection.getCollectionId(), syncCollection.getSyncState());
	}
	
}
