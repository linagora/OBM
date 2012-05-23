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
package org.obm.mail.conversation;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.minig.imap.Address;
import org.minig.imap.Envelope;
import org.minig.imap.Flag;
import org.minig.imap.mime.ContentType;
import org.minig.imap.mime.IMimePart;
import org.obm.icalendar.ICalendar;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class EmailView {

	public static class Builder {

		private Long uid;
		private Collection<Flag> flags;
		private Envelope envelope;
		private InputStream bodyMimePartData;
		private IMimePart bodyMimePart;
		private Integer bodyTruncation;
		private List<EmailViewAttachment> attachments;
		private ICalendar iCalendar;
		private EmailViewInvitationType invitationType;
		
		public Builder flags(Collection<Flag> flags) {
			this.flags = ImmutableSet.<Flag>builder().addAll(flags).build();
			return this;
		}
		
		public Builder envelope(Envelope envelope) {
			this.envelope = envelope;
			return this;
		}
		
		public Builder uid(long uid) {
			this.uid = uid;
			return this;
		}

		public Builder bodyMimePartData(InputStream bodyMimePartData) {
			this.bodyMimePartData = bodyMimePartData;
			return this;
		}

		public Builder bodyMimePart(IMimePart bodyMimePart) {
			this.bodyMimePart = bodyMimePart;
			return this;
		}

		public Builder bodyTruncation(Integer bodyTruncation) {
			this.bodyTruncation = bodyTruncation;
			return this;
		}
		
		public Builder attachments(List<EmailViewAttachment> attachments) {
			if (attachments != null) {
				this.attachments = ImmutableList.<EmailViewAttachment>builder().addAll(attachments).build();
			}
			return this;
		}
		
		public Builder iCalendar(ICalendar iCalendar) {
			this.iCalendar = iCalendar;
			return this;
		}
		
		public Builder invitationType(EmailViewInvitationType invitationType) {
			this.invitationType = invitationType;
			return this;
		}
		
		public EmailView build() {
			Preconditions.checkState(uid != null, "The uid is required");
			Preconditions.checkState(envelope != null, "The envelope is required");
			Preconditions.checkState(bodyMimePartData != null, "The bodyMimePartData is required");
			Preconditions.checkState(bodyMimePart != null, "The bodyMimePart is required");

			if (flags == null) {
				this.flags = ImmutableSet.<Flag>of();
			}
			
			return new EmailView(uid, flags, envelope, bodyMimePartData, bodyMimePart, bodyTruncation, attachments, iCalendar, invitationType);
		}
		
	}
	
	private final long uid;
	private final Collection<Flag> flags;
	private final Envelope envelope;
	private final InputStream bodyMimePartData;
	private final IMimePart bodyMimePart;
	private final Integer bodyTruncation;
	private final ContentType bodyContentType;
	private final List<EmailViewAttachment> attachments;
	private final ICalendar iCalendar;
	private final EmailViewInvitationType invitationType;

	private EmailView(long uid, Collection<Flag> flags, Envelope envelope,
			InputStream bodyMimePartData, IMimePart bodyMimePart, Integer bodyTruncation, 
			List<EmailViewAttachment> attachments, ICalendar iCalendar, EmailViewInvitationType invitationType) {
		
		this.uid = uid;
		this.flags = flags;
		this.envelope = envelope;
		this.bodyMimePartData = bodyMimePartData;
		this.bodyMimePart = bodyMimePart;
		this.bodyTruncation = bodyTruncation;
		this.attachments = attachments;
		this.iCalendar = iCalendar;
		this.invitationType = invitationType;
		this.bodyContentType = new ContentType.Builder()
			.primaryType(bodyMimePart.getPrimaryType())
			.subType(bodyMimePart.getSubtype())
			.build();
	}

	public long getUid() {
		return uid;
	}

	public Collection<Flag> getFlags() {
		return flags;
	}

	public Envelope getEnvelope() {
		return envelope;
	}
	
	public List<Address> getFrom() {
		return envelope.getFrom();
	}

	public List<Address> getTo() {
		return envelope.getTo();
	}

	public List<Address> getCc() {
		return envelope.getCc();
	}

	public String getSubject() {
		return envelope.getSubject();
	}

	public Date getDate() {
		return envelope.getDate();
	}

	public InputStream getBodyMimePartData() {
		return bodyMimePartData;
	}

	public IMimePart getBodyMimePart() {
		return bodyMimePart;
	}

	public Integer getBodyTruncation() {
		return bodyTruncation;
	}
	
	public ContentType getBodyContentType() {
		return bodyContentType;
	}

	public List<EmailViewAttachment> getAttachments() {
		return attachments;
	}

	public ICalendar getICalendar() {
		return iCalendar;
	}

	public EmailViewInvitationType getInvitationType() {
		return invitationType;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(uid, flags, envelope, bodyMimePartData, bodyMimePart, bodyTruncation, bodyContentType, attachments, iCalendar, invitationType);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof EmailView) {
			EmailView that = (EmailView) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.flags, that.flags)
				&& Objects.equal(this.envelope, that.envelope)
				&& Objects.equal(this.bodyMimePartData, that.bodyMimePartData)
				&& Objects.equal(this.bodyMimePart, that.bodyMimePart)
				&& Objects.equal(this.bodyTruncation, that.bodyTruncation)
				&& Objects.equal(this.bodyContentType, that.bodyContentType)
				&& Objects.equal(this.attachments, that.attachments)
				&& Objects.equal(this.iCalendar, that.iCalendar)
				&& Objects.equal(this.invitationType, that.invitationType);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("flags", flags)
			.add("envelope", envelope)
			.add("bodyMimePartData", bodyMimePartData)
			.add("bodyMimePart", bodyMimePart)
			.add("bodyTruncation", bodyTruncation)
			.add("bodyContentType", bodyContentType)
			.add("attachments", attachments)
			.add("iCalendar", iCalendar)
			.add("invitationType", invitationType)
			.toString();
	}
}
