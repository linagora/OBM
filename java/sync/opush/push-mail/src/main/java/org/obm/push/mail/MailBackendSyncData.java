/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.mail;

import java.util.Collection;
import java.util.Date;

import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.minig.imap.impl.MessageSetUtils;
import org.obm.push.service.DateService;
import org.obm.push.service.impl.MappingService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class MailBackendSyncData {
	
	@Singleton
	public static class MailBackendSyncDataFactory {
		
		private final DateService dateService;
		private final MappingService mappingService;
		private final MailboxService mailboxService;
		private final SnapshotService snapshotService;
		private final EmailChangesComputer emailChangesComputer;

		@Inject
		@VisibleForTesting MailBackendSyncDataFactory(DateService dateService,
				MappingService mappingService,
				MailboxService mailboxService,
				SnapshotService snapshotService,
				EmailChangesComputer emailChangesComputer) {
			
			this.dateService = dateService;
			this.mappingService = mappingService;
			this.mailboxService = mailboxService;
			this.snapshotService = snapshotService;
			this.emailChangesComputer = emailChangesComputer;
		}
		
		public MailBackendSyncData create(UserDataRequest udr, ItemSyncState state, Integer collectionId, 
				SyncCollectionOptions options) throws ProcessingEmailException, 
				CollectionNotFoundException, DaoException, FilterTypeChangedException {
			
			Date dataDeltaDate = dateService.getCurrentDate();
			String collectionPath = mappingService.getCollectionPathFor(collectionId);
			long currentUIDNext = mailboxService.fetchUIDNext(udr, collectionPath);
			
			Snapshot previousStateSnapshot = snapshotService.getSnapshot(udr.getDevId(), state.getSyncKey(), collectionId);
			Collection<Email> managedEmails = getManagedEmails(previousStateSnapshot);
			Collection<Email> newManagedEmails = searchEmailsToManage(udr, collectionId, collectionPath, previousStateSnapshot, options, dataDeltaDate, currentUIDNext);
			
			EmailChanges emailChanges = emailChangesComputer.computeChanges(managedEmails, newManagedEmails);
				
			return new MailBackendSyncData(dataDeltaDate, collectionPath, currentUIDNext, previousStateSnapshot, managedEmails, newManagedEmails, emailChanges);
		}

		@VisibleForTesting Collection<Email> getManagedEmails(Snapshot previousStateSnapshot) {
			if (previousStateSnapshot != null) {
				return previousStateSnapshot.getEmails();
			}
			return ImmutableSet.of(); 
		}

		@VisibleForTesting Collection<Email> searchEmailsToManage(UserDataRequest udr, Integer collectionId, String collectionPath,
				Snapshot previousStateSnapshot, SyncCollectionOptions actualOptions,
				Date dataDeltaDate, long currentUIDNext) throws FilterTypeChangedException {
			
			assertSnapshotHasSameOptionsThanRequest(previousStateSnapshot, actualOptions, collectionId, udr);
			if (mustSyncByDate(previousStateSnapshot)) {
				Date searchEmailsFromDate = searchEmailsFromDate(actualOptions.getFilterType(), dataDeltaDate);
				return mailboxService.fetchEmails(udr, collectionPath, searchEmailsFromDate);
			}
			return searchSnapshotAndActualChanges(udr, collectionPath, previousStateSnapshot, currentUIDNext);
		}

		@VisibleForTesting Date searchEmailsFromDate(FilterType filterType, Date dataDeltaDate) {
			return Objects.firstNonNull(filterType, FilterType.ALL_ITEMS).getFilteredDate(dataDeltaDate);	
		}

		private void assertSnapshotHasSameOptionsThanRequest(Snapshot snapshot, SyncCollectionOptions options, Integer collectionId, UserDataRequest udr)
				throws FilterTypeChangedException {
			
			if (!snapshotIsAbsent(snapshot) && filterTypeHasChanged(snapshot, options)) {
				manageFilterTypeChanged(udr, collectionId, snapshot.getFilterType(), options.getFilterType());
			}
		}

		private void manageFilterTypeChanged(UserDataRequest udr, Integer collectionId, FilterType previousFilterType, FilterType currentFilterType) throws FilterTypeChangedException {
			snapshotService.deleteSnapshotAndSyncKeys(udr.getDevId(), collectionId);
			throw new FilterTypeChangedException(collectionId, previousFilterType, currentFilterType);
		}

		@VisibleForTesting boolean mustSyncByDate(Snapshot previousStateSnapshot) {
			return snapshotIsAbsent(previousStateSnapshot);
		}

		private boolean snapshotIsAbsent(Snapshot previousStateSnapshot) {
			return previousStateSnapshot == null;
		}

		private boolean filterTypeHasChanged(Snapshot snapshot, SyncCollectionOptions options) {
			return snapshot.getFilterType() != options.getFilterType();
		}

		private Collection<Email> searchSnapshotAndActualChanges(UserDataRequest udr, 
				String collectionPath, Snapshot previousStateSnapshot, long currentUIDNext) {
			
			MessageSet messages = MessageSetUtils.computeEmailsUID(previousStateSnapshot, currentUIDNext);
			return mailboxService.fetchEmails(udr, collectionPath, messages);
		}
		
	}
	
	private final Date dataDeltaDate;
	private final String collectionPath;
	private final long currentUIDNext;
	private final Snapshot previousStateSnapshot;
	private final Collection<Email> managedEmails;
	private final Collection<Email> newManagedEmails;
	private final EmailChanges emailChanges;
	
	@VisibleForTesting MailBackendSyncData(Date dataDeltaDate,
			String collectionPath,
			long currentUIDNext,
			Snapshot previousStateSnapshot,
			Collection<Email> managedEmails,
			Collection<Email> newManagedEmails,
			EmailChanges emailChanges) {
		
		this.dataDeltaDate = dataDeltaDate;
		this.collectionPath = collectionPath;
		this.currentUIDNext = currentUIDNext;
		this.previousStateSnapshot = previousStateSnapshot;
		this.managedEmails = managedEmails;
		this.newManagedEmails = newManagedEmails;
		this.emailChanges = emailChanges;
	}
	
	public Date getDataDeltaDate() {
		return dataDeltaDate;
	}
	
	public String getCollectionPath() {
		return collectionPath;
	}
	
	public long getCurrentUIDNext() {
		return currentUIDNext;
	}
	
	public Snapshot getPreviousStateSnapshot() {
		return previousStateSnapshot;
	}
	
	public Collection<Email> getManagedEmails() {
		return managedEmails;
	}
	
	public Collection<Email> getNewManagedEmails() {
		return newManagedEmails;
	}
	
	public EmailChanges getEmailChanges() {
		return emailChanges;
	}
	
}
