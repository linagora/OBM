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

import com.google.common.base.Objects;

public class SearchQuery {

	public static final SearchQuery MATCH_ALL = new SearchQuery(null, null); 
	public static final SearchQuery MATCH_ALL_EVEN_DELETED = new SearchQuery(null, null, true);
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Date before;
		private Date after;
		private boolean includeDeleted;
		
		private Builder() {
			this.includeDeleted = false;
		}
		
		public Builder before(Date before) {
			this.before = before;
			return this;
		}
		
		public Builder after(Date after) {
			this.after = after;
			return this;
		}
		
		public Builder includeDeleted(boolean includeDeleted) {
			this.includeDeleted = includeDeleted;
			return this;
		}
		
		public SearchQuery build() {
			return new SearchQuery(before, after, includeDeleted);
		}
	}

	private final Date after;
	private final Date before;
	private final boolean matchDeleted;
	
	/**
	 * 
	 * @param after
	 *            Messages whose internal date (disregarding time and timezone)
	 *            is within or later than the specified date.
	 */
	private SearchQuery(Date before, Date after) {
		this(before, after, false);
	}
	
	private SearchQuery(Date before, Date after, boolean matchDeleted) {
		this.after = after;
		this.before = before;
		this.matchDeleted = matchDeleted;
	}

	public Date getAfter() {
		return after;
	}

	public Date getBefore() {
		return before;
	}

	public boolean isMatchDeleted() {
		return matchDeleted;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(after, before, matchDeleted);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SearchQuery) {
			SearchQuery that = (SearchQuery) object;
				return Objects.equal(this.after, that.after)
				&& Objects.equal(this.before, that.before)
				&& Objects.equal(this.matchDeleted, that.matchDeleted);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("after", after)
			.add("before", before)
			.add("matchDeleted", matchDeleted)
			.toString();
	}
}
