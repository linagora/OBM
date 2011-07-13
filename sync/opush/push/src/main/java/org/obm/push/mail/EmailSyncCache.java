/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.mail;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.transaction.TransactionManager;

import org.minig.imap.FastFetch;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.dbcp.DBCP;
import org.obm.dbcp.DataSource;
import org.obm.push.backend.BackendSession;
import org.obm.push.exception.ServerErrorException;
import org.obm.push.store.EmailCache;
import org.obm.push.store.FilterType;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implements cache update policy.
 * 
 * The following policy is used for cache updates : refresh each folder.
 * 
 * Folder refresh loads a list of cached uids, then this list is compared to the
 * uid list on the server.
 * 
 */
@Singleton
public class EmailSyncCache implements IEmailSync {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final ISyncStorage storage;
	private final DataSource dataSource;

	@Inject
	public EmailSyncCache(ISyncStorage storage, DBCP dbcp) {
		this.storage = storage;
		this.dataSource = dbcp.getDataSource();
	}

	private Collection<Long> getRemoved(Integer devId, Integer collectionId,
			Set<EmailCache> syncedMails, Collection<EmailCache> allMailToSync,
			Date lastSync) {
		Set<Long> removed = new HashSet<Long>();
		Collection<Long> uidDeletedMails = storage.getDeletedMail(devId,
				collectionId, lastSync);
		Collection<Long> uidSyncedMails = transformEmailCacheToUid(syncedMails);
		Collection<Long> uidAllMailToSync = transformEmailCacheToUid(allMailToSync);

		if (syncedMails != null) {
			removed.addAll(uidDeletedMails);
			removed.addAll(uidSyncedMails);
			removed.removeAll(uidAllMailToSync);
		}
		return removed;
	}

	private Set<EmailCache> getUpdated(final Integer devId, final Integer collectionId, final Set<EmailCache> syncedMail,
			final Collection<FastFetch> allMailToSync,
			final Date startWindowSync, Date lastSync) {
		Builder<EmailCache> builder = ImmutableSet.builder();
		if (syncedMail != null) {
			for (FastFetch fast : allMailToSync) {
				EmailCache imapMail = transformFastFetchToEmailCache(fast);
				if (fast.getInternalDate().after(startWindowSync)
						&& !syncedMail.contains(imapMail)) {
					builder.add(imapMail);
				}
			}
		}

		Set<EmailCache> updated = storage.getUpdatedMail(devId, collectionId, lastSync);
		builder.addAll(updated.iterator());
		
		return builder.build();
	}

	private Collection<FastFetch> loadAllMailInSyncWindowFromIMAP(
			BackendSession bs, StoreClient imapStore, Date windows) {
		Collection<FastFetch> mails = ImmutableSet.of();
		long time = System.currentTimeMillis();
		try {
			Collection<Long> uids = imapStore.uidSearch(new SearchQuery(null,
					windows));
			mails = imapStore.uidFetchFast(uids);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(bs.getLoginAtDomain()
					+ " loadAllMailInSyncWindowFromIMAP in " + time + "ms.");
		}
		logger.info("[" + bs.getLoginAtDomain() + "] " + mails.size()
				+ " mails on imap after " + windows);
		return mails;
	}

	private void updateDbCache(Integer devId, Integer collectionId,
			Date lastSync,
			final Collection<Long> removed, final Collection<EmailCache> updated)
			throws SQLException {
		if (removed.size() > 0) {
			storage.removeMessages(devId, collectionId, lastSync,
					removed);
		}
		if (updated.size() > 0) {
			storage.addMessages(devId, collectionId, lastSync, updated);
		}

	}

	private Set<EmailCache> transformFastFetchToEmailCache(
			Collection<FastFetch> fetchs) {
		Builder<EmailCache> builder = ImmutableSet.builder();
		for (FastFetch f : fetchs) {
			builder.add(transformFastFetchToEmailCache(f));
		}
		return builder.build();
	}

	private EmailCache transformFastFetchToEmailCache(FastFetch fast) {
		return new EmailCache(fast.getUid(), fast.isRead());
	}

	private Collection<Long> transformEmailCacheToUid(
			Collection<EmailCache> oldUids) {
		return Collections2.transform(oldUids,
				new Function<EmailCache, Long>() {
					@Override
					public Long apply(EmailCache input) {
						return input.getUid();
					}
				});
	}

	@Override
	public synchronized MailChanges getSync(StoreClient imapStore,
			Integer devId, BackendSession bs, SyncState state,
			Integer collectionId, FilterType filter)
			throws ServerErrorException {
		TransactionManager ut = dataSource.getTransactionManager();
		try {
			long time = System.currentTimeMillis();
			long ct = System.currentTimeMillis();
			ut.begin();
			Set<EmailCache> syncedMail = storage.getSyncedMail(devId,
					collectionId);
			ct = System.currentTimeMillis() - ct;
			Date startSyncWindows = getStartOfSynchronizationWindow(filter,
					state);
			Date lastSync = state.getLastSync();
			Collection<FastFetch> allMailInSynchronizationWindow = loadAllMailInSyncWindowFromIMAP(
					bs, imapStore, startSyncWindows);
			Set<EmailCache> allEmailCacheInSynchronizationWindow = transformFastFetchToEmailCache(allMailInSynchronizationWindow);

			long computeChangesTime = System.currentTimeMillis();
			Collection<Long> removed = getRemoved(devId, collectionId,
					syncedMail, allEmailCacheInSynchronizationWindow, lastSync);
			Set<EmailCache> updated = getUpdated(devId, collectionId, syncedMail,
					allMailInSynchronizationWindow, startSyncWindows, lastSync);
			MailChanges sync = getMailChanges(updated, removed);
			computeChangesTime = System.currentTimeMillis()
					- computeChangesTime;

			long writeTime = System.currentTimeMillis();
			if (!syncedMail.equals(allEmailCacheInSynchronizationWindow)) {
				updateDbCache(devId, collectionId, lastSync, removed, updated);
			}
			writeTime = System.currentTimeMillis() - writeTime;
			time = System.currentTimeMillis() - time;
			logger.info("["
					+ bs.getLoginAtDomain()
					+ "]: collectionId ["
					+ collectionId
					+ "] filter["
					+ filter
					+ "] "
					+ (sync.getUpdated().size() + " changes found," + removed
							.size()) + " removes found" + " (" + time
					+ "ms (loadCache: " + ct + "ms, updCache: " + writeTime
					+ "ms, computeChanges: " + computeChangesTime + "ms))");
			ut.commit();
			return sync;
		} catch (Exception t) {
			rollback(ut);
			logger.error(t.getMessage(), t);
			throw new ServerErrorException(t);
		}

	}

	private void rollback(TransactionManager ut) {
		try {
			ut.rollback();
		} catch (Throwable e) {
			logger.error("Error while rollbacking transaction");
		}
	}
	
	private MailChanges getMailChanges(Set<EmailCache> updated,
			Collection<Long> removed) {
		MailChanges ret = new MailChanges();
		ret.addRemoved(removed);
		ret.addUpdated(transformEmailCacheToUid(updated));

		Calendar lastSync = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		lastSync.setTime(new Date());
		ret.setLastSync(lastSync.getTime());

		return ret;
	}

	private Date getStartOfSynchronizationWindow(FilterType filter,
			SyncState state) {
		Calendar today = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		today.set(Calendar.MILLISECOND, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR, 0);
		return filter != null ? filter.getFilteredDate(today).getTime() : state
				.getLastSync();
	}

}
