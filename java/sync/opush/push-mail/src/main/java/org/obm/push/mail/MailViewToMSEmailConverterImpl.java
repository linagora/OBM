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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Set;

import org.obm.icalendar.ICalendar;
import org.obm.icalendar.ical4jwrapper.ICalendarEvent;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.conversation.EmailViewAttachment;
import org.obm.push.mail.conversation.EmailViewInvitationType;
import org.obm.push.mail.mime.ContentType;
import org.obm.push.service.EventService;
import org.obm.push.utils.SerializableInputStream;
import org.obm.sync.calendar.EventExtId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MailViewToMSEmailConverterImpl implements MailViewToMSEmailConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(MailViewToMSEmailConverterImpl.class);
	private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;
	private static final String CONTENT_TYPE_RFC_822 = "message/rfc822";

	private final MSEmailHeaderConverter emailHeaderConverter;
	private final EventService eventService;

	@Inject
	@VisibleForTesting MailViewToMSEmailConverterImpl(MSEmailHeaderConverter emailHeaderConverter,
			EventService eventService) {
		
		this.emailHeaderConverter = emailHeaderConverter;
		this.eventService = eventService;
	}
	
	@Override
	public UidMSEmail convert(EmailView emailView, UserDataRequest userDataRequest) throws DaoException {
		MSEmail.MSEmailBuilder msEmailBuilder = MSEmail.builder();
		
		fillFlags(msEmailBuilder, emailView);
		msEmailBuilder.header(convertHeader(emailView));
		msEmailBuilder.body(convertBody(emailView));
		msEmailBuilder.attachements(convertAttachment(emailView));
		
		MSMeetingRequest msMeetingRequest = convertICalendar(emailView);
		msEmailBuilder.meetingRequest(fillMSEventUid(msMeetingRequest, userDataRequest));
		msEmailBuilder.messageClass(convertInvitationType(emailView));
		msEmailBuilder.subject(convertSubject(emailView));
		return UidMSEmail.uidBuilder()
				.email(msEmailBuilder.build())
				.uid(emailView.getUid())
				.build();
	}

	private MSMeetingRequest fillMSEventUid(MSMeetingRequest msMeetingRequest, UserDataRequest userDataRequest) throws DaoException {
		if (msMeetingRequest != null) {
			EventExtId eventExtId = new EventExtId(msMeetingRequest.getMSEventExtId().serializeToString());
			MSEventUid msEventUid = eventService.getMSEventUidFor(eventExtId, userDataRequest.getDevice());
			return MSMeetingRequest.builder()
				.copy(msMeetingRequest)
				.msEventUid(msEventUid).build();
		} else {
			return null;
		}
	}

	private String convertSubject(EmailView emailView) {
		ICalendar iCalendar = emailView.getICalendar();
		if (iCalendar != null && iCalendar.hasEvent()) {
			String iCalendarSummary = iCalendar.getICalendarEvent().summary();
			return Strings.emptyToNull(iCalendarSummary);
		}
		return emailView.getSubject();
	}

	private void fillFlags(MSEmail.MSEmailBuilder msEmailBuilder, EmailView emailView) {
		msEmailBuilder.answered(emailView.hasFlag(Flag.ANSWERED));
		msEmailBuilder.read(emailView.hasFlag(Flag.SEEN));
		msEmailBuilder.starred(emailView.hasFlag(Flag.FLAGGED));
	}
	
	private MSEmailHeader convertHeader(EmailView emailView) {
		return emailHeaderConverter.convertToMSEmailHeader(emailView.getEnvelope());
	}

	private MSEmailBody convertBody(EmailView emailView) {
		return MSEmailBody.builder()
				.mimeData(new SerializableInputStream(emailView.getBodyMimePartData()))
				.estimatedDataSize(emailView.getEstimatedDataSize())
				.truncated(emailView.isTruncated())
				.charset(chooseSupportedCharset(emailView.getCharset()))
				.bodyType(convertContentType(emailView))
				.build();
	}

	private Charset chooseSupportedCharset(String charset) {
		if (Strings.isNullOrEmpty(charset)) {
			return DEFAULT_CHARSET;
		}
		
		try {
			if (Charset.isSupported(charset)) {
				return Charset.forName(charset);
			}
		} catch (IllegalCharsetNameException e) {
			logger.warn(e.getMessage());
		} catch (UnsupportedCharsetException e) {
			logger.warn(e.getMessage());
		}
		return DEFAULT_CHARSET;
	}
	
	private MSEmailBodyType convertContentType(EmailView emailView) {
		return emailView.getBodyType();
	}

	private Set<MSAttachement> convertAttachment(EmailView emailView) {
		Set<MSAttachement> msAttachments = Sets.newHashSet();
		if (emailView.getAttachments() != null) {
			for (EmailViewAttachment attachment : emailView.getAttachments()) {
				MSAttachement msAttachment = new MSAttachement();
				msAttachment.setDisplayName(attachment.getDisplayName());
				msAttachment.setEstimatedDataSize(attachment.getSize());
				msAttachment.setFileReference(attachment.getFileReference());
				msAttachment.setMethod(method(attachment.getContentType()));
				msAttachments.add(msAttachment);
			}
		}
		return msAttachments;
	}
	
	@VisibleForTesting MethodAttachment method(ContentType contentType) {
		if (contentType != null && CONTENT_TYPE_RFC_822.equalsIgnoreCase(contentType.getFullMimeType())) {
			return MethodAttachment.EmbeddedMessage;
		}
		return MethodAttachment.NormalAttachment;
	}

	private boolean isSupportedICalendar(EmailView emailView) {
		ICalendar iCalendar = emailView.getICalendar();
		if (iCalendar != null && iCalendar.hasEvent()) {
			ICalendarEvent iCalendarEvent = iCalendar.getICalendarEvent();
			return iCalendarEvent.recurrenceId() == null;
		}
		return false;
	}
	
	private MSMeetingRequest convertICalendar(EmailView emailView) {
		if (isSupportedICalendar(emailView)) {
			return new ICalendarConverter().convertToMSMeetingRequest(emailView.getICalendar());
		}
		return null;
	}

	private MSMessageClass convertInvitationType(EmailView emailView) {
		if (isSupportedICalendar(emailView)) {
			if (emailView.getInvitationType() == EmailViewInvitationType.REQUEST) {
				return MSMessageClass.SCHEDULE_MEETING_REQUEST;
			}
			if (emailView.getInvitationType() == EmailViewInvitationType.CANCELED) {
				return MSMessageClass.SCHEDULE_MEETING_CANCELED;
			}
			if (emailView.getInvitationType() == EmailViewInvitationType.REPLY) {
				return inferInvitationTypeFromReply(emailView);
			}
		}
		return null;
	}

	private MSMessageClass inferInvitationTypeFromReply(EmailView emailView) {
		String status = emailView.getICalendar().getICalendarEvent().status();
		if (status != null) {
			if ("CANCELLED".equals(status)) {
				return MSMessageClass.SCHEDULE_MEETING_RESP_NEG;
			} else if ("CONFIRMED".equals(status)) {
				return MSMessageClass.SCHEDULE_MEETING_RESP_POS;
			} else if ("TENTATIVE".equals(status)) {
				return MSMessageClass.SCHEDULE_MEETING_RESP_TENT;
			}
		}
		return null;
	}
}
