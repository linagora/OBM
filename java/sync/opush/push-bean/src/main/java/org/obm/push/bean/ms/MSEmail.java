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

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSImportance;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailBody;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.PIMDataType;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Objects;

public class MSEmail implements IApplicationData, Serializable {

	@Override
	public PIMDataType getType() {
		return PIMDataType.EMAIL;
	}

	private MSEmailHeader header;
	private MSEmailBody body;
	private Set<MSEmail> forwardMessage;
	private Set<MSAttachement> attachements;
	private long uid;
	private MSEvent invitation;
	private MSMessageClass messageClass;
	private MSImportance importance;

	private SerializableInputStream mimeData;

	private String smtpId;
	
	private boolean read;
	private boolean starred;
	private boolean answered;

	public MSEmail() {
		this("subject", new MSEmailBody(MSEmailBodyType.PlainText, "body"),
				new HashSet<MSAttachement>(), new Date(), null, null, null, null);
	}

	public MSEmail(String subject, MSEmailBody body,
			Set<MSAttachement> attachements, Date date, MSAddress from,
			List<MSAddress>  to, List<MSAddress>  cc, List<MSAddress>  bcc) {
		this.body = body;
		this.attachements = attachements;
		this.forwardMessage = new HashSet<MSEmail>();
		this.read = false;
		this.messageClass = MSMessageClass.Note;
		this.importance = MSImportance.NORMAL;
		this.header = new MSEmailHeader.Builer()
				.from(from)
				.to(to)
				.cc(cc)
				.bcc(bcc)
				.subject(subject)
				.date(date)
				.build();
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

	public List<MSAddress> getBcc() {
		return header.getBcc();
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

	public void setHeader(MSEmailHeader header) {
		this.header = header;
	}

	public MSEmailBody getBody() {
		return body;
	}
	
	public void setBody(MSEmailBody body) {
		this.body = body;
	}

	public void setForwardMessage(Set<MSEmail> forwardMessage) {
		this.forwardMessage = forwardMessage;
	}

	public Set<MSEmail> getForwardMessage() {
		return forwardMessage;
	}

	public void addForwardMessage(MSEmail forwardMessage) {
		this.forwardMessage.add(forwardMessage);
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public MSEvent getInvitation() {
		return invitation;
	}

	public void setInvitation(MSEvent invitation, MSMessageClass messageClass) {
		this.invitation = invitation;
		if(messageClass != null){
			this.messageClass = messageClass;
		} else {
			this.messageClass = MSMessageClass.ScheduleMeetingRequest;
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
		return Objects.hashCode(answered, attachements, header, body, 
				forwardMessage, importance, invitation, messageClass,
				mimeData, read, smtpId, starred, uid);
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
				.append(invitation, other.invitation)
				.append(messageClass, other.messageClass)
				.append(mimeData, other.mimeData)
				.append(read, other.read)
				.append(smtpId, other.smtpId)
				.append(starred, other.starred)
				.append(uid, other.uid)
				.isEquals();
		}
		return false;
	}
}

