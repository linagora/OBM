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
package org.obm.push.mail.bean;

import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class Envelope {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Date date;
		private String subject;
		private List<Address> to;
		private List<Address> cc;
		private List<Address> bcc;
		private List<Address> from;
		private String messageId;
		private List<Address> replyTo;
		
		private Builder() {
		}
		
		public Builder date(Date date) {
			this.date = date;
			return this;
		}
		
		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}
		
		public Builder to(List<Address> to) {
			this.to = to;
			return this;
		}
		
		public Builder cc(List<Address> cc) {
			this.cc = cc;
			return this;
		}
		
		public Builder bcc(List<Address> bcc) {
			this.bcc = bcc;
			return this;
		}
		
		public Builder from(List<Address> from) {
			this.from = from;
			return this;
		}
		
		public Builder messageID(String messageId) {
			this.messageId = messageId;
			return this;
		}
		
		public Builder replyTo(List<Address> replyTo) {
			this.replyTo = replyTo;
			return this;
		}
		
		public Envelope build() {
			if (to == null) {
				this.to = ImmutableList.<Address>of();
			}
			if (cc == null) {
				this.cc = ImmutableList.<Address>of();
			}
			if (from == null) {
				this.from = ImmutableList.<Address>of();
			}
			if (bcc == null) {
				this.bcc = ImmutableList.<Address>of();
			}
			if (replyTo == null) {
				this.replyTo = ImmutableList.<Address>of();
			}
			return new Envelope(this.date, this.subject, this.messageId,  
					this.from, this.to, this.cc, this.bcc, this.replyTo);
		}
	}
	
	private final Date date;
	private final String subject;
	private final String messageId;
	private final List<Address> from;
	private final List<Address> to;
	private final List<Address> cc;
	private final List<Address> bcc;
	private final List<Address> replyTo;

	private Envelope(Date date, String subject, String messageId, 
			List<Address> from, List<Address> to, List<Address> cc, List<Address> bcc, List<Address> replyTo) {
		
		super();
		this.date = date;
		this.subject = subject;
		this.messageId = messageId;
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.replyTo = replyTo;
	}
	
	public Date getDate() {
		return date;
	}

	public String getSubject() {
		return subject;
	}

	public List<Address> getTo() {
		return to;
	}

	public List<Address> getCc() {
		return cc;
	}

	public List<Address> getFrom() {
		return from;
	}

	public String getMessageId() {
		return messageId;
	}

	public List<Address> getReplyTo() {
		return replyTo;
	}

	public List<Address> getBcc() {
		return bcc;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(date, subject, to, cc, bcc, from, messageId, replyTo);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Envelope) {
			Envelope that = (Envelope) object;
				return Objects.equal(this.date, that.date)
				&& Objects.equal(this.subject, that.subject)
				&& Objects.equal(this.to, that.to)
				&& Objects.equal(this.cc, that.cc)
				&& Objects.equal(this.bcc, that.bcc)
				&& Objects.equal(this.from, that.from)
				&& Objects.equal(this.messageId, that.messageId)
				&& Objects.equal(this.replyTo, that.replyTo);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("date", date)
			.add("subject", subject)
			.add("from", from)
			.toString();
	}
}
