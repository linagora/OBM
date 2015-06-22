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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class SearchQuery {

	public static final SearchQuery MATCH_ALL = new SearchQuery(null, null); 
	public static final SearchQuery MATCH_ALL_EVEN_DELETED = new SearchQuery(null, null, false, true, null, Optional.<Flag> absent(), Optional.<Flag> absent());
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Date before;
		private Date after;
		private boolean between;
		private boolean includeDeleted;
		private MessageSet messageSet;
		private Flag matchingFlag;
		private Flag unmatchingFlag;
		
		private Builder() {
			this.includeDeleted = false;
		}
		
		public Builder beforeExclusive(Date before) {
			this.before = before;
			return this;
		}
		
		public Builder afterInclusive(Date after) {
			this.after = after;
			return this;
		}

		public Builder between(boolean between) {
			this.between = between;
			return this;
		}
		
		public Builder includeDeleted(boolean includeDeleted) {
			this.includeDeleted = includeDeleted;
			return this;
		}
		
		public Builder messageSet(MessageSet messageSet) {
			this.messageSet = messageSet;
			return this;
		}
		
		public Builder matchingFlag(Flag matchingFlag) {
			this.matchingFlag = matchingFlag;
			return this;
		}
		
		public Builder unmatchingFlag(Flag imapArchiveFlag) {
			this.unmatchingFlag = imapArchiveFlag;
			return this;
		}
		
		public SearchQuery build() {
			if (messageSet != null) {
				Preconditions.checkState(!messageSet.isEmpty());
			}
			if (between) {
				Preconditions.checkState(before != null);
				Preconditions.checkState(after != null);
			}
			return new SearchQuery(before, after, between, includeDeleted, messageSet, Optional.fromNullable(matchingFlag), Optional.fromNullable(unmatchingFlag));
		}
	}

	private final Date before;
	private final Date after;
	private final boolean between;
	private final boolean matchDeleted;
	private final MessageSet messageSet;
	private final Optional<Flag> matchingFlag;
	private final Optional<Flag> unmatchingFlag;
	
	/**
	 * 
	 * @param after
	 *            Messages whose internal date (disregarding time and timezone)
	 *            is within or later than the specified date.
	 */
	private SearchQuery(Date before, Date after) {
		this(before, after, false, false, null, Optional.<Flag> absent(), Optional.<Flag> absent());
	}
	
	private SearchQuery(Date before, Date after, boolean between, boolean matchDeleted, MessageSet messageSet, Optional<Flag> matchingFlag, Optional<Flag> unmatchingFlag) {
		this.after = after;
		this.before = before;
		this.between = between;
		this.matchDeleted = matchDeleted;
		this.messageSet = messageSet;
		this.matchingFlag = matchingFlag;
		this.unmatchingFlag = unmatchingFlag;
	}

	public Date getBefore() {
		return before;
	}

	public Date getAfter() {
		return after;
	}

	public boolean isBetween() {
		return between;
	}

	public boolean isMatchDeleted() {
		return matchDeleted;
	}

	public MessageSet getMessageSet() {
		return messageSet;
	}
	
	public Optional<Flag> getMatchingFlag() {
		return matchingFlag;
	}
	
	public Optional<Flag> getUnmatchingFlag() {
		return unmatchingFlag;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(before, after, between, matchDeleted, messageSet, matchingFlag, unmatchingFlag);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SearchQuery) {
			SearchQuery that = (SearchQuery) object;
				return Objects.equal(this.before, that.before)
				&& Objects.equal(this.after, that.after)
				&& Objects.equal(this.between, that.between)
				&& Objects.equal(this.matchDeleted, that.matchDeleted)
				&& Objects.equal(this.messageSet, that.messageSet)
				&& Objects.equal(this.matchingFlag, that.matchingFlag)
				&& Objects.equal(this.unmatchingFlag, that.unmatchingFlag);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("before", before)
			.add("after", after)
			.add("between", between)
			.add("matchDeleted", matchDeleted)
			.add("messageSet", messageSet)
			.add("matchingFlag", matchingFlag)
			.add("unmatchingFlag", unmatchingFlag)
			.toString();
	}
}
