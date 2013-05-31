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
package org.obm.push.bean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Objects;

public class MSEmail implements Serializable {

	private String subject;
	private MSEmailBody body;
	private MSAddress from;
	private Date date;
	private Map<String, String> headers;
	private Set<MSAttachement> attachements;
	private long uid;
	private MSEvent invitation;
	private MSMessageClass messageClass;
	private MSImportance importance;

	private List<MSAddress> to;
	private List<MSAddress> cc;
	private List<MSAddress> bcc;
	private SerializableInputStream mimeData;

	private String smtpId;
	
	private boolean read;
	private boolean starred;
	private boolean answered;

	public MSEmail() {
		this("subject", new MSEmailBody(MSEmailBodyType.PlainText, "body"),
				new HashSet<MSAttachement>(), new Date(), null, null, null,
				null, null);
	}

	public MSEmail(String subject, MSEmailBody body,
			Set<MSAttachement> attachements, Date d, MSAddress from,
			List<MSAddress>  to, List<MSAddress>  cc, List<MSAddress>  bcc,
			Map<String, String> headers) {
		this.subject = subject;
		this.body = body;
		this.date = d;
		this.from = from;
		this.headers = headers;
		this.attachements = attachements;
		if (to != null) {
			this.to = to;
		} else {
			this.to = new LinkedList<MSAddress>();
		}
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = new LinkedList<MSAddress>();
		}
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = new LinkedList<MSAddress>();
		}
		this.read = false;
		this.messageClass = MSMessageClass.NOTE;
		this.importance = MSImportance.NORMAL;
	}

	public String getSubject() {
		return subject;
	}

	public MSEmailBody getBody() {
		return body;
	}

	public MSAddress getFrom() {
		return from;
	}

	public void setFrom(MSAddress from) {
		this.from = from;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(MSEmailBody body) {
		this.body = body;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setTo(List<MSAddress> to) {
		if (to != null) {
			this.to = to;
		} else {
			this.to = new LinkedList<MSAddress>();
		}
	}

	public List<MSAddress> getTo() {
		return to;
	}

	public void setCc(List<MSAddress> cc) {
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = new LinkedList<MSAddress>();
		}
	}

	public List<MSAddress> getCc() {
		return cc;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public List<MSAddress> getBcc() {
		return bcc;
	}

	public void setBcc(List<MSAddress> bcc) {
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = new LinkedList<MSAddress>();
		}
	}

	public MSEvent getInvitation() {
		return invitation;
	}

	public void setInvitation(MSEvent invitation, MSMessageClass messageClass) {
		this.invitation = invitation;
		if(messageClass != null){
			this.messageClass = messageClass;
		} else {
			this.messageClass = MSMessageClass.SCHEDULE_MEETING_REQUEST;
		}
	}

	public String getSmtpId() {
		return smtpId;
	}

	public void setSmtpId(String smtpId) {
		this.smtpId = smtpId;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
	
	public MSMessageClass getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(MSMessageClass messageClass) {
		this.messageClass = messageClass;
	}
	
	public MSImportance getImportance() {
		return importance;
	}

	public void setImportance(MSImportance importance) {
		this.importance = importance;
	}
	
	public Set<MSAttachement> getAttachements() {
		return attachements;
	}

	public void setAttachements(Set<MSAttachement> attachements) {
		this.attachements = attachements;
	}
	
	public InputStream getMimeData() {
		return mimeData;
	}

	public void setMimeData(InputStream mimeData) {
		this.mimeData = new SerializableInputStream(mimeData);
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(answered, attachements, bcc, body, cc, date, 
				from, headers, importance, invitation, messageClass,
				mimeData, read, smtpId, starred, subject, to, uid);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof MSEmail) {
			MSEmail other = (MSEmail) obj;
			return Objects.equal(answered, other.answered)
				&& Objects.equal(attachements, other.attachements)
				&& Objects.equal(bcc, other.bcc)
				&& Objects.equal(body, other.body)
				&& Objects.equal(cc, other.cc)
				&& Objects.equal(date, other.date)
				&& Objects.equal(from, other.from)
				&& Objects.equal(headers, other.headers)
				&& Objects.equal(importance, other.importance)
				&& Objects.equal(invitation, other.invitation)
				&& Objects.equal(messageClass, other.messageClass)
				&& Objects.equal(mimeData, other.mimeData)
				&& Objects.equal(read, other.read)
				&& Objects.equal(smtpId, other.smtpId)
				&& Objects.equal(starred, other.starred)
				&& Objects.equal(subject, other.subject)
				&& Objects.equal(to, other.to)
				&& Objects.equal(uid, other.uid);
		}
		return false;
	}
}

