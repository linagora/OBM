package org.obm.push.mail;

import org.minig.imap.StoreClient;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;

public interface IEmailSync {

	MailChanges getSync(StoreClient imapStore, Integer devId, SyncState state, Integer collectionId, FilterType filter) 
			throws DaoException;

}
