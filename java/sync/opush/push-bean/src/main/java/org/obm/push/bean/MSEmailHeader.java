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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.google.common.base.Objects;

public class MSEmailHeader implements Serializable {

	public static class Builer {
		private MSAddress from;
		private List<MSAddress> to;
		private List<MSAddress> cc;
		private List<MSAddress> bcc;
		private String subject;
		private Date date;
		
		public Builer from(MSAddress from) {
			this.from = from;
			return this;
		}
		
		public Builer to(List<MSAddress> to) {
			this.to = to;
			return this;
		}
		
		public Builer cc(List<MSAddress> cc) {
			this.cc = cc;
			return this;
		}
		
		public Builer bcc(List<MSAddress> bcc) {
			this.bcc = bcc;
			return this;
		}
		
		public Builer subject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builer date(Date date) {
			this.date = date;
			return this;
		}
		
		public MSEmailHeader build() {
			return new MSEmailHeader(from, to, cc, bcc, subject, date);
		}
	}
	
	private final MSAddress from;
	private final List<MSAddress> to;
	private final List<MSAddress> cc;
	private final List<MSAddress> bcc;
	private final String subject;
	private final Date date;
	
	private MSEmailHeader(MSAddress from, List<MSAddress> to, List<MSAddress> cc, 
			List<MSAddress> bcc, String subject, Date date) {
		
		super();
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.date = date;
	}
	
	public MSAddress getFrom() {
		return from;
	}
	
	public List<MSAddress> getTo() {
		return to;
	}
	
	public List<MSAddress> getCc() {
		return cc;
	}
	
	public List<MSAddress> getBcc() {
		return bcc;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public Date getDate() {
		return date;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(from, to, cc, bcc, subject, date);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEmailHeader) {
			MSEmailHeader that = (MSEmailHeader) object;
			return Objects.equal(this.from, that.from)
				&& Objects.equal(this.to, that.to)
				&& Objects.equal(this.cc, that.cc)
				&& Objects.equal(this.bcc, that.bcc)
				&& Objects.equal(this.subject, that.subject)
				&& Objects.equal(this.date, that.date);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("from", from)
			.add("to", to)
			.add("cc", cc)
			.add("bcc", bcc)
			.add("subject", subject)
			.add("date", date)
			.toString();
	}
}
