package org.obm.push.mail;

import org.minig.imap.StoreClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.exception.ServerErrorException;
import org.obm.push.store.FilterType;
import org.obm.push.store.SyncState;

public interface IEmailSync {

	MailChanges getSync(StoreClient imapStore, Integer devId,
			BackendSession bs, SyncState state, Integer collectionId, FilterType filter)
			throws ServerErrorException;
}
