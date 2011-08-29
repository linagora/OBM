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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.minig.imap.FastFetch;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.push.bean.Email;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.store.EmailDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public MailChanges getSync(StoreClient imapStore, Integer devId, SyncState state, Integer collectionId, FilterType filter) 
			throws DaoException {

		Date syncStartDate = getStartDateSynchronizationWindowEmails(filter, state);
		Set<Email> listSyncedEmailFromDatabase = emailDao.getSyncedMail(devId, collectionId);
		
		Set<Email> listEmailFromIMAPOfPDA = loadAllMailInSyncWindowFromIMAP(imapStore, syncStartDate);
		Set<Email> listEmailsToUpdated = getUpdated(listSyncedEmailFromDatabase, listEmailFromIMAPOfPDA);
		Collection<Long> listEmailsToRemoved = getRemoved(listSyncedEmailFromDatabase, listEmailFromIMAPOfPDA);

		updateData(devId, collectionId, state.getLastSync(), listEmailsToRemoved, listEmailsToUpdated);
		
		MailChanges mailChanges = getMailChanges(listEmailsToUpdated, listEmailsToRemoved);
		
		logger.info("sync emails [ emailFromDatabase = {} | emailFromPDA = {} | " +
				"emailsToUpdatedDB = {} | emailsToRemovedDB = {} | emailSendToPDA = upd {}, rm {} ]", 
				new Object[]{listSyncedEmailFromDatabase.size(), listEmailFromIMAPOfPDA.size(), listEmailsToUpdated.size(), 
				listEmailsToRemoved.size(), mailChanges.getUpdated().size(), mailChanges.getRemoved().size()});
		
		return mailChanges;
	}

	private Date getStartDateSynchronizationWindowEmails(FilterType filter, SyncState state) {
		if (filter != null) {
			Calendar today = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			today.set(Calendar.MILLISECOND, 0);
			today.set(Calendar.SECOND, 0);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.HOUR, 0);
			return filter.getFilteredDate(today).getTime();
		} else {
			return state.getLastSync();
		}
	}
	
	private Set<Email> loadAllMailInSyncWindowFromIMAP(StoreClient imapStore, Date windows) {
		Collection<Long> uids = imapStore.uidSearch(new SearchQuery(null, windows));
		Collection<FastFetch> mails = imapStore.uidFetchFast(uids);
		return EmailFactory.listEmailFromFastFetch(mails);
	}
	
	private Collection<Long> getRemoved( Set<Email> listSyncedEmailFromDatabase, Collection<Email> listEmailFromIMAPOfPDA) {
		Collection<Long> listEmailToRemoved = new ArrayList<Long>();
		listEmailToRemoved.addAll( EmailFactory.listUIDFromEmail(listSyncedEmailFromDatabase) );
		listEmailToRemoved.removeAll( EmailFactory.listUIDFromEmail(listEmailFromIMAPOfPDA) );
		return listEmailToRemoved;
	}

	private Set<Email> getUpdated(Set<Email> listSyncedEmailFromDatabase, Collection<Email> listEmailFromIMAPOfPDA) {
		Builder<Email> builder = ImmutableSet.builder();
		if (listSyncedEmailFromDatabase != null) {
			for (Email imapMail: listEmailFromIMAPOfPDA) {
				if (!listSyncedEmailFromDatabase.contains(imapMail)) {
					builder.add(imapMail);
				}
			}
		}
		return builder.build();
	}

	private void updateData(Integer devId, Integer collectionId, Date lastSync, Collection<Long> removed, Collection<Email> updated)
			throws DaoException {
		
		if (removed != null && removed.size() > 0) {
			emailDao.removeMessages(devId, collectionId, lastSync, removed);
		}
		if (updated != null && updated.size() > 0) {
			emailDao.addMessages(devId, collectionId, lastSync, updated);
		}
	}
	
	private MailChanges getMailChanges(Set<Email> updated, Collection<Long> removed) {
		Collection<Long> longs = new HashSet<Long>();
		for (Email email: updated) {
			if (!email.isRead()) {
				longs.add(email.getUid());
			}
		}
		return new MailChanges(removed, longs, EmailFactory.getNowDate());
	}

}
