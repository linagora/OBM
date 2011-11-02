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
package org.minig.imap;

import java.util.Date;
import java.util.List;

public class Envelope {

	public Envelope(Date data, String subject, List<Address> to,
			List<Address> cc, List<Address> bcc, Address from,
			String messageId, String inReplyTo) {
		super();
		this.date = data;
		this.subject = subject;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.from = from;
		this.messageId = messageId;
		setInReplyTo(inReplyTo);
	}

	private long uid;
	private Date date;
	private String subject;
	private List<Address> to;
	private List<Address> cc;
	private List<Address> bcc;
	private Address from;
	private String messageId;
	private String inReplyTo;

	public Date getDate() {
		return date;
	}

	public void setDate(Date data) {
		this.date = data;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public List<Address> getTo() {
		return to;
	}

	public void setTo(List<Address> to) {
		this.to = to;
	}

	public List<Address> getCc() {
		return cc;
	}

	public void setCc(List<Address> cc) {
		this.cc = cc;
	}

	public Address getFrom() {
		return from;
	}

	public void setFrom(Address from) {
		this.from = from;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
		if ("NIL".equals(inReplyTo)) {
			this.inReplyTo = null;
		}
	}

	public List<Address> getBcc() {
		return bcc;
	}

	public void setBcc(List<Address> bcc) {
		this.bcc = bcc;
	}

}
