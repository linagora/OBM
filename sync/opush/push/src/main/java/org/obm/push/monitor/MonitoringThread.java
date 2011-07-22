package org.obm.push.monitor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obm.dbcp.DBCP;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.impl.ChangedCollections;
import org.obm.push.impl.PushNotification;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public abstract class MonitoringThread extends OpushMonitoringThread implements Runnable {
	
	protected final DBCP dbcp;
	
	private final Set<ICollectionChangeListener> ccls;
	private final long freqMillisec;
	private boolean stopped;
	
	protected abstract ChangedCollections getChangedCollections(Date lastSync);
	
	protected MonitoringThread(long freqMillisec,
			Set<ICollectionChangeListener> ccls,
			DBCP dbcp, IContentsExporter contentsExporter) {
		
		super(contentsExporter);
		
		this.freqMillisec = freqMillisec;
		this.stopped = false;
		this.ccls = ccls;
		this.dbcp = dbcp;
	}
	
	@Override
	public void run() {
		Date lastSync = getBaseLastSync();
		
		while (!stopped) {
			try {
				Thread.sleep(freqMillisec);
			} catch (InterruptedException e) {
				stopped = true;
				continue;
			}
				
			List<PushNotification> toNotify = ImmutableList.<PushNotification>of();
			
			synchronized (ccls) {
				if (ccls.isEmpty()) {
					continue;
				}
				ChangedCollections changedCollections = getChangedCollections(lastSync);
				toNotify = listPushNotification(selectListenersToNotify(changedCollections, ccls));
			}
			
			for (PushNotification listener: toNotify) {
				listener.emit();
			}
		}
	}

	private Set<ICollectionChangeListener> selectListenersToNotify(ChangedCollections changedCollections,
			Set<ICollectionChangeListener> ccls) {
		
		if (changedCollections.getChanged().isEmpty()) {
			return ImmutableSet.<ICollectionChangeListener>of();
		}
		
		HashSet<ICollectionChangeListener> listeners = new HashSet<ICollectionChangeListener>();
		for (ICollectionChangeListener listener: ccls) {
			if (listener.monitorOneOf(changedCollections)) {
				listeners.add(listener);
			}
		}
		
		return listeners;
		
	}

	private Date getBaseLastSync() {
		ChangedCollections collections = getChangedCollections(new Date(0));
		return collections.getLastSync();
	}
	
}
