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

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

import org.minig.imap.FastFetch;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.push.bean.Email;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.store.EmailDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EmailSync implements IEmailSync {

	private final class UidComparator implements Comparator<Email> {
		@Override
		public int compare(Email o1, Email o2) {
			return (int) (o1.getUid() - o2.getUid());
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(EmailSync.class);
	private final EmailDao emailDao;

	@Inject
	public EmailSync(EmailDao emailDao) {
		this.emailDao = emailDao;
	}

	@Override
	public MailChanges getSync(StoreClient imapStore, Integer devId, SyncState state, Integer collectionId) throws DaoException {
		Set<Email> emailsFromIMAP = getImapEmails(imapStore, state.getLastSync());
		Set<Email> alreadySyncedEmails = emailDao.listSyncedEmails(devId, collectionId, state);
		Set<Email> newAndUpdatedEmails = Sets.difference(emailsFromIMAP, alreadySyncedEmails);
		Set<Email> deletedEmails = findDeletedEmails(emailsFromIMAP, alreadySyncedEmails);
		MailChanges mailChanges = new MailChanges(deletedEmails, newAndUpdatedEmails);
		loggerInfo(state.getLastSync(), emailsFromIMAP, mailChanges, alreadySyncedEmails);
		return mailChanges;
	}

	private Set<Email> findDeletedEmails(Set<Email> emailsFromIMAP, Set<Email> alreadySyncedEmails) {
		Set<Email> deletedEmails = 
				org.obm.push.utils.collection.Sets.difference(
						alreadySyncedEmails, emailsFromIMAP, new UidComparator());
		return deletedEmails;
	}

	private void loggerInfo(Date syncStartDate, Set<Email> emailsFromIMAP, MailChanges mailChanges, Set<Email> alreadySyncedEmails) {
		logger.info("Synchronization date {}", syncStartDate);
		logger.info("{} email(s) from imap", emailsFromIMAP.size());
		logger.info("{} new or updated emails", mailChanges.getNewAndUpdatedEmails().size());
		logger.info("{} already synced emails", alreadySyncedEmails.size());
	}

	private Set<Email> getImapEmails(StoreClient imapStore, Date windows) {
		Collection<Long> uids = imapStore.uidSearch(new SearchQuery(null, windows));
		Collection<FastFetch> mails = imapStore.uidFetchFast(uids);
		return EmailFactory.listEmailFromFastFetch(mails);
	}

}
