package org.obm.push.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.obm.dbcp.DBCP;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.SyncCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MonitoringThread implements Runnable {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * SynchronizedSet, all accesses should be synchronized
	 */
	private Set<ICollectionChangeListener> ccls;
	private boolean stopped;
	private long freqMillisec;
	
	private ObmSyncBackend backend;
	protected DBCP dbcp;

	protected MonitoringThread(long freqMillisec,
			Set<ICollectionChangeListener> ccls, ObmSyncBackend backend,
			DBCP dbcp) {
	
		this.freqMillisec = freqMillisec;
		this.stopped = false;
		this.ccls = ccls;
		this.backend = backend;
		this.dbcp = dbcp;
	}

	protected abstract ChangedCollections getChangedCollections(Date lastSync);
	
	@Override
	public void run() {
		Date lastSync = new Date();
		ChangedCollections cols = getChangedCollections(lastSync);
		lastSync = cols.getLastSync();
		logger.info("DB lastSync = {} ", lastSync);
		while (!stopped) {
			try {
				Thread.sleep(freqMillisec);
			} catch (InterruptedException e) {
				stopped = true;
				continue;
			}
			synchronized (ccls) {
				if (ccls.isEmpty()) {
					continue;
				}
			}
			cols = getChangedCollections(lastSync);
			lastSync = cols.getLastSync();
			LinkedList<PushNotification> toNotify = new LinkedList<PushNotification>();
			synchronized (ccls) {
				for (ICollectionChangeListener ccl : ccls) {
					Collection<SyncCollection> monitoredCollections = ccl
							.getMonitoredCollections();
					Set<SyncCollection> changes = getChangedCollections(ccl
							.getSession(), cols, monitoredCollections);
					if (!changes.isEmpty()) {
						toNotify.add(new PushNotification(changes, ccl));
					}
				}
			}
			for (PushNotification pn : toNotify) {
				pn.emit();
			}
		}
	}

	private Set<SyncCollection> getChangedCollections(BackendSession session,
			ChangedCollections cols, Collection<SyncCollection> monitoredCollections) {
		Set<SyncCollection> ret = new HashSet<SyncCollection>();

		for (SyncCollection sc : cols.getChanged()) {
			int id;
			try {
				id = backend.getCollectionIdFor(session.getDevId(), sc
						.getCollectionPath());
				sc.setCollectionId(id);
				logger.info("processing sc : id = {} & name = {} ", 
						new Object[]{ sc.getCollectionId(), sc.getCollectionPath()});
				if (monitoredCollections.contains(sc)) {
					logger.info("******** PUSH " + sc.getCollectionId() + " name: "
							+ sc.getCollectionPath() + " ********");
					ret.add(sc);
				} else {
					logger.info("** " + sc.getCollectionId()
							+ " modified but nobody cares **");
					for (SyncCollection mon : monitoredCollections) {
						logger.info("   * monitored: " + mon.getCollectionId());
					}
				}
			} catch (ActiveSyncException e) {
			}
			
		}

		for (SyncCollection toPush : ret) {
			try {
				String colName = toPush.getCollectionPath();
				int collectionId = backend.getCollectionIdFor(session.getDevId(),
						colName);
				toPush.setCollectionId(collectionId);
			} catch (ActiveSyncException e) {
				logger.error(e.getMessage(), e);
			}
			
		}

		return ret;
	}
	
}
