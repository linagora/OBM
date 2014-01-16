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
package org.obm.push.bean.ms;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSImportance;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class MSEmail implements IApplicationData, Serializable {

	public static MSEmailBuilder builder() {
		return new MSEmailBuilder();
	}
	
	public static class MSEmailBuilder {

		private String subject;
		private MSEmailHeader header;
		private MSEmailBody body;
		private Set<MSAttachement> attachments;
		private MSMeetingRequest meetingRequest;
		private MSMessageClass messageClass;
		private MSImportance importance;
		
		private boolean read;
		private boolean starred;
		private boolean answered;
		
		private MSEmailBuilder() {
			attachments = Sets.newHashSet();
		}
		
		public MSEmailBuilder header(MSEmailHeader header) {
			this.header = header;
			return this;
		}
		
		public MSEmailBuilder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public MSEmailBuilder body(MSEmailBody body) {
			this.body = body;
			return this;
		}
		
		public MSEmailBuilder attachements(Set<MSAttachement> attachements) {
			this.attachments = attachements;
			return this;
		}
		
		public MSEmailBuilder meetingRequest(MSMeetingRequest meetingRequest) {
			return meetingRequest(meetingRequest, MSMessageClass.SCHEDULE_MEETING_REQUEST);
		}
		
		public MSEmailBuilder meetingRequest(MSMeetingRequest meetingRequest, MSMessageClass messageClass) {
			if (meetingRequest != null) {
				Preconditions.checkArgument(messageClass != null);
				this.messageClass = messageClass;
			}
			this.meetingRequest = meetingRequest;
			return this;
		}
		
		public MSEmailBuilder messageClass(MSMessageClass messageClass) {
			this.messageClass = messageClass;
			return this;
		}
		
		public MSEmailBuilder importance(MSImportance importance) {
			this.importance = importance;
			return this;
		}
		
		public MSEmailBuilder read(boolean read) {
			this.read = read;
			return this;
		}
		
		public MSEmailBuilder starred(boolean starred) {
			this.starred = starred;
			return this;
		}
		
		public MSEmailBuilder answered(boolean answered) {
			this.answered = answered;
			return this;
		}
		
		public MSEmail build() {
			Preconditions.checkState(header != null, "The header is required");
			Preconditions.checkState(body != null, "The body is required");
			Preconditions.checkState(attachments != null, "The attachments cannot be null");
			
			if (messageClass == null) {
				messageClass = MSMessageClass.NOTE;
			}
			if (importance == null) {
				importance = MSImportance.NORMAL;
			}
			String emailSubject = Strings.emptyToNull(subject);
			return new MSEmail(emailSubject, header, body, attachments, meetingRequest, messageClass,
					importance, read, starred, answered);
		}

	}
	
	private static final long serialVersionUID = 3353216715438634538L;
	
	@Override
	public PIMDataType getType() {
		return PIMDataType.EMAIL;
	}

	private final String subject;
	private final MSEmailHeader header;
	private final MSEmailBody body;
	private final Set<MSAttachement> attachments;
	private final MSMeetingRequest meetingRequest;
	private final MSMessageClass messageClass;
	private final MSImportance importance;
	
	private final boolean read;
	private final boolean starred;
	private final boolean answered;

	protected MSEmail(String subject, MSEmailHeader header, MSEmailBody body, Set<MSAttachement> attachments,
			MSMeetingRequest meetingRequest, MSMessageClass messageClass, MSImportance importance,
			boolean read, boolean starred, boolean answered) {
		this.subject = subject;
		this.header = header;
		this.body = body;
		this.attachments = attachments;
		this.meetingRequest = meetingRequest;
		this.messageClass = messageClass;
		this.importance = importance;
		this.read = read;
		this.starred = starred;
		this.answered = answered;
	}
	
	public MSAddress getDisplayTo() {
		return header.getDisplayTo();
	}
	
	public List<MSAddress> getFrom() {
		return header.getFrom();
	}

	public List<MSAddress> getTo() {
		return header.getTo();
	}

	public List<MSAddress> getCc() {
		return header.getCc();
	}

	public List<MSAddress> getReplyTo() {
		return header.getReplyTo();
	}
	
	public String getSubject() {
		return subject;
	}

	public Date getDate() {
		return header.getDate();
	}

	public MSEmailHeader getHeader() {
		return header;
	}
	
	public MSEmailBody getBody() {
		return body;
	}

	public Set<MSAttachement> getAttachments() {
		return attachments;
	}

	public MSMeetingRequest getMeetingRequest() {
		return meetingRequest;
	}

	public MSMessageClass getMessageClass() {
		return messageClass;
	}

	public MSImportance getImportance() {
		return importance;
	}

	public boolean isRead() {
		return read;
	}

	public boolean isStarred() {
		return starred;
	}

	public boolean isAnswered() {
		return answered;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(answered, attachments, header, body, 
				importance, meetingRequest, messageClass,
				read, starred);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MSEmail) {
			MSEmail other = (MSEmail) obj;
			if (other.canEquals(this)) {
				return Objects.equal(this.answered, other.answered)
					&& Objects.equal(this.attachments, other.attachments)
					&& Objects.equal(this.header, other.header)
					&& Objects.equal(this.body, other.body)
					&& Objects.equal(this.importance, other.importance)
					&& Objects.equal(this.meetingRequest, other.meetingRequest)
					&& Objects.equal(this.messageClass, other.messageClass)
					&& Objects.equal(this.read, other.read)
					&& Objects.equal(this.starred, other.starred);
			}
		}
		return false;
	}

	protected boolean canEquals(Object obj) {
		return obj instanceof MSEmail;
	}

}

