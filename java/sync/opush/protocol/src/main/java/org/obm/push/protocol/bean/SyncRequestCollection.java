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
package org.obm.push.protocol.bean;

import org.apache.commons.lang.NotImplementedException;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.exception.activesync.ASRequestStringFieldException;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class SyncRequestCollection {

	public static class Builder {

		private Integer id;
		private SyncKey syncKey;
		private String dataClass;
		private Integer windowSize;

		public Builder id(Integer id) {
			this.id = id;
			return this;
		}
		
		public Builder syncKey(SyncKey syncKey) {
			this.syncKey = syncKey;
			return this;
		}
		
		public Builder dataClass(String dataClass) {
			this.dataClass = dataClass;
			return this;
		}
		
		public Builder windowSize(Integer windowSize) {
			this.windowSize = windowSize;
			return this;
		}
		
		public SyncRequestCollection build() {
			if (id == null) {
				throw new ASRequestIntegerFieldException("Collection id field is required");
			}
			if (syncKey == null || Strings.isNullOrEmpty(syncKey.getSyncKey())) {
				throw new ASRequestStringFieldException("Collection SyncKey field is required");
			}
			
			return new SyncRequestCollection(id, syncKey, dataClass, windowSize);
		}

	}
	
	private final int id;
	private final SyncKey syncKey;
	private final String dataClass;
	private final Integer windowSize;
	
	protected SyncRequestCollection(int id, SyncKey syncKey, String dataClass, Integer windowSize) {
		this.id = id;
		this.syncKey = syncKey;
		this.dataClass = dataClass;
		this.windowSize = windowSize;
	}
	
	public int getId() {
		return id;
	}

	public String getDataClass() {
		return dataClass;
	}

	public SyncKey getSyncKey() {
		return syncKey;
	}

	public Integer getWindowSize() {
		return windowSize;
	}
	
	public boolean hasWindowSize() {
		return windowSize != null;
	}

	public SyncCollectionOptions getOptions() {
		throw new NotImplementedException("Will be implemented in next commits");
	}
	
	public SyncRequestCollectionCommands getCommands() {
		throw new NotImplementedException("Will be implemented in next commits");
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(id, syncKey, dataClass, windowSize);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncRequestCollection) {
			SyncRequestCollection that = (SyncRequestCollection) object;
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.dataClass, that.dataClass)
				&& Objects.equal(this.windowSize, that.windowSize);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("syncKey", syncKey)
			.add("dataClass", dataClass)
			.add("windowSize", windowSize)
			.toString();
	}
}
