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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.mail.bean.Address;
import org.obm.push.mail.message.MailMessageAttachment;
import org.obm.push.mail.mime.IMimePart;

public class MailMessage implements Serializable, Comparable<MailMessage> {

	private static final long serialVersionUID = 1165242659388338781L;

	private String subject;
	private MailBody body;
	private List<MailMessageAttachment> attachements;
	private Address sender;
	private Date date;
	private Map<String, String> headers;
	private Set<MailMessage> forwardMessage;
	private long uid;
	private IMimePart invitation;

	private List<Address> to;
	private List<Address> cc;
	private List<Address> bcc;
	private List<Address> dispositionNotificationTo;

	private String smtpId;
	
	private boolean read;
	private boolean starred;
	private boolean answered;

	public MailMessage() {
		this("subject", new MailBody("text/plain", "body"),
				new ArrayList<MailMessageAttachment>(), new Date(), null, null, null,
				null, null, null);
	}

	public MailMessage(String subject, MailBody body,
			List<MailMessageAttachment> attachements, Date d, Address sender,
			List<Address> to, List<Address> cc, List<Address> bcc,
			List<Address> dispositionNotificationTo, 
			Map<String, String> headers) {
		this.subject = subject;
		this.body = body;
		this.attachements = attachements;
		this.date = d;
		this.sender = sender;
		this.headers = headers;
		this.forwardMessage = new HashSet<MailMessage>();
		if (to != null) {
			this.to = to;
		} else {
			this.to = Collections.emptyList();
		}
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = Collections.emptyList();
		}
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = Collections.emptyList();
		}
		if (dispositionNotificationTo != null) {
			this.dispositionNotificationTo = dispositionNotificationTo;
		} else {
			this.dispositionNotificationTo = Collections.emptyList();
		}
	}

	public String getSubject() {
		return subject;
	}

	public MailBody getBody() {
		return body;
	}

	public List<MailMessageAttachment> getAttachments() {
		return attachements;
	}

	public Address getSender() {
		return sender;
	}

	public void setSender(Address sender) {
		this.sender = sender;
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

	@Override
	public int compareTo(MailMessage o) {
		return date.compareTo(o.date);
	}

	public void setAttachments(List<MailMessageAttachment> attachements) {
		this.attachements = attachements;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(MailBody body) {
		this.body = body;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setTo(List<Address> to) {
		if (to != null) {
			this.to = to;
		} else {
			this.to = Collections.emptyList();
		}
	}

	public List<Address> getTo() {
		return to;
	}

	public void setCc(List<Address> cc) {
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = Collections.emptyList();
		}
	}

	public List<Address> getCc() {
		return cc;
	}

	public void setForwardMessage(Set<MailMessage> forwardMessage) {
		this.forwardMessage = forwardMessage;
	}

	public Set<MailMessage> getForwardMessage() {
		return forwardMessage;
	}

	public void addForwardMessage(MailMessage forwardMessage) {
		this.forwardMessage.add(forwardMessage);
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public List<Address> getBcc() {
		return bcc;
	}

	public void setBcc(List<Address> bcc) {
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = Collections.emptyList();
		}
	}

	public IMimePart getInvitation() {
		return invitation;
	}

	public void setInvitation(IMimePart invitation) {
		this.invitation = invitation;
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

	public boolean isHighPriority() {
		// TODO read this value....
		return false;
	}

	public List<Address> getDispositionNotificationTo() {
		return dispositionNotificationTo;
	}
	
	public void setDispositionNotificationTo(
			List<Address> dispositionNotificationTo) {
		this.dispositionNotificationTo = dispositionNotificationTo;
	}
	
}
