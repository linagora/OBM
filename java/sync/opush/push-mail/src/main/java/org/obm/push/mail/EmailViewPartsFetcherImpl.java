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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.property.Organizer;

import org.apache.commons.io.IOUtils;
import org.obm.icalendar.ICalendar;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.EmailViewBuildException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.exception.MailException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.conversation.EmailView.Builder;
import org.obm.push.mail.conversation.EmailViewAttachment;
import org.obm.push.mail.conversation.EmailViewInvitationType;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;
import org.obm.push.mail.transformer.Transformer;
import org.obm.push.mail.transformer.Transformer.TransformersFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

public class EmailViewPartsFetcherImpl implements EmailViewPartsFetcher {

	private static final Logger logger = LoggerFactory.getLogger(EmailViewPartsFetcherImpl.class);

	private final TransformersFactory transformersFactory;
	private final MailboxService mailboxService;
	private final UserDataRequest udr;
	private final String collectionPath;
	private final Integer collectionId;
	private final List<BodyPreference> bodyPreferences;

	public EmailViewPartsFetcherImpl(TransformersFactory transformersFactory, MailboxService mailboxService, 
			List<BodyPreference> bodyPreferences, UserDataRequest udr, String collectionPath, Integer collectionId) {
		
		this.transformersFactory = transformersFactory;
		this.mailboxService = mailboxService;
		this.udr = udr;
		this.collectionPath = collectionPath;
		this.collectionId = collectionId;
		this.bodyPreferences = bodyPreferences;
	}

	@Override
	public EmailView fetch(long uid, BodyPreferencePolicy bodyPreferencePolicy) throws EmailViewPartsFetcherException, EmailViewBuildException {
		try {
			EmailMetadata emailViewResponse = mailboxService.fetchEmailMetadata(udr, collectionPath, uid);
			Builder emailViewBuilder = EmailView.builder()
					.uid(uid)
					.flags(emailViewResponse.getFlags())
					.envelope(emailViewResponse.getEnvelope());
			
			MimeMessage mimeMessage = emailViewResponse.getMimeMessage();
			FetchInstruction fetchInstruction = getFetchInstruction(bodyPreferencePolicy, mimeMessage);
			if (fetchInstruction != null) {
				fetchBody(emailViewBuilder, fetchInstruction, uid);
				fetchAttachments(emailViewBuilder, fetchInstruction, uid);
			}
			fetchInvitation(emailViewBuilder, mimeMessage, uid, emailViewResponse);
			
			return emailViewBuilder.build();
		} catch (MailException e) {
			throw new EmailViewPartsFetcherException(e);
		} catch (IOException e) {
			throw new EmailViewPartsFetcherException(e);
		} catch (ParserException e) {
			throw new EmailViewPartsFetcherException(e);
		}
	}

	@Override
	public ICalendar fetchInvitation(long uid) throws EmailViewPartsFetcherException {
		try {
			EmailMetadata emailViewResponse = mailboxService.fetchEmailMetadata(udr, collectionPath, uid);
			
			MimeMessage mimeMessage = emailViewResponse.getMimeMessage();
			MimePart parentMessage = mimeMessage.findRootMimePartInTree();
			for (MimePart mp : parentMessage.listLeaves(true, true)) {
				if (mp.isInvitation()) {
					return fetchICalendar(mp, uid, emailViewResponse);
				}
			}
			return null;
		} catch (MailException e) {
			throw new EmailViewPartsFetcherException(e);
		} catch (IOException e) {
			throw new EmailViewPartsFetcherException(e);
		} catch (ParserException e) {
			throw new EmailViewPartsFetcherException(e);
		} catch (EmailViewBuildException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private FetchInstruction getFetchInstruction(BodyPreferencePolicy bodyPreferencePolicy, MimeMessage mimeMessage) {
		return new MimePartSelector().select(bodyPreferencePolicy, bodyPreferences, mimeMessage);
	}

	private void fetchBody(Builder emailViewBuilder, FetchInstruction fetchInstruction, 
			long uid) throws MailException, IOException, EmailViewPartsFetcherException {
		
		InputStream bodyData = fetchBodyData(fetchInstruction, uid);
		
		Transformer transformedMail = transformersFactory.create(fetchInstruction);
		
		MimePart mimePart = fetchInstruction.getMimePart();
		emailViewBuilder.bodyMimePartData(transformedMail.transform(mimePart.decodeMimeStream(bodyData), transformationCharset(mimePart)));
		emailViewBuilder.bodyType(transformedMail.targetType());
		emailViewBuilder.estimatedDataSize(fetchInstruction.getMimePart().getSize());
		emailViewBuilder.truncated(fetchInstruction.mustTruncate());
		emailViewBuilder.charset(fetchInstruction.getMimePart().getCharset());
	}

	private Charset transformationCharset(MimePart mimePart) {
		try {
			String charset = mimePart.getCharset();
			if (charset != null) {
				return Charset.forName(charset);
			}
		} catch (IllegalCharsetNameException e) {
			logger.info("mail with illegal charset : " + mimePart.getCharset());
		} catch (UnsupportedCharsetException e) {
			logger.info("mail with unsupported charset : " + mimePart.getCharset());
		}
		return Charsets.UTF_8;
	}

	@VisibleForTesting InputStream fetchBodyData(FetchInstruction fetchInstruction, long uid)
			throws MailException, EmailViewPartsFetcherException {
		
		InputStream bodyData = null;
		try {
			if (fetchInstruction.hasMimePartAddressDefined()) {
				MimeAddress address = fetchInstruction.getMimePart().getAddress();
				Integer truncation = fetchInstruction.getTruncation();
				if (truncation != null) {
					bodyData = mailboxService.fetchPartialMimePartStream(udr, collectionPath, uid, address, truncation);
				} else {
					bodyData = mailboxService.fetchMimePartStream(udr, collectionPath, uid, address);
				}
				if (bodyData != null) {
					return new ByteArrayInputStream(ByteStreams.toByteArray(bodyData));
				}
				throw new EmailViewPartsFetcherException(String.format(
						"Cannot fetch bodyData for collectionPath:%s, uid:%d, address:%s, truncation:%b",
						collectionPath, uid, address, truncation!=null));
			} else {
				return mailboxService.fetchMailStream(udr, collectionPath, uid);
			}
		} catch (IOException e) {
			throw new MailException(e);
		} finally {
			IOUtils.closeQuietly(bodyData);
		}
		
	}
	
	@VisibleForTesting void fetchAttachments(Builder emailViewBuilder, FetchInstruction fetchInstruction, long uid) {
		List<EmailViewAttachment> attachments = Lists.newArrayList();
		MimePart parentMessage = fetchInstruction.getMimePart().findRootMimePartInTree();
		Collection<MimePart> children = parentMessage.listLeaves(true, true);
		if (isCalendarOperation(children)) {
			return;
		}
		int attachmentId = 0;
		for (MimePart mp : parentMessage.listLeaves(true, true)) {
			if (mp.isAttachment()) {
				EmailViewAttachment emailViewAttachment = extractEmailViewAttachment(mp, attachmentId++, uid);
				if (emailViewAttachment != null) {
					attachments.add(emailViewAttachment);
				}
			}
		}
		emailViewBuilder.attachments(attachments);
	}
	
	private boolean isCalendarOperation(Collection<MimePart> children) {
		return Iterables.any(children, new Predicate<MimePart>() {
				@Override
				public boolean apply(MimePart input) {
					return input.containsCalendarMethod();
				}
			});
	}

	private EmailViewAttachment extractEmailViewAttachment(MimePart mp, int attachmentId, long uid) {
		String id = "at_" + uid + "_" + attachmentId;
		String fileReference = AttachmentHelper.getAttachmentId(String.valueOf(collectionId), String.valueOf(uid), 
				mp.getAddress().getAddress(), mp.getFullMimeType(), mp.getContentTransfertEncoding());
		
		Optional<String> displayName = selectDisplayName(mp, attachmentId);
		if (displayName.isPresent()) {
			return EmailViewAttachment.builder()
					.id(id)
					.displayName(displayName.get())
					.fileReference(fileReference)
					.size(mp.getSize())
					.contentType(mp.getContentType())
					.contentId(mp.getContentId())
					.contentLocation(mp.getContentLocation())
					.inline(mp.isInline())
					.build();
		}
		return null;
	}

	@VisibleForTesting Optional<String> selectDisplayName(MimePart attachment, int attachmentId) {
		String partName = attachment.getName();
		if (!Strings.isNullOrEmpty(partName)) {
			return Optional.of(partName);
		}
		String contentId = attachment.getContentId();
		if (Strings.isNullOrEmpty(contentId)) {
			return Optional.absent();
		}
		return Optional.of(String.format("ATT%05d%s", attachmentId, Strings.nullToEmpty(attachment.getAttachmentExtension())));
	}
	
	private void fetchInvitation(Builder emailViewBuilder, MimeMessage mimeMessage, long uid, EmailMetadata emailMetadata) 
			throws MailException, IOException, ParserException {
		
		MimePart parentMessage = mimeMessage.findRootMimePartInTree();
		for (MimePart mp : parentMessage.listLeaves(true, true)) {
			if (mp.isInvitation()) {
				fetchICalendar(emailViewBuilder, mp, uid, emailMetadata);
				emailViewBuilder.invitationType(EmailViewInvitationType.REQUEST);
			}
			if (mp.isCancelInvitation()) {
				fetchICalendar(emailViewBuilder, mp, uid, emailMetadata);
				emailViewBuilder.invitationType(EmailViewInvitationType.CANCELED);
			}
			if (mp.isReplyInvitation()) {
				fetchICalendar(emailViewBuilder, mp, uid, emailMetadata);
				emailViewBuilder.invitationType(EmailViewInvitationType.REPLY);
			}
		}
	}

	private void fetchICalendar(Builder emailViewBuilder, MimePart mp, long uid, EmailMetadata emailMetadata)
			throws MailException, IOException, ParserException {

		emailViewBuilder.iCalendar(fetchICalendar(mp, uid, emailMetadata));
	}

	private ICalendar fetchICalendar(MimePart mp, long uid, EmailMetadata emailMetadata)
			throws MailException, IOException, ParserException {

		InputStream inputStream = mailboxService.findAttachment(udr, collectionPath, uid, mp.getAddress());
		return ICalendar.builder()
			.organizerFallback(organizerFallback(emailMetadata))
			.inputStream(mp.decodeMimeStream(inputStream))
			.build();
	}

	@VisibleForTesting Organizer organizerFallback(EmailMetadata emailMetadata) {
		List<Address> fromList = emailMetadata.getEnvelope().getFrom();
		if (!fromList.isEmpty()) {
			return addressToOrganizer(Iterables.getLast(fromList));
		}
		return null;
	}

	@VisibleForTesting Organizer addressToOrganizer(Address from) {
		if (from.isDefined()) {
			try {
				return new Organizer(from.asICSAttendee());
			} catch (URISyntaxException e) {
				logger.error("Invalid From email syntax : " + from.getMail(), e);
			}
		}
		return null;
	}
}
