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
package org.obm.push.java.mail;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

import org.obm.push.bean.EmailHeader;
import org.obm.push.bean.EmailHeaders;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.OpushImapFolder;
import org.obm.push.mail.imap.StreamedLiteral;
import org.obm.push.mail.imap.command.IMAPCommand;
import org.obm.push.mail.imap.command.UIDCopyMessage;
import org.obm.push.mail.mime.MimeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.ProtocolCommand;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.protocol.IMAPProtocol;

public class OpushImapFolderImpl implements OpushImapFolder {

	private final static Logger logger = LoggerFactory.getLogger(OpushImapFolderImpl.class);
	
	private final MessageInputStreamProvider imapResourceProvider;
	private final ImapMailBoxUtils imapMailBoxUtils;
	private final IMAPFolder folder;
	
	public OpushImapFolderImpl(ImapMailBoxUtils imapMailBoxUtils, MessageInputStreamProvider imapResourceProvider, IMAPFolder folder) {
		this.imapResourceProvider = imapResourceProvider;
		this.imapMailBoxUtils = imapMailBoxUtils;
		this.folder = folder;
	}

	@Override
	public void noop() throws MessagingException {
		folder.doCommand(new ProtocolCommand() {
			@Override
			public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
				protocol.noop();
				return true;
			}
		});
	}
	
	@Override
	public void appendMessageStream(final StreamedLiteral literal, final Flags flags, final Date messageDate) throws MessagingException {
		folder.doCommand(new ProtocolCommand() {
			
			@Override
			public Object doCommand(IMAPProtocol p)
				throws ProtocolException {
				p.append(folder.getFullName(), flags, messageDate, literal);
				return null;
			}
		});
	}

	@Override
	public void appendMessage(Message message) throws MessagingException {
		folder.appendMessages(new Message[]{message});
	}

	@Override
	public void deleteMessage(Message messageToDelete) throws MessagingException {
		folder.setFlags(new Message[]{messageToDelete}, new Flags(Flags.Flag.DELETED), true);
		expunge();
	}

	@Override
	public void expunge() throws MessagingException {
		folder.expunge();
	}

	@Override
	public String getFullName() {
		return folder.getFullName();
	}

	public boolean create(int type) throws MessagingException {
		return folder.create(type);
	}

	@Override
	public Folder[] list(String pattern) throws MessagingException {
		return folder.list(pattern);
	}

	@Override
	public Folder[] listSubscribed(String pattern) throws MessagingException {
		return folder.listSubscribed(pattern);
	}

	@Override
	public void open(int mode) throws MessagingException {
		folder.open(mode);
	}

	@Override
	public IMAPMessage getMessageByUID(long messageUid) throws MessagingException, ImapMessageNotFoundException {
		Message messageFound = folder.getMessageByUID(messageUid);

		if (messageFound != null) {
			return (IMAPMessage) messageFound;
		}
		throw new ImapMessageNotFoundException("No message correspond to given UID, UID:" + String.valueOf(messageUid));
	}

	private <T> T doCommand(IMAPCommand<T> command) throws MessagingException {
		return (T) folder.doCommand(command);
	}

	@Override
	public long copyMessageThenGetNewUID(String folderDst, long messageUid) throws MessagingException {
		return doCommand(new UIDCopyMessage(folderDst, messageUid));
	}

	@Override
	public Map<Long, IMAPMessage> fetch(MessageSet messages, FetchProfile fetchProfile) throws MessagingException, ImapMessageNotFoundException {
		ImmutableMap.Builder<Long, IMAPMessage> result = ImmutableMap.builder();
		for (long uid: messages) {
			IMAPMessage message = getMessageByUID(uid);
			folder.fetch(new Message[]{message}, fetchProfile);
			result.put(uid, message);
		}
		return result.build();
	}
	
	public Message fetchHeaders(long messageUid, EmailHeaders headersToFetch) throws MessagingException, ImapMessageNotFoundException {
		FetchProfile fetchProfile = new FetchProfile();
		for (EmailHeader header: headersToFetch) {
			fetchProfile.add(header.getHeader());
		}
		return Iterables.getOnlyElement(fetch(MessageSet.singleton(messageUid), fetchProfile).values());
	}

	@Override
	public Map<Long, IMAPMessage> fetchEnvelope(MessageSet messages) throws MessagingException, ImapMessageNotFoundException {
		return fetch(messages, fetchProfile(fetchEnvelopeProfileItems));
	}

	@Override
	public Map<Long, IMAPMessage> fetchEmailView(MessageSet messages) throws MessagingException, ImapMessageNotFoundException {
		return fetch(messages, fetchProfile(Iterables.concat(
				fetchBodyStructureProfileItems,
				fetchEnvelopeProfileItems,
				fetchFlagsProfileItems
			)));
	}

	@Override
	public Map<Long, FlagsList> uidFetchFlags(MessageSet messages) throws MessagingException, ImapMessageNotFoundException {
		ImmutableMap.Builder<Long, FlagsList> response = ImmutableMap.builder();
		for (long uid: messages) {
			IMAPMessage messageToFetch = getMessageByUID(uid);
			folder.fetch(new Message[]{messageToFetch}, fetchProfile(fetchFlagsProfileItems));
			response.put(uid, flagsList(messageToFetch.getFlags()));
		}
		return response.build();
	}

	private FlagsList flagsList(Flags flagsToConvert) {
		Set<Flag> convertedFlags = imapMailBoxUtils.buildFlagToIMAPMessageFlags(flagsToConvert);
		return new FlagsList(convertedFlags);
	}

	@Override
	public InputStream uidFetchMessage(long messageUid) throws MessagingException, ImapMessageNotFoundException {
		return peekAtMessageStream(messageUid);
	}

	@Override
	public InputStream uidFetchPart(long messageUid, MimeAddress mimePartAddress) throws MessagingException, ImapMessageNotFoundException {
		return peekAtMessageStream(messageUid, mimePartAddress);
	}

	private InputStream peekAtMessageStream(long messageUid) throws MessagingException, ImapMessageNotFoundException {
		return peekAtMessageStream(messageUid, null);
	}

	private InputStream peekAtMessageStream(long messageUid, MimeAddress mimePartAddress) throws MessagingException, ImapMessageNotFoundException {
		IMAPMessage messageToFetch = getMessageByUID(messageUid);
		return imapResourceProvider.createMessageInputStream(messageToFetch, mimePartAddress);
	}

	@Override
	public InputStream getMessageInputStream(long messageUID) throws MessagingException, ImapMessageNotFoundException {
		return peekAtMessageStream(messageUID, new MimeAddress(null));
	}

	@Override
	public Map<Long, IMAPMessage> fetchFast(MessageSet messages) throws MessagingException {
		try {
			return fetch(messages, fetchProfile(Iterables.concat(
					fetchEnvelopeProfileItems,
					fetchFlagsProfileItems,
					fetchSizeProfileItems)));
		} catch (ImapMessageNotFoundException e) {
			logger.debug("Message {} not found", messages);
		}
		return ImmutableMap.<Long, IMAPMessage>of();
	}
	
	@Override
	public MessageSet uidSearch(SearchQuery query) throws MessagingException {
		final SearchTerm searchTerm = toSearchTerm(query);
		return (MessageSet) folder.doCommand(new ProtocolCommand() {
			@Override
			public Object doCommand(IMAPProtocol protocol)
					throws ProtocolException {
				try {
					int[] array = protocol.search(searchTerm);
					MessageSet.Builder builder = MessageSet.builder();
					for (int i: array) {
						builder.add(protocol.fetchUID(i).uid);
					}
					return builder.build();
				} catch (SearchException e) {
					throw new ProtocolException(e.getMessage(), e);
				}
			}
		});
	}
	
	@VisibleForTesting SearchTerm toSearchTerm(SearchQuery query) {
		SearchTerm dateSearchTerm = toDateSearchTerm(query);
		if (dateSearchTerm != null) {
			return new AndTerm(buildNotDeleted(), dateSearchTerm);
		} else {
			return buildNotDeleted();
		}
	}

	private SearchTerm toDateSearchTerm(SearchQuery query) {
		Date before = query.getBefore();
		Date after = query.getAfter();
		if (after != null) {
			if (before != null) {
				return new AndTerm(buildBeforeTerm(before), buildAfterTerm(after));
			} else {
				return buildAfterTerm(after);
			}
		} else {
			if (before != null) {
				return buildBeforeTerm(before);
			} else {
				return null;
			}
		}
	}

	private NotTerm buildAfterTerm(Date after) {
		return new NotTerm(buildBeforeTerm(after));
	}

	private ReceivedDateTerm buildBeforeTerm(Date before) {
		return new ReceivedDateTerm(ComparisonTerm.LT, before);
	}
	
	private SearchTerm buildNotDeleted() {
		return new NotTerm(new FlagTerm(new Flags(Flags.Flag.DELETED), true));
	}

	@Override
	public Map<Long, IMAPMessage> fetchBodyStructure(MessageSet messages) throws MessagingException, ImapMessageNotFoundException {
		return fetch(messages, fetchProfile(fetchBodyStructureProfileItems));
	}

	@Override
	public InputStream uidFetchPart(long messageUid, MimeAddress mimePartAddress, Integer limit) 
			throws MessagingException, ImapMessageNotFoundException {
		
		IMAPMessage messageToFetch = getMessageByUID(messageUid);
		return imapResourceProvider.createMessageInputStream(messageToFetch, mimePartAddress, limit);
	}
	
	public void subscribe() throws MessagingException {
		folder.setSubscribed(true);
	}

	public void unsubscribe() throws MessagingException {
		folder.setSubscribed(false);
	}

	@Override
	public boolean isSubscribed() {
		return folder.isSubscribed();
	}
	
	@Override
	public void close() throws MessagingException {
		if (folder.isOpen()) {
			folder.close(false);
		}
	}
	
	@Override
	public boolean isOpen() throws MessagingException {
		return folder.isOpen();
	}

	@Override
	public long uidNext(String mailbox) throws MessagingException {
		return folder.getUIDNext();
	}

	@Override
	public long uidValidity(String mailbox) throws MessagingException {
		return folder.getUIDValidity();
	}

	private Iterable<FetchProfile.Item> fetchBodyStructureProfileItems = ImmutableSet.of(
			FetchProfile.Item.CONTENT_INFO,
			IMAPFolder.FetchProfileItem.SIZE	
	);
	
	private Iterable<FetchProfile.Item> fetchFlagsProfileItems = ImmutableSet.of(
			FetchProfile.Item.FLAGS
	);
	
	private Iterable<FetchProfile.Item> fetchEnvelopeProfileItems = ImmutableSet.of(
			FetchProfile.Item.ENVELOPE
	);
	
	private Iterable<? extends FetchProfile.Item> fetchSizeProfileItems = ImmutableSet.of(
			IMAPFolder.FetchProfileItem.SIZE
	);
	
	private FetchProfile fetchProfile(Iterable<FetchProfile.Item> items) {
		FetchProfile fetchProfile = new FetchProfile();
		for (FetchProfile.Item item : items) {
			fetchProfile.add(item);
		}
		return fetchProfile;
	}
}
