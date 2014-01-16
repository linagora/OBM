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

import java.util.Map;
import java.util.Set;

import org.joda.time.Minutes;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class Sync {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private final Map<Integer, AnalysedSyncCollection> collections;
		private Integer waitInMinutes;
		
		public Builder() {
			collections = Maps.newHashMap();
		}
		
		public Builder addCollection(AnalysedSyncCollection collection) {
			collections.put(collection.getCollectionId(), collection);
			return this;
		}

		public Builder waitInMinutes(Integer waitInMinutes) {
			this.waitInMinutes = waitInMinutes;
			return this;
		}
		
		public Sync build() {
			if (waitInMinutes == null) {
				waitInMinutes = 0;
			}
			int waitInSeconds = Minutes.minutes(waitInMinutes).toStandardSeconds().getSeconds();
			return new Sync(collections, waitInSeconds);
		}
	}
	
	private final Map<Integer, AnalysedSyncCollection> collections;
	private final int waitInSecond;
	
	private Sync(Map<Integer, AnalysedSyncCollection> collections, int waitInSecond) {
		this.collections = collections;
		this.waitInSecond = waitInSecond;
	}
	
	public int getWaitInSecond() {
		return waitInSecond;
	}
	
	public Set<AnalysedSyncCollection> getCollections() {
		return ImmutableSet.copyOf(collections.values());
	}
	
	public AnalysedSyncCollection getCollection(Integer collectionId) {
		return collections.get(collectionId);
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(collections, waitInSecond);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Sync) {
			Sync that = (Sync) object;
			return Objects.equal(this.collections, that.collections)
				&& Objects.equal(this.waitInSecond, that.waitInSecond);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("collections", collections)
			.add("waitInSecond", waitInSecond)
			.toString();
	}
	
}
