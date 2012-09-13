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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.minig.imap.Address;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.StoreClient;
import org.minig.imap.UIDEnvelope;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeMessage;
import org.obm.mail.conversation.MailBody;
import org.obm.mail.conversation.MailMessage;
import org.obm.mail.conversation.MessageId;
import org.obm.mail.imap.StoreException;
import org.obm.mail.message.MailMessageAttachment;
import org.obm.mail.message.MessageLoader;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBody;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ConversionException;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.EventParsingException;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

/**
 * Creates a {@link MailMessage} from a {@link MessageId}.
 */
public class MailMessageLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(MailMessageLoader.class);
	
	private final List<String> htmlMimeSubtypePriority;
	private final StoreClient storeClient;
	private final EventService eventService;
	
	public MailMessageLoader(StoreClient store, EventService eventService) {
		this.storeClient = store;
		this.eventService = eventService;
		this.htmlMimeSubtypePriority = Arrays.asList("html", "plain");
	}

	public MSEmail fetch(final Integer collectionId, final long messageId, final UserDataRequest udr) {
		MSEmail msEmail = null;
		try {
			
			final List<Long> messageIdAsList = Arrays.asList(messageId);
			final Collection<UIDEnvelope> envelopes = storeClient.uidFetchEnvelope(messageIdAsList);
			if (envelopes.size() != 1 || envelopes.iterator().next() == null) {
				return null;
			}
			
			final MimeMessage mimeMessage = getFirstMimeMessage(messageIdAsList);
			if (mimeMessage != null) {
				final MessageFetcherImpl messageFetcherImpl = new MessageFetcherImpl(storeClient);
				final MessageLoader helper = new MessageLoader(messageFetcherImpl, htmlMimeSubtypePriority, false, mimeMessage);
				final MailMessage message = helper.fetch();
				
				msEmail = convertMailMessageToMSEmail(message, udr, mimeMessage.getUid(), collectionId, messageId);
				setMsEmailFlags(msEmail, messageIdAsList);
				fetchMimeData(msEmail, messageId);
				msEmail.setSmtpId(envelopes.iterator().next().getEnvelope().getMessageId());
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		} catch (StoreException e) {
			logger.error(e.getMessage(), e);
			return null;
		} catch (ConversionException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		return msEmail;
	}

	private MimeMessage getFirstMimeMessage(final List<Long> messageIdAsList) {
		final Collection<MimeMessage> mts = storeClient.uidFetchBodyStructure(messageIdAsList);
		final MimeMessage tree = Iterables.getFirst(mts, null);
		return tree;
	}
	
	private void setMsEmailFlags(final MSEmail msEmail, final List<Long> messageIdAsList) {
		final Collection<FlagsList> fl = storeClient.uidFetchFlags(messageIdAsList);
		if (!fl.isEmpty()) {
			final FlagsList fl0 = fl.iterator().next();
			msEmail.setRead(fl0.contains(Flag.SEEN));
			msEmail.setStarred(fl0.contains(Flag.FLAGGED));
			msEmail.setAnswered(fl0.contains(Flag.ANSWERED));
		}
	}
	
	private void fetchMimeData(final MSEmail mm, final long messageId) {
		final InputStream mimeData = storeClient.uidFetchMessage(messageId);
		mm.setMimeData(mimeData);
	}

	private MSEmail convertMailMessageToMSEmail(final MailMessage mailMessage, final UserDataRequest udr, 
			final long uid, final Integer collectionId, long messageId) throws ConversionException {
		
		final MSEmail msEmail = new MSEmail();
		msEmail.setSubject(mailMessage.getSubject());
		msEmail.setBody(convertMailBodyToMSEmailBody(mailMessage.getBody()));
		msEmail.setFrom(convertAdressToMSAddress(mailMessage.getSender()));
		msEmail.setDate(mailMessage.getDate());
		msEmail.setHeaders(mailMessage.getHeaders());
		msEmail.setAttachements(convertMailMessageAttachmentToMSAttachment(mailMessage, uid, collectionId, messageId));	
		msEmail.setUid(mailMessage.getUid());
		
		msEmail.setTo(convertAllAdressToMSAddress(mailMessage.getTo()));
		msEmail.setBcc(convertAllAdressToMSAddress(mailMessage.getBcc()));
		msEmail.setCc(convertAllAdressToMSAddress(mailMessage.getCc()));
		
		if (mailMessage.getInvitation() != null) {
			setInvitation(msEmail, udr, mailMessage.getInvitation(), uid);
		}
		
		return msEmail;
	}

	private void setInvitation(final MSEmail msEmail, final UserDataRequest udr, final IMimePart mimePart, final long uid)
			throws ConversionException {
		
		try {	
			final InputStream inputStreamInvitation = extractInputStreamInvitation(mimePart, uid);
			final MSEvent event = getInvitation(udr, inputStreamInvitation);
			if (mimePart.isInvitation()) {
				msEmail.setInvitation(event, MSMessageClass.SCHEDULE_MEETING_REQUEST);
			} else if (mimePart.isCancelInvitation()) {
				msEmail.setInvitation(event, MSMessageClass.SCHEDULE_MEETING_CANCELED);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private InputStream extractInputStreamInvitation(final IMimePart mp, final long uid) throws IOException {
		final InputStream part = storeClient.uidFetchPart(uid, mp.getAddress().getAddress());
		byte[] data = extractPartData(mp, part);
		if (data != null) {
			return new ByteArrayInputStream(data);
		}
		return null;
	}
	
	private MSEvent getInvitation(UserDataRequest udr, InputStream invitation) throws IOException, ConversionException {
		final String ics = FileUtils.streamString(invitation, true);
		if (ics != null && !"".equals(ics) && ics.startsWith("BEGIN")) {
			try {
				return eventService.parseEventFromICalendar(udr, ics);
			} catch (EventParsingException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	private Set<MSAttachement> convertMailMessageAttachmentToMSAttachment(MailMessage mailMessage, long uid, 
			Integer collectionId, long messageId) {
		
		Set<MSAttachement> msAttachements = new HashSet<MSAttachement>();
		for (MailMessageAttachment mailMessageAttachment: mailMessage.getAttachments()) {			
			
			IMimePart part = mailMessageAttachment.getPart();
			if (part != null && !part.isInvitation()) {
				MSAttachement extractAttachments = extractAttachmentData(part, uid, collectionId, messageId);
				if (isNotICSAttachments(extractAttachments)) {
					msAttachements.add(extractAttachments);
				}
			}
			
		}
		return msAttachements;
	}

	private boolean isNotICSAttachments(MSAttachement msAttachment) {
		if (msAttachment != null) {
			String displayName = msAttachment.getDisplayName();
			if (displayName != null && !displayName.endsWith(".ics")) {
				return true;
			}
		}
		return false;
	}
	
	private MSAddress convertAdressToMSAddress(Address adress) {
		if (adress != null) {
			return new MSAddress(adress.getDisplayName(), adress.getMail());
		}
		return null;
	}
	
	private List<MSAddress> convertAllAdressToMSAddress(List<Address> adresses) {
		List<MSAddress> msAdresses = new ArrayList<MSAddress>();
		if (adresses != null) {
			for (Address adress: adresses) {
				msAdresses.add(convertAdressToMSAddress(adress));
			}
		}
		return msAdresses;
	}

	private MSEmailBody convertMailBodyToMSEmailBody(final MailBody body) {
		final MSEmailBody emailBody = new MSEmailBody();
		for (final String format: body.availableFormats()) {
			final String value = body.getValue(format);
			emailBody.addConverted(mimeTypeToBodyType(format), value);
		}		
		return emailBody;
	}
	
	private MSEmailBodyType mimeTypeToBodyType(String mimeType) {
		if ("text/rtf".equals(mimeType)) {
			return MSEmailBodyType.RTF;
		} else if ("text/html".equals(mimeType)) {
			return MSEmailBodyType.HTML;
		} else {
			return MSEmailBodyType.PlainText;
		}
	}
	
	private byte[] extractPartData(final IMimePart mp, final InputStream bodyText) throws IOException {
		return ByteStreams.toByteArray(mp.decodeMimeStream(bodyText));
	}
	
	private MSAttachement extractAttachmentData(final IMimePart mp, final long uid, 
			final Integer collectionId, final long messageId) {
		try {
			
			if (mp.getName() != null || mp.getContentId() != null) {
				final InputStream part = storeClient.uidFetchPart(uid, mp.getAddress().getAddress());
				byte[] data = extractPartData(mp, part);
				
				final String id = AttachmentHelper.getAttachmentId(collectionId.toString(), String.valueOf(messageId), 
						mp.getAddress().getAddress(), mp.getFullMimeType(), mp.getContentTransfertEncoding());
				
				String name = mp.getName();
				if (name == null) {
					name = mp.getContentId();
				}
				
				MSAttachement att = new MSAttachement();
				att.setFileReference(id);
				att.setMethod(MethodAttachment.NormalAttachment);
				att.setEstimatedDataSize(data.length);
				att.setDisplayName(name);
				return att;
			}			
			
		} catch (Exception e) {
			logger.error("Error extract attachment["+uid+"]");
		}
		return null;
	}
	
}
