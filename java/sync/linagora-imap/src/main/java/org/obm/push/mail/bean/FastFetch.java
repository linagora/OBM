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
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class FastFetch {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private long uid;
		private Date internalDate;
		private ImmutableSet.Builder<Flag> flags;
		private Integer size;
		
		private Builder() {
			flags = ImmutableSet.builder();
		}
		
		public Builder uid(long uid) {
			this.uid = uid;
			return this;
		}
		
		public Builder internalDate(Date date) {
			this.internalDate = date;
			return this;
		}
		
		public Builder seen() {
			flags.add(Flag.SEEN);
			return this;
		}
		
		public Builder answered() {
			flags.add(Flag.ANSWERED);
			return this;
		}
		
		public Builder draft() {
			flags.add(Flag.DRAFT);
			return this;
		}
		
		public Builder deleted() {
			flags.add(Flag.DELETED);
			return this;
		}
		
		public Builder flagged() {
			flags.add(Flag.FLAGGED);
			return this;
		}
		
		public Builder size(Integer size) {
			this.size = size;
			return this;
		}
		
		public FastFetch build() {
			return new FastFetch(uid, internalDate, flags.build(), size);
		}

		public Builder flags(Set<Flag> flags) {
			this.flags.addAll(flags);
			return this;
		}
	}
	
	private final long uid;
	private final Date internalDate;
	private final Set<Flag> flags;
	private final Integer size;
	
	private FastFetch(long uid, Date internalDate, Set<Flag> flags, Integer size){
		this.uid = uid;
		this.internalDate = internalDate;
		this.flags = flags;
		this.size = size;
	}

	public long getUid() {
		return uid;
	}

	public Date getInternalDate() {
		return internalDate;
	}

	public Set<Flag> getFlags() {
		return flags;
	}
	
	public boolean isRead(){
		return flags != null && flags.contains(Flag.SEEN);
	}
	
	public boolean isAnswered() {
		return flags != null && flags.contains(Flag.ANSWERED);
	}
	
	public boolean isFlagged() {
		return flags != null && flags.contains(Flag.FLAGGED);
	}

	public boolean isDeleted() {
		return flags != null && flags.contains(Flag.DELETED);
	}
	
	public Integer getSize() {
		return size;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(uid, internalDate, flags, size);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof FastFetch) {
			FastFetch that = (FastFetch) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.internalDate, that.internalDate)
				&& Objects.equal(this.flags, that.flags)
				&& Objects.equal(this.size, that.size);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("internalDate", internalDate)
			.add("flags", flags)
			.add("size", size)
			.toString();
	}
}