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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.minig.imap.FastFetch;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.push.bean.Email;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.store.EmailDao;
import org.obm.push.utils.DateUtils;
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
	public MailChanges getSync(StoreClient imapStore, Integer devId, SyncState state, Integer collectionId) 
			throws DaoException {

		Set<Email> dbEmails = emailDao.getSyncedMail(devId, collectionId);
		Set<Email> imapEmails = getImapEmails(imapStore, state.getLastSync());
		Set<Email> emailsToUpdated = getUpdated(dbEmails, imapEmails);
		Set<Email> emailsToRemoved = getRemoved(dbEmails, imapEmails);

		loggerInfo(state.getLastSync(), dbEmails, imapEmails, emailsToUpdated, emailsToRemoved);
		return getMailChanges(emailsToUpdated, emailsToRemoved);
	}

	private void loggerInfo(Date syncStartDate, Set<Email> emailsFromDB, Set<Email> emailsFromIMAP, 
			Set<Email> emailsToUpdated, Set<Email> emailsToRemoved) {
		logger.info("Synchronization date {}", syncStartDate.toString());
		logger.info("{} email(s) from database", emailsFromDB.size());
		logger.info("{} email(s) from imap", emailsFromIMAP.size());
		logger.info("{} email(s) will be updated", emailsToUpdated.size());
		logger.info("{} email(s) will be removed", emailsToRemoved.size());
	}

	private Set<Email> getImapEmails(StoreClient imapStore, Date windows) {
		Collection<Long> uids = imapStore.uidSearch(new SearchQuery(null, windows));
		Collection<FastFetch> mails = imapStore.uidFetchFast(uids);
		return EmailFactory.listEmailFromFastFetch(mails);
	}
	
	private Set<Email> getRemoved(Set<Email> emailsFromDB, Set<Email> emailsFromIMAP) {
		Set<Email> listEmailToRemoved = new HashSet<Email>();
		listEmailToRemoved.addAll(emailsFromDB);
		listEmailToRemoved.removeAll(emailsFromIMAP);
		return listEmailToRemoved;
	}

	private Set<Email> getUpdated(Set<Email> emailsFromDB, Collection<Email> emailsFromIMAP) {
		Builder<Email> builder = ImmutableSet.builder();
		if (emailsFromDB != null) {
			for (Email imapMail: emailsFromIMAP) {
				if (!emailsFromDB.contains(imapMail)) {
					builder.add(imapMail);
				}
			}
		}
		return builder.build();
	}

	private MailChanges getMailChanges(Set<Email> updated, Set<Email> removed) {
		return new MailChanges(removed, updated, DateUtils.getCurrentDate());
	}

}
