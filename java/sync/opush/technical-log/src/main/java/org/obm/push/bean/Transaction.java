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

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import com.google.common.base.Objects;

public class Transaction {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Transaction createEmptyTransaction() {
		return builder().build();
	}
	
	public static class Builder {
		private long id;
		private DateTime transactionStartTime;
		private DateTime transactionEndTime;
		
		private Builder() {}
		
		public Builder id(long id) {
			this.id = id;
			return this;
		}
		
		public Builder transactionStartTime(DateTime transactionStartTime) {
			this.transactionStartTime = transactionStartTime;
			return this;
		}
		
		public Builder transactionEndTime(DateTime transactionEndTime) {
			this.transactionEndTime = transactionEndTime;
			return this;
		}
		
		public Transaction build() {
			return new Transaction(id, transactionStartTime, transactionEndTime);
		}
	}

	private final long id;
	private final DateTime transactionStartTime;
	private final DateTime transactionEndTime;
	
	private Transaction(long id, DateTime transactionStartTime, DateTime transactionEndTime) {
		this.id = id;
		this.transactionStartTime = transactionStartTime;
		this.transactionEndTime = transactionEndTime;
	}
	
	public long getId() {
		return id;
	}
	
	public DateTime getTransactionStartTime() {
		return transactionStartTime;
	}
	
	public DateTime getTransactionEndTime() {
		return transactionEndTime;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(id, transactionStartTime, transactionEndTime);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Transaction) {
			Transaction that = (Transaction) object;
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.transactionStartTime, that.transactionStartTime)
				&& Objects.equal(this.transactionEndTime, that.transactionEndTime);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("transactionStartTime", DateTimeUtils.getInstantMillis(transactionStartTime))
			.add("transactionEndTime", DateTimeUtils.getInstantMillis(transactionEndTime))
			.toString();
	}
}
