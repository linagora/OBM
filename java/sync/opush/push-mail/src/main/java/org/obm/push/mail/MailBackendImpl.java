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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.EmailConfiguration.ExpungePolicy;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.OpushBackend;
import org.obm.push.backend.OpushCollection;
import org.obm.push.backend.PathsToCollections;
import org.obm.push.bean.Address;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.MSEmailChanges;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewBuildException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.exception.HierarchyChangesException;
import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.exception.MailException;
import org.obm.push.exception.OpushLocatorException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.MailBackendSyncData.MailBackendSyncDataFactory;
import org.obm.push.mail.bean.EmailReader;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.conversation.EmailViewAttachment;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.transformer.Transformer.TransformersFactory;
import org.obm.push.service.AuthenticationService;
import org.obm.push.service.SmtpSender;
import org.obm.push.service.impl.MappingService;
import org.obm.push.tnefconverter.TNEFConverterException;
import org.obm.push.tnefconverter.TNEFUtils;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
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

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final MailboxService mailboxService;
	private final Mime4jUtils mime4jUtils;
	private final OpushConfiguration opushConfiguration;
	private final AuthenticationService authenticationService;
	private final MSEmailFetcher msEmailFetcher;
	private final TransformersFactory transformersFactory;
	private final SnapshotService snapshotService;
	private final EmailChangesFetcher emailChangesFetcher;
	private final MailBackendSyncDataFactory mailBackendSyncDataFactory;
	private final WindowingService windowingService;
	private final SmtpSender smtpSender;


	private final EmailConfiguration emailConfiguration;


	@Inject
	/* package */ MailBackendImpl(MailboxService mailboxService, 
			AuthenticationService authenticationService, 
			Mime4jUtils mime4jUtils, OpushConfiguration opushConfiguration,
			SnapshotService snapshotService,
			EmailChangesFetcher emailChangesFetcher,
			MappingService mappingService,
			MSEmailFetcher msEmailFetcher,
			TransformersFactory transformersFactory,
			Provider<CollectionPath.Builder> collectionPathBuilderProvider,
			MailBackendSyncDataFactory mailBackendSyncDataFactory,
			WindowingService windowingService,
			SmtpSender smtpSender, 
			EmailConfiguration emailConfiguration)  {

		super(mappingService, collectionPathBuilderProvider);
		this.mailboxService = mailboxService;
		this.mime4jUtils = mime4jUtils;
		this.opushConfiguration = opushConfiguration;
		this.authenticationService = authenticationService;
		this.snapshotService = snapshotService;
		this.emailChangesFetcher = emailChangesFetcher;
		this.msEmailFetcher = msEmailFetcher;
		this.transformersFactory = transformersFactory;
		this.mailBackendSyncDataFactory = mailBackendSyncDataFactory;
		this.windowingService = windowingService;
		this.smtpSender = smtpSender;
		this.emailConfiguration = emailConfiguration;
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
					.toList());
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

	@Override
	public int getItemEstimateSize(UserDataRequest udr, ItemSyncState state, Integer collectionId, 
			SyncCollectionOptions options) throws ProcessingEmailException, 
			CollectionNotFoundException, DaoException, FilterTypeChangedException {
		
		MailBackendSyncData syncData = mailBackendSyncDataFactory.create(udr, state, collectionId, options);
		return syncData.getEmailChanges().sumOfChanges();
	}

	/**
	 * @throws FilterTypeChangedException when a snapshot 
	 * exists for the given syncKey and the snapshot.filterType != options.filterType
	 */
	@Override
	public DataDelta getChanged(UserDataRequest udr, ItemSyncState syncState, AnalysedSyncCollection syncCollection, 
			SyncClientCommands clientCommands, SyncKey newSyncKey)
		throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException, ProcessingEmailException, FilterTypeChangedException {

		try {
			SyncKey requestSyncKey = syncCollection.getSyncKey();
			WindowingIndexKey key = new WindowingIndexKey(udr.getUser(), udr.getDevId(), syncCollection.getCollectionId());
			
			if (windowingService.hasPendingElements(key, requestSyncKey)) {
				snapshotService.actualizeSnapshot(udr.getDevId(), requestSyncKey, syncCollection.getCollectionId(), newSyncKey);
				return continueWindowing(udr, syncCollection, key, syncState.getSyncDate(), newSyncKey);
			} else {
				return startWindowing(udr, syncState, syncCollection, key, newSyncKey);
			}
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		}
	}

	private DataDelta startWindowing(UserDataRequest udr, ItemSyncState syncState, AnalysedSyncCollection collection, WindowingIndexKey key, SyncKey newSyncKey)
			throws EmailViewPartsFetcherException {
		
		Integer collectionId = collection.getCollectionId();
		SyncCollectionOptions options = collection.getOptions();
		
		MailBackendSyncData syncData = mailBackendSyncDataFactory.create(udr, syncState, collectionId, options);
		takeSnapshot(udr, collectionId, collection.getOptions(), syncData, newSyncKey);

		if (collection.getWindowSize() >= syncData.getEmailChanges().sumOfChanges()) {
			return fetchChanges(udr, collection, key, syncData.getDataDeltaDate(), newSyncKey, syncData.getEmailChanges());
		} else {
			windowingService.pushPendingElements(key, newSyncKey, syncData.getEmailChanges(), collection.getWindowSize());
			return continueWindowing(udr, collection, key, syncData.getDataDeltaDate(), newSyncKey);
		}
	}

	private DataDelta continueWindowing(UserDataRequest udr, AnalysedSyncCollection collection, WindowingIndexKey key,
			Date dataDelaSyncDate, SyncKey dataDeltaSyncKey)
		throws DaoException, EmailViewPartsFetcherException {
		
		EmailChanges pendingChanges = windowingService.popNextPendingElements(key, collection.getWindowSize(), dataDeltaSyncKey);
		return fetchChanges(udr, collection, key, dataDelaSyncDate, dataDeltaSyncKey, pendingChanges);
	}

	private DataDelta fetchChanges(UserDataRequest udr, AnalysedSyncCollection collection, WindowingIndexKey key,
			Date dataDelaSyncDate, SyncKey dataDeltaSyncKey, EmailChanges pendingChanges)
		throws EmailViewPartsFetcherException {
		
		int collectionId = collection.getCollectionId();
		MSEmailChanges serverItemChanges = emailChangesFetcher.fetch(udr, collectionId,
				mappingService.getCollectionPathFor(collectionId), collection.getOptions().getBodyPreferences(), pendingChanges);
		
		return DataDelta.builder()
				.changes(serverItemChanges.getItemChanges())
				.deletions(serverItemChanges.getItemDeletions())
				.syncDate(dataDelaSyncDate)
				.syncKey(dataDeltaSyncKey)
				.moreAvailable(windowingService.hasPendingElements(key, dataDeltaSyncKey))
				.build();
	}

	private void takeSnapshot(UserDataRequest udr, Integer collectionId, 
			SyncCollectionOptions syncCollectionOptions, MailBackendSyncData syncData, SyncKey newSyncKey) {
		
		snapshotService.storeSnapshot(Snapshot.builder()
				.emails(syncData.getNewManagedEmails())
				.collectionId(collectionId)
				.deviceId(udr.getDevId())
				.filterType(syncCollectionOptions.getFilterType())
				.syncKey(newSyncKey)
				.uidNext(syncData.getCurrentUIDNext())
				.build());
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
			final List<UidMSEmail> emails = 
					msEmailFetcher.fetch(udr, collectionId, collectionPath, uids, bodyPreferences);
			
			for (final UidMSEmail email: emails) {
				ItemChange ic = new ItemChange();
				ic.setServerId(mappingService.getServerIdFor(collectionId, String.valueOf(email.getUid())));
				ic.setData(email);
				ret.add(ic);
			}
			return ret.build();	
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
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
				final String destinationCollectionPath = mappingService.getCollectionPathFor(collectionId);

				CollectionPath wasteBasketPath = getWasteBasketPath(udr);
				if (trash && !wasteBasketPath.collectionPath().equals(destinationCollectionPath)) {
					mailboxService.move(udr, destinationCollectionPath, wasteBasketPath.collectionPath(), MessageSet.singleton(uid));
				} else {
					mailboxService.delete(udr, destinationCollectionPath, MessageSet.singleton(uid));
				}
				expunge(udr, destinationCollectionPath);
			}	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
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
		
		org.obm.push.bean.ms.MSEmail msEmail = (org.obm.push.bean.ms.MSEmail)data;
		try {
			String collectionPath = mappingService.getCollectionPathFor(collectionId);
			logger.info("createOrUpdate( {}, {}, {} )", collectionPath, serverId, clientId);
			if (serverId != null) {
				MessageSet messages = MessageSet.singleton(getEmailUidFromServerId(serverId));
				mailboxService.updateReadFlag(udr, collectionPath, messages, msEmail.isRead());
			}
			return serverId;
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		}
	}

	@Override
	public String move(UserDataRequest udr, String srcFolder, String dstFolder, String messageId) 
			throws CollectionNotFoundException, ProcessingEmailException, UnsupportedBackendFunctionException {
		
		try {
			logger.info("move( messageId =  {}, from = {}, to = {} )", messageId, srcFolder, dstFolder);
			final Long currentMailUid = getEmailUidFromServerId(messageId);
			final Integer dstFolderId = mappingService.getCollectionIdFor(udr.getDevice(), dstFolder);
			MessageSet messages = mailboxService.move(udr, srcFolder, dstFolder, MessageSet.singleton(currentMailUid));
			if (!messages.isEmpty()) {
				expunge(udr, srcFolder);
				return ServerId.buildServerIdString(dstFolderId, Iterables.getOnlyElement(messages));	
			}
			throw new ItemNotFoundException("The item to move may not exists anymore");
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
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
			Map<MSEmailBodyType, EmailView> emailViews = fetchMailInHTMLThenText(udr, collectionId, collectionPath, uid);

			if (emailViews.size() > 0) {
				Message message = mime4jUtils.parseMessage(mailContent);
				ReplyEmail replyEmail = new ReplyEmail(opushConfiguration, mime4jUtils, getUserEmail(udr), emailViews, message,
						ImmutableMap.<String, MSAttachementData>of());
				send(udr, replyEmail, saveInSent);
				mailboxService.setAnsweredFlag(udr, collectionPath, MessageSet.singleton(uid));
			} else {
				sendEmail(udr, mailContent, saveInSent);
			}
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (UnexpectedObmSyncServerException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		} 
	}

	@Override
	public void forwardEmail(UserDataRequest udr, byte[] mailContent, boolean saveInSent, String collectionId, String serverId) 
			throws ProcessingEmailException, CollectionNotFoundException {
		
		try {
			Integer collectionIdInt = Integer.parseInt(collectionId);
			String collectionPath = mappingService.getCollectionPathFor(collectionIdInt);
			Long uid = getEmailUidFromServerId(serverId);

			Map<MSEmailBodyType, EmailView> emailViews = fetchMailInHTMLThenText(udr, collectionIdInt, collectionPath, uid);
			if (emailViews.size() > 0) {
				Message message = mime4jUtils.parseMessage(mailContent);
				
				Map<String, MSAttachementData> originalMailAttachments = new HashMap<String, MSAttachementData>();
				if (!mime4jUtils.isAttachmentsExist(message)) {
					loadAttachments(originalMailAttachments, udr, emailViews);
				}
				
				ForwardEmail forwardEmail = 
						new ForwardEmail(opushConfiguration, mime4jUtils, getUserEmail(udr), emailViews, message, originalMailAttachments);
				send(udr, forwardEmail, saveInSent);
				try{
					mailboxService.setAnsweredFlag(udr, collectionPath, MessageSet.singleton(uid));
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
		} catch (OpushLocatorException e) {
			throw new ProcessingEmailException(e);
		} catch (MimeException e) {
			throw new ProcessingEmailException(e);
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		} 
	}
	
	@VisibleForTesting Map<MSEmailBodyType, EmailView> fetchMailInHTMLThenText(UserDataRequest udr, Integer collectionId, 
			String collectionPath, Long uid) throws EmailViewPartsFetcherException {
		
		ImmutableMap.Builder<MSEmailBodyType, EmailView> emailViews = ImmutableMap.builder();
		try {
			emailViews.put(MSEmailBodyType.HTML, fetchBodyType(udr, collectionId, collectionPath, uid, MSEmailBodyType.HTML));
		} catch (EmailViewBuildException e) {
			try {
				emailViews.put(MSEmailBodyType.PlainText, fetchBodyType(udr, collectionId, collectionPath, uid, MSEmailBodyType.PlainText));
			} catch (EmailViewBuildException e2) {
			}
		}
		return emailViews.build();
	}

	private EmailView fetchBodyType(UserDataRequest udr, Integer collectionId, String collectionPath, Long uid, MSEmailBodyType bodyType)
			throws EmailViewPartsFetcherException, EmailViewBuildException {
		
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = 
				new EmailViewPartsFetcherImpl(transformersFactory, mailboxService, 
						ImmutableList.of(BodyPreference.builder().bodyType(bodyType).build()), 
						udr, collectionPath, collectionId);
		return emailViewPartsFetcherImpl.fetch(uid, new StrictMatchBodyPreferencePolicy());
	}
	
	private void loadAttachments(Map<String, MSAttachementData> attachments, 
			UserDataRequest udr, Map<MSEmailBodyType, EmailView> emailViews) throws ProcessingEmailException {
		
		Collection<EmailView> values = emailViews.values();
		if (values == null || values.isEmpty()) {
			return;
		}
		
		EmailView emailView = FluentIterable.from(values).first().get();
		for (EmailViewAttachment msAttachement: emailView.getAttachments()) {
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
	
	private String getUserEmail(UserDataRequest udr) throws UnexpectedObmSyncServerException {
		return authenticationService.getUserEmail(udr);
	}

	private void send(UserDataRequest udr, SendEmail sendEmail, boolean saveInSent) throws ProcessingEmailException {
		try {
			boolean isScheduleMeeting = !TNEFUtils.isScheduleMeetingRequest(sendEmail.getMessage());

			if (!sendEmail.isInvitation() && isScheduleMeeting) {
				sendEmail(udr, sendEmail, saveInSent);
			} else {
				logger.warn("OPUSH blocks email invitation sending by PDA. Now that obm-sync handle email sending on event creation/modification/deletion, we must filter mail from PDA for these actions.");
			}
		} catch (TNEFConverterException e) {
			throw new ProcessingEmailException(e);
		} catch (StoreEmailException e) {
			throw new ProcessingEmailException(e);
		}
	}

	@VisibleForTesting void sendEmail(UserDataRequest udr, SendEmail email, boolean saveInSent) {
		InputStream emailStream = null;
		try {
			emailStream = loadEmailInMemory(email);
			smtpSender.sendEmail(udr, validateFrom(email.getFrom()), email.getTo(), email.getCc(), email.getCci(), emailStream);
			if (saveInSent) {
				emailStream.reset();
				mailboxService.storeInSent(udr, multipleTimesReadable(emailStream, email.getMimeMessage().getCharset()));
			} else {
				logger.info("The email mustn't be stored in Sent folder.{saveInSent=false}");
			}
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
			throw new ProcessingEmailException(e);
		} catch (SendEmailException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (IllegalCharsetNameException e) {
			throw new ProcessingEmailException(e);
		} finally {
			IOUtils.closeQuietly(emailStream);
		}
	}

	private InputStream loadEmailInMemory(SendEmail email) throws IOException {
		InputStream emailStream = new ByteArrayInputStream(FileUtils.streamBytes(email.getMessage(), true));
		emailStream.mark(0);
		return emailStream;
	}

	private EmailReader multipleTimesReadable(InputStream streamMail, String charsetName) {
		return new EmailReader(new InputStreamReader(streamMail, Charset.forName(charsetName)));
	}

	private Address validateFrom(String from) throws ProcessingEmailException {
		if(from == null || !from.contains("@")){
			throw new ProcessingEmailException(""+from+"is not a valid email");
		}
		return new Address(from);
	}

	@Override
	public UidMSEmail getEmail(UserDataRequest udr, Integer collectionId, String serverId) throws CollectionNotFoundException, ProcessingEmailException {
		try {
			String collectionName = mappingService.getCollectionPathFor(collectionId);
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<UidMSEmail> emails = msEmailFetcher.fetch(udr, collectionId, collectionName, uids, null);
			if (emails.size() > 0) {
				return emails.get(0);
			}
			return null;	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
			throw new ProcessingEmailException(e);
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		}
	}

	@Override
	public org.obm.icalendar.ICalendar getInvitation(UserDataRequest udr, Integer collectionId, String serverId) 
			throws CollectionNotFoundException, ProcessingEmailException {
		
		try {
			return fetchInvitation(udr, collectionId, getEmailUidFromServerId(serverId));
			
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
			throw new ProcessingEmailException(e);
		} catch (EmailViewPartsFetcherException e) {
			throw new ProcessingEmailException(e);
		}
	}
	
	private org.obm.icalendar.ICalendar fetchInvitation(UserDataRequest udr, Integer collectionId, Long uid) throws DaoException, EmailViewPartsFetcherException {
		
		final String collectionPath = mappingService.getCollectionPathFor(collectionId);
		return msEmailFetcher.fetchInvitation(udr, collectionId, collectionPath, uid);
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
			} catch (OpushLocatorException e) {
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
			mailboxService.purgeFolder(udr, devDbId, collectionPath, collectionId);
			expunge(udr, collectionPath);
			if (deleteSubFolder) {
				logger.warn("deleteSubFolder isn't implemented because opush doesn't yet manage folders");
			}	
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} catch (DaoException e) {
			throw new ProcessingEmailException(e);
		} catch (OpushLocatorException e) {
			throw new ProcessingEmailException(e);
		}
	}

	private void expunge(UserDataRequest udr, String collectionPath) {
		if (ExpungePolicy.NEVER != emailConfiguration.expungePolicy()) {
			mailboxService.expunge(udr, collectionPath);
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

	@Override
	public List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> itemIds, SyncCollectionOptions collectionOptions) 
			throws ProcessingEmailException {
		
		LinkedList<ItemChange> fetchs = new LinkedList<ItemChange>();
		Map<Integer, Collection<Long>> emailUids = getEmailUidByCollectionId(itemIds);
		for (Entry<Integer, Collection<Long>> entry : emailUids.entrySet()) {
			Collection<Long> uids = entry.getValue();
			try {
				fetchs.addAll(fetchItems(udr, collectionId, uids, collectionOptions.getBodyPreferences()));
			} catch (CollectionNotFoundException e) {
				logger.error("fetchItems : collection {} not found !", collectionId);
			}
		}
		return fetchs;
	}

	@Override
	public List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> itemIds, SyncCollectionOptions collectionOptions, 
				ItemSyncState previousItemSyncState) 
			throws ProcessingEmailException {

		Snapshot snapshot = snapshotService.getSnapshot(udr.getDevId(), previousItemSyncState.getSyncKey(), collectionId);
		if (snapshot == null) {
			throw new InvalidSyncKeyException(previousItemSyncState.getSyncKey());
		}
		if (!snapshot.containsAllIds(itemIds)) {
			throw new ItemNotFoundException();
		}
		return fetch(udr, collectionId, itemIds, collectionOptions);
	}
}
