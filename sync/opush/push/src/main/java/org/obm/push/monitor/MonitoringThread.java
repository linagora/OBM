package org.obm.push.monitor;

import java.util.Date;
import java.util.Set;

import org.obm.dbcp.DBCP;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.impl.ChangedCollections;

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
			
			emit(ccls);
		}
	}
	
}
