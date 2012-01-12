package org.obm.push.mail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.MailMonitoringBackend;
import org.obm.push.backend.PushMonitoringManager;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.monitor.EmailMonitoringThread;
import org.obm.push.service.PushPublishAndSubscribe;
import org.obm.push.service.PushPublishAndSubscribe.Factory;
import org.obm.push.service.impl.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ImapMonitoringImpl implements MailMonitoringBackend {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<Integer, EmailMonitoringThread> emailPushMonitors;
	private final ImapClientProvider imapClientProvider;
	private final MailboxService emailManager;
	private final MappingService mappingService;
	private final Factory pubSubFactory;
	private final MailBackend mailBackend;
	
	
	@Inject
	private ImapMonitoringImpl(ImapClientProvider imapClientProvider,
			MappingService mappingService, MailboxService emailManager,
			MailBackend mailBackend, PushPublishAndSubscribe.Factory pubSubFactory) {
		this.mailBackend = mailBackend;
		this.pubSubFactory = pubSubFactory;
		this.emailPushMonitors = Collections
				.synchronizedMap(new HashMap<Integer, EmailMonitoringThread>());
		this.emailManager = emailManager;
		this.imapClientProvider = imapClientProvider;
		this.mappingService = mappingService;

	}
	
	private class Manager implements PushMonitoringManager {

		private final Set<ICollectionChangeListener> registeredListeners;
		private PushPublishAndSubscribe pushPublishAndSubscribe;

		public Manager(Set<ICollectionChangeListener> registeredListeners) {
			this.registeredListeners = registeredListeners;
			pushPublishAndSubscribe = pubSubFactory.create(mailBackend);
		}
		
		@Override
		public void emit() {
			pushPublishAndSubscribe.emit(registeredListeners);
		}
		
	}
	
	@Override
	public void startMonitoringCollection(BackendSession bs, Integer collectionId,
			Set<ICollectionChangeListener> registeredListeners) throws CollectionNotFoundException, DaoException {
		
		String collectionName = mappingService.getCollectionPathFor(collectionId);
		
		EmailMonitoringThread emt = null;
		synchronized (emailPushMonitors) {
			emt = emailPushMonitors.get(collectionId);
		}
		try {
			if (emt != null) {
				emt.stopIdle();
			} else {
				emt = new EmailMonitoringThread(
						new Manager(registeredListeners), bs, collectionName, emailManager, imapClientProvider);
			}

			emt.startIdle();

			synchronized (emailPushMonitors) {
				emailPushMonitors.put(collectionId, emt);
			}
		} catch (MailException e) {
			stopIdle(emt, collectionId, e);
		}
	}
	
	private void stopIdle(EmailMonitoringThread emt, Integer collectionId, Exception exception) {
		logger.error("Error while starting idle on collection [ " + collectionId + " ]", exception);
		if (emt != null) {
			emt.stopIdle();	
		}
	}

	
}
