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
package org.obm.sync.push.client;

import java.util.LinkedList;
import java.util.List;
import com.google.common.base.Objects;

/**
 * <Collection> <SyncKey>f0e0ec53-40a6-432a-bfee-b8c1d391478c</SyncKey>
 * <CollectionId>179</CollectionId> <Status>1</Status> </Collection>
 */
public final class Collection {

	private String syncKey;
	private String collectionId;
	private SyncStatus status;
	private List<Add> adds = new LinkedList<Add>();
	private List<Delete> deletes = new LinkedList<Delete>();

	public String getSyncKey() {
		return syncKey;
	}

	public void setSyncKey(String syncKey) {
		this.syncKey = syncKey;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public SyncStatus getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = SyncStatus.getSyncStatus(status);
	}

	public List<Add> getAdds() {
		return adds;
	}

	public void addAdd(Add applicationData) {
		adds.add(applicationData);
	}

	public List<Delete> getDeletes() {
		return deletes;
	}
	
	public void addDelete(Delete data) {
		deletes.add(data);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(syncKey, collectionId, status, adds, deletes);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Collection) {
			Collection that = (Collection) object;
			return Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.status, that.status)
				&& Objects.equal(this.adds, that.adds)
				&& Objects.equal(this.deletes, that.deletes);
		}
		return false;
	}
}
