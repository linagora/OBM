/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.OpushBackend;
import org.obm.push.backend.OpushCollection;
import org.obm.push.backend.PathsToCollections;
import org.obm.push.bean.Address;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.change.item.MSEmailChanges;
import org.obm.push.bean.ms.MSRead;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.exception.HierarchyChangesException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.minig.imap.impl.MessageSetUtils;
import org.obm.push.service.DateService;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.MappingService;
import org.obm.push.store.EmailDao;
import org.obm.push.store.SnapshotDao;
import org.obm.push.tnefconverter.TNEFConverterException;
import org.obm.push.tnefconverter.TNEFUtils;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.utils.Mime4jUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.ICalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.mail.util.QPDecoderStream;

@Singleton
public class MailBackendImpl extends OpushBackend implements MailBackend {

	private static final ImmutableList<String> SPECIAL_FOLDERS = 
			ImmutableList.of(EmailConfiguration.IMAP_INBOX_NAME,
							EmailConfiguration.IMAP_DRAFTS_NAME,
							EmailConfiguration.IMAP_SENT_NAME,
							EmailConfiguration.IMAP_TRASH_NAME);
	
	private static final ImmutableMap<String, FolderType> SPECIAL_FOLDERS_TYPES = 
			ImmutableMap.of(EmailConfiguration.IMAP_INBOX_NAME, FolderType.DEFAULT_INBOX_FOLDER,
							EmailConfiguration.IMAP_DRAFTS_NAME, FolderType.DEFAULT_DRAFTS_FOLDER,
							EmailConfiguration.IMAP_SENT_NAME, FolderType.DEFAULT_SENT_EMAIL_FOLDER,
							EmailConfiguration.IMAP_TRASH_NAME, FolderType.DEFAULT_DELETED_ITEMS_FOLDER);

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private final MailboxService mailboxService;
	private final Mime4jUtils mime4jUtils;
	private final ConfigurationService configurationService;
	private final ICalendar calendarClient;
	private final LoginService login;
	private final EmailDao emailDao;
	private final EmailSync emailSync;
	private final EventService eventService;
	private final DateService dateService;
	private final MSEmailFetcher msEmailFetcher;
	private final SnapshotDao snapshotDao;
	private final EmailChangesComputer emailChangesComputer;
	private final EmailChangesFetcher emailChangesFetcher;


	@Inject
	/* package */ MailBackendImpl(MailboxService mailboxService, 
			@Named(CalendarType.CALENDAR) ICalendar calendarClient, 
			EmailDao emailDao, EmailSync emailSync,
			LoginService login, Mime4jUtils mime4jUtils, ConfigurationService configurationService,
			SnapshotDao snapshotDao,
			EmailChangesComputer emailChangesComputer,
			EmailChangesFetcher emailChangesFetcher,
			MappingService mappingService,
			EventService eventService,
			DateService dateService,
			MSEmailFetcher msEmailFetcher,
			Provider<CollectionPath.Builder> collectionPathBuilderProvider)  {

		super(mappingService, collectionPathBuilderProvider);
		this.mailboxService = mailboxService;
		this.emailDao = emailDao;
		this.emailSync = emailSync;
		this.mime4jUtils = mime4jUtils;
		this.configurationService = configurationService;
		this.calendarClient = calendarClient;
		this.login = login;
		this.snapshotDao = snapshotDao;
		this.emailChangesComputer = emailChangesComputer;
		this.emailChangesFetcher = emailChangesFetcher;
		this.eventService = eventService;
		this.dateService = dateService;
		this.msEmailFetcher = msEmailFetcher;
	}

	@Override
	public PIMDataType getPIMDataType() {
		return PIMDataType.EMAIL;
	}
	
	@Override
	public HierarchyCollectionChanges getHierarchyChanges(UserDataRequest udr, 
			FolderSyncState lastKnownState, FolderSyncState outgoingSyncState)
			throws DaoException, MailException {
		
		try {
			PathsToCollections currentSubscribedFolders = PathsToCollections.builder()
					.putAll(listSpecialFolders(udr))
					.putAll(listSubscribedFolders(udr))
					.build();
			snapshotHierarchy(udr, currentSubscribedFolders.pathKeys(), outgoingSyncState);
			return computeChanges(udr, lastKnownState, currentSubscribedFolders);
		} catch (CollectionNotFoundException e) {
			throw new HierarchyChangesException(e);
		}
	}
	
	@VisibleForTesting PathsToCollections listSpecialFolders(UserDataRequest udr) {
		return imapFolderNamesToCollectionPath(udr, SPECIAL_FOLDERS);
	}
	
	private PathsToCollections imapFolderNamesToCollectionPath(UserDataRequest udr, Iterable<String> imapFolderNames) {
		PathsToCollections.Builder builder = PathsToCollections.builder();
		for (String imapFolderName: imapFolderNames) {
			CollectionPath collectionPath = collectionPathBuilderProvider.get()
					.userDataRequest(udr)
					.pimType(PIMDataType.EMAIL)
					.backendName(imapFolderName)
					.build();
			builder.put(collectionPath, OpushCollection.builder()
					.collectionPath(collectionPath)
					.displayName(imapFolderName)
					.build());
		}
		return builder.build();
	}
	
	@VisibleForTesting PathsToCollections listSubscribedFolders(UserDataRequest udr) throws MailException {
		return imapFolderNamesToCollectionPath(udr,
					FluentIterable
					.from(
						mailboxService.listSubscribedFolders(udr))
					.transform(
							new Function<MailboxFolder, String>() {
								@Override
								public String apply(MailboxFolder input) {
									return input.getName();
								}})
					.toImmutableList());
	}

	private HierarchyCollectionChanges computeChanges(UserDataRequest udr, FolderSyncState lastKnownState,
			PathsToCollections currentSubscribedFolders) throws DaoException, CollectionNotFoundException {
		
		Set<CollectionPath> previousEmailCollections = lastKnownCollectionPath(udr, lastKnownState, getPIMDataType());
		Set<CollectionPath> deletedFolders = Sets.difference(previousEmailCollections, currentSubscribedFolders.pathKeys());
		Iterable<OpushCollection> newFolders = addedCollections(previousEmailCollections, currentSubscribedFolders);
		
		return buildHierarchyItemsChanges(udr, newFolders, deletedFolders);
	}

	private FolderType folderType(String folder) {
		return Objects.firstNonNull(SPECIAL_FOLDERS_TYPES.get(folder), FolderType.USER_CREATED_EMAIL_FOLDER);
	}
	
	@Override
	protected CollectionChange createCollectionChange(UserDataRequest udr, OpushCollection imapFolder)
			throws DaoException, CollectionNotFoundException {
		
		CollectionPath collectionPath = imapFolder.collectionPath();
		Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath.collectionPath());
		return CollectionChange.builder()
			.collectionId(mappingService.collectionIdToString(collectionId))
			.parentCollectionId("0")
			.displayName(imapFolder.displayName())
			.folderType(folderType(imapFolder.displayName()))
			.isNew(true)
			.build();
	}

	@Override
	protected CollectionDeletion createCollectionDeletion(UserDataRequest udr, CollectionPath imapFolder)
			throws CollectionNotFoundException, DaoException {

		Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), imapFolder.collectionPath());
		return CollectionDeletion.builder()
				.collectionId(mappingService.collectionIdToString(collectionId))
				.build();
	}
	
	private CollectionPath getWasteBasketPath(UserDataRequest udr) {
		return collectionPathBuilderProvider.get().pimType(PIMDataType.EMAIL).userDataRequest(udr).backendName(EmailConfiguration.IMAP_TRASH_NAME).build();
	}

	private MailChanges getSync(UserDataRequest udr, SyncState state, Integer collectionId, FilterType filterType) 
			throws ProcessingEmailException, CollectionNotFoundException {
		
		try {
			String collectionPath = mappingService.getCollectionPathFor(collectionId);
			SyncState newState = state.newWindowedSyncState(filterType);
			return emailSync.getSync(udr, mailboxService, newState, collectionPath, collectionId);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	@Override
	public int getItemEstimateSize(UserDataRequest udr, Integer collectionId, SyncState state, 
			SyncCollectionOptions syncCollectionOptions) throws ProcessingEmailException, 
			CollectionNotFoundException, DaoException {
		
		MailChanges mailChanges = getSync(udr, state, collectionId, syncCollectionOptions.getFilterType());
		DataDelta dataDelta = getDataDelta(udr, collectionId, mailChanges, syncCollectionOptions.getBodyPreferences());
		return dataDelta.getItemEstimateSize();
	}

	private DataDelta getDataDelta(UserDataRequest udr, Integer collectionId, MailChanges mailChanges, 
			List<BodyPreference> bodyPreferences) throws ProcessingEmailException, 
			CollectionNotFoundException, DaoException {
		
		List<ItemChange> itemChanges = fetchMails(udr, collectionId, 
				mappingService.getCollectionPathFor(collectionId), mailChanges.getNewEmailsUids(), bodyPreferences);
		List<ItemDeletion> itemsToDelete = mappingService.buildItemsToDeleteFromUids(collectionId, mailChanges.getRemovedEmailsUids());
		
		return DataDelta.builder()
				.changes(itemChanges)
				.deletions(itemsToDelete)
				.syncDate(mailChanges.getLastSync())
				.build();
	}
	
	private List<ItemChange> fetchMails(UserDataRequest udr, Integer collectionId, String collectionPath, 
			Collection<Long> emailsUids, List<BodyPreference> bodyPreferences) throws ProcessingEmailException {
		
		ImmutableList.Builder<ItemChange> itch = ImmutableList.builder();
		try {
			List<org.obm.push.bean.ms.MSEmail> msMails = msEmailFetcher.fetch(udr, collectionId, collectionPath, emailsUids, bodyPreferences);
			for (org.obm.push.bean.ms.MSEmail mail: msMails) {
				itch.add(getItemChange(collectionId, mail.getUid(), mail));
			}
			return itch.build();
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	private ItemChange getItemChange(Integer collectionId, Long uid, IApplicationData data) {
		ItemChange ic = new ItemChange();
		ic.setServerId(mappingService.getServerIdFor(collectionId, "" + uid));
		ic.setData(data);
		return ic;
	}
	
	/**
	 * @throws FilterTypeChangedException when a snapshot 
	 * exists for the given syncKey and the snapshot.filterType != options.filterType
	 */
	@Override
	public DataDelta getChanged(UserDataRequest udr, SyncState state, Integer collectionId, SyncCollectionOptions options, SyncKey newSyncKey)
			throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException, ProcessingEmailException, FilterTypeChangedException {

		try {
			Date dataDeltaDate = dateService.getCurrentDate();
			String collectionPath = mappingService.getCollectionPathFor(collectionId);
			long currentUIDNext = mailboxService.fetchUIDNext(udr, collectionPath);
			
			Snapshot previousStateSnapshot = snapshotDao.get(udr.getDevId(), state.getSyncKey(), collectionId);
			Collection<Email> managedEmails = getManagedEmails(previousStateSnapshot);
			Collection<Email> newManagedEmails = searchEmailsToManage(udr, collectionId, collectionPath, previousStateSnapshot, options, dataDeltaDate, currentUIDNext);
			takeSnapshot(udr, collectionId, options, newManagedEmails, currentUIDNext, newSyncKey);
			
			EmailChanges emailChanges = emailChangesComputer.computeChanges(managedEmails, newManagedEmails);
			MSEmailChanges serverItemChanges = emailChangesFetcher.fetch(udr, collectionId,
					collectionPath, options.getBodyPreferences(), emailChanges);
			
			return DataDelta.builder()
					.changes(serverItemChanges.getItemChanges())
					.deletions(serverItemChanges.getItemDeletions())
					.syncDate(dataDeltaDate)
					.build();
			
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		}
	}

	private void takeSnapshot(UserDataRequest udr, Integer collectionId, 
			SyncCollectionOptions syncCollectionOptions, Collection<Email> managedEmails, long currentUIDNext, SyncKey newSyncKey) {
		
		snapshotDao.put(Snapshot.builder()
				.emails(managedEmails)
				.collectionId(collectionId)
				.deviceId(udr.getDevId())
				.filterType(syncCollectionOptions.getFilterType())
				.syncKey(newSyncKey)
				.uidNext(currentUIDNext)
				.build());
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
			Date searchEmailsFromDate = actualOptions.getFilterType().getFilteredDate(dataDeltaDate);
			return mailboxService.fetchEmails(udr, collectionPath, searchEmailsFromDate);
		}
		return searchSnapshotAndActualChanges(udr, collectionPath, previousStateSnapshot, currentUIDNext);
	}

	private void assertSnapshotHasSameOptionsThanRequest(Snapshot snapshot, SyncCollectionOptions options, Integer collectionId, UserDataRequest udr)
			throws FilterTypeChangedException {
		
		if (!snapshotIsAbsent(snapshot) && filterTypeHasChanged(snapshot, options)) {
			manageFilterTypeChanged(udr, collectionId, snapshot.getFilterType(), options.getFilterType());
		}
	}

	private void manageFilterTypeChanged(UserDataRequest udr, Integer collectionId, FilterType previousFilterType, FilterType currentFilterType) throws FilterTypeChangedException {
		snapshotDao.deleteCollectionForDevice(udr.getDevId(), collectionId);
		throw new FilterTypeChangedException(previousFilterType, currentFilterType);
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
		
		MessageSet messageSet = MessageSetUtils.computeEmailsUID(previousStateSnapshot, currentUIDNext);
		Iterable<Long> emailsUIDToFetch = messageSet.asDiscreteValues();
		return mailboxService.fetchEmails(udr, collectionPath, ImmutableList.copyOf(emailsUIDToFetch));
	}
	
	private Map<Integer, Collection<Long>> getEmailUidByCollectionId(List<String> fetchIds) {
		Map<Integer, Collection<Long>> ret = Maps.newHashMap();
		for (String serverId : fetchIds) {
			Integer collectionId = mappingService.getCollectionIdFromServerId(serverId);
			Collection<Long> set = ret.get(collectionId);
			if (set == null) {
				set = Sets.newHashSet();
				ret.put(collectionId, set);
			}
			set.add(getEmailUidFromServerId(serverId));
		}
		return ret;
	}

	private List<ItemChange> fetchItems(UserDataRequest udr, Integer collectionId, Collection<Long> uids, 
			List<BodyPreference> bodyPreferences) throws CollectionNotFoundException, ProcessingEmailException {
		
		try {
			final Builder<ItemChange> ret = ImmutableList.builder();
			final String collectionPath = mappingService.getCollectionPathFor(collectionId);
			final List<org.obm.push.bean.ms.MSEmail> emails = 
					msEmailFetcher.fetch(udr, collectionId, collectionPath, uids, bodyPreferences);
			
			for (final org.obm.push.bean.ms.MSEmail email: emails) {
				ItemChange ic = new ItemChange();
				ic.setServerId(mappingService.getServerIdFor(collectionId, String.valueOf(email.getUid())));
				ic.setData(email);
				ret.add(ic);
			}
			return ret.build();	
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	@Override
	public void delete(UserDataRequest udr, Integer collectionId, String serverId, Boolean moveToTrash)
			throws CollectionNotFoundException, DaoException,
			UnexpectedObmSyncServerException, ItemNotFoundException, ProcessingEmailException, UnsupportedBackendFunctionException {
		try {
			boolean trash = Objects.firstNonNull(moveToTrash, true);
			if (trash) {
				logger.info("move to trash serverId {}", serverId);
			} else {
				logger.info("delete serverId {}", serverId);
			}
			if (serverId != null) {
				final Long uid = getEmailUidFromServerId(serverId);
				final String collectionName = mappingService.getCollectionPathFor(collectionId);
				final Integer devDbId = udr.getDevice().getDatabaseId();

				if (trash) {
					CollectionPath wasteBasketPath = getWasteBasketPath(udr);
					Integer wasteBasketId = mappingService.getCollectionIdFor(udr.getDevice(), wasteBasketPath.collectionPath());
					long newUID = mailboxService.moveItem(udr, collectionName, wasteBasketPath.collectionPath(), uid);
					deleteEmails(devDbId, collectionId, Arrays.asList(newUID));
					addMessageInCache(udr, devDbId, wasteBasketId, newUID, wasteBasketPath.collectionPath());
				} else {
					mailboxService.delete(udr, collectionName, uid);
				}
			}	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		}
	}


	protected String getDefaultCalendarName(UserDataRequest udr) {
		return "obm:\\\\" + udr.getUser().getLoginAtDomain() + "\\calendar\\"
				+ udr.getUser().getLoginAtDomain();
	}
	
	@Override
	public String createOrUpdate(UserDataRequest udr, Integer collectionId, String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException, DaoException, ItemNotFoundException {
		
		MSRead msRead = (MSRead) data;
		try {
			String collectionPath = mappingService.getCollectionPathFor(collectionId);
			logger.info("createOrUpdate( {}, {}, {} )", new Object[]{collectionPath, serverId, clientId});
			if (serverId != null) {
				Long mailUid = getEmailUidFromServerId(serverId);
				mailboxService.updateReadFlag(udr, collectionPath, mailUid, msRead.isRead());
			}
			return serverId;
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		}
	}

	@Override
	public String move(UserDataRequest udr, String srcFolder, String dstFolder, String messageId) 
			throws CollectionNotFoundException, ProcessingEmailException, UnsupportedBackendFunctionException {
		
		try {
			logger.info("move( messageId =  {}, from = {}, to = {} )", new Object[]{messageId, srcFolder, dstFolder});
			final Long currentMailUid = getEmailUidFromServerId(messageId);
			final Integer srcFolderId = mappingService.getCollectionIdFor(udr.getDevice(), srcFolder);
			final Integer dstFolderId = mappingService.getCollectionIdFor(udr.getDevice(), dstFolder);
			final Integer devDbId = udr.getDevice().getDatabaseId();
			Long newUidMail = mailboxService.moveItem(udr, srcFolder, dstFolder, currentMailUid);
			deleteEmails(devDbId, srcFolderId, Arrays.asList(currentMailUid));
			addMessageInCache(udr, devDbId, dstFolderId, newUidMail, dstFolder);
			return dstFolderId + ":" + newUidMail;	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ProcessingEmailException(e);
		}
	}


	@Override
	public void sendEmail(UserDataRequest udr, byte[] mailContent, boolean saveInSent) throws ProcessingEmailException {
		try {
			Message message = mime4jUtils.parseMessage(mailContent);
			SendEmail sendEmail = new SendEmail(getUserEmail(udr), message);
			send(udr, sendEmail, saveInSent);
		} catch (UnexpectedObmSyncServerException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (AuthFault e) {
			throw new ProcessingEmailException(e);
		} 
	}

	@Override
	public void replyEmail(UserDataRequest udr, byte[] mailContent, boolean saveInSent, Integer collectionId, String serverId)
			throws ProcessingEmailException, CollectionNotFoundException, ItemNotFoundException {
		
		try {
			String collectionPath = "";
			if (collectionId != null && collectionId > 0) {
				collectionPath = mappingService.getCollectionPathFor(collectionId);
			}
			
			if (serverId == null || !serverId.isEmpty()) {
				collectionId = mappingService.getCollectionIdFromServerId(serverId);
				collectionPath = mappingService.getCollectionPathFor(collectionId);
			}
			
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> mail = fetchMails(udr, collectionId, collectionPath, uids);

			if (mail.size() > 0) {
				Message message = mime4jUtils.parseMessage(mailContent);
				ReplyEmail replyEmail = new ReplyEmail(configurationService, mime4jUtils, getUserEmail(udr), mail.get(0), message,
						ImmutableMap.<String, MSAttachementData>of());
				send(udr, replyEmail, saveInSent);
				mailboxService.setAnsweredFlag(udr, collectionPath, uid);
			} else {
				sendEmail(udr, mailContent, saveInSent);
			}
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (UnexpectedObmSyncServerException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (AuthFault e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		} 
	}

	@Override
	public void forwardEmail(UserDataRequest udr, byte[] mailContent, boolean saveInSent, String collectionId, String serverId) 
			throws ProcessingEmailException, CollectionNotFoundException {
		
		try {
			Integer collectionIdInt = Integer.parseInt(collectionId);
			String collectionPath = mappingService.getCollectionPathFor(collectionIdInt);
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);

			List<MSEmail> mail = fetchMails(udr, collectionIdInt, collectionPath, uids);
			if (mail.size() > 0) {
				Message message = mime4jUtils.parseMessage(mailContent);
				MSEmail originMail = mail.get(0);
				
				Map<String, MSAttachementData> originalMailAttachments = new HashMap<String, MSAttachementData>();
				if (!mime4jUtils.isAttachmentsExist(message)) {
					loadAttachments(originalMailAttachments, udr, originMail);
				}
				
				ForwardEmail forwardEmail = 
						new ForwardEmail(configurationService, mime4jUtils, getUserEmail(udr), originMail, message, originalMailAttachments);
				send(udr, forwardEmail, saveInSent);
				try{
					mailboxService.setAnsweredFlag(udr, collectionPath, uid);
				} catch (Throwable e) {
					logger.info("Can't set Answered Flag to mail["+uid+"]");
				}
			} else {
				sendEmail(udr, mailContent, saveInSent);
			}
		} catch (NumberFormatException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (UnexpectedObmSyncServerException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (AuthFault e) {
			throw new ProcessingEmailException(e);
		} 
	}

	private List<MSEmail> fetchMails(UserDataRequest udr, Integer collectionId, 
			String collectionPath, Collection<Long> uids) throws MailException {
		
		final List<MSEmail> mails = new LinkedList<MSEmail>();
		String collectionName = mailboxService.parseMailBoxName(udr, collectionPath);

		final MailMessageLoader mailLoader = new MailMessageLoader(mailboxService, eventService);
		for (final Long uid: uids) {
			final MSEmail email = mailLoader.fetch(collectionName, collectionId, uid, udr);
			if (email != null) {
				mails.add(email);
			}
		}
		return mails;
	}
	
	private void loadAttachments(Map<String, MSAttachementData> attachments, 
			UserDataRequest udr, MSEmail originMail) throws ProcessingEmailException {
		
		for (MSAttachement msAttachement: originMail.getAttachements()) {
			try {
				MSAttachementData msAttachementData = getAttachment(udr, msAttachement.getFileReference());
				attachments.put(msAttachement.getDisplayName(), msAttachementData);
			} catch (AttachementNotFoundException e) {
				throw new ProcessingEmailException(e);
			} catch (CollectionNotFoundException e) {
				throw new ProcessingEmailException(e);
			} 
		}
	}

	private AccessToken login(UserDataRequest session) throws AuthFault {
		return login.login(session.getUser().getLoginAtDomain(), session.getPassword());
	}
	
	private String getUserEmail(UserDataRequest udr) throws UnexpectedObmSyncServerException, AuthFault {
		ICalendar cal = calendarClient;
		AccessToken at = login(udr);
		try {
			return cal.getUserEmail(at);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			login.logout(at);
		}
	}

	private void send(UserDataRequest udr, SendEmail sendEmail, boolean saveInSent) throws ProcessingEmailException {
		try {
			boolean isScheduleMeeting = !TNEFUtils.isScheduleMeetingRequest(sendEmail.getMessage());

			Address from = getAddress(sendEmail.getFrom());
			if (!sendEmail.isInvitation() && isScheduleMeeting) {
				mailboxService.sendEmail(udr, from, sendEmail.getTo(),
						sendEmail.getCc(), sendEmail.getCci(), sendEmail.getMessage(), saveInSent);	
			} else {
				logger.warn("OPUSH blocks email invitation sending by PDA. Now that obm-sync handle email sending on event creation/modification/deletion, we must filter mail from PDA for these actions.");
			}
		} catch (TNEFConverterException e) {
			throw new ProcessingEmailException(e);
		} catch (StoreEmailException e) {
			throw new ProcessingEmailException(e);
		} catch (SendEmailException e) {
			throw new ProcessingEmailException(e);
		} catch (SmtpInvalidRcptException e) {
			throw new ProcessingEmailException(e);
		}
	}

	private Address getAddress(String from) throws ProcessingEmailException {
		if(from == null || !from.contains("@")){
			throw new ProcessingEmailException(""+from+"is not a valid email");
		}
		return new Address(from);
	}

	@Override
	public MSEmail getEmail(UserDataRequest udr, Integer collectionId, String serverId) throws CollectionNotFoundException, ProcessingEmailException {
		try {
			String collectionName = mappingService.getCollectionPathFor(collectionId);
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> emails = fetchMails(udr, collectionId, collectionName, uids);
			if (emails.size() > 0) {
				return emails.get(0);
			}
			return null;	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}

	@Override
	public MSAttachementData getAttachment(UserDataRequest udr, String attachmentId) 
			throws AttachementNotFoundException, CollectionNotFoundException, ProcessingEmailException {
		
		if (attachmentId != null && !attachmentId.isEmpty()) {
			Map<String, String> parsedAttId = AttachmentHelper.parseAttachmentId(attachmentId);
			try {
				String collectionId = parsedAttId
						.get(AttachmentHelper.COLLECTION_ID);
				String messageId = parsedAttId.get(AttachmentHelper.MESSAGE_ID);
				String mimePartAddress = parsedAttId
						.get(AttachmentHelper.MIME_PART_ADDRESS);
				String contentType = parsedAttId
						.get(AttachmentHelper.CONTENT_TYPE);
				String contentTransferEncoding = parsedAttId
						.get(AttachmentHelper.CONTENT_TRANSFERE_ENCODING);
				logger.info("attachmentId= [collectionId:" + collectionId
						+ "] [emailUid" + messageId + "] [mimePartAddress:"
						+ mimePartAddress + "] [contentType" + contentType
						+ "] [contentTransferEncoding"
						+ contentTransferEncoding + "]");

				String collectionName = mappingService.getCollectionPathFor(Integer
						.parseInt(collectionId));
				InputStream is = mailboxService.findAttachment(udr,
						collectionName, Long.parseLong(messageId),
						new MimeAddress(mimePartAddress));

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				FileUtils.transfer(is, out, true);
				byte[] rawData = out.toByteArray();

				if ("QUOTED-PRINTABLE".equals(contentTransferEncoding)) {
					out = new ByteArrayOutputStream();
					InputStream in = new QPDecoderStream(new ByteArrayInputStream(rawData));
					FileUtils.transfer(in, out, true);
					rawData = out.toByteArray();
				} else if ("BASE64".equals(contentTransferEncoding)) {
					rawData = new Base64().decode(rawData);
				}

				return new MSAttachementData(contentType,
						new ByteArrayInputStream(rawData));
		
			} catch (NumberFormatException e) {
				throw new ProcessingEmailException(e);
			} catch (IOException e) {
				throw new ProcessingEmailException(e);
			} catch (MailException e) {
				throw new ProcessingEmailException(e);
			} catch (DaoException e) {
				throw new ProcessingEmailException(e);
			} catch (LocatorClientException e) {
				throw new ProcessingEmailException(e);
			}
		}
		
		throw new AttachementNotFoundException();
	}

	@Override
	public void emptyFolderContent(UserDataRequest udr, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException, CollectionNotFoundException, ProcessingEmailException {
		
		try {
			CollectionPath wasteBasketPath = getWasteBasketPath(udr);
			if (!wasteBasketPath.collectionPath().equals(collectionPath)) {
				throw new NotAllowedException(
						"Only the Trash folder can be purged.");
			}
			final Integer devDbId = udr.getDevice().getDatabaseId();
			int collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath);
			Collection<Long> uids = mailboxService.purgeFolder(udr, devDbId, collectionPath, collectionId);
			deleteEmails(devDbId, collectionId, uids);
			if (deleteSubFolder) {
				logger.warn("deleteSubFolder isn't implemented because opush doesn't yet manage folders");
			}	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	@Override
	public Long getEmailUidFromServerId(String serverId) {
		Integer itemIdFromServerId = mappingService.getItemIdFromServerId(serverId);
		if (itemIdFromServerId != null) {
			return itemIdFromServerId.longValue();
		} else {
			return null;
		}
	}

	private void addMessageInCache(UserDataRequest udr, Integer devId, Integer collectionId, Long mailUids, String collectionPath) throws DaoException, MailException {
		Collection<Email> emails = mailboxService.fetchEmails(udr, collectionPath, ImmutableList.of(mailUids));
		try {
			markEmailsAsSynced(devId, collectionId, emails);
		} catch (DaoException e) {
			throw new DaoException("Error while adding messages in db", e);
		}
	}

	public void markEmailsAsSynced(Integer devId, Integer collectionId, Collection<Email> messages) throws DaoException {
		markEmailsAsSynced(devId, collectionId, DateUtils.getCurrentDate(), messages);
	}

	public void markEmailsAsSynced(Integer devId, Integer collectionId, Date lastSync, Collection<Email> emails) throws DaoException {
		Set<Email> allEmailsToMark = Sets.newHashSet(emails);
		Set<Email> alreadySyncedEmails = emailDao.alreadySyncedEmails(collectionId, devId, emails);
		Set<Email> modifiedEmails = findModifiedEmails(allEmailsToMark, alreadySyncedEmails);
		Set<Email> emailsNeverTrackedBefore = filterOutAlreadySyncedEmails(allEmailsToMark, alreadySyncedEmails);
		logger.info("mark {} updated mail(s) as synced", modifiedEmails.size());
		emailDao.updateSyncEntriesStatus(devId, collectionId, modifiedEmails);
		logger.info("mark {} new mail(s) as synced", emailsNeverTrackedBefore.size());
		emailDao.createSyncEntries(devId, collectionId, emailsNeverTrackedBefore, lastSync);
	}

	private Set<Email> findModifiedEmails(Set<Email> allEmailsToMark, Set<Email> alreadySyncedEmails) {
		Map<Long, Email> indexedEmailsToMark = getEmailsAsUidTreeMap(allEmailsToMark);
		HashSet<Email> modifiedEmails = Sets.newHashSet();
		for (Email email: alreadySyncedEmails) {
			Email modifiedEmail = indexedEmailsToMark.get(email.getUid());
			if (modifiedEmail != null && !modifiedEmail.equals(email)) {
				modifiedEmails.add(modifiedEmail);
			}
		}
		return modifiedEmails;
	}

	private Set<Email> filterOutAlreadySyncedEmails(Set<Email> allEmailsToMark, Set<Email> alreadySyncedEmails) {
		return org.obm.push.utils.collection.Sets.difference(allEmailsToMark, alreadySyncedEmails, new Comparator<Email>() {
			@Override
			public int compare(Email o1, Email o2) {
				return Long.valueOf(o1.getUid() - o2.getUid()).intValue();
			}
		});
	}

	private Map<Long, Email> getEmailsAsUidTreeMap(Set<Email> emails) {
		return Maps.uniqueIndex(emails, new Function<Email, Long>() {
			@Override
			public Long apply(Email email) {
				return email.getIndex();
			}
		});
	}
	

	private void deleteEmails(Integer devId, Integer collectionId, Collection<Long> mailUids) throws DaoException {
		try {
			emailDao.deleteSyncEmails(devId, collectionId, mailUids);
		} catch (DaoException e) {
			throw new DaoException("Error while deleting messages in db", e);
		}
	}

	@Override
	public List<ItemChange> fetch(UserDataRequest udr, List<String> itemIds, SyncCollectionOptions collectionOptions) 
			throws ProcessingEmailException {
		
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		Map<Integer, Collection<Long>> emailUids = getEmailUidByCollectionId(itemIds);
		for (Entry<Integer, Collection<Long>> entry : emailUids.entrySet()) {
			Integer collectionId = entry.getKey();
			Collection<Long> uids = entry.getValue();
			try {
				ret.addAll(fetchItems(udr, collectionId, uids, collectionOptions.getBodyPreferences()));
			} catch (CollectionNotFoundException e) {
				logger.error("fetchItems : collection {} not found !", collectionId);
			}
		}
		return ret;
	}
}
