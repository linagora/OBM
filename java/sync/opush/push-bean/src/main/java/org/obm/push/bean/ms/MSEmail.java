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
package org.obm.push.bean.ms;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSImportance;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class MSEmail implements IApplicationData, Serializable {

	public static class MSEmailBuilder {

		private long uid;
		
		private MSEmailHeader header;
		private MSEmailBody body;
		private Set<MSEmail> forwardMessage;
		private Set<MSAttachement> attachements;
		private MSMeetingRequest meetingRequest;
		private MSMessageClass messageClass;
		private MSImportance importance;

		private SerializableInputStream mimeData;
		
		private boolean read;
		private boolean starred;
		private boolean answered;

		public MSEmailBuilder uid(long uid) {
			this.uid = uid;
			return this;
		}

		public MSEmailBuilder header(MSEmailHeader header) {
			this.header = header;
			return this;
		}

		public MSEmailBuilder body(MSEmailBody body) {
			this.body = body;
			return this;
		}
		
		public MSEmailBuilder forwardMessage(Set<MSEmail> forwardMessage) {
			this.forwardMessage = forwardMessage;
			return this;
		}
		
		public MSEmailBuilder attachements(Set<MSAttachement> attachements) {
			this.attachements = attachements;
			return this;
		}
		
		public MSEmailBuilder meetingRequest(MSMeetingRequest meetingRequest) {
			return meetingRequest(meetingRequest, MSMessageClass.ScheduleMeetingRequest);
		}
		
		public MSEmailBuilder meetingRequest(MSMeetingRequest meetingRequest, MSMessageClass messageClass) {
			Preconditions.checkArgument(messageClass != null);
			this.meetingRequest = meetingRequest;
			this.messageClass = messageClass;
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
		
		public MSEmailBuilder mimeData(SerializableInputStream mimeData) {
			this.mimeData = mimeData;
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
			if (messageClass == null) {
				messageClass = MSMessageClass.Note;
			}
			if (importance == null) {
				importance = MSImportance.NORMAL;
			}
			return new MSEmail(uid, header, body, attachements, meetingRequest, messageClass,
					importance, forwardMessage, mimeData, read, starred, answered);
		}
	}
	
	@Override
	public PIMDataType getType() {
		return PIMDataType.EMAIL;
	}

	private final long uid;

	private final MSEmailHeader header;
	private final MSEmailBody body;
	private final Set<MSEmail> forwardMessage;
	private final Set<MSAttachement> attachements;
	private final MSMeetingRequest meetingRequest;
	private final MSMessageClass messageClass;
	private final MSImportance importance;

	private final SerializableInputStream mimeData;
	
	private final boolean read;
	private final boolean starred;
	private final boolean answered;

	private MSEmail(long uid, MSEmailHeader header, MSEmailBody body, Set<MSAttachement> attachements,
			MSMeetingRequest meetingRequest, MSMessageClass messageClass, MSImportance importance,
			Set<MSEmail> forwardMessage, SerializableInputStream mimeData,
			boolean read, boolean starred, boolean answered) {
		this.uid = uid;
		this.header = header;
		this.body = body;
		this.attachements = attachements;
		this.meetingRequest = meetingRequest;
		this.messageClass = messageClass;
		this.importance = importance;
		this.forwardMessage = forwardMessage;
		this.mimeData = mimeData;
		this.read = read;
		this.starred = starred;
		this.answered = answered;
	}

	public MSAddress getFrom() {
		return header.getFrom();
	}

	public List<MSAddress> getTo() {
		return header.getTo();
	}

	public List<MSAddress> getCc() {
		return header.getCc();
	}

	public String getSubject() {
		return header.getSubject();
	}

	public Date getDate() {
		return header.getDate();
	}

	public MSEmailHeader getHeader() {
		return header;
	}
	
	public long getUid() {
		return uid;
	}

	public MSEmailBody getBody() {
		return body;
	}

	public Set<MSEmail> getForwardMessage() {
		return forwardMessage;
	}

	public Set<MSAttachement> getAttachements() {
		return attachements;
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

	public SerializableInputStream getMimeData() {
		return mimeData;
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
	public final int hashCode() {
		return Objects.hashCode(answered, attachements, header, body, 
				forwardMessage, importance, meetingRequest, messageClass,
				mimeData, read, starred, uid);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof MSEmail) {
			MSEmail other = (MSEmail) obj;
			return new EqualsBuilder()
				.append(answered, other.answered)
				.append(attachements, other.attachements)
				.append(header, other.header)
				.append(body, other.body)
				.append(forwardMessage, other.forwardMessage)
				.append(importance, other.importance)
				.append(meetingRequest, other.meetingRequest)
				.append(messageClass, other.messageClass)
				.append(mimeData, other.mimeData)
				.append(read, other.read)
				.append(starred, other.starred)
				.append(uid, other.uid)
				.isEquals();
		}
		return false;
	}
}

