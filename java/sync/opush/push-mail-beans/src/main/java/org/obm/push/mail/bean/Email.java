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
package org.obm.push.mail.bean;

import java.io.Serializable;
import java.util.Date;

import org.obm.push.utils.index.Indexed;

import com.google.common.base.Objects;

public class Email implements Indexed<Long>, Serializable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private long uid;
		private boolean read;
		private Date date;
		private boolean answered;
		
		private Builder() {
			answered = false;
		}
		
		public Builder uid(long uid) {
			this.uid = uid;
			return this;
		}

		public Builder read(boolean read) {
			this.read = read;
			return this;
		}
		
		public Builder date(Date date) {
			this.date = date;
			return this;
		}
		
		public Builder answered(boolean answered) {
			this.answered = answered;
			return this;
		}
		
		public Email build() {
			return new Email(uid, read, date, answered);
		}

	}
	
	private static final long serialVersionUID = 9022743605981571920L;
	
	private final long uid;
	private final boolean read;
	private final Date date;
	private boolean answered;
	
	private Email(long uid, boolean read, Date date, boolean answered) {
		this.uid = uid;
		this.read = read;
		this.date = date;
		this.answered = answered;
	}

	public long getUid() {
		return uid;
	}

	@Override
	public Long getIndex() {
		return getUid();
	}
	
	public boolean isRead() {
		return read;
	}
	
	public Date getDate() {
		return date;
	}

	public boolean isAnswered() {
		return answered;
	}
	
	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(uid, read, date, answered);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Email) {
			Email that = (Email) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.read, that.read)
				&& Objects.equal(this.date, that.date)
				&& Objects.equal(this.answered, that.answered);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("read", read)
			.add("date", date)
			.add("answered", answered)
			.toString();
	}
	
}
