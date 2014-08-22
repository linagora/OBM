/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.beans;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Boundaries {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private DateTime lowerBoundary;
		private DateTime higherBoundary;
		
		private Builder() {}
		
		public Builder firstSync() {
			this.lowerBoundary = new DateTime(0, DateTimeZone.UTC);
			return this;
		}
		
		public Builder lowerBoundary(DateTime lowerBoundary) {
			Preconditions.checkNotNull(lowerBoundary);
			this.lowerBoundary = lowerBoundary;
			return this;
		}
		
		public Builder higherBoundary(DateTime higherBoundary) {
			Preconditions.checkNotNull(higherBoundary);
			this.higherBoundary = higherBoundary;
			return this;
		}
		
		public Boundaries build() {
			Preconditions.checkState(lowerBoundary != null);
			Preconditions.checkState(higherBoundary != null);
			return new Boundaries(lowerBoundary, higherBoundary);
		}
	}
	
	private final DateTime lowerBoundary;
	private final DateTime higherBoundary;

	private Boundaries(DateTime lowerBoundary, DateTime higherBoundary) {
		this.lowerBoundary = lowerBoundary;
		this.higherBoundary = higherBoundary;
	}

	public DateTime getLowerBoundary() {
		return lowerBoundary;
	}

	public DateTime getHigherBoundary() {
		return higherBoundary;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(lowerBoundary, higherBoundary);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Boundaries) {
			Boundaries that = (Boundaries) object;
			return Objects.equal(this.lowerBoundary, that.lowerBoundary)
				&& Objects.equal(this.higherBoundary, that.higherBoundary);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("lowerBoundary", lowerBoundary)
			.add("higherBoundary", higherBoundary)
			.toString();
	}
}
