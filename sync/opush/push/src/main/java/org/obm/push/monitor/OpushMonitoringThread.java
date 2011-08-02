package org.obm.push.monitor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.impl.PushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpushMonitoringThread {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private final IContentsExporter contentsExporter;
	
	public OpushMonitoringThread(IContentsExporter contentsExporter) {
		this.contentsExporter = contentsExporter;
	}
	
	protected void emit(final Set<ICollectionChangeListener> ccls) {
		final LinkedList<PushNotification> pushNotifyList = listPushNotification(ccls);
		for (PushNotification pushNotify: pushNotifyList) {
			pushNotify.emit();
		}
	}
	
	protected LinkedList<PushNotification> listPushNotification(
			Set<ICollectionChangeListener> ccls) {
		
		final LinkedList<PushNotification> pushNotifyList = new LinkedList<PushNotification>();
		for (final ICollectionChangeListener ccl : ccls) {

			final Collection<SyncCollection> monitoredCollections = ccl
					.getMonitoredCollections();
			
			final BackendSession backendSession = ccl.getSession();
			for (SyncCollection syncCollection : monitoredCollections) {
			
				try {
					int count = contentsExporter.getCount(backendSession,
							syncCollection.getSyncState(),
							syncCollection.getOptions().getFilterType(),
							syncCollection.getCollectionId());
					
					if (count > 0) {
						addPushNotification(pushNotifyList, ccl);
					}
				} catch (ActiveSyncException e) {
					logger.error(e.getMessage(), e);
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return pushNotifyList;
	}

	protected void addPushNotification(
			final LinkedList<PushNotification> pushNotifyList,
			final ICollectionChangeListener ccl) {

		pushNotifyList.add(new PushNotification(ccl));
	}
	
}
