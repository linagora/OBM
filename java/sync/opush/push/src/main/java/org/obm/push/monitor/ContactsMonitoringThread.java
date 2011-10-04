package org.obm.push.monitor;

import java.util.Date;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CollectionDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class ContactsMonitoringThread extends MonitoringThread {

	@Singleton
	public static class Factory {
		private final CollectionDao collectionDao;
		private final IContentsExporter contentsExporter;

		@Inject
		private Factory(CollectionDao collectionDao, IContentsExporter contentsExporter) {
			this.collectionDao = collectionDao;
			this.contentsExporter = contentsExporter;
		}

		public ContactsMonitoringThread createClient(long freqMs,
				Set<ICollectionChangeListener> ccls) {
			
			return new ContactsMonitoringThread(freqMs, ccls,
					this.collectionDao, this.contentsExporter);
		}
	}
	
	private ContactsMonitoringThread(long freqMs,
			Set<ICollectionChangeListener> ccls,
			CollectionDao collectionDao, IContentsExporter contentsExporter) {
		super(freqMs, ccls, collectionDao, contentsExporter);
	}

	@Override
	protected ChangedCollections getChangedCollections(Date lastSync) throws ChangedCollectionsException {
		try {
			return collectionDao.getContactChangedCollections(lastSync);
		} catch (DaoException e) {
			throw new ChangedCollectionsException(e);
		}
	}

}
