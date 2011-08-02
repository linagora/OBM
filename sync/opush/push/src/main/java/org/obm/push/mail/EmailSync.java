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

import org.minig.imap.FastFetch;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.bean.Email;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.ServerErrorException;
import org.obm.push.store.EmailDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EmailSync implements IEmailSync {

	private final static Logger logger = LoggerFactory.getLogger(EmailSync.class);

	private final EmailDao emailDao;

	@Inject
	public EmailSync(EmailDao emailDao) {
		this.emailDao = emailDao;
	}

	@Override
	public MailChanges getSync(StoreClient imapStore, Integer devId, BackendSession bs, 
			SyncState state, Integer collectionId, FilterType filter) throws ServerErrorException {
		
		try {
			
			long time = getCurrentTime();
			long ct = getCurrentTime();
			
			final Set<Email> syncedMail = emailDao.getSyncedMail(devId, collectionId);
			
			ct = computeTime(ct);
			
			final Date startSyncWindows = getStartOfSynchronizationWindow(filter, state);
			final Date lastSync = state.getLastSync();
			
			final Collection<FastFetch> allMailInSynchronizationWindow = loadAllMailInSyncWindowFromIMAP(
					bs, imapStore, startSyncWindows);
			
			final Set<Email> allEmailInSynchronizationWindow = transformFastFetchToEmail(allMailInSynchronizationWindow);

			long computeChangesTime = getCurrentTime();
			
			final Collection<Long> removed = getRemoved(devId, collectionId,
					syncedMail, allEmailInSynchronizationWindow, lastSync);
			
			final Set<Email> updated = getUpdated(devId, collectionId, syncedMail,
					allMailInSynchronizationWindow, startSyncWindows, lastSync);
			
			final MailChanges sync = getMailChanges(updated, removed);
			
			computeChangesTime = computeTime(computeChangesTime);

			long writeTime = getCurrentTime();
			if (!syncedMail.equals(allEmailInSynchronizationWindow)) {
				updateData(devId, collectionId, lastSync, removed, updated);
			}
			
			writeTime = computeTime(writeTime);
			time = computeTime(time);
			
			logger.info("CollectionId = {} | Filter = {} | Changes found = {} | removes found = {} | " +
					" TIME : total = {}, loading = {}, updating = {}, computeChanges = {}", 
					new Object[]{collectionId, filter, sync.getUpdated().size(), removed.size(), 
					time, ct, writeTime, computeChangesTime});			
			
			return sync;
		} catch (Exception t) {
			logger.error(t.getMessage(), t);
			throw new ServerErrorException(t);
		}
	}

	private long computeTime(long time) {
		return getCurrentTime() - time;
	}
	
	private long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	private Date getStartOfSynchronizationWindow(FilterType filter, SyncState state) {
		if (filter != null) {
			
			final Calendar today = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			today.set(Calendar.MILLISECOND, 0);
			today.set(Calendar.SECOND, 0);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.HOUR, 0);
			
			return filter.getFilteredDate(today).getTime();
		} else {
			return state.getLastSync();
		}
	}
	
	private Collection<FastFetch> loadAllMailInSyncWindowFromIMAP(BackendSession bs, 
			StoreClient imapStore, Date windows) {
		
		Collection<FastFetch> mails = ImmutableSet.of();
		long time = getCurrentTime();
		
		try {
			
			Collection<Long> uids = imapStore.uidSearch(
					new SearchQuery(null, windows));
			mails = imapStore.uidFetchFast(uids);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		
		if (logger.isDebugEnabled()) {
			time = getCurrentTime() - time;
			logger.debug(bs.getLoginAtDomain()
					+ " loadAllMailInSyncWindowFromIMAP in " + time + "ms.");
		}
		
		logger.info("[" + bs.getLoginAtDomain() + "] " + mails.size()
				+ " mails on imap after " + windows);
		
		return mails;
	}
	
	private Collection<Long> getRemoved(Integer devId, Integer collectionId,
			Set<Email> syncedMails, Collection<Email> allMailToSync, Date lastSync) {
		
		final Set<Long> removed = new HashSet<Long>();
		Collection<Long> uidDeletedMails = emailDao.getDeletedMail(devId, collectionId, lastSync);
		Collection<Long> uidSyncedMails = transformEmailToUid(syncedMails);
		Collection<Long> uidAllMailToSync = transformEmailToUid(allMailToSync);
		
		if (syncedMails != null) {
			removed.addAll(uidDeletedMails);
			removed.addAll(uidSyncedMails);
			removed.removeAll(uidAllMailToSync);
		}
		return removed;
	}

	private Set<Email> getUpdated(final Integer devId, final Integer collectionId, final Set<Email> syncedMail,
			final Collection<FastFetch> allMailToSync, final Date startWindowSync, Date lastSync) {
		
		Builder<Email> builder = ImmutableSet.builder();
		if (syncedMail != null) {
			for (FastFetch fast : allMailToSync) {
				Email imapMail = transformFastFetchToEmail(fast);
				if (fast.getInternalDate().after(startWindowSync)
						&& !syncedMail.contains(imapMail)) {
					builder.add(imapMail);
				}
			}
		}

		Set<Email> updated = emailDao.getUpdatedMail(devId, collectionId, lastSync);
		builder.addAll(updated.iterator());
		
		return builder.build();
	}

	private void updateData(Integer devId, Integer collectionId, Date lastSync, 
			final Collection<Long> removed, final Collection<Email> updated)
			throws SQLException {
		
		if (removed.size() > 0) {
			emailDao.removeMessages(devId, collectionId, lastSync, removed);
		}
		
		if (updated.size() > 0) {
			emailDao.addMessages(devId, collectionId, lastSync, updated);
		}
	}

	private Set<Email> transformFastFetchToEmail(Collection<FastFetch> fetchs) {
		Builder<Email> builder = ImmutableSet.builder();
		for (FastFetch f: fetchs) {
			builder.add(transformFastFetchToEmail(f));
		}
		return builder.build();
	}

	private Email transformFastFetchToEmail(FastFetch fast) {
		return new Email(fast.getUid(), fast.isRead());
	}

	private Collection<Long> transformEmailToUid(Collection<Email> oldUids) {
		return Collections2.transform(oldUids,
				new Function<Email, Long>() {
					@Override
					public Long apply(Email input) {
						return input.getUid();
					}
				});
	}

	private MailChanges getMailChanges(final Set<Email> updated, 
			final Collection<Long> removed) {
		
		Calendar lastSync = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		lastSync.setTime(new Date());
		return new MailChanges(
				removed, transformEmailToUid(updated), lastSync.getTime());
	}

}
