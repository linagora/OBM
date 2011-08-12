package org.obm.push.mail;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.columba.ristretto.message.Address;
import org.minig.imap.IMAPException;
import org.minig.mime.QuotedPrintableDecoderInputStream;
import org.obm.configuration.ObmConfigurationService;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ActiveSyncException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.FolderTypeNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ObjectNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ServerErrorException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FiltrageInvitationDao;
import org.obm.push.tnefconverter.TNEFUtils;
import org.obm.push.utils.FileUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.calendar.AbstractEventSyncClient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MailBackend extends ObmSyncBackend {

	private final IEmailManager emailManager;
	private final FiltrageInvitationDao filtrageInvitationDao;

	@Inject
	private MailBackend(IEmailManager emailManager,
			ObmConfigurationService configurationService, CollectionDao collectionDao,
			FiltrageInvitationDao filtrageInvitationDao)
			throws ConfigurationException {
		
		super(configurationService, collectionDao);
		this.emailManager = emailManager;
		this.filtrageInvitationDao = filtrageInvitationDao;
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) throws DaoException {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		ret.add(genItemChange(bs, EmailConfiguration.IMAP_INBOX_NAME, FolderType.DEFAULT_INBOX_FOLDER));
		ret.add(genItemChange(bs,  EmailConfiguration.IMAP_DRAFTS_NAME, FolderType.DEFAULT_DRAFTS_FOLDERS));
		ret.add(genItemChange(bs,  EmailConfiguration.IMAP_SENT_NAME, FolderType.DEFAULT_SENT_EMAIL_FOLDER));
		ret.add(genItemChange(bs,  EmailConfiguration.IMAP_TRASH_NAME,FolderType.DEFAULT_DELETED_ITEMS_FOLDERS));
		return ret;
	}

	private ItemChange genItemChange(BackendSession bs, String imapFolder,
			FolderType type) throws DaoException {
		ItemChange ic = new ItemChange();
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " " + imapFolder);
		ic.setItemType(type);

		StringBuilder sb = new StringBuilder();
		sb.append("obm:\\\\");
		sb.append(bs.getLoginAtDomain());
		sb.append("\\email\\");
		sb.append(imapFolder);
		String s = buildPath(bs, imapFolder);
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevice(), s);
			serverId = getServerIdFor(collectionId);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getDevice(), sb.toString());
			ic.setIsNew(true);
		}

		ic.setServerId(serverId);
		return ic;
	}

	private String buildPath(BackendSession bs, String imapFolder) {
		StringBuilder sb = new StringBuilder();
		sb.append("obm:\\\\");
		sb.append(bs.getLoginAtDomain());
		sb.append("\\email\\");
		sb.append(imapFolder);
		return sb.toString();
	}

	public String getWasteBasketPath(BackendSession bs) {
		return buildPath(bs, "Trash");
	}

	public DataDelta getContentChanges(BackendSession bs, SyncState state, Integer collectionId, FilterType filter) 
			throws CollectionNotFoundException, DaoException {
		String collectionPath = getCollectionPathFor(collectionId);
		logger.info("Collection [ " + collectionPath + " ]");
		List<ItemChange> changes = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		Date lastSync = null;
		try {
			final Integer devDbId = bs.getDevice().getDatabaseId();
			
			final MailChanges mc = emailManager.getSync(bs, state, devDbId, collectionId, collectionPath, filter);
			
			changes = getChanges(bs, collectionId, collectionPath, mc.getUpdated());
			deletions.addAll(getDeletions(collectionId, mc.getRemoved()));
			lastSync = mc.getLastSync();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new DataDelta(changes, deletions, lastSync);
	}

	private List<ItemChange> getChanges(BackendSession bs, Integer collectionId, String collection, Set<Long> updated) {
		ImmutableList.Builder<ItemChange> itch = ImmutableList.builder();
		try {
			final List<MSEmail> msMails = emailManager.fetchMails(bs,	getCalendarClient(bs), collectionId, collection, updated);
			for (final MSEmail mail : msMails) {
				itch.add(getItemChange(collectionId, mail.getUid(), mail));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return itch.build();
	}
	
	private ItemChange getItemChange(Integer collectionId, Long uid, IApplicationData data) {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(collectionId, "" + uid));
		ic.setData(data);
		return ic;
	}
	
	public List<ItemChange> fetchItems(BackendSession bs, List<String> fetchIds) throws CollectionNotFoundException, DaoException {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		Map<Integer, Collection<Long>> emailUids = getEmailUidByCollectionId(fetchIds);
		for (Entry<Integer, Collection<Long>> entry : emailUids.entrySet()) {
			Integer collectionId = entry.getKey();
			Collection<Long> uids = entry.getValue();
			ret.addAll(fetchItems(bs, collectionId, uids));
		}
		return ret;
	}
	
	private Map<Integer, Collection<Long>> getEmailUidByCollectionId(List<String> fetchIds) {
		Map<Integer, Collection<Long>> ret = Maps.newHashMap();
		for(String serverId : fetchIds){
			Integer collectionId = getCollectionIdFor(serverId);
			Collection<Long> set = ret.get(collectionId);
			if(set == null){
				set = Sets.newHashSet();
				ret.put(collectionId, set);
			}
			set.add(getEmailUidFromServerId(serverId));
		}
		return ret;
	}

	public List<ItemChange> fetchItems(BackendSession bs, Integer collectionId, Collection<Long> uids) 
			throws CollectionNotFoundException, DaoException {
		
		final Builder<ItemChange> ret = ImmutableList.builder();
		final String collectionPath = getCollectionPathFor(collectionId);
		try {
			final List<MSEmail> emails = emailManager.fetchMails(bs, getCalendarClient(bs), collectionId, collectionPath, uids);
			for (final MSEmail email: emails) {
				ItemChange ic = new ItemChange();
				ic.setServerId(getServerIdFor(collectionId, String.valueOf(email.getUid())));
				ic.setData(email);
				ret.add(ic);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ret.build();
	}

	public void delete(BackendSession bs, String serverId, Boolean moveToTrash) {
		if (moveToTrash) {
			logger.info("move to trash serverId " + serverId);
		} else {
			logger.info("delete serverId " + serverId);
		}
		if (serverId != null) {
			try {
				final Long uid = getEmailUidFromServerId(serverId);
				final Integer collectionId = getCollectionIdFor(serverId);
				final String collectionName = getCollectionPathFor(collectionId);
				final Integer devDbId = bs.getDevice().getDatabaseId();

				if (moveToTrash) {
					String wasteBasketPath = getWasteBasketPath(bs);
					Integer wasteBasketId = getCollectionIdFor(bs.getDevice(),
							wasteBasketPath);
					emailManager.moveItem(bs, devDbId, collectionName,
							collectionId, wasteBasketPath, wasteBasketId, uid);
				} else {
					emailManager.delete(bs, devDbId, collectionName,
							collectionId, uid);
				}
				removeInvitationStatus(bs, collectionId, uid);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void removeInvitationStatus(BackendSession bs,
			Integer emailCollectionId, Long mailUid) throws DaoException {
		try {
			String calPath = getDefaultCalendarName(bs);
			Integer eventCollectionId = getCollectionIdFor(bs.getDevice(),
					calPath);
			filtrageInvitationDao.removeInvitationStatus(eventCollectionId,
					emailCollectionId, mailUid);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

	}

	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, String clientId, MSEmail data) throws CollectionNotFoundException, DaoException {
		
		String collectionPath = getCollectionPathFor(collectionId);
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collectionPath + ", " + serverId + ", " + clientId + ")");
		if (serverId != null) {
			Long mailUid = getEmailUidFromServerId(serverId);
			try {
				emailManager.updateReadFlag(bs, collectionPath, mailUid,
						data.isRead());
			} catch (IMAPException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	public String move(BackendSession bs, String srcFolder, String dstFolder,
			String messageId) throws ServerErrorException {
		logger.info("move(" + bs.getLoginAtDomain() + ", messageId "
				+ messageId + " from " + srcFolder + " to " + dstFolder + ")");
		try {
			final Long currentMailUid = getEmailUidFromServerId(messageId);
			final Integer srcFolderId = getCollectionIdFor(bs.getDevice(), srcFolder);
			final Integer dstFolderId = getCollectionIdFor(bs.getDevice(), dstFolder);
			final Integer devDbId = bs.getDevice().getDatabaseId();

			Long newUidMail = emailManager.moveItem(bs, devDbId, srcFolder,
					srcFolderId, dstFolder, dstFolderId, currentMailUid);
			removeInvitationStatus(bs, srcFolderId, currentMailUid);
			return dstFolderId + ":" + newUidMail;
		} catch (Exception e) {
			throw new ServerErrorException(e);
		}

	}

	private Integer getCollectionIdFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		Integer collectionId = 0;
		if (idx > 0) {
			collectionId = Integer.parseInt(serverId.substring(0, idx));
		}
		return collectionId;
	}

	public void sendEmail(BackendSession bs, InputStream mailContent,
			Boolean saveInSent) throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		try {
			SendEmailHandler handler = new SendEmailHandler(getUserEmail(bs));
			send(bs, mailContent, handler, saveInSent);
		} catch (SmtpInvalidRcptException se){
			throw se;
		} catch (SendEmailException e){
			throw e;
		}	catch (Throwable e) {
			throw new ProcessingEmailException(e);
		}
	}

	public void replyEmail(BackendSession bs, InputStream mailContent,
			Boolean saveInSent, Integer collectionId, String serverId)
			throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		String collectionPath = "";
		try {
			if (collectionId != null && collectionId > 0) {
				try {
					collectionPath = getCollectionPathFor(collectionId);
				} catch (Throwable e) {
				}
			}
			if (serverId == null || !serverId.isEmpty()) {
				collectionId = getCollectionIdFor(serverId);
				collectionPath = getCollectionPathFor(collectionId);

			}
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> mail = emailManager.fetchMails(bs,
					getCalendarClient(bs), collectionId, collectionPath, uids);

			if (mail.size() > 0) {
				//TODO uses headers References and In-Reply-To
				ReplyEmailHandler reh = new ReplyEmailHandler(getUserEmail(bs),
						mail.get(0));
				send(bs, mailContent, reh, saveInSent);
				try{
					emailManager.setAnsweredFlag(bs, collectionPath, uid);
				} catch (Throwable e) {
					logger.info("Can't set Answered Flag to mail["+uid+"]");
				}
			} else {
				SendEmailHandler handler = new SendEmailHandler(
						getUserEmail(bs));
				send(bs, mailContent, handler, saveInSent);
			}
		} catch (SmtpInvalidRcptException se){
			throw se;
		} catch (SendEmailException e){
			throw e;
		} catch (Throwable e) {
			throw new ProcessingEmailException(e);
		}
	}

	public void forwardEmail(BackendSession bs, InputStream mailContent,
			Boolean saveInSent, String collectionId, String serverId) throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		try {
			String collectionName = getCollectionPathFor(Integer
					.parseInt(collectionId));
			Long uid = getEmailUidFromServerId(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<InputStream> mail = emailManager.fetchMIMEMails(bs,
					getCalendarClient(bs), collectionName, uids);

			if (mail.size() > 0) {
				ForwardEmailHandler reh = new ForwardEmailHandler(
						getUserEmail(bs), mail.get(0));
				send(bs, mailContent, reh, saveInSent);
				try{
					emailManager.setAnsweredFlag(bs, collectionName, uid);
				} catch (Throwable e) {
					logger.info("Can't set Answered Flag to mail["+uid+"]");
				}
			} else {
				SendEmailHandler handler = new SendEmailHandler(
						getUserEmail(bs));
				send(bs, mailContent, handler, saveInSent);
			}
		} catch (SmtpInvalidRcptException se){
			throw se;
		} catch (SendEmailException e){
			throw e;
		} catch (Throwable e) {
			throw new ProcessingEmailException(e);
		}
	}

	private String getUserEmail(BackendSession bs) throws Exception {
		AbstractEventSyncClient cal = getCalendarClient(bs);
		AccessToken at = cal.login(bs.getLoginAtDomain(), bs.getPassword(),
				OBM_SYNC_ORIGIN);
		String from = "";
		try {
			from = cal.getUserEmail(at);
		} finally {
			cal.logout(at);
		}
		return from;
	}

	private void send(BackendSession bs, InputStream mailContent,
			SendEmailHandler handler, Boolean saveInSent)
			throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		InputStream emailData = null;
		try {
			MimeStreamParser parser = new MimeStreamParser();
			parser.setContentHandler(handler);
			parser.parse(mailContent);
			emailData = new BufferedInputStream(handler.getMessage());
			emailData.mark(emailData.available());
			
			Boolean isScheduleMeeting = !TNEFUtils.isScheduleMeetingRequest(emailData);
			emailData.reset();
			Address from = getAddress(handler.getFrom());
			if(!handler.isInvitation()  &&  isScheduleMeeting){
				emailManager.sendEmail(bs, from, handler.getTo(),
						handler.getCc(), handler.getCci(), emailData, saveInSent);	
			} else {
				logger.warn("OPUSH blocks email invitation sending by PDA. Now that obm-sync handle email sending on event creation/modification/deletion, we must filter mail from PDA for these actions.");
			}
		} catch (SmtpInvalidRcptException se){
			throw se;
		} catch (SendEmailException e){
			throw e;
		} catch (Throwable e) {
			throw new ProcessingEmailException(e);
		} finally {
			if(emailData != null){
				try{emailData.close();} catch (Throwable e) {}
			}
		}
	}

	private Address getAddress(String from) throws ProcessingEmailException {
		if(from == null || !from.contains("@")){
			throw new ProcessingEmailException(""+from+"is not a valid email");
		}
		return new Address(from);
	}

	public MSEmail getEmail(BackendSession bs, Integer collectionId,
			String serverId) throws ActiveSyncException, DaoException {
		String collectionName = getCollectionPathFor(collectionId);
		Long uid = getEmailUidFromServerId(serverId);
		Set<Long> uids = new HashSet<Long>();
		uids.add(uid);
		List<MSEmail> emails;
		try {
			emails = emailManager.fetchMails(bs, getCalendarClient(bs),
					collectionId, collectionName, uids);
			if (emails.size() > 0) {
				return emails.get(0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public MSAttachementData getAttachment(BackendSession bs,
			String attachmentId) throws ObjectNotFoundException {
		if (attachmentId != null && !attachmentId.isEmpty()) {
			Map<String, String> parsedAttId = AttachmentHelper
					.parseAttachmentId(attachmentId);
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

				String collectionName = getCollectionPathFor(Integer
						.parseInt(collectionId));
				InputStream is = emailManager.findAttachment(bs,
						collectionName, Long.parseLong(messageId),
						mimePartAddress);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				FileUtils.transfer(is, out, true);
				byte[] rawData = out.toByteArray();

				if ("QUOTED-PRINTABLE".equals(contentTransferEncoding)) {
					out = new ByteArrayOutputStream();
					InputStream in = new QuotedPrintableDecoderInputStream(
							new ByteArrayInputStream(rawData));
					FileUtils.transfer(in, out, true);
					rawData = out.toByteArray();
				} else if ("BASE64".equals(contentTransferEncoding)) {
					rawData = new Base64().decode(rawData);
				}

				return new MSAttachementData(contentType,
						new ByteArrayInputStream(rawData));
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
		throw new ObjectNotFoundException();
	}

	public void purgeFolder(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException {
		String wasteBasketPath = getWasteBasketPath(bs);
		if (!wasteBasketPath.equals(collectionPath)) {
			throw new NotAllowedException(
					"Only the Trash folder can be purged.");
		}
		try {

			final Integer devDbId = bs.getDevice().getDatabaseId();
			int collectionId = getCollectionIdFor(bs.getDevice(), collectionPath);
			emailManager.purgeFolder(bs, devDbId, collectionPath, collectionId);
			if (deleteSubFolder) {
				logger.warn("deleteSubFolder isn't implemented because opush doesn't yet manage folders");
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new NotAllowedException(e);
		}
	}

	public List<ItemChange> createItemsChangeToDeletedFromUidsInvitation(Integer collectionId, Collection<Long> uids) {
		return getDeletions(collectionId, uids);
	}
	
	public Long getEmailUidFromServerId(String serverId){
		return getItemIdFor(serverId).longValue();
	}
	
	
	/**
	 *  obm:\\adrien@test.tlse.lng\email\INBOX
	 *	obm:\\adrien@test.tlse.lng\email\Drafts
	 *	obm:\\adrien@test.tlse.lng\email\Sent
	 *	obm:\\adrien@test.tlse.lng\email\Trash
	 * @param collectionPath
	 * @return
	 * @throws FolderTypeNotFoundException 
	 */
	public FolderType getFolderType(String collectionPath) throws FolderTypeNotFoundException {
		if (collectionPath != null) {
			if(collectionPath.contains(EmailConfiguration.IMAP_INBOX_NAME)){
				return FolderType.DEFAULT_INBOX_FOLDER;
			} 
			if(collectionPath.contains(EmailConfiguration.IMAP_DRAFTS_NAME)){
				return FolderType.DEFAULT_DRAFTS_FOLDERS;
			}
			if(collectionPath.contains(EmailConfiguration.IMAP_SENT_NAME)){
				return FolderType.DEFAULT_SENT_EMAIL_FOLDER;
			}
			if(collectionPath.contains(EmailConfiguration.IMAP_TRASH_NAME)){
				return FolderType.DEFAULT_DELETED_ITEMS_FOLDERS;
			}
		}
		throw new FolderTypeNotFoundException("The collection's path["+collectionPath+"] is invalid");
	}
	
}
