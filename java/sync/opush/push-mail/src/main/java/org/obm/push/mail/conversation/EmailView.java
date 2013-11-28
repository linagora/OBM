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
package org.obm.push.mail.conversation;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obm.icalendar.ICalendar;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.exception.EmailViewBuildException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.Flag;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class EmailView {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private Long uid;
		private Collection<Flag> flags;
		private Envelope envelope;
		private InputStream bodyMimePartData;
		private int estimatedDataSize;
		private final ImmutableList.Builder<EmailViewAttachment> attachments;
		private ICalendar iCalendar;
		private EmailViewInvitationType invitationType;
		private MSEmailBodyType bodyType;
		private String charset;
		private Boolean truncated;
		
		private Builder() {
			attachments = ImmutableList.<EmailViewAttachment>builder();
		}
		
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

		public Builder estimatedDataSize(int estimatedDataSize) {
			this.estimatedDataSize = estimatedDataSize;
			return this;
		}
		
		public Builder attachments(List<EmailViewAttachment> attachments) {
			if (attachments != null) {
				this.attachments.addAll(attachments);
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
		
		public Builder bodyType(MSEmailBodyType bodyType) {
			this.bodyType = bodyType;
			return this;
		}
		
		public Builder charset(String charset) {
			this.charset = charset;
			return this;
		}

		public Builder truncated(Boolean truncated) {
			this.truncated = truncated;
			return this;
		}
		
		public EmailView build() {
			if (uid == null) {
				throw new EmailViewBuildException("The uid is required");
			}
			if (envelope == null) {
				throw new EmailViewBuildException("The envelope is required");
			}
			if (bodyMimePartData == null) {
				throw new EmailViewBuildException("The bodyMimePartData is required");
			}
			if (bodyType == null) {
				throw new EmailViewBuildException("The bodyType is required");
			}
			if (flags == null) {
				this.flags = ImmutableSet.<Flag>of();
			}
			if (truncated == null) {
				throw new EmailViewBuildException("The truncated field is required");
			}
			return new EmailView(uid, flags, envelope, bodyMimePartData, 
					estimatedDataSize, attachments.build(), iCalendar, invitationType, bodyType,
					charset, truncated);
		}
	}
	
	private final long uid;
	private final Collection<Flag> flags;
	private final Envelope envelope;
	private final InputStream bodyMimePartData;
	private final Integer estimatedDataSize;
	private final List<EmailViewAttachment> attachments;
	private final ICalendar iCalendar;
	private final EmailViewInvitationType invitationType;
	private final String charset;
	private final MSEmailBodyType bodyType;
	private final boolean truncated;

	private EmailView(long uid, Collection<Flag> flags, Envelope envelope,
			InputStream bodyMimePartData, int estimatedDataSize, List<EmailViewAttachment> attachments, 
			ICalendar iCalendar, EmailViewInvitationType invitationType, MSEmailBodyType bodyType,
			String charset, boolean truncated) {
		
		this.uid = uid;
		this.flags = flags;
		this.envelope = envelope;
		this.bodyMimePartData = bodyMimePartData;
		this.estimatedDataSize = estimatedDataSize;
		this.attachments = attachments;
		this.iCalendar = iCalendar;
		this.invitationType = invitationType;
		this.bodyType = bodyType;
		this.charset = charset;
		this.truncated = truncated;
	}

	public long getUid() {
		return uid;
	}

	public Collection<Flag> getFlags() {
		return flags;
	}

	public boolean hasFlag(Flag flag) {
		return getFlags().contains(flag);
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

	public int getEstimatedDataSize() {
		return estimatedDataSize;
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
	
	public String getCharset() {
		return charset;
	}

	public MSEmailBodyType getBodyType() {
		return bodyType;
	}

	public boolean isTruncated() {
		return truncated;
	}

	
	@Override
	public int hashCode(){
		return Objects.hashCode(uid, flags, envelope, bodyMimePartData, 
				estimatedDataSize, attachments, iCalendar, invitationType, bodyType, charset,
				truncated);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof EmailView) {
			EmailView that = (EmailView) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.flags, that.flags)
				&& Objects.equal(this.envelope, that.envelope)
				&& Objects.equal(this.bodyMimePartData, that.bodyMimePartData)
				&& Objects.equal(this.estimatedDataSize, that.estimatedDataSize)
				&& Objects.equal(this.attachments, that.attachments)
				&& Objects.equal(this.iCalendar, that.iCalendar)
				&& Objects.equal(this.invitationType, that.invitationType)
				&& Objects.equal(this.bodyType, that.bodyType)
				&& Objects.equal(this.charset, that.charset)
				&& Objects.equal(this.truncated, that.truncated);
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
			.add("bodyTruncation", estimatedDataSize)
			.add("attachments", attachments)
			.add("iCalendar", iCalendar)
			.add("invitationType", invitationType)
			.add("bodyType", bodyType)
			.add("charset", charset)
			.add("truncated", truncated)
			.toString();
	}

}
