package org.obm.push.monitor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
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
					int count = contentsExporter.getItemEstimateSize(backendSession, syncCollection.getOptions().getFilterType(),
							syncCollection.getCollectionId(), syncCollection.getSyncState(), syncCollection.getDataType());
					
					if (count > 0) {
						addPushNotification(pushNotifyList, ccl);
					}
				} catch (CollectionNotFoundException e) {
					logger.error(e.getMessage(), e);
				} catch (DaoException e) {
					logger.error(e.getMessage(), e);
				} catch (UnknownObmSyncServerException e) {
					logger.error(e.getMessage(), e);
				} catch (ProcessingEmailException e) {
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
