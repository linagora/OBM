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
package org.obm.push.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MSEmailHeader implements Serializable {

	public static final MSAddress DEFAULT_FROM_ADDRESS = new MSAddress("Empty From", "o-push@linagora.com");
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private List<MSAddress> from;
		private List<MSAddress> replyTo;
		private List<MSAddress> to;
		private List<MSAddress> cc;
		private String subject;
		private Date date;
		
		private Builder() {}
		
		public Builder from(List<MSAddress> from) {
			this.from = from;
			return this;
		}
		
		public Builder from(MSAddress... from) {
			this.from = Lists.newArrayList(from);
			return this;
		}
		
		public Builder replyTo(List<MSAddress> replyTo) {
			this.replyTo = replyTo;
			return this;
		}
		
		public Builder replyTo(MSAddress... replyTo) {
			this.replyTo = Lists.newArrayList(replyTo);
			return this;
		}
		
		public Builder to(List<MSAddress> to) {
			this.to = to;
			return this;
		}
		
		public Builder to(MSAddress... to) {
			this.to = Lists.newArrayList(to);
			return this;
		}
		
		public Builder cc(List<MSAddress> cc) {
			this.cc = cc;
			return this;
		}
		
		public Builder cc(MSAddress... cc) {
			this.cc = Lists.newArrayList(cc);
			return this;
		}
		
		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder date(Date date) {
			this.date = date;
			return this;
		}
		
		public MSEmailHeader build() {
			if (from == null || from.isEmpty()) {
				from = Lists.newArrayList(DEFAULT_FROM_ADDRESS);
			}
			if (to == null) {
				to = ImmutableList.of();
			}
			if (cc == null) {
				cc = ImmutableList.of();
			}
			if (replyTo == null) {
				replyTo = ImmutableList.of();
			}
			String headerSubject = Strings.emptyToNull(subject);
			return new MSEmailHeader(from, replyTo, to, cc, headerSubject, date);
		}
	}
	
	private static final long serialVersionUID = 556207964519799832L;
	
	private final List<MSAddress> from;
	private final List<MSAddress> replyTo;
	private final List<MSAddress> to;
	private final List<MSAddress> cc;
	private final String subject;
	private final Date date;
	
	private MSEmailHeader(List<MSAddress> from, List<MSAddress> replyTo, List<MSAddress> to, List<MSAddress> cc, 
			String subject, Date date) {
		
		super();
		this.from = from;
		this.replyTo = replyTo;
		this.to = to;
		this.cc = cc;
		this.subject = subject;
		this.date = date;
	}
	
	public List<MSAddress> getFrom() {
		return from;
	}
	
	public List<MSAddress> getReplyTo() {
		return replyTo;
	}
	
	public List<MSAddress> getTo() {
		return to;
	}
	
	public List<MSAddress> getCc() {
		return cc;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public Date getDate() {
		return date;
	}

	public MSAddress getDisplayTo() {
		if (to != null && !to.isEmpty()) {
			return Iterables.get(to, 0);
		} else {
			return null;
		}
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(from, replyTo, to, cc, subject, date);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEmailHeader) {
			MSEmailHeader that = (MSEmailHeader) object;
			return Objects.equal(this.from, that.from)
				&& Objects.equal(this.replyTo, that.replyTo)
				&& Objects.equal(this.to, that.to)
				&& Objects.equal(this.cc, that.cc)
				&& Objects.equal(this.subject, that.subject)
				&& Objects.equal(this.date, that.date);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("from", from)
			.add("replyTo", replyTo)
			.add("to", to)
			.add("cc", cc)
			.add("subject", subject)
			.add("date", date)
			.toString();
	}
}
